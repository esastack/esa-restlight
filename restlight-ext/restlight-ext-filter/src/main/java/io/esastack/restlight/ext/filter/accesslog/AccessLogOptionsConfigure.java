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
package io.esastack.restlight.ext.filter.accesslog;

public final class AccessLogOptionsConfigure {
    private String directory = "logs";
    private String fileName = "access.log";
    private String charset;
    private boolean rolling = true;
    private String datePattern = "yyyy-MM-dd";
    private int maxHistory = 10;
    private boolean fullUri;

    private AccessLogOptionsConfigure() {
    }

    public static AccessLogOptionsConfigure newOpts() {
        return new AccessLogOptionsConfigure();
    }

    public static AccessLogOptions defaultOpts() {
        return newOpts().configured();
    }

    public AccessLogOptionsConfigure directory(String directory) {
        this.directory = directory;
        return this;
    }

    public AccessLogOptionsConfigure fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public AccessLogOptionsConfigure charset(String charset) {
        this.charset = charset;
        return this;
    }

    public AccessLogOptionsConfigure rolling(boolean rolling) {
        this.rolling = rolling;
        return this;
    }

    public AccessLogOptionsConfigure datePattern(String datePattern) {
        this.datePattern = datePattern;
        return this;
    }

    public AccessLogOptionsConfigure maxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
        return this;
    }

    public AccessLogOptionsConfigure fullUri(boolean fullUri) {
        this.fullUri = fullUri;
        return this;
    }

    public AccessLogOptions configured() {
        AccessLogOptions accessLogOptions = new AccessLogOptions();
        accessLogOptions.setDirectory(directory);
        accessLogOptions.setFileName(fileName);
        accessLogOptions.setCharset(charset);
        accessLogOptions.setRolling(rolling);
        accessLogOptions.setDatePattern(datePattern);
        accessLogOptions.setMaxHistory(maxHistory);
        accessLogOptions.setFullUri(fullUri);
        return accessLogOptions;
    }
}
