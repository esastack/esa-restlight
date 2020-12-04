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
package esa.restlight.core.util;

import esa.commons.Checks;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ClassUtils {

    private static final String[] EMPTY_STR_ARR = new String[0];

    private ClassUtils() {
    }

    /**
     * Gets the parameter names of given {@code method}. We will try to get parameter names by reflection or local
     * variable table(if failed to get by reflection)
     * <p>
     * !Note: We can not get the parameter names of {@link Method} that is declared in a interface unless including the
     * -g argument to include debug information.
     *
     * @param method target method
     *
     * @return names or {@link #EMPTY_STR_ARR} if failed.
     */
    public static String[] getParameterNames(Method method) {
        if (method == null) {
            return null;
        }
        if (method.getParameterCount() == 0) {
            return EMPTY_STR_ARR;
        }
        String[] names = getParameterNamesByJdk(method, true);
        if (names == null) {
            // the function in the interface does not have the local variable table
            names = getParameterNamesByAsm(method);
        }
        return names.length == 0 ? getParameterNamesByJdk(method, false) : names;
    }

    private static String[] getParameterNamesByJdk(Method method, boolean needPresent) {
        String[] names = new String[method.getParameterCount()];
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isNamePresent() || !needPresent) {
                names[i] = parameters[i].getName();
            } else {
                return null;
            }
        }
        return names;
    }

    private static String[] getParameterNamesByAsm(Method method) {
        Checks.checkNotNull(method);
        final Class<?> declaringClass = method.getDeclaringClass();
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
                    if (!isSameMethod(name, parameterTypes, method)) {
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

    private static boolean isSameMethod(String name,
                                        org.objectweb.asm.Type[] parameterTypes,
                                        Method method) {
        if (!method.getName().equals(name)) {
            return false;
        }
        Class<?>[] targetParameterTypes = method.getParameterTypes();
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

    /**
     * @deprecated use {@link esa.commons.ClassUtils#forName(String)} please
     */
    @Deprecated
    public static Class<?> forName(String clz) {
        return esa.commons.ClassUtils.forName(clz);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#forName(String, boolean)} please
     */
    @Deprecated
    public static Class<?> forName(String clz, boolean initialize) {
        return esa.commons.ClassUtils.forName(clz, initialize);
    }

    /**
     * Retrieve the generic types of the given {@link Type}.
     *
     * @param requiredType requiredType
     *
     * @return {@link Type} of the first type, {@link Object} if could not find a actual {@link Type} such as {@code T}.
     * @deprecated use {@link esa.commons.ClassUtils#retrieveGenericTypes(Type)} please
     */
    @Deprecated
    public static Class<?>[] retrieveGenericTypes(Type requiredType) {
        return esa.commons.ClassUtils.retrieveGenericTypes(requiredType);
    }

    /**
     * Retrieve the first generic type of the given {@link Type}. For instance, this method will return {@link Integer}
     * if passed a {@link Type} of list. Aso, this method will return {@link String} if passed a {@link Type} of map.
     *
     * @param requiredType requiredType
     *
     * @return {@link Type} of the first type, {@link Object} if could not find a actual {@link Type} such as {@code T}.
     * @deprecated use {@link esa.commons.ClassUtils#retrieveFirstGenericType(Type)} please
     */
    @Deprecated
    public static Optional<Class<?>> retrieveFirstGenericType(Type requiredType) {
        return esa.commons.ClassUtils.retrieveFirstGenericType(requiredType);
    }


    /**
     * @see #findFirstGenericType(Class, Class)
     * @deprecated use {@link esa.commons.ClassUtils#findFirstGenericType(Class)} please
     */
    @Deprecated
    public static Optional<Class<?>> findFirstGenericType(Class<?> concrete) {
        return esa.commons.ClassUtils.findFirstGenericType(concrete);
    }

    /**
     * @see #findGenericTypes(Class, Class)
     * @deprecated use {@link esa.commons.ClassUtils#findFirstGenericType(Class, Class)} please
     */
    @Deprecated
    public static Optional<Class<?>> findFirstGenericType(Class<?> concrete, Class<?> interfaceType) {
        return esa.commons.ClassUtils.findFirstGenericType(concrete, interfaceType);
    }

    /**
     * @see #findGenericTypes(Class, Class)
     * @deprecated use {@link esa.commons.ClassUtils#findGenericTypes(Class)} please
     */
    @Deprecated
    public static Class<?>[] findGenericTypes(Class<?> concrete) {
        return esa.commons.ClassUtils.findGenericTypes(concrete);
    }

    /**
     * Finds generic type declared in given target raw type of the given {@link Class} with find semantic which will try
     * to find the generic types from the supper class and the interfaces of super class recursively.
     *
     * @param concrete      real type
     * @param targetRawType target raw type, {@code null} if no required target raw type.
     *
     * @return generic types found
     * @deprecated use {@link esa.commons.ClassUtils#findFirstGenericType(Class, Class)} please
     */
    @Deprecated
    public static Class<?>[] findGenericTypes(Class<?> concrete,
                                              Class<?> targetRawType) {
        return esa.commons.ClassUtils.findGenericTypes(concrete, targetRawType);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#getUserType(Object)} please
     */
    @Deprecated
    public static Class<?> getUserType(Object target) {
        return esa.commons.ClassUtils.getUserType(target);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#getUserType(Class)} please
     */
    @Deprecated
    public static Class<?> getUserType(Class<?> clz) {
        return esa.commons.ClassUtils.getUserType(clz);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#doWithUserDeclaredMethodsMethods(Class, Consumer, Predicate)}
     * please
     */
    @Deprecated
    public static void doWithUserDeclaredMethodsMethods(Class<?> clz,
                                                        Consumer<Method> c,
                                                        Predicate<Method> p) {
        esa.commons.ClassUtils.doWithUserDeclaredMethodsMethods(clz, c, p);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#userDeclaredMethods(Class)} please
     */
    @Deprecated
    public static Set<Method> userDeclaredMethods(Class<?> clz) {
        return esa.commons.ClassUtils.userDeclaredMethods(clz);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#userDeclaredMethods(Class, Predicate)} please
     */
    @Deprecated
    public static Set<Method> userDeclaredMethods(Class<?> clz, Predicate<Method> p) {
        return esa.commons.ClassUtils.userDeclaredMethods(clz, p);
    }

    /**
     * @deprecated use {@link esa.commons.ClassUtils#doWithMethods(Class, Consumer, Predicate)} please
     */
    @Deprecated
    public static void doWithMethods(Class<?> clz,
                                     Consumer<Method> c,
                                     Predicate<Method> p) {
        esa.commons.ClassUtils.doWithMethods(clz, c, p);
    }

}
