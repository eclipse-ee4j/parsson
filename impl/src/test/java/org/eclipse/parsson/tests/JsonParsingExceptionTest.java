/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.eclipse.parsson.tests;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JsonParsingException Tests
 *
 * @author Jitendra Kotamraju
 */
public class JsonParsingExceptionTest {

    @Test
    void testWrongJson() {
        // testMalformedJson("", null); Allowed in 1.1
    }

    @Test
    void testWrongJson1() {
        // testMalformedJson("{}{}", null);  Allowed in 1.1
    }

    @Test
    void testWrongJson2() {
        // testMalformedJson("{", null);  Allowed in 1.1
    }

    @Test
    void testWrongJson3() {
        testMalformedJson("{[]", null);
    }

    @Test
    void testWrongJson4() {
        testMalformedJson("{]", null);
    }

    @Test
    void testWrongJson5() {
        testMalformedJson("{\"a\":[]]", null);
    }

    @Test
    void testWrongJson6() {
        testMalformedJson("[ {}, [] }", null);
    }

    @Test
    void testWrongJson61() {
        testMalformedJson("[ {}, {} }", null);
    }

    @Test
    void testWrongJson7() {
        testMalformedJson("{ \"a\" : {}, \"b\": {} ]", null);
    }

    @Test
    void testWrongJson8() {
        testMalformedJson("{ \"a\" : {}, \"b\": [] ]", null);
    }

    @Test
    void testWrongUnicode() {
        testMalformedJson("[ \"\\uX00F\" ]", null);
        testMalformedJson("[ \"\\u000Z\" ]", null);
        testMalformedJson("[ \"\\u000\" ]", null);
        testMalformedJson("[ \"\\u00\" ]", null);
        testMalformedJson("[ \"\\u0\" ]", null);
        testMalformedJson("[ \"\\u\" ]", null);
        testMalformedJson("[ \"\\u\"", null);
        testMalformedJson("[ \"\\", null);
    }

    @Test
    void testControlChar() {
        testMalformedJson("[ \"\u0000\" ]", null);
        testMalformedJson("[ \"\u000c\" ]", null);
        testMalformedJson("[ \"\u000f\" ]", null);
        testMalformedJson("[ \"\u001F\" ]", null);
        testMalformedJson("[ \"\u001f\" ]", null);
    }

    @Test
    void testLocation1() {
        testMalformedJson("x", new MyLocation(1, 1, 0));
        testMalformedJson("{]", new MyLocation(1, 2, 1));
        testMalformedJson("[}", new MyLocation(1, 2, 1));
        testMalformedJson("[a", new MyLocation(1, 2, 1));
        testMalformedJson("[nuLl]", new MyLocation(1, 4, 3));
        testMalformedJson("[falsE]", new MyLocation(1, 6, 5));
        // testMalformedJson("[][]", new MyLocation(1, 3, 2));   allowed in 1.1
        testMalformedJson("[1234L]", new MyLocation(1, 6, 5));
    }

    @Test
    void testLocation2() {
        testMalformedJson("[null\n}", new MyLocation(2, 1, 6));
        testMalformedJson("[null\r\n}", new MyLocation(2, 1, 7));
        testMalformedJson("[null\n, null\n}", new MyLocation(3, 1, 13));
        testMalformedJson("[null\r\n, null\r\n}", new MyLocation(3, 1, 15));
    }

    private void testMalformedJson(String json, JsonLocation expected) {
        try (JsonParser parser = Json.createParser(new StringReader(json))) {
            while (parser.hasNext()) {
                parser.next();
            }
            Assertions.fail("Expected to throw JsonParsingException for " + json);
        } catch (JsonParsingException je) {
            // Expected
            if (expected != null) {
                JsonLocation got = je.getLocation();
                Assertions.assertEquals(expected.getLineNumber(), got.getLineNumber());
                Assertions.assertEquals(expected.getColumnNumber(), got.getColumnNumber());
                Assertions.assertEquals(expected.getStreamOffset(), got.getStreamOffset());
            }
        }
    }

    private static class MyLocation implements JsonLocation {
        private final long columnNo;
        private final long lineNo;
        private final long streamOffset;

        MyLocation(long lineNo, long columnNo, long streamOffset) {
            this.lineNo = lineNo;
            this.columnNo = columnNo;
            this.streamOffset = streamOffset;
        }

        @Override
        public long getLineNumber() {
            return lineNo;
        }

        @Override
        public long getColumnNumber() {
            return columnNo;
        }

        @Override
        public long getStreamOffset() {
            return streamOffset;
        }
    }

}
