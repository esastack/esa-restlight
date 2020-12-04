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
package esa.restlight.ext.interceptor.signature;

import esa.commons.SecurityUtils;
import esa.restlight.ext.interceptor.config.SignatureOptions;

public class HmacSha1SignatureHandlerInterceptor extends AbstractSignatureHandlerInterceptor {

    public HmacSha1SignatureHandlerInterceptor(SignatureOptions options, SecretProvider secretProvider,
                                               SignValidationScope scope) {
        super(options, secretProvider, scope);
    }

    @Override
    protected boolean validate(byte[] data, String signature, String sk) {
        return SecurityUtils.getHmacSHA1(data, sk).equals(signature);
    }

}
