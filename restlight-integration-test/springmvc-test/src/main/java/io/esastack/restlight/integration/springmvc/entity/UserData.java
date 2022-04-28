/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.springmvc.entity;

import io.esastack.restlight.integration.springmvc.cases.annotation.CustomFieldParam;

import java.math.BigDecimal;
import java.util.Date;

public class UserData {

    @CustomFieldParam
    private String name;

    private Integer age;

    private BigDecimal weight;

    public BigDecimal getWeight() {
        return weight;
    }

    private Date birthDay;

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", weight=" + weight +
                ", birthDay=" + birthDay +
                '}';
    }

    public static final class Builder {
        private String name;
        private Integer age;
        private BigDecimal weight;
        private Date birthDay;

        private Builder() {
        }

        public static Builder anUserData() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder weight(BigDecimal weight) {
            this.weight = weight;
            return this;
        }

        public Builder birthDay(Date birthDay) {
            this.birthDay = birthDay;
            return this;
        }

        public UserData build() {
            UserData userData = new UserData();
            userData.setName(name);
            userData.setAge(age);
            userData.setWeight(weight);
            userData.setBirthDay(birthDay);
            return userData;
        }
    }
}
