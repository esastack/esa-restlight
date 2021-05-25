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
package esa.restlight.ext.validator;

import esa.restlight.core.annotation.ValidGroup;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

public class BeanSubject {

    public String testSimpleValidation(String name) {
        return name;
    }

    public String testParametersValidation(@Valid SimpleBean bean, @NotEmpty String message) {
        return message;
    }

    @ValidGroup({Serializable.class})
    public String testParametersValidationWithGroup(@Valid SimpleBean2 bean, @NotEmpty(groups = {Serializable.class,
            Comparable.class}) String message) {
        return message;
    }

    public @Valid SimpleBean testReturnValueValidation(String name, Integer age) {
        return new SimpleBean(name, age, new SimpleBean.InnerBean("", 15));
    }

    @ValidGroup(Serializable.class)
    @Valid
    public SimpleBean2 testReturnValueValidationWithGroup(String name, Integer age) {
        return new SimpleBean2(name, age, null);
    }

    @Valid
    public SimpleBean testParametersAndReturnValueValidation(String name, Integer age, @Max(10L) Long temp) {
        return new SimpleBean(name, age, null);
    }

    @Valid
    @ValidGroup(Serializable.class)
    public SimpleBean2 testParametersAndReturnValueValidationWithGroup(String name, @Range(min = 0, max = 100,
            groups = Serializable.class) Integer age, Long temp) {
        return new SimpleBean2(name, age, null);
    }

    public static class SimpleBean {
        @NotEmpty
        private String name;

        @Max(128)
        @Min(0)
        private int age;

        @Valid
        private InnerBean innerBean;

        public SimpleBean(String name, Integer age, InnerBean innerBean) {
            this.name = name;
            this.age = age;
            this.innerBean = innerBean;
        }

        public static class InnerBean {

            @Length(min = 3, max = 20)
            private String address;

            @Range(min = 16, max = 128)
            private int age;

            public InnerBean(String address, int age) {
                this.address = address;
                this.age = age;
            }
        }
    }

    public static class SimpleBean2 {
        @NotEmpty
        private String name;

        @Max(value = 128, groups = Serializable.class)
        @Min(value = 0, groups = Serializable.class)
        private int age;

        @Valid
        private SimpleBean2.InnerBean innerBean;

        public SimpleBean2(String name, Integer age, SimpleBean2.InnerBean innerBean) {
            this.name = name;
            this.age = age;
            this.innerBean = innerBean;
        }

        public static class InnerBean {

            @Length(min = 3, max = 20, groups = {Serializable.class, Comparable.class})
            private String address;

            @Range(min = 16, max = 128)
            private int age;

            public InnerBean(String address, int age) {
                this.address = address;
                this.age = age;
            }
        }
    }

}
