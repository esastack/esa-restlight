/*
 * Copyright 2020 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.core.util;

import esa.commons.Checks;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class ClassUtils {

    private static final String[] EMPTY_STR_ARR = new String[0];

    private ClassUtils() {
    }

    /**
     * Gets the parameter names of given {@code method}. We will try to get parameter names by reflection or local
     * variable table(if failed to get by reflection)
     * <p>
     * !Note: We can not get the parameter names of {@link Constructor} that is declared in a interface unless
     * including the -g argument to include debug information.
     *
     * @param constructor target constructor
     * @return names or {@link #EMPTY_STR_ARR} if failed.
     */
    public static String[] getParameterNames(Constructor<?> constructor) {
        Checks.checkNotNull(constructor, "constructor");
        return getParameterNames(new ConstructorParameters(constructor));
    }

    /**
     * Gets the parameter names of given {@code method}. We will try to get parameter names by reflection or local
     * variable table(if failed to get by reflection)
     * <p>
     * !Note: We can not get the parameter names of {@link Method} that is declared in a interface unless including the
     * -g argument to include debug information.
     *
     * @param method target method
     * @return names or {@link #EMPTY_STR_ARR} if failed.
     */
    public static String[] getParameterNames(Method method) {
        Checks.checkNotNull(method, "method");
        return getParameterNames(new MethodParameters(method));
    }

    private static String[] getParameterNames(Parameters parameters) {
        if (parameters.count() == 0) {
            return EMPTY_STR_ARR;
        }
        String[] names = getParameterNamesByJdk(parameters, true);
        if (names == null) {
            // the function in the interface does not have the local variable table
            names = getParameterNamesByAsm(parameters);
        }
        return names.length == 0 ? getParameterNamesByJdk(parameters, false) : names;
    }

    private static String[] getParameterNamesByJdk(Parameters parameters, boolean needPresent) {
        String[] names = new String[parameters.count()];
        Parameter[] parameters0 = parameters.parameters();
        for (int i = 0; i < parameters0.length; i++) {
            if (parameters0[i].isNamePresent() || !needPresent) {
                names[i] = parameters0[i].getName();
            } else {
                return null;
            }
        }
        return names;
    }

    private static String[] getParameterNamesByAsm(Parameters parameters) {
        Checks.checkNotNull(parameters);
        final Class<?> declaringClass = parameters.declaringClass();
        String uri = declaringClass.getName();
        int lastDotIndex = uri.lastIndexOf('.');
        uri = uri.substring(lastDotIndex + 1) + ".class";
        try (InputStream is = declaringClass.getResourceAsStream(uri)) {
            if (is == null) {
                return EMPTY_STR_ARR;
            }
            final ClassReader classReader = new ClassReader(is);
            final List<String> names = new LinkedList<>();
            classReader.accept(new ClassVisitor(Opcodes.ASM7) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                                 String[] exceptions) {
                    // exclude synthetic method, bridge method, <clinit> method and <init>
                    if ((((access & Opcodes.ACC_SYNTHETIC) | (access & Opcodes.ACC_BRIDGE)) > 0)
                            || "<clinit>".equals(name) || "<init>".equals(name)) {
                        return null;
                    }
                    // filter method
                    org.objectweb.asm.Type[] parameterTypes = org.objectweb.asm.Type.getArgumentTypes(desc);
                    if (!isSameParameters(name, parameterTypes, parameters)) {
                        return null;
                    }
                    return new ParameterNameMethodVisitor(names, parameterTypes, (access & Opcodes.ACC_STATIC) > 0);
                }
            }, 0);
            return names.isEmpty() ? EMPTY_STR_ARR : names.toArray(new String[0]);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading class file of '"
                    + uri
                    + "' for searching parameter names.");
        }
    }

    private static boolean isSameParameters(String name,
                                            org.objectweb.asm.Type[] parameterTypes,
                                            Parameters parameters) {
        if (!parameters.name().equals(name)) {
            return false;
        }
        Class<?>[] targetParameterTypes = parameters.types();
        if (parameterTypes.length != targetParameterTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (targetParameterTypes[i].isArray()) {
                // type.getDescriptor() will return [Ljava/lang/Object;
                // while Object[].class.getName returns [Ljava.lang.Object;
                if (!parameterTypes[i].getDescriptor().replace("/", ".")
                        .equals(targetParameterTypes[i].getName())) {
                    return false;
                }
            } else if (!parameterTypes[i].getClassName().equals(targetParameterTypes[i].getName())) {
                return false;
            }
        }
        return true;
    }

    private interface Parameters {

        /**
         * Obtains the name.
         *
         * @return name
         */
        String name();

        /**
         * Obtains parameter types.
         *
         * @return types.
         */
        Class<?>[] types();

        /**
         * Obtains the declaringClass of current parameters.
         *
         * @return declaringClass
         */
        Class<?> declaringClass();

        /**
         * Obtains the count of parameters.
         *
         * @return count
         */
        int count();

        /**
         * Obtains the parameters.
         *
         * @return parameters
         */
        Parameter[] parameters();

    }

    private static final class MethodParameters implements Parameters {

        private final Method method;

        private MethodParameters(Method method) {
            this.method = method;
        }

        @Override
        public String name() {
            return method.getName();
        }

        @Override
        public Class<?>[] types() {
            return method.getParameterTypes();
        }

        @Override
        public Class<?> declaringClass() {
            return method.getDeclaringClass();
        }

        @Override
        public int count() {
            return method.getParameterCount();
        }

        @Override
        public Parameter[] parameters() {
            return method.getParameters();
        }
    }

    private static final class ConstructorParameters implements Parameters {

        private final Constructor<?> constructor;

        private ConstructorParameters(Constructor<?> constructor) {
            this.constructor = constructor;
        }

        @Override
        public String name() {
            return constructor.getName();
        }

        @Override
        public Class<?>[] types() {
            return constructor.getParameterTypes();
        }

        @Override
        public Class<?> declaringClass() {
            return constructor.getDeclaringClass();
        }

        @Override
        public int count() {
            return constructor.getParameterCount();
        }

        @Override
        public Parameter[] parameters() {
            return constructor.getParameters();
        }
    }

    private static final class ParameterNameMethodVisitor extends MethodVisitor {

        private final List<String> names;
        private final String[] resolved;
        private final int[] lvtSlotIndex;
        private final boolean isStatic;
        private boolean visited;


        ParameterNameMethodVisitor(List<String> names,
                                   org.objectweb.asm.Type[] parameterTypes,
                                   boolean isStatic) {
            super(Opcodes.ASM7);
            this.names = names;
            this.isStatic = isStatic;
            resolved = new String[parameterTypes.length];
            this.lvtSlotIndex = computeLvtSlotIndices(isStatic, parameterTypes);

        }

        @Override
        public void visitLocalVariable(String name,
                                       String desc,
                                       String signature,
                                       Label start,
                                       Label end,
                                       int index) {
            visited = true;
            for (int i = 0; i < this.lvtSlotIndex.length; i++) {
                if (this.lvtSlotIndex[i] == index) {
                    this.resolved[i] = name;
                }
            }
        }

        @Override
        public void visitEnd() {
            if (this.visited || (this.isStatic && this.resolved.length == 0)) {
                names.addAll(Arrays.asList(resolved));
            }
        }

        private static int[] computeLvtSlotIndices(boolean isStatic, org.objectweb.asm.Type[] paramTypes) {
            int[] lvtIndex = new int[paramTypes.length];
            int nextIndex = (isStatic ? 0 : 1);
            for (int i = 0; i < paramTypes.length; i++) {
                lvtIndex[i] = nextIndex;
                if (isWideType(paramTypes[i])) {
                    nextIndex += 2;
                } else {
                    nextIndex++;
                }
            }
            return lvtIndex;
        }

        private static boolean isWideType(org.objectweb.asm.Type type) {
            // float is not a wide type
            return (type == org.objectweb.asm.Type.LONG_TYPE
                    || type == org.objectweb.asm.Type.DOUBLE_TYPE);
        }

    }

}
