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
package io.esastack.restlight.server.util;

import esa.commons.StringUtils;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

/**
 * This class is inspired from org.springframework.util.PathMatcher that is implemented as a global path mather which
 * maintains multiple caches such as a {@link ConcurrentHashMap}, a field which is declared as a {@code volatile}
 * value.
 * <p>
 * {@link PathMatcher} does not use any operation which could lead to competitions between multi-threads, and tries to
 * reduce the unnecessary operation which would reduce the performance or memory such as reflection calling, string
 * array creation and so on...
 * <p>
 * The mapping matches URLs using the following rules:<br>
 * <ul>
 * <li>{@code ?} matches one character</li>
 * <li>{@code *} matches zero or more characters</li>
 * <li>{@code **} matches zero or more <em>directories</em> in a path</li>
 * <li>{@code {spring:[a-z]+}} matches the regexp {@code [a-z]+} as a path variable named "spring"</li>
 * </ul>
 */
public class PathMatcher {

    private static final String SEPARATOR = "/";
    private static final char[] WILDCARD_CHARS = {'*', '?', '{'};
    private static final String ENDS_ON_WILD_CARD = SEPARATOR + "*";
    private static final String ENDS_ON_DOUBLE_WILD_CARD = SEPARATOR + "**";
    private static final int MAX_TL_ARRAY_LEN = 16;
    private static final FastThreadLocal<String[]> TEMP_ARRAY = new FastThreadLocal<String[]>() {
        @Override
        protected String[] initialValue() {
            return new String[MAX_TL_ARRAY_LEN];
        }
    };

    private static final Map<String, PathMatcher> CACHE = new ConcurrentHashMap<>(64);

    private final String pattern;
    private final boolean isStartWithSeparator;
    private final PatternDir[] patternDirs;
    private final boolean caseSensitive;
    private final boolean isPattern;
    private final boolean isTemplateVarPattern;

    public PathMatcher(String pattern) {
        this(pattern, true);
    }

    public PathMatcher(String pattern, boolean caseSensitive) {
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
        this.patternDirs = getPatternDirs(pattern, caseSensitive);
        this.isStartWithSeparator = pattern.startsWith(SEPARATOR);
        this.isPattern = isPattern(pattern);
        this.isTemplateVarPattern = isTemplateVarPattern(pattern);
    }

    private static PatternDir[] getPatternDirs(String pattern, boolean caseSensitive) {
        return toDirs(pattern).stream()
                .map(p -> new PatternDir(p, caseSensitive))
                .toArray(PatternDir[]::new);
    }

    /**
     * Matches the given path against the given pattern.
     *
     * @param pattern the pattern to match against
     * @param path    the path String to test
     * @return {@code true} if the given path matched, otherwise {@code false}
     */
    public static boolean match(String pattern, String path) {
        PathMatcher matcher = CACHE.get(pattern);
        if (matcher == null) {
            PathMatcher pre = CACHE.putIfAbsent(pattern, matcher = new PathMatcher(pattern));
            if (pre != null) {
                matcher = pre;
            }
        }
        return matcher.match(path);
    }

    /**
     * Determines Whether the given pattern is a pattern, such as {@code /fo?/bar}, {@code /f*o/bar}
     *
     * @param pattern path
     * @return {@code true} if it is a pattern, otherwise {@code false}.
     */
    public static boolean isPattern(String pattern) {
        if (pattern == null) {
            return false;
        }
        return (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) || isTemplateVarPattern(pattern);
    }

    /**
     * Determines Whether the given pattern contains a template variable pattern, such as "/{foo}", "/foo/{bar}".
     *
     * @param pattern path
     * @return {@code true} if it is a template variable pattern, otherwise {@code false}.
     */
    public static boolean isTemplateVarPattern(String pattern) {
        if (pattern == null) {
            return false;
        }
        boolean uriVar = false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{') {
                uriVar = true;
                continue;
            }
            if (c == '/') {
                uriVar = false;
                continue;
            }
            if (c == '}' && uriVar) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the given pattern's semantic certainly includes given subject path.
     * <p>
     * eg.
     * <ul>
     * <li>{@code /foo} certainly includes {@code /foo}</li>
     * <li>{@code /f??/bar} certainly includes {@code /foo/bar}</li>
     * <li>{@code /*o/bar} certainly includes {@code /foo/bar}</li>
     * <li>{@code /**} certainly includes {@code /foo/bar}</li>
     * <li>{@code /**} certainly includes {@code /f?o/bar}</li>
     * <li>{@code /**} certainly includes {@code /foo/bar}</li>
     * </ul>
     * <p>
     * Note: It is not means that the given pattern's semantic does not includes the given subject while returns a
     * {@code false}.
     *
     * @param pattern pattern, such as '/foo/?ar/q*x'
     * @param subject target path or a pattern.
     * @return {@code true} if give pattern's semantic certainly includes given subject path, otherwise {@code false}
     */
    public static boolean certainlyIncludes(String pattern, String subject) {
        if (!isPotentialIntersect(pattern, subject)) {
            return false;
        }
        if (pattern.equals(subject)) {
            return true;
        }
        if (!isPattern(subject)) {
            // pattern1 => /a?c/d*,  patter2 => /abc/d
            return match(pattern, subject);
        }
        if (ENDS_ON_DOUBLE_WILD_CARD.equals(pattern)) {
            return true;
        }
        return false;
    }

    /**
     * Determines out whether the given patterns may intersect potentially or not.
     * <p>
     * eg.
     * <ul>
     * <li>{@code /foo} is potential intersect {@code /foo}</li>
     * <li>{@code /f??/bar} is potential intersect {@code /foo/bar}</li>
     * <li>{@code /*o/bar} is potential intersect {@code /foo/bar}</li>
     * <li>{@code /**} is potential intersect {@code /foo/bar}</li>
     * <li>{@code /**} is potential intersect {@code /f?o/bar}</li>
     * <li>{@code /**} is potential intersect {@code /foo/bar}</li>
     * <li>{@code /f*o/bar} is potential intersect {@code /foo/b?r}</li>
     * <li>{@code /f*o/bar} is potential intersect {@code /**}</li>
     * </ul>
     * <p>
     * Note: It is not means that the given pattern's semantic does not intersect the another pattern while returns a
     * {@code false}.
     *
     * @param pattern1 pattern1
     * @param pattern2 pattern2
     * @return {@code true} patterns may intersect potentially, otherwise {@code false}.
     */
    public static boolean isPotentialIntersect(String pattern1, String pattern2) {
        if (pattern1.equals(pattern2)) {
            return true;
        }
        if (!isPattern(pattern1)) {
            // pattern1 => /abc/def,  patter2 => /a?c/d*
            return match(pattern2, pattern1);
        } else if (!isPattern(pattern2)) {
            // pattern1 => /a?c/d*,  patter2 => /abc/def
            return match(pattern1, pattern2);
        } else {
            // pattern1 => /a?c/d*,  patter2 => /acc/d*
            PatternDir[] patternDirs1 = getPatternDirs(pattern1, true);
            PatternDir[] patternDirs2 = getPatternDirs(pattern2, true);
            int end = Math.min(patternDirs1.length, patternDirs2.length);
            int index = 0;
            while (index < end) {
                PatternDir dir1 = patternDirs1[index];
                PatternDir dir2 = patternDirs2[index];
                if (!isPattern(dir1.dir)) {
                    // ptt1 => abc, ptt2 => a?c
                    if (!dir2.matcher.matchStrings(dir1.dir)) {
                        return false;
                    }
                } else if (!isPattern(dir2.dir)) {
                    // ptt1 => a?c, ptt2 => abc
                    if (!dir1.matcher.matchStrings(dir2.dir)) {
                        return false;
                    }
                } else if (dir1.isDoubleWildcards || dir2.isDoubleWildcards) {
                    return true;
                } else if (!isTemplateVarPattern(dir1.dir)
                        && !isTemplateVarPattern(dir2.dir)) {

                    // ptt1 => a*c, ptt2 => a?c
                    int idx1 = 0;
                    int idx2 = 0;

                    // match from first char
                    while (idx1 < dir1.dir.length() && idx2 < dir2.dir.length()) {
                        char c1 = dir1.dir.charAt(idx1);
                        char c2 = dir2.dir.charAt(idx2);
                        if (!isWildcardChar(c1) && !isWildcardChar(c2)) {
                            // neither c1 or c2 are not pattern
                            if (c1 == c2) {
                                idx1++;
                                idx2++;
                            } else {
                                // ptt1 => abc*, ptt2 => de?
                                return false;
                            }
                        } else if (isWildcardChar(c1) && isWildcardChar(c2)) {
                            // both c1 and c2 is pattern
                            if ((c1 == '?' && c2 == '?')
                                    || c1 == '*' && c2 == '*') {
                                idx1++;
                                idx2++;
                            } else {
                                break;
                            }
                            // TODO: further lookup
                        } else if (isWildcardChar(c1)) {
                            // c1 is pattern, c2 is not pattern
                            if (c1 == '?') {
                                // c1 is '?'
                                idx1++;
                                idx2++;
                            } else {
                                // c1 is '*'
                                // TODO: further lookup
                                break;
                            }
                        } else if (isWildcardChar(c2)) {
                            // c1 is not pattern, c2 ispattern
                            if (c2 == '?') {
                                // c1 is '?', c2 is
                                idx1++;
                                idx2++;
                            } else {
                                // c2 is '*'
                                // TODO: further lookup
                                break;
                            }
                        }
                    }

                    // match from last char

                    // ptt1 => a*c, ptt2 => a?c
                    idx1 = dir1.dir.length() - 1;
                    idx2 = dir2.dir.length() - 1;

                    // match from first char
                    while (idx1 >= 0 && idx2 >= 0) {
                        char c1 = dir1.dir.charAt(idx1);
                        char c2 = dir2.dir.charAt(idx2);
                        if (!isWildcardChar(c1) && !isWildcardChar(c2)) {
                            // neither c1 or c2 are not pattern
                            if (c1 == c2) {
                                idx1--;
                                idx2--;
                            } else {
                                // ptt1 => abc*, ptt2 => de?
                                return false;
                            }
                        } else {
                            break;
                        }
                    }
                }
                index++;
            }

            if (patternDirs1.length > patternDirs2.length
                    && !patternDirs2[patternDirs2.length - 1].isDoubleWildcards) {
                return false;
            }
            if (patternDirs2.length > patternDirs1.length
                    && !patternDirs1[patternDirs1.length - 1].isDoubleWildcards) {
                return false;
            }
        }
        return true;
    }

    /**
     * Combine two patterns into a new pattern.
     * <p>This implementation simply concatenates the two patterns, unless
     * the first pattern contains a file extension match (e.g., {@code *.html}). In that case, the second pattern will
     * be merged into the first. Otherwise, an {@code IllegalArgumentException} will be thrown.
     * <h3>Examples</h3>
     * <table border="1" summary = "Examples">
     * <tr><th>Pattern 1</th><th>Pattern 2</th><th>Result</th></tr>
     * <tr><td>{@code null}</td><td>{@code null}</td><td>&nbsp;</td></tr>
     * <tr><td>/hotels</td><td>{@code null}</td><td>/hotels</td></tr>
     * <tr><td>{@code null}</td><td>/hotels</td><td>/hotels</td></tr>
     * <tr><td>/hotels</td><td>/bookings</td><td>/hotels/bookings</td></tr>
     * <tr><td>/hotels</td><td>bookings</td><td>/hotels/bookings</td></tr>
     * <tr><td>/hotels/*</td><td>/bookings</td><td>/hotels/bookings</td></tr>
     * <tr><td>/hotels/&#42;&#42;</td><td>/bookings</td><td>/hotels/&#42;&#42;/bookings</td></tr>
     * <tr><td>/hotels</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
     * <tr><td>/hotels/*</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
     * <tr><td>/hotels/&#42;&#42;</td><td>{hotel}</td><td>/hotels/&#42;&#42;/{hotel}</td></tr>
     * <tr><td>/*.html</td><td>/hotels.html</td><td>/hotels.html</td></tr>
     * <tr><td>/*.html</td><td>/hotels</td><td>/hotels.html</td></tr>
     * <tr><td>/*.html</td><td>/*.txt</td><td>{@code IllegalArgumentException}</td></tr>
     * </table>
     *
     * @param pattern1 the first pattern
     * @param pattern2 the second pattern
     * @return the combination of the two patterns
     */
    public static String combine(String pattern1, String pattern2) {
        if (StringUtils.isEmpty(pattern1) && StringUtils.isEmpty(pattern2)) {
            return StringUtils.empty();
        }
        if (StringUtils.isEmpty(pattern1)) {
            return pattern2;
        }
        if (StringUtils.isEmpty(pattern2)) {
            return pattern1;
        }

        boolean pattern1ContainsUriVar = (pattern1.indexOf('{') != -1);
        if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
            // /* + /hotel -> /hotel ; "/*.*" + "/*.html" -> /*.html
            // However /user + /user -> /usr/user ; /{foo} + /bar -> /{foo}/bar
            return pattern2;
        }

        // /hotels/* + /booking -> /hotels/booking
        // /hotels/* + booking -> /hotels/booking
        if (pattern1.endsWith(ENDS_ON_WILD_CARD)) {
            return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
        }

        // /hotels/** + /booking -> /hotels/**/booking
        // /hotels/** + booking -> /hotels/**/booking
        if (pattern1.endsWith(ENDS_ON_DOUBLE_WILD_CARD)) {
            return concat(pattern1, pattern2);
        }

        int starDotPos1 = pattern1.indexOf("*.");
        if (pattern1ContainsUriVar || starDotPos1 == -1) {
            // simply concatenate the two patterns
            return concat(pattern1, pattern2);
        }

        String ext1 = pattern1.substring(starDotPos1 + 1);
        int dotPos2 = pattern2.indexOf('.');
        String file2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
        String ext2 = (dotPos2 == -1 ? StringUtils.empty() : pattern2.substring(dotPos2));
        boolean ext1All = (".*".equals(ext1) || ext1.isEmpty());
        boolean ext2All = (".*".equals(ext2) || ext2.isEmpty());
        if (!ext1All && !ext2All) {
            throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2);
        }
        String ext = (ext1All ? ext2 : ext1);
        return file2 + ext;
    }

    private static String concat(String path1, String path2) {
        boolean path1EndsWithSeparator = path1.endsWith(SEPARATOR);
        boolean path2StartsWithSeparator = path2.startsWith(SEPARATOR);

        if (path1EndsWithSeparator && path2StartsWithSeparator) {
            return path1 + path2.substring(1);
        } else if (path1EndsWithSeparator || path2StartsWithSeparator) {
            return path1 + path2;
        } else {
            return path1 + SEPARATOR + path2;
        }
    }

    /**
     * Whether {@link #pattern} is a pattern.
     *
     * @return {@code true} if {@link #pattern} is a pattern, otherwise {@code false}.
     */
    public boolean isPattern() {
        return isPattern;
    }

    /**
     * Gets the {@link #pattern}.
     *
     * @return pattern
     */
    public String pattern() {
        return pattern;
    }

    /**
     * Whether {@link #pattern} is a pattern which contains one or more template variables.
     *
     * @return {@code true} if {@link #pattern} is a is a pattern which contains one or more template variables,
     * otherwise {@code false}.
     */
    public boolean isTemplateVarPattern() {
        return isTemplateVarPattern;
    }

    /**
     * Matches the given path against the given {@link #pattern}, and extracts the template variables if {@link
     * #pattern} is a template variable pattern.
     *
     * @param path the path String to test
     * @return a not {@code null} map if the given path matched, otherwise {@code null}.
     */
    public Map<String, String> matchAndExtractUriTemplateVariables(String path) {
        Map<String, String> variables = new LinkedHashMap<>();
        boolean result = doMatch(path, true, variables);
        if (result) {
            return variables;
        } else {
            return null;
        }
    }

    /**
     * Matches the given path against the given {@link #pattern}.
     *
     * @param path the path String to test
     * @return {@code true} if the given path matched, otherwise {@code false}.
     */
    public boolean match(String path) {
        return doMatch(path, true, null);
    }

    /**
     * Match the given path against the corresponding part of the {@link #pattern}.
     * <p>
     * Determines whether the pattern at least matches as far as the given base path goes, assuming that a full path may
     * then match as well.
     *
     * @param path the path String to test
     * @return {@code true} if the supplied {@code path} matched, {@code false} if it didn't
     */
    public boolean matchStart(String path) {
        return doMatch(path, false, null);
    }

    private boolean doMatch(String path, boolean fullMatch, Map<String, String> uriTemplateVariables) {
        if (isStartWithSeparator != path.startsWith(SEPARATOR)) {
            return false;
        }
        if (caseSensitive && !isPotentialMatch(path)) {
            return false;
        }

        final List<String> tokens = toDirs(path);
        // prefer to use thread local temporary array
        String[] pathDirs = tokens.toArray(threadLocalTempArray(tokens.size()));

        int patternIdxStart = 0;
        int patternIdxEnd = patternDirs.length - 1;
        int pathIdxStart = 0;
        int pathIdxEnd = tokens.size() - 1;

        // Match all elements up to the first **
        while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            PatternDir pattDir = patternDirs[patternIdxStart];
            if (pattDir.isDoubleWildcards) {
                break;
            }
            if (!pattDir.matcher.matchStrings(pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false;
            }
            patternIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (patternIdxStart > patternIdxEnd) {
                return (pattern.endsWith(SEPARATOR) == path.endsWith(SEPARATOR));
            }
            if (!fullMatch) {
                return true;
            }
            if (patternIdxStart == patternIdxEnd
                    && patternDirs[patternIdxStart].isSingleWildcard
                    && path.endsWith(SEPARATOR)) {
                return true;
            }
            for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
                if (!patternDirs[i].isDoubleWildcards) {
                    return false;
                }
            }
            return true;
        } else if (patternIdxStart > patternIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else if (!fullMatch && patternDirs[patternIdxStart].isDoubleWildcards) {
            // Path start definitely matches due to "**" part in pattern.
            return true;
        }

        // up to last '**'
        while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            PatternDir pattDir = patternDirs[patternIdxEnd];
            if (pattDir.isDoubleWildcards) {
                break;
            }
            if (!pattDir.matcher.matchStrings(pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false;
            }
            patternIdxEnd--;
            pathIdxEnd--;
        }
        if (pathIdxStart > pathIdxEnd) {
            // String is exhausted
            for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
                if (!patternDirs[i].isDoubleWildcards) {
                    return false;
                }
            }
            return true;
        }

        while (patternIdxStart != patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patternIdxStart + 1; i <= patternIdxEnd; i++) {
                if (patternDirs[i].isDoubleWildcards) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patternIdxStart + 1) {
                // '**/**' situation, so skipRowChars one
                patternIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patternIdxStart - 1);
            int strLength = (pathIdxEnd - pathIdxStart + 1);
            int foundIdx = -1;

            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subStr = pathDirs[pathIdxStart + i + j];
                    if (!patternDirs[patternIdxStart + j + 1].matcher.matchStrings(subStr, uriTemplateVariables)) {
                        continue strLoop;
                    }
                }
                foundIdx = pathIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patternIdxStart = patIdxTmp;
            pathIdxStart = foundIdx + patLength;
        }

        for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
            if (!patternDirs[i].isDoubleWildcards) {
                return false;
            }
        }

        return true;
    }

    private boolean isPotentialMatch(String path) {
        int pos = 0;
        for (PatternDir patternDir : patternDirs) {
            int skipped = skipSeparator(path, pos);
            pos += skipped;
            skipped = skipSegment(path, pos, patternDir.dir);
            if (skipped < patternDir.dir.length()) {
                return (skipped > 0 || (patternDir.dir.length() > 0 && isWildcardChar(patternDir.dir.charAt(0))));
            }
            pos += skipped;
        }
        return true;
    }

    private static int skipSegment(String path, int pos, String prefix) {
        int skipped = 0;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (isWildcardChar(c)) {
                return skipped;
            }
            int currPos = pos + skipped;
            if (currPos >= path.length()) {
                return 0;
            }
            if (c == path.charAt(currPos)) {
                skipped++;
            }
        }
        return skipped;
    }

    private static int skipSeparator(String path, int pos) {
        int skipped = 0;
        while (path.startsWith(SEPARATOR, pos + skipped)) {
            ++skipped;
        }
        return skipped;
    }

    public static boolean isWildcardChar(char c) {
        for (char candidate : WILDCARD_CHARS) {
            if (c == candidate) {
                return true;
            }
        }
        return false;
    }

    private static List<String> toDirs(String pattern) {
        final StringTokenizer st = new StringTokenizer(pattern, SEPARATOR);
        final List<String> tokens = new LinkedList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > 0) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Allocates a new array if minLength > {@link #MAX_TL_ARRAY_LEN}
     */
    private static String[] threadLocalTempArray(int minLength) {
        return minLength <= MAX_TL_ARRAY_LEN ? TEMP_ARRAY.get()
                : new String[minLength];
    }

    private static class PatternDir {
        private final String dir;
        private final Matcher matcher;
        private final boolean isDoubleWildcards;
        private final boolean isSingleWildcard;

        PatternDir(String dir, boolean caseSensitive) {
            this.dir = dir;
            this.matcher = new Matcher(dir, caseSensitive);
            this.isDoubleWildcards = "**".equals(dir);
            this.isSingleWildcard = "*".equals(dir);
        }
    }

    protected static class Matcher {

        private static final Pattern GLOB_PATTERN =
                Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?}|[^/{}]|\\\\[{}])+?)}");
        private final BiPredicate<String, Map<String, String>> matcher;

        Matcher(String pattern, boolean caseSensitive) {
            this.matcher = toMatcher(pattern, caseSensitive);
        }

        boolean matchStrings(String str) {
            return matchStrings(str, null);
        }

        boolean matchStrings(String str, Map<String, String> uriTemplateVariables) {
            return matcher.test(str, uriTemplateVariables);
        }

        private static BiPredicate<String, Map<String, String>> toMatcher(String patternStr, boolean caseSensitive) {
            final StringBuilder patternBuilder = new StringBuilder();
            final java.util.regex.Matcher matcher = GLOB_PATTERN.matcher(patternStr);
            final List<String> variableNames = new LinkedList<>();
            boolean isPathVarOnly = false;
            int start = 0;
            int end = 0;
            while (matcher.find()) {
                isPathVarOnly = false;
                patternBuilder.append(quote(patternStr, end, start = matcher.start()));
                String match = matcher.group();
                if ("?".equals(match)) {
                    patternBuilder.append('.');
                } else if ("*".equals(match)) {
                    patternBuilder.append(".*");
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    int colonIdx = match.indexOf(':');
                    if (colonIdx == -1) {
                        patternBuilder.append("(.*)");
                        variableNames.add(matcher.group(1));
                        isPathVarOnly = true;
                    } else {
                        String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
                        patternBuilder.append('(');
                        patternBuilder.append(variablePattern);
                        patternBuilder.append(')');
                        String variableName = match.substring(1, colonIdx);
                        variableNames.add(variableName);
                    }
                }
                end = matcher.end();
            }

            if (patternBuilder.length() == 0) {
                return caseSensitive
                        ? (target, uriTemplateVars) -> patternStr.equals(target)
                        : (target, uriTemplateVars) -> patternStr.equalsIgnoreCase(target);
            } else if (isPathVarOnly && variableNames.size() == 1) {
                final String prefix = start == 0 ? null : patternStr.substring(0, start);
                final String suffix = end == patternStr.length() ? null : patternStr.substring(end);
                final String varName = variableNames.get(0);
                if (prefix == null && suffix == null) {
                    // /{foo} -> /bar
                    return (target, uriTemplateVars) -> {
                        if (uriTemplateVars != null) {
                            uriTemplateVars.put(varName, target);
                        }
                        return true;
                    };
                } else if (prefix == null) {
                    // /{foo}bar -> /fbar
                    return (target, uriTemplateVars) -> {
                        if (caseSensitive) {
                            if (target.endsWith(suffix)) {
                                if (uriTemplateVars != null) {
                                    uriTemplateVars.put(varName,
                                            target.substring(0, target.length() - suffix.length()));
                                }
                                return true;
                            }
                        } else if (target.length() >= suffix.length()) {
                            int idx = target.length() - suffix.length();
                            if (target.substring(idx).equalsIgnoreCase(suffix)) {
                                if (uriTemplateVars != null) {
                                    uriTemplateVars.put(varName, target.substring(0, idx));
                                }
                                return true;
                            }
                        }
                        return false;
                    };
                } else if (suffix == null) {
                    // /foo{bar} -> /foob
                    return (target, uriTemplateVars) -> {
                        if (caseSensitive) {
                            if (target.startsWith(prefix)) {
                                if (uriTemplateVars != null) {
                                    uriTemplateVars.put(varName,
                                            target.substring(prefix.length()));
                                }
                                return true;
                            }
                        } else if (target.length() >= prefix.length()) {
                            if (target.substring(0, prefix.length()).equalsIgnoreCase(prefix)) {
                                if (uriTemplateVars != null) {
                                    uriTemplateVars.put(varName, target.substring(prefix.length()));
                                }
                                return true;
                            }
                        }
                        return false;
                    };
                } else {
                    // /foo{bar}baz -> /foobbaz
                    return (target, uriTemplateVars) -> {
                        if (target.length() >= prefix.length() + suffix.length()) {
                            if (caseSensitive) {
                                if (target.startsWith(prefix) && target.endsWith(suffix)) {
                                    if (uriTemplateVars != null) {
                                        uriTemplateVars.put(varName, target.substring(prefix.length(),
                                                target.length() - suffix.length()));
                                    }
                                    return true;
                                }
                            } else {
                                int idx = target.length() - suffix.length();
                                if (target.substring(0, prefix.length()).equalsIgnoreCase(prefix)
                                        && target.substring(idx).equalsIgnoreCase(suffix)) {
                                    if (uriTemplateVars != null) {
                                        uriTemplateVars.put(varName, target.substring(prefix.length(), idx));
                                    }
                                    return true;
                                }
                            }
                        }
                        return false;
                    };
                }
            } else {
                patternBuilder.append(quote(patternStr, end, patternStr.length()));
                final Pattern pattern = (caseSensitive ? Pattern.compile(patternBuilder.toString()) :
                        Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
                final String[] varNames = variableNames.toArray(new String[0]);
                return (target, uriTemplateVars) -> matchByPattern(target, uriTemplateVars, pattern, varNames);
            }
        }

        private static String quote(String s, int start, int end) {
            if (start == end) {
                return StringUtils.empty();
            }
            return Pattern.quote(s.substring(start, end));
        }

        private static boolean matchByPattern(String str,
                                              Map<String, String> uriTemplateVariables,
                                              Pattern pattern,
                                              String[] varNames) {
            java.util.regex.Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                if (uriTemplateVariables != null) {
                    final int groupCount = matcher.groupCount();
                    if (varNames.length != groupCount) {
                        throw new IllegalArgumentException("The number of capturing groups in the pattern segment " +
                                pattern + " does not match the number of URI template variables it defines, " +
                                "which can occur if capturing groups are used in a URI template regex. " +
                                "Use non-capturing groups instead.");
                    }
                    for (int i = 1; i <= groupCount; i++) {
                        uriTemplateVariables.put(varNames[i - 1], matcher.group(i));
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
