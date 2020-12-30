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
package esa.restlight.server.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PathMatcherTest {

    @Test
    void testExactMatch() {
        // test exact matching
        assertTrue(PathMatcher.match("foo", "foo"));
        assertTrue(PathMatcher.match("/foo", "/foo"));
        assertFalse(PathMatcher.match("foo", "/foo"));
        assertFalse(PathMatcher.match("/foo", "foo"));
        assertFalse(PathMatcher.match("/foo/", "/foo"));
        assertFalse(PathMatcher.match("foo", "FOO"));

        assertTrue(new PathMatcher("foo", false).match("foo"));
        assertTrue(new PathMatcher("foo", false).match("FOO"));
    }

    @Test
    void testMatchWithWildcard() {
        // test matching with ?'s
        assertTrue(PathMatcher.match("f?o", "foo"));
        assertTrue(PathMatcher.match("??o", "foo"));
        assertTrue(PathMatcher.match("fo?", "foo"));
        assertTrue(PathMatcher.match("f??", "foo"));
        assertTrue(PathMatcher.match("?o?", "foo"));
        assertFalse(PathMatcher.match("fo?", "fo"));
        assertFalse(PathMatcher.match("fo?", "fooo"));
        assertFalse(PathMatcher.match("fo?", "fao"));

        // test matching with *'s
        assertTrue(PathMatcher.match("*", "foo"));
        assertTrue(PathMatcher.match("foo*", "foo"));
        assertTrue(PathMatcher.match("foo*", "fooabc"));
        assertTrue(PathMatcher.match("foo/*", "foo/abc"));
        assertTrue(PathMatcher.match("foo/*", "foo/a"));
        assertTrue(PathMatcher.match("foo/*", "foo/"));
        assertTrue(PathMatcher.match("*foo*", "abcfoode"));
        assertTrue(PathMatcher.match("*foo", "abcfoo"));
        assertTrue(PathMatcher.match("*.*", "foo."));
        assertTrue(PathMatcher.match("*.*", "foo.bar"));
        assertTrue(PathMatcher.match("*.*", "foo.bar.baz"));
        assertTrue(PathMatcher.match("foo*bar", "fooabcbar"));
        assertFalse(PathMatcher.match("foo*", "bar"));
        assertFalse(PathMatcher.match("foo*", "barfoo"));
        assertFalse(PathMatcher.match("foo*", "foo/"));
        assertFalse(PathMatcher.match("foo*", "foo/f"));
        assertFalse(PathMatcher.match("foo/*", "foo"));
        assertFalse(PathMatcher.match("*foo*", "fos"));
        assertFalse(PathMatcher.match("*foo", "coo"));
        assertFalse(PathMatcher.match("*.*", "foo"));
        assertFalse(PathMatcher.match("foo*bar", "foo"));
        assertFalse(PathMatcher.match("foo*bar", "fooabcbardef"));

        // test matching with ?'s and /'s
        assertTrue(PathMatcher.match("/?", "/a"));
        assertTrue(PathMatcher.match("/?/a", "/a/a"));
        assertTrue(PathMatcher.match("/a/?", "/a/b"));
        assertTrue(PathMatcher.match("/??/a", "/aa/a"));
        assertTrue(PathMatcher.match("/a/??", "/a/bb"));
        assertTrue(PathMatcher.match("/?", "/a"));

        // test matching with **'s
        assertTrue(PathMatcher.match("/**", "/foo/bar"));
        assertTrue(PathMatcher.match("/*/**", "/foo/bar"));
        assertTrue(PathMatcher.match("/**/*", "/foo/bar"));
        assertTrue(PathMatcher.match("/foo/**/bar", "/foo/aaa/bbb/bar"));
        assertTrue(PathMatcher.match("/**/foo", "/aa/bb/foo"));
        assertTrue(PathMatcher.match("/foo/**/**/bar", "/foo/aaa/bbb/ccc/ddd/bar"));

        assertFalse(PathMatcher.match("/????", "/foo/bar"));
        assertFalse(PathMatcher.match("/**/*foo", "/foo/bar/baz/bbb/afoa"));

        assertFalse(PathMatcher.match("/foo/bar/**/baz", "/a/b/c/"));

        assertTrue(PathMatcher.match("/foo/bar/**", "/foo/bar"));

        assertTrue(PathMatcher.match("", ""));

        assertTrue(PathMatcher.match("/{foo}.*", "/testing.bar"));

        assertTrue(PathMatcher.match("/{foo}", "/f"));
        assertTrue(PathMatcher.match("/{foo}bar", "/bar"));
        assertTrue(new PathMatcher("/{foo}bar", false).match("/BAR"));
        assertTrue(PathMatcher.match("/foo{bar}", "/foo"));
        assertTrue(new PathMatcher("/foo{bar}", false).match("/FOO"));
        assertTrue(PathMatcher.match("/{foo}bar/{baz}", "/bar/b"));
        assertTrue(new PathMatcher("/{foo}bar/{baz}", false).match("/BAR/B"));
        assertTrue(PathMatcher.match("/foo{bar}/{baz}", "/foo/b"));
        assertTrue(new PathMatcher("/foo{bar}/{baz}", false).match("/FOO/B"));
        assertTrue(PathMatcher.match("/foo{bar}baz", "/foobbaz"));
        assertTrue(new PathMatcher("/foo{bar}baz", false).match("/FOObBAZ"));
        assertTrue(PathMatcher.match("/foo{bar}baz", "/foobaz"));
        assertTrue(new PathMatcher("/foo{bar}baz", false).match("/FOOBAZ"));

        assertFalse(PathMatcher.match("/{foo}bar", "/BAR"));
        assertFalse(PathMatcher.match("/{foo}bar", "/ar"));
        assertFalse(new PathMatcher("/{foo}bar", false).match("/AR"));
        assertFalse(PathMatcher.match("/{foo}bar", "/aaaaaaar"));
        assertFalse(new PathMatcher("/{foo}bar", false).match("/AAAAAAAR"));
        assertFalse(PathMatcher.match("/foo{bar}", "/FOO"));
        assertFalse(PathMatcher.match("/foo{bar}", "/fo"));
        assertFalse(new PathMatcher("/foo{bar}", false).match("/FO"));
        assertFalse(PathMatcher.match("/foo{bar}", "/fffffffo"));
        assertFalse(new PathMatcher("/foo{bar}", false).match("/FFFFFFFFFO"));
        assertFalse(PathMatcher.match("/foo{bar}baz", "/FOOBAZ"));
        assertFalse(PathMatcher.match("/foo{bar}baz", "/foaz"));
        assertFalse(new PathMatcher("/foo{bar}baz", false).match("/FOAZ"));
        assertFalse(PathMatcher.match("/foo{bar}baz", "/fooaz"));
        assertFalse(new PathMatcher("/foo{bar}baz", false).match("/FOOAZ"));
        assertFalse(PathMatcher.match("/foo{bar}baz", "/fobaz"));
        assertFalse(new PathMatcher("/foo{bar}baz", false).match("/FOBAZ"));
    }

    @Test
    void isPattern() {
        assertTrue(PathMatcher.isPattern("/test/*"));
        assertTrue(PathMatcher.isPattern("/test/**/name"));
        assertTrue(PathMatcher.isPattern("/test?"));
        assertTrue(PathMatcher.isPattern("/test/{name}"));

        assertFalse(PathMatcher.isPattern("/test/name"));
        assertFalse(PathMatcher.isPattern("/test/foo{bar"));
        assertFalse(PathMatcher.isPattern("/tes{t/foobar}"));
        assertFalse(PathMatcher.isPattern(null));
    }

    @Test
    void testPotentialIntersect() {
        // non-pattern
        assertTrue(PathMatcher.isPotentialIntersect("abc", "abc"));
        assertTrue(PathMatcher.isPotentialIntersect("abc/abc", "abc/abc"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/abc", "/abc/abc"));
        assertTrue(PathMatcher.isPotentialIntersect("//abc//abc", "/abc/abc"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/abc", "//abc//abc"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/abc", "//abc//abc"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/abc/def", "//abc//abc/def"));
        assertFalse(PathMatcher.isPotentialIntersect("/abc/def", "abc/de"));
        assertFalse(PathMatcher.isPotentialIntersect("abc", "def"));
        assertFalse(PathMatcher.isPotentialIntersect("/abc", "/def"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab/def", "/abc/def"));
        assertFalse(PathMatcher.isPotentialIntersect("/abc/def/g", "/abc/def"));

        // first parameter is pattern
        assertTrue(PathMatcher.isPotentialIntersect("ab?", "abc"));
        assertTrue(PathMatcher.isPotentialIntersect("ab*", "abc"));
        assertTrue(PathMatcher.isPotentialIntersect("ab*", "ab"));
        assertTrue(PathMatcher.isPotentialIntersect("*", "ab"));
        assertTrue(PathMatcher.isPotentialIntersect("**", "ab"));
        assertTrue(PathMatcher.isPotentialIntersect("{name}", "ab"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab?", "/abc"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*", "/abc"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*", "/ab"));
        assertTrue(PathMatcher.isPotentialIntersect("/*", "/ab"));
        assertTrue(PathMatcher.isPotentialIntersect("/**", "/ab"));
        assertTrue(PathMatcher.isPotentialIntersect("/{name}", "/ab"));
        assertTrue(PathMatcher.isPotentialIntersect("/a?c/def", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/a??/def", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/a??/de?", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/a*/def", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/a*/def*", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/a*/*", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/a*/d*", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/**", "/abc/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/**/def", "/a/b/c/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/ababca/de", "/ab**a/de"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab?", "/ab"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab*", "/a"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab*/de?", "/abcd/de"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab??/def", "/abcd/de"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab??/d", "/abcd/de"));
        assertFalse(PathMatcher.isPotentialIntersect("/a**d/de", "/ab/ad/de"));

        // second parameter is pattern
        assertTrue(PathMatcher.isPotentialIntersect("abc", "ab?"));
        assertTrue(PathMatcher.isPotentialIntersect("abc", "ab*"));
        assertTrue(PathMatcher.isPotentialIntersect("ab", "ab*"));
        assertTrue(PathMatcher.isPotentialIntersect("ab", "*"));
        assertTrue(PathMatcher.isPotentialIntersect("ab", "**"));
        assertTrue(PathMatcher.isPotentialIntersect("ab", "{name}"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc", "/ab?"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc", "/ab*"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab", "/ab*"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab", "/*"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab", "/**"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab", "/{name}"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a?c/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a??/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a??/de?"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a*/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a*/def*"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a*/*"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/a*/d*"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/def", "/**"));
        assertTrue(PathMatcher.isPotentialIntersect("/a/b/c/def", "/**/def"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab**a/de", "/ababca/de"));

        assertFalse(PathMatcher.isPotentialIntersect("/ab", "/ab?"));
        assertFalse(PathMatcher.isPotentialIntersect("/a", "/ab*"));
        assertFalse(PathMatcher.isPotentialIntersect("/abcd/de", "/ab*/de?"));
        assertFalse(PathMatcher.isPotentialIntersect("/abcd/de", "/ab??/def"));
        assertFalse(PathMatcher.isPotentialIntersect("/abcd/de", "/ab??/d"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab/ad/de", "/a**d/de"));

        // both are pattern
        assertTrue(PathMatcher.isPotentialIntersect("**", "**"));
        assertTrue(PathMatcher.isPotentialIntersect("*", "*"));
        assertTrue(PathMatcher.isPotentialIntersect("?", "?"));
        assertTrue(PathMatcher.isPotentialIntersect("*", "??"));
        assertTrue(PathMatcher.isPotentialIntersect("?*", "?? "));
        assertTrue(PathMatcher.isPotentialIntersect("/**", "/**"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab?", "/ab?"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc?", "/ab?d"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab??", "/ab?"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*", "/ab*"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*", "/ab?"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*", "/ab"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*", "/ab*?"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*/d?f", "/ab?/de?"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*/**", "/ab?/de?"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*/**", "/ab?/de?/ss"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab?/de?/ss", "/ab*/**"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*/**", "/ab?/de/ss"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab?/de/ss", "/ab*/**"));
        assertTrue(PathMatcher.isPotentialIntersect("/**/ss", "/ab?/de/ss"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab?/de/ss", "/**/ss"));
        assertTrue(PathMatcher.isPotentialIntersect("/**/ss", "/ab?/de/ssa"));
        assertTrue(PathMatcher.isPotentialIntersect("/*/ss", "/ab?/ss"));
        assertTrue(PathMatcher.isPotentialIntersect("/ab*d", "/a*d"));
        assertTrue(PathMatcher.isPotentialIntersect("/{name}", "/a*d"));
        assertTrue(PathMatcher.isPotentialIntersect("/??sdf", "/{name}"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/de?", "/abc/{name}"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/??", "/abc/{name}"));
        assertTrue(PathMatcher.isPotentialIntersect("/abc/**", "/abc/{name}"));

        assertFalse(PathMatcher.isPotentialIntersect("/ab?d", "/tb?d"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab?d", "/ab?e"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab**d", "/ab?e"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab*cd", "/ab?e"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab??f", "/ab??d"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab??/ef", "/ab??"));
        assertFalse(PathMatcher.isPotentialIntersect("/ab??", "/ab??/ef"));
        assertFalse(PathMatcher.isPotentialIntersect("/*/abc", "/*/ef"));
        assertFalse(PathMatcher.isPotentialIntersect("/a*/abc", "/*/ef"));
        assertFalse(PathMatcher.isPotentialIntersect("/a*/abc", "/a*/ef"));
        assertFalse(PathMatcher.isPotentialIntersect("/*a/abc", "/a*a/ef"));
        assertFalse(PathMatcher.isPotentialIntersect("/{name}/abc", "/a*a/ef"));
        assertFalse(PathMatcher.isPotentialIntersect("/{name}/abc", "/a*a"));
        assertFalse(PathMatcher.isPotentialIntersect("/{name}/abc", "/a*a/**/def"));

    }

    @Test
    void testCertainlyIncludes() {
        assertTrue(PathMatcher.certainlyIncludes("abc", "abc"));
        assertTrue(PathMatcher.certainlyIncludes("/abc", "/abc"));
        assertTrue(PathMatcher.certainlyIncludes("/a?c", "/abc"));
        assertTrue(PathMatcher.certainlyIncludes("/a??", "/abc"));
        assertTrue(PathMatcher.certainlyIncludes("/**", "/abc"));
        assertTrue(PathMatcher.certainlyIncludes("/abc/**", "/abc/ac/ad"));
        assertTrue(PathMatcher.certainlyIncludes("/*/ab", "/abc/ab"));
        assertTrue(PathMatcher.certainlyIncludes("/*/a??", "/abc/abc"));
        assertTrue(PathMatcher.certainlyIncludes("/*/{name}", "/abc/def"));

        // have no potential to intersect
        assertFalse(PathMatcher.certainlyIncludes("/abc/def", "abc/de"));
        assertFalse(PathMatcher.certainlyIncludes("abc", "def"));
        assertFalse(PathMatcher.certainlyIncludes("/abc", "/def"));
        assertFalse(PathMatcher.certainlyIncludes("/ab/def", "/abc/def"));
        assertFalse(PathMatcher.certainlyIncludes("/abc/def/g", "/abc/def"));
        assertFalse(PathMatcher.certainlyIncludes("/ab?", "/ab"));
        assertFalse(PathMatcher.certainlyIncludes("/ab*", "/a"));
        assertFalse(PathMatcher.certainlyIncludes("/ab*/de?", "/abcd/de"));
        assertFalse(PathMatcher.certainlyIncludes("/ab??/def", "/abcd/de"));
        assertFalse(PathMatcher.certainlyIncludes("/ab??/d", "/abcd/de"));
        assertFalse(PathMatcher.certainlyIncludes("/a**d/de", "/ab/ad/de"));
        assertFalse(PathMatcher.certainlyIncludes("/ab", "/ab?"));
        assertFalse(PathMatcher.certainlyIncludes("/a", "/ab*"));
        assertFalse(PathMatcher.certainlyIncludes("/abcd/de", "/ab*/de?"));
        assertFalse(PathMatcher.certainlyIncludes("/abcd/de", "/ab??/def"));
        assertFalse(PathMatcher.certainlyIncludes("/abcd/de", "/ab??/d"));
        assertFalse(PathMatcher.certainlyIncludes("/ab/ad/de", "/a**d/de"));
        assertFalse(PathMatcher.certainlyIncludes("/ab?d", "/tb?d"));
        assertFalse(PathMatcher.certainlyIncludes("/ab?d", "/ab?e"));
        assertFalse(PathMatcher.certainlyIncludes("/ab**d", "/ab?e"));
        assertFalse(PathMatcher.certainlyIncludes("/ab*cd", "/ab?e"));
        assertFalse(PathMatcher.certainlyIncludes("/ab??f", "/ab??d"));
        assertFalse(PathMatcher.certainlyIncludes("/ab??/ef", "/ab??"));
        assertFalse(PathMatcher.certainlyIncludes("/ab??", "/ab??/ef"));
        assertFalse(PathMatcher.certainlyIncludes("/*/abc", "/*/ef"));
        assertFalse(PathMatcher.certainlyIncludes("/a*/abc", "/*/ef"));
        assertFalse(PathMatcher.certainlyIncludes("/a*/abc", "/a*/ef"));
        assertFalse(PathMatcher.certainlyIncludes("/*a/abc", "/a*a/ef"));
        assertFalse(PathMatcher.certainlyIncludes("/{name}/abc", "/a*a/ef"));
        assertFalse(PathMatcher.certainlyIncludes("/{name}/abc", "/a*a"));
        assertFalse(PathMatcher.certainlyIncludes("/{name}/abc", "/a*a/**/def"));

    }

    @Test
    void testIsTemplateVarPatter() {
        assertTrue(PathMatcher.isTemplateVarPattern("/{name}"));
        assertTrue(PathMatcher.isTemplateVarPattern("/foo/{name}"));

        assertFalse(PathMatcher.isTemplateVarPattern("/foo/{name"));
        assertFalse(PathMatcher.isTemplateVarPattern("/foo/name}"));
        assertFalse(PathMatcher.isTemplateVarPattern("/foo/name"));
        assertFalse(PathMatcher.isTemplateVarPattern("/{foo/n}ame"));
        assertFalse(PathMatcher.isTemplateVarPattern(null));
    }

    @Test
    void testMatchAndExtractUriTemplateVariables() {
        assertTrue(new PathMatcher("").matchAndExtractUriTemplateVariables("").isEmpty());
        assertTrue(new PathMatcher("/foo").matchAndExtractUriTemplateVariables("/foo").isEmpty());
        assertNull(new PathMatcher("/foo").matchAndExtractUriTemplateVariables("/bar"));

        Map<String, String> ret = new PathMatcher("/foo/{bar}")
                .matchAndExtractUriTemplateVariables("/foo/baz");
        assertEquals("baz", ret.get("bar"));

        ret = new PathMatcher("/foo/{bar}", false)
                .matchAndExtractUriTemplateVariables("/FOO/baz");
        assertEquals("baz", ret.get("bar"));

        ret = new PathMatcher("/{foo}/abc{bar}/{baz}def/gh{qux}ijk")
                .matchAndExtractUriTemplateVariables("/fff/abc/def/ghijk");
        assertEquals("fff", ret.get("foo"));
        assertEquals("", ret.get("bar"));
        assertEquals("", ret.get("bar"));
        assertEquals("", ret.get("baz"));

        ret = new PathMatcher("/{foo}/abc{bar}/{baz}def/gh{qux}ijk", false)
                .matchAndExtractUriTemplateVariables("/fff/ABC/DEF/GHIJK");
        assertEquals("fff", ret.get("foo"));
        assertEquals("", ret.get("bar"));
        assertEquals("", ret.get("bar"));
        assertEquals("", ret.get("baz"));

        ret = new PathMatcher("{symbolicName:[\\w\\.]+}-{version:[\\w\\.]+}.jar")
                .matchAndExtractUriTemplateVariables("com.example-1.0.0.jar");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0", ret.get("version"));

        ret = new PathMatcher("{symbolicName:[\\w\\.]+}-{version:[\\w\\.]+}.jar", false)
                .matchAndExtractUriTemplateVariables("com.example-1.0.0.JAR");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0", ret.get("version"));

        ret = new PathMatcher("{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.]+}.jar")
                .matchAndExtractUriTemplateVariables("com.example-sources-1.0.0.jar");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0", ret.get("version"));

        ret = new PathMatcher("{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.]+}.jar", false)
                .matchAndExtractUriTemplateVariables("com.example-SOURCES-1.0.0.JAR");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0", ret.get("version"));

        ret = new PathMatcher("{symbolicName:[\\w\\.]+}-sources-{version:[\\d\\" +
                ".]+}-{year:\\d{4}}{month:\\d{2}}{day:\\d{2}}.jar")
                .matchAndExtractUriTemplateVariables("com.example-sources-1.0.0-20201229.jar");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0", ret.get("version"));
        assertEquals("2020", ret.get("year"));
        assertEquals("12", ret.get("month"));
        assertEquals("29", ret.get("day"));

        ret = new PathMatcher("{symbolicName:[\\w\\.]+}-sources-{version:[\\d\\" +
                ".]+}-{year:\\d{4}}{month:\\d{2}}{day:\\d{2}}.jar", false)
                .matchAndExtractUriTemplateVariables("com.example-SOURCES-1.0.0-20201229.JAR");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0", ret.get("version"));
        assertEquals("2020", ret.get("year"));
        assertEquals("12", ret.get("month"));
        assertEquals("29", ret.get("day"));

        ret = new PathMatcher("{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.\\{\\}]+}.jar")
                .matchAndExtractUriTemplateVariables("com.example-sources-1.0.0.{12}.jar");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0.{12}", ret.get("version"));

        ret = new PathMatcher("{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.\\{\\}]+}.jar", false)
                .matchAndExtractUriTemplateVariables("com.example-SOURCES-1.0.0.{12}.JAR");
        assertEquals("com.example", ret.get("symbolicName"));
        assertEquals("1.0.0.{12}", ret.get("version"));

        assertThrows(IllegalArgumentException.class,
                () -> new PathMatcher("/foo/{id:bar(baz)?}")
                        .matchAndExtractUriTemplateVariables("/foo/barbaz"));

        assertThrows(IllegalArgumentException.class,
                () -> new PathMatcher("/foo/{id:bar(baz)?}", false)
                        .matchAndExtractUriTemplateVariables("/foo/BARBAZ"));
    }
}
