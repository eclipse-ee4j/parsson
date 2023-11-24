/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromObject;
import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromString;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import jakarta.json.*;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParserFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jakarta.json.stream.JsonParsingException;

import org.eclipse.parsson.api.BufferPool;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * JsonParser Tests
 *
 * @author Jitendra Kotamraju
 */
public class JsonParserTest {
    static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    static final Charset UTF_32BE = Charset.forName("UTF-32BE");

    private static final EnumSet<Event> GET_STRING_EVENT_ENUM_SET =
            EnumSet.of(JsonParser.Event.KEY_NAME, JsonParser.Event.VALUE_STRING, JsonParser.Event.VALUE_NUMBER);

    private static final EnumSet<JsonParser.Event> NOT_GET_VALUE_EVENT_ENUM_SET = EnumSet.of(JsonParser.Event.END_OBJECT, JsonParser.Event.END_ARRAY);

    private static final Collector<Map.Entry<String, JsonValue>, ?, ArrayList<String>> MAP_TO_LIST_COLLECTOR = Collector.of(ArrayList::new,
            (list, entry) -> {
                list.add(entry.getKey());
                list.add(entry.getValue().toString());
            },
            (left, right) -> {
                left.addAll(right);
                return left;
            },
            Collector.Characteristics.IDENTITY_FINISH);

    @Test
    void testReader() {
        JsonParser reader = Json.createParser(
                new StringReader("{ \"a\" : \"b\", \"c\" : null, \"d\" : [null, \"abc\"] }"));
        reader.close();
    }


    @Test
    void testEmptyArrayReader() {
        try (JsonParser parser = Json.createParser(new StringReader("[]"))) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStream() {
        try (JsonParser parser = Json.createParser(
                new ByteArrayInputStream(new byte[]{'[', ']'}))) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamUTF8() {
        ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
        try (JsonParser parser = Json.createParser(bin)) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamUTF16LE() {
        ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_16LE));
        try (JsonParser parser = Json.createParser(bin)) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamUTF16BE() {
        ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_16BE));
        try (JsonParser parser = Json.createParser(bin)) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamUTF32LE() {
        ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_32LE));
        try (JsonParser parser = Json.createParser(bin)) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamUTF32BE() {
        ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_32BE));
        try (JsonParser parser = Json.createParser(bin)) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamUTF16() {
        ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_16));
        try (JsonParser parser = Json.createParser(bin)) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStreamWithConfig() {
        Map<String, ?> config = new HashMap<>();
        try (JsonParser parser = Json.createParserFactory(config).createParser(
                new ByteArrayInputStream(new byte[]{'[', ']'}))) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStructure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createArrayBuilder().build())) {
            testEmptyArray(parser);
        }
    }

    @Test
    void testEmptyArrayStructureWithConfig() {
        Map<String, ?> config = new HashMap<>();
        try (JsonParser parser = Json.createParserFactory(config).createParser(
                Json.createArrayBuilder().build())) {
            testEmptyArray(parser);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    static void testEmptyArray(JsonParser parser) {
        while (parser.hasNext()) {
            parser.next();
        }
    }


    @Test
    void testEmptyArrayReaderIterator() {
        try (JsonParser parser = Json.createParser(new StringReader("[]"))) {
            testEmptyArrayIterator(parser);
        }
    }

    @Test
    void testEmptyArrayStructureIterator() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createArrayBuilder().build())) {
            testEmptyArrayIterator(parser);
        }
    }

    static void testEmptyArrayIterator(JsonParser parser) {
        assertEquals(true, parser.hasNext());
        assertEquals(true, parser.hasNext());
        assertEquals(Event.START_ARRAY, parser.next());

        assertEquals(true, parser.hasNext());
        assertEquals(true, parser.hasNext());
        assertEquals(Event.END_ARRAY, parser.next());

        assertEquals(false, parser.hasNext());
        assertEquals(false, parser.hasNext());
        try {
            parser.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException ne) {
        }
    }


    @Test
    void testEmptyArrayIterator2Reader() {
        try (JsonParser parser = Json.createParser(new StringReader("[]"))) {
            testEmptyArrayIterator2(parser);
        }
    }

    @Test
    void testEmptyArrayIterator2Structure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createArrayBuilder().build())) {
            testEmptyArrayIterator2(parser);
        }
    }

    static void testEmptyArrayIterator2(JsonParser parser) {
        assertEquals(Event.START_ARRAY, parser.next());
        assertEquals(Event.END_ARRAY, parser.next());
        try {
            parser.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException ne) {
        }
    }

    @Test
    void testEmptyArrayIterator3Reader() {
        try (JsonParser parser = Json.createParser(new StringReader("[]"))) {
            testEmptyArrayIterator3(parser);
        }
    }

    @Test
    void testEmptyArrayIterator3Structure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createArrayBuilder().build())) {
            testEmptyArrayIterator3(parser);
        }
    }

    static void testEmptyArrayIterator3(JsonParser parser) {
        assertEquals(Event.START_ARRAY, parser.next());
        assertEquals(Event.END_ARRAY, parser.next());
        assertEquals(false, parser.hasNext());
        try {
            parser.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException ne) {
        }
    }


    // Tests empty object
    @Test
    void testEmptyObjectReader() {
        try (JsonParser parser = Json.createParser(new StringReader("{}"))) {
            testEmptyObject(parser);
        }
    }

    @Test
    void testEmptyObjectStream() {
        try (JsonParser parser = Json.createParser(
                new ByteArrayInputStream(new byte[]{'{', '}'}))) {
            testEmptyObject(parser);
        }
    }

    @Test
    void testEmptyObjectStructure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createObjectBuilder().build())) {
            testEmptyObject(parser);
        }
    }

    @Test
    void testEmptyObjectStructureWithConfig() {
        Map<String, ?> config = new HashMap<>();
        try (JsonParser parser = Json.createParserFactory(config).createParser(
                Json.createObjectBuilder().build())) {
            testEmptyObject(parser);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    static void testEmptyObject(JsonParser parser) {
        while (parser.hasNext()) {
            parser.next();
        }
    }


    @Test
    void testEmptyObjectIteratorReader() {
        try (JsonParser parser = Json.createParser(new StringReader("{}"))) {
            testEmptyObjectIterator(parser);
        }
    }

    @Test
    void testEmptyObjectIteratorStructure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createObjectBuilder().build())) {
            testEmptyObjectIterator(parser);
        }
    }

    static void testEmptyObjectIterator(JsonParser parser) {
        assertEquals(true, parser.hasNext());
        assertEquals(true, parser.hasNext());
        assertEquals(Event.START_OBJECT, parser.next());

        assertEquals(true, parser.hasNext());
        assertEquals(true, parser.hasNext());
        assertEquals(Event.END_OBJECT, parser.next());

        assertEquals(false, parser.hasNext());
        assertEquals(false, parser.hasNext());
        try {
            parser.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException ne) {
        }
    }


    @Test
    void testEmptyObjectIterator2Reader() {
        try (JsonParser parser = Json.createParser(new StringReader("{}"))) {
            testEmptyObjectIterator2(parser);
        }
    }

    @Test
    void testEmptyObjectIterator2Structure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createObjectBuilder().build())) {
            testEmptyObjectIterator2(parser);
        }
    }

    static void testEmptyObjectIterator2(JsonParser parser) {
        assertEquals(Event.START_OBJECT, parser.next());
        assertEquals(Event.END_OBJECT, parser.next());
        try {
            parser.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException ne) {
        }
    }


    @Test
    void testEmptyObjectIterator3Reader() {
        try (JsonParser parser = Json.createParser(new StringReader("{}"))) {
            testEmptyObjectIterator3(parser);
        }
    }

    @Test
    void testEmptyObjectIterator3Structure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createObjectBuilder().build())) {
            testEmptyObjectIterator3(parser);
        }
    }

    static void testEmptyObjectIterator3(JsonParser parser) {
        assertEquals(Event.START_OBJECT, parser.next());
        assertEquals(Event.END_OBJECT, parser.next());
        assertEquals(false, parser.hasNext());
        try {
            parser.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException ne) {
            // expected
        }
    }


    @Test
    void testWikiIteratorReader() throws Exception {
        try (JsonParser parser = Json.createParser(wikiReader())) {
            testWikiIterator(parser);
        }
    }

    @Test
    void testWikiIteratorStructure() throws Exception {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                JsonBuilderTest.buildPerson())) {
            testWikiIterator(parser);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    static void testWikiIterator(JsonParser parser) throws Exception {
        while (parser.hasNext()) {
            parser.next();
        }
    }

    @Test
    void testWikiInputStream() throws Exception {
        try (JsonParser parser = Json.createParser(wikiStream())) {
            testWiki(parser);
        }
    }

    @Test
    void testWikiInputStreamUTF16LE() throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(wikiString()
                .getBytes(StandardCharsets.UTF_16LE));
        try (JsonParser parser = Json.createParser(bin)) {
            testWiki(parser);
        }
    }

    @Test
    void testWikiReader() throws Exception {
        try (JsonParser parser = Json.createParser(wikiReader())) {
            testWiki(parser);
        }
    }

    @Test
    void testWikiStructure() throws Exception {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                JsonBuilderTest.buildPerson())) {
            testWiki(parser);
        }
    }

    static void testWiki(JsonParser parser) {

        Event event = parser.next();
        assertEquals(Event.START_OBJECT, event);

        testObjectStringValue(parser, "firstName", "John");
        testObjectStringValue(parser, "lastName", "Smith");

        event = parser.next();
        assertEquals(Event.KEY_NAME, event);
        assertEquals("age", parser.getString());

        event = parser.next();
        assertEquals(Event.VALUE_NUMBER, event);
        assertEquals(25, parser.getInt());
        assertEquals(25, parser.getLong());
        assertEquals(25, parser.getBigDecimal().intValue());
        assertTrue( parser.isIntegralNumber());

        event = parser.next();
        assertEquals(Event.KEY_NAME, event);
        assertEquals("address", parser.getString());

        event = parser.next();
        assertEquals(Event.START_OBJECT, event);


        testObjectStringValue(parser, "streetAddress", "21 2nd Street");
        testObjectStringValue(parser, "city", "New York");
        testObjectStringValue(parser, "state", "NY");
        testObjectStringValue(parser, "postalCode", "10021");

        event = parser.next();
        assertEquals(Event.END_OBJECT, event);

        event = parser.next();
        assertEquals(Event.KEY_NAME, event);
        assertEquals("phoneNumber", parser.getString());

        event = parser.next();
        assertEquals(Event.START_ARRAY, event);
        event = parser.next();
        assertEquals(Event.START_OBJECT, event);
        testObjectStringValue(parser, "type", "home");
        testObjectStringValue(parser, "number", "212 555-1234");
        event = parser.next();
        assertEquals(Event.END_OBJECT, event);

        event = parser.next();
        assertEquals(Event.START_OBJECT, event);
        testObjectStringValue(parser, "type", "fax");
        testObjectStringValue(parser, "number", "646 555-4567");
        event = parser.next();
        assertEquals(Event.END_OBJECT, event);
        event = parser.next();
        assertEquals(Event.END_ARRAY, event);

        event = parser.next();
        assertEquals(Event.END_OBJECT, event);
    }

    static void testObjectStringValue(JsonParser parser, String name, String value) {
        Event event = parser.next();
        assertEquals(Event.KEY_NAME, event);
        assertEquals(name, parser.getString());

        event = parser.next();
        assertEquals(Event.VALUE_STRING, event);
        assertEquals(value, parser.getString());
    }

    @Test
    void testNestedArrayReader() {
        try (JsonParser parser = Json.createParser(new StringReader("[[],[[]]]"))) {
            testNestedArray(parser);
        }
    }

    @Test
    void testNestedArrayStructure() {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                Json.createArrayBuilder()
                        .add(Json.createArrayBuilder())
                        .add(Json.createArrayBuilder()
                                .add(Json.createArrayBuilder()))
                        .build())) {
            testNestedArray(parser);
        }
    }

    static void testNestedArray(JsonParser parser) {
        assertEquals(Event.START_ARRAY, parser.next());
        assertEquals(Event.START_ARRAY, parser.next());
        assertEquals(Event.END_ARRAY, parser.next());
        assertEquals(Event.START_ARRAY, parser.next());
        assertEquals(Event.START_ARRAY, parser.next());
        assertEquals(Event.END_ARRAY, parser.next());
        assertEquals(Event.END_ARRAY, parser.next());
        assertEquals(Event.END_ARRAY, parser.next());
        assertEquals(false, parser.hasNext());
        assertEquals(false, parser.hasNext());
    }

    @Test
    void testExceptionsReader() throws Exception {
        try (JsonParser parser = Json.createParser(wikiReader())) {
            testExceptions(parser);
        }
    }

    @Test
    void testExceptionsStructure() throws Exception {
        try (JsonParser parser = Json.createParserFactory(null).createParser(
                JsonBuilderTest.buildPerson())) {
            testExceptions(parser);
        }
    }

    static void testExceptions(JsonParser parser) {

        Event event = parser.next();
        assertEquals(Event.START_OBJECT, event);

        try {
            parser.getString();
            fail("JsonParser#getString() should have thrown exception in START_OBJECT state");
        } catch (IllegalStateException expected) {
            // no-op
        }

        try {
            parser.isIntegralNumber();
            fail("JsonParser#getNumberType() should have thrown exception in START_OBJECT state");
        } catch (IllegalStateException expected) {
            // no-op
        }

        try {
            parser.getInt();
            fail("JsonParser#getInt() should have thrown exception in START_OBJECT state");
        } catch (IllegalStateException expected) {
            // no-op
        }

        try {
            parser.getLong();
            fail("JsonParser#getLong() should have thrown exception in START_OBJECT state");
        } catch (IllegalStateException expected) {
            // no-op
        }

        try {
            parser.getBigDecimal();
            fail("JsonParser#getBigDecimal() should have thrown exception in START_OBJECT state");
        } catch (IllegalStateException expected) {
            // no-op
        }
    }

    static String wikiString() {
        String str;
        try (Scanner scanner = new Scanner(wikiReader())
                .useDelimiter("\\A")) {
            str = scanner.hasNext() ? scanner.next() : "";
        }
        return str;
    }

    static InputStream wikiStream() {
        return JsonParserTest.class.getResourceAsStream("/wiki.json");
    }

    static Reader wikiReader() {
        return new InputStreamReader(
                JsonParserTest.class.getResourceAsStream("/wiki.json"), StandardCharsets.UTF_8);
    }

    @Test
    void testIntNumber() {
        JsonParserFactory factory = Json.createParserFactory(null);

        Random r = new Random(System.currentTimeMillis());

        for(int i=0; i < 100000; i++) {
            long num = i%2 == 0 ? r.nextInt() : r.nextLong();
            try (JsonParser parser = factory.createParser(new StringReader("["+num+"]"))) {
                parser.next();
                parser.next();
                assertEquals(new BigDecimal(num).intValue(), parser.getInt(), "Fails for num="+num);
            }
        }

    }

    @Test
    void testBigDecimalGetString() {
        JsonParserFactory f = Json.createParserFactory(null);
        JsonObject obj = Json.createObjectBuilder().add("a", BigDecimal.ONE).build();
        try (JsonParser parser = f.createParser(obj)) {
            parser.next();
            parser.next();
            parser.next();
            assertEquals("1", parser.getString(), "Fails for BigDecimal=1");
        }
    }

    @Test
    void testIntGetString() {
        JsonParserFactory f = Json.createParserFactory(null);
        JsonObject obj = Json.createObjectBuilder().add("a", 5).build();
        try (JsonParser parser = f.createParser(obj)) {
            parser.next();
            parser.next();
            parser.next();
            assertEquals("5", parser.getString(), "Fails for int=5");
        }
    }

    static class MyBufferPool implements BufferPool {
        private boolean takeCalled;
        private boolean recycleCalled;
        private final char[] buf;

        MyBufferPool(int size) {
            buf = new char[size];
        }

        @Override
        public char[] take() {
            takeCalled = true;
            return buf;
        }

        @Override
        public void recycle(char[] buf) {
            recycleCalled = true;
        }

        boolean isTakeCalled() {
            return takeCalled;
        }

        boolean isRecycleCalled() {
            return recycleCalled;
        }
    }

    @Test
    void testBufferPoolFeature() {
        final MyBufferPool bufferPool = new MyBufferPool(1024);
        Map<String, Object> config = new HashMap<String, Object>() {{
            put(BufferPool.class.getName(), bufferPool);
        }};

        JsonParserFactory factory = Json.createParserFactory(config);
        try (JsonParser parser = factory.createParser(new StringReader("[]"))) {
            parser.next();
            parser.next();
        }
        assertTrue(bufferPool.isTakeCalled());
        assertTrue(bufferPool.isRecycleCalled());
    }

    @Test
    void testBufferSizes() {
        Random r = new Random(System.currentTimeMillis());
        for(int size=100; size < 1000; size++) {
            final MyBufferPool bufferPool = new MyBufferPool(size);
            Map<String, Object> config = new HashMap<String, Object>() {{
                put(BufferPool.class.getName(), bufferPool);
            }};
            JsonParserFactory factory = Json.createParserFactory(config);

            StringBuilder sb = new StringBuilder();
            for(int i=0; i < 1000; i++) {
                sb.append('a');
                String name = sb.toString();
                long num = i%2 == 0 ? r.nextInt() : r.nextLong();
                String str = "{\""+name+"\":["+num+"]}";
                try (JsonParser parser = factory.createParser(new StringReader(str))) {
                    parser.next();
                    parser.next();
                    assertEquals(name, parser.getString(), "Fails for " + str);
                    parser.next();
                    parser.next();
                    assertEquals(new BigDecimal(num).intValue(), parser.getInt(), "Fails for "+str);
                }
            }
        }
    }

    // Tests for string starting on buffer boundary (JSONP-15)
    // xxxxxxx"xxxxxxxxx"
    //        ^
    //        |
    //       4096
    @Test
    void testStringUsingStandardBuffer() throws Throwable {
        JsonParserFactory factory = Json.createParserFactory(null);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < 40000; i++) {
            sb.append('a');
            String name = sb.toString();
            String str = "{\""+name+"\":\""+name+"\"}";
            try (JsonParser parser = factory.createParser(new StringReader(str))) {
                parser.next();
                parser.next();
                assertEquals(name, parser.getString(), "Fails for size=" + i);
                parser.next();
                assertEquals(name, parser.getString(), "Fails for size=" + i);
            } catch (Throwable e) {
                throw new Throwable("Failed for size=" + i, e);
            }
        }
    }

    // Tests for int starting on buffer boundary
    // xxxxxxx"xxxxxxxxx"
    //        ^
    //        |
    //       4096
    @Test
    void testIntegerUsingStandardBuffer() throws Throwable {
        Random r = new Random(System.currentTimeMillis());
        JsonParserFactory factory = Json.createParserFactory(null);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < 40000; i++) {
            sb.append('a');
            String name = sb.toString();
            int num = r.nextInt();
            String str = "{\"" + name + "\":" + num + "}";
            try (JsonParser parser = factory.createParser(new StringReader(str))) {
                parser.next();
                parser.next();
                assertEquals(name, parser.getString(), "Fails for size=" + i);
                parser.next();
                assertEquals(num, parser.getInt(), "Fails for size=" + i);
            } catch (Throwable e) {
                throw new Throwable("Failed for size=" + i, e);
            }
        }
    }

    @Test
    void testStringUsingBuffers() throws Throwable {
        for(int size=20; size < 500; size++) {
            final MyBufferPool bufferPool = new MyBufferPool(size);
            Map<String, Object> config = new HashMap<String, Object>() {{
                put(BufferPool.class.getName(), bufferPool);
            }};
            JsonParserFactory factory = Json.createParserFactory(config);

            StringBuilder sb = new StringBuilder();
            for(int i=0; i < 1000; i++) {
                sb.append('a');
                String name = sb.toString();
                String str = "{\""+name+"\":\""+name+"\"}";
                JsonLocation location;
                try (JsonParser parser = factory.createParser(new StringReader(str))) {
                    parser.next();
                    parser.next();
                    assertEquals(name, parser.getString(), "name fails for buffer size=" + size + " name length=" + i);
                    location = parser.getLocation();
                    assertEquals(name.length() + 3, location.getStreamOffset(), "Stream offset fails for buffer size=" + size + " name length=" + i);
                    assertEquals(name.length() + 4, location.getColumnNumber(), "Column value fails for buffer size=" + size + " name length=" + i);
                    assertEquals(1, location.getLineNumber(), "Line value fails for buffer size=" + size + " name length=" + i);

                    parser.next();
                    assertEquals(name, parser.getString(), "value fails for buffer size=" + size + " name length=" + i);
                    location = parser.getLocation();
                    assertEquals(2 * name.length() + 6, location.getStreamOffset(), "Stream offset fails for buffer size=" + size + " name length=" + i);
                    assertEquals(2 * name.length() + 7, location.getColumnNumber(), "Column value fails for buffer size=" + size + " name length=" + i);
                    assertEquals(1, location.getLineNumber(), "Line value fails for buffer size=" + size + " name length=" + i);
                } catch (Throwable e) {
                    throw new Throwable("Failed for buffer size=" + size + " name length=" + i, e);
                }
            }
        }
    }

    @Test
    void testExceptionsFromHasNext() {
        checkExceptionFromHasNext("{");
        checkExceptionFromHasNext("{\"key\"");
        checkExceptionFromHasNext("{\"name\" : \"prop\"");
        checkExceptionFromHasNext("{\"name\" : 3");
        checkExceptionFromHasNext("{\"name\" : true");
        checkExceptionFromHasNext("{\"name\" : null");
        checkExceptionFromHasNext("{\"name\" : {\"$eq\":\"cdc\"}");
        checkExceptionFromHasNext("{\"name\" : [{\"$eq\":\"cdc\"}]");
        checkExceptionFromHasNext("[");
        checkExceptionFromHasNext("{\"name\" : [{\"key\" : [[{\"a\" : 1}]");
        checkExceptionFromHasNext("{\"unique\":true,\"name\":\"jUnitTestIndexNeg005\", \"fields\":[{\"order\":-1,\"path\":\"city.zip\"}");
    }

    @Test
    void testEOFFromHasNext() {
        checkExceptionFromHasNext("{ \"d\" : 1 } 2 3 4");
        checkExceptionFromHasNext("[ {\"d\" : 1 }] 2 3 4");
        checkExceptionFromHasNext("1 2 3 4");
        checkExceptionFromHasNext("null 2 3 4");
    }

    @Test
    void testExceptionsFromNext() {
        checkExceptionFromNext("{\"name\" : fal");
        checkExceptionFromNext("{\"name\" : nu");
        checkExceptionFromNext("{\"name\" : \"pro");
        checkExceptionFromNext("{\"key\":");
        checkExceptionFromNext("fal");
    }

    private void checkExceptionFromHasNext(String input) {
        try (JsonParser parser = Json.createParser(new StringReader(input))) {
            try {
                while (parser.hasNext()) {
                    try {
                        parser.next();
                    } catch (Throwable t1) {
                        fail("Exception should occur from hasNext() for '" + input + "'");
                    }
                }
            } catch (JsonParsingException t) {
                //this is OK
                return;
            }
        }
        fail();
    }

    private void checkExceptionFromNext(String input) {
        try (JsonParser parser = Json.createParser(new StringReader(input))) {
            while (parser.hasNext()) {
                try {
                    parser.next();
                } catch (JsonParsingException t) {
                    //this is OK
                    return;
                }
            }
        }
        fail();
    }

    @Nested
    public class DirectParserTests {
        @Test
        void testNumbersStructure() {
            testWithCreateParserFromObject(Json.createObjectBuilder()
                    .add("int", 1)
                    .add("long", 1L)
                    .add("double", 1d)
                    .add("BigInteger", BigInteger.TEN)
                    .add("BigDecimal", BigDecimal.TEN)
                    .build(), this::testNumbers);
        }

        @Test
        void testNumbersString() {
            testWithCreateParserFromString("{\"int\":1,\"long\":1,\"double\":1.0,\"BigInteger\":10,\"BigDecimal\":10}", this::testNumbers);
        }

        private void testNumbers(JsonParser parser) {
            parser.next();
            parser.next();
            parser.getString();
            parser.next();
            assertTrue(parser.isIntegralNumber());
            assertEquals(1, parser.getInt());

            parser.next();
            parser.getString();
            parser.next();
            assertTrue(parser.isIntegralNumber());
            assertEquals(1L, parser.getLong());

            parser.next();
            parser.getString();
            parser.next();
            assertFalse(parser.isIntegralNumber());
            assertEquals(BigDecimal.valueOf(1d), parser.getBigDecimal());

            parser.next();
            parser.getString();
            parser.next();
            assertTrue(parser.isIntegralNumber());
            assertEquals(BigDecimal.TEN, parser.getBigDecimal());

            parser.next();
            parser.getString();
            parser.next();
            assertTrue(parser.isIntegralNumber());
            assertEquals(BigDecimal.TEN, parser.getBigDecimal());
        }

        @Test
        void testParser_getStringStructure(){
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::testParser_getString);
        }

        @Test
        void testParser_getStringString(){
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::testParser_getString);
        }

        private void testParser_getString(JsonParser parser) {
            List<String> values = new ArrayList<>();
            parser.next();
            while (parser.hasNext()) {
                Event event = parser.next();
                if (GET_STRING_EVENT_ENUM_SET.contains(event)) {
                    String strValue = Objects.toString(parser.getString(), "null");
                    values.add(strValue);
                }
            }

            assertThat(values,TestData.FAMILY_MATCHER_WITH_NO_QUOTATION);
        }

        @Test
        void testParser_getValueStructure(){
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::testParser_getValue);
        }

        @Test
        void testParser_getValueString(){
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::testParser_getValue);
        }

        private void testParser_getValue(JsonParser parser) {
            List<String> values = new ArrayList<>();
            parser.next();
            while (parser.hasNext()) {
                Event event = parser.next();
                if (!NOT_GET_VALUE_EVENT_ENUM_SET.contains(event)) {
                    String strValue = Objects.toString(parser.getValue(), "null");
                    values.add(strValue);
                }
            }

            assertThat(values, TestData.FAMILY_MATCHER_KEYS_WITH_QUOTATION);
        }

        @Test
        void testSkipArrayStructure() {
            testWithCreateParserFromObject(TestData.createObjectWithArrays(), this::testSkipArray);
        }

        @Test
        void testSkipArrayString() {
            testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_ARRAYS, this::testSkipArray);
        }

        private void testSkipArray(JsonParser parser) {
            parser.next();
            parser.next();
            parser.getString();
            parser.next();
            parser.skipArray();
            parser.next();
            String key = parser.getString();

            assertEquals("secondElement", key);
        }

        @Test
        void testSkipObjectStructure() {
            testWithCreateParserFromObject(TestData.createJsonObject(), this::testSkipObject);
        }

        @Test
        void testSkipObjectString() {
            testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_OBJECTS, this::testSkipObject);
        }

        private void testSkipObject(JsonParser parser) {
            parser.next();
            parser.next();
            parser.getString();
            parser.next();
            parser.skipObject();
            parser.next();
            String key = parser.getString();

            assertEquals("secondPerson", key);
        }

        private void assertThrowsIllegalStateException(Executable executable) {
            assertThrows(IllegalStateException.class, executable);
        }

        @Test
        void testErrorGetObjectStructure() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromObject(TestData.createJsonObject(), JsonParser::getObject));
        }

        @Test
        void testErrorGetObjectString() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_OBJECTS, JsonParser::getObject));
        }

        @Test
        void testErrorGetArrayStructure() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromObject(TestData.createJsonObject(), this::testErrorGetArray));
        }

        @Test
        void testErrorGetArrayString() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_OBJECTS, this::testErrorGetArray));
        }

        private void testErrorGetArray(JsonParser parser) {
            parser.next();
            parser.getArray();
        }

        @Test
        void testErrorGetValueEndOfObjectStructure() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromObject(TestData.createJsonObject(), this::testErrorGetValueEndOfObject));
        }

        @Test
        void testErrorGetValueEndOfObjectString() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_OBJECTS, this::testErrorGetValueEndOfObject));
        }

        private void testErrorGetValueEndOfObject(JsonParser parser) {
            parser.next();
            parser.skipObject();
            parser.getValue();
        }

        @Test
        void testErrorGetValueEndOfArrayStructure() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromObject(TestData.createObjectWithArrays(), this::testErrorGetValueEndOfArray));
        }

        @Test
        void testErrorGetValueEndOfArrayString() {
            assertThrowsIllegalStateException(() -> testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_ARRAYS, this::testErrorGetValueEndOfArray));
        }

        private void testErrorGetValueEndOfArray(JsonParser parser) {
            parser.next();
            parser.next();
            parser.getString();
            parser.next();
            parser.skipArray();
            parser.getValue();
        }

        @Test
        void testBooleanNullandCurrentEventStructure() {
            testWithCreateParserFromObject(Json.createObjectBuilder()
                    .add("true", true)
                    .add("false", false)
                    .addNull("null")
                    .build(), this::testBooleanNullandCurrentEvent);
        }

        @Test
        void testBooleanNullandCurrentEventString() {
            testWithCreateParserFromString("{\"true\":true,\"false\":false,\"null\":null}", this::testBooleanNullandCurrentEvent);
        }

        private void testBooleanNullandCurrentEvent(JsonParser parser) {
            parser.next();
            parser.next();
            parser.getValue();
            parser.next();
            assertEquals(JsonValue.ValueType.TRUE, parser.getValue().getValueType());
            parser.next();
            parser.getValue();
            parser.next();
            assertEquals(JsonValue.ValueType.FALSE, parser.getValue().getValueType());
            parser.next();
            parser.getValue();
            parser.next();
            assertEquals(JsonValue.ValueType.NULL, parser.getValue().getValueType());
            assertEquals(Event.VALUE_NULL, parser.currentEvent());
        }

        @Test
        void testBigLongAndDecimalsStructure() {
            testWithCreateParserFromObject(Json.createObjectBuilder()
                    .add("long", 12345678901234567L)
                    .add("longer", 1234567890123456789L)
                    .build(), this::testBigLongAndDecimals);
        }

        @Test
        void testBigLongAndDecimalsString() {
            testWithCreateParserFromString("{\"long\":12345678901234567,\"longer\":1234567890123456789}", this::testBigLongAndDecimals);
        }

        private void testBigLongAndDecimals(JsonParser parser) {
            parser.next();
            parser.next();
            parser.getString();
            parser.next();
            assertEquals("12345678901234567", parser.getValue().toString());
            parser.next();
            parser.getString();
            parser.next();
            assertEquals("1234567890123456789", parser.getValue().toString());
        }

        private void assertThrowsJsonParsingException(Executable executable) {
            assertThrows(JsonParsingException.class, executable);
        }

        @Test
        void testWrongValueAndEndOfObjectInArray() {//509 ArrayContext.getNextEvent, no coma
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("{\"a\":[5 }]}", parser -> {
                parser.next();
                parser.next();
                parser.getString();
                parser.next();
                parser.getValue();
            }));
        }

        @Test
        void testWrongEndOfObjectInArray() {//518 ArrayContext.getNextEvent, at the end
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("{\"a\":[}, 3]}", parser -> {
                parser.next();
                parser.next();
                parser.getString();
                parser.next();
                parser.getValue();
            }));
        }

        @Test
        void testWrongKey() {//477 ObjectContext.getNextEvent, at the end
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("{\"a\":1, 5}", parser -> {
                parser.next();
                parser.next();
                parser.getString();
                parser.next();
                parser.getValue();
                parser.next();
            }));
        }

        @Test
        void testErrorInTheValue() {//470 ObjectContext.getNextEvent, no coma
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("{\"a\":1:}", parser -> {
                parser.next();
                parser.next();
                parser.getString();
                parser.next();
                parser.getValue();
                parser.next();
            }));
        }

        @Test
        void testNoValueAfterKey() {//452 ObjectContext.getNextEvent, no colon
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("{\"a\"}", parser -> {
                parser.next();
                parser.next();
                parser.getString();
                parser.next();
            }));
        }

        @Test
        void testNoJSONAtAll() {//382 NoneContext.getNextEvent, at the end
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("", JsonParser::next));
        }

        @Test
        void testWrongArrayEndWithComa() {//518 ArrayContext.getNextEvent, at the end
            assertThrowsJsonParsingException(() -> testWithCreateParserFromString("[,", parser -> {
                parser.next();
                parser.getArray();
            }));
        }
    }

    @Nested
    class StreamTests {
        @Test
        void testGetValueStream_GetOneElement_Structure() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::testGetValueStream_GetOneElement);
        }

        @Test
        void testGetValueStream_GetOneElement_String() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::testGetValueStream_GetOneElement);
        }

        private void testGetValueStream_GetOneElement(JsonParser parser) {
            JsonString name = (JsonString) parser.getValueStream()
                    .map(JsonValue::asJsonObject)
                    .map(JsonObject::values)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No value present"))
                    .stream()
                    .filter(e -> e.getValueType()  == JsonValue.ValueType.STRING)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Name not found"));

            assertEquals("John", name.getString());
        }

        @Test
        void testGetValueStream_GetListStructure() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::testGetValueStream_GetList);
        }

        @Test
        void testGetValueStream_GetListString() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::testGetValueStream_GetList);
        }

        private void testGetValueStream_GetList(JsonParser parser) {
            List<String> values = parser.getValueStream().map(value -> Objects.toString(value, "null")).collect(Collectors.toList());

            assertThat(values, contains(TestData.JSON_FAMILY_STRING));
        }

        @Test
        void testGetArrayStream_GetOneElementStructure() {
            testWithCreateParserFromObject(TestData.createObjectWithArrays(), this::testGetArrayStream_GetOneElement);
        }

        @Test
        void testGetArrayStream_GetOneElementString() {
            testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_ARRAYS, this::testGetArrayStream_GetOneElement);
        }

        private void testGetArrayStream_GetOneElement(JsonParser parser) {
            parser.next();
            parser.next();
            String key = parser.getString();
            parser.next();
            JsonString element = (JsonString) parser.getArrayStream().filter(e -> e.getValueType()  == JsonValue.ValueType.STRING)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Element not found"));

            assertEquals("first", element.getString());
            assertEquals("firstElement", key);
        }

        @Test
        void testGetArrayStream_GetListStructure() {
            testWithCreateParserFromObject(TestData.createObjectWithArrays(), this::testGetArrayStream_GetList);
        }

        @Test
        void testGetArrayStream_GetListString() {
            testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_ARRAYS, this::testGetArrayStream_GetList);
        }

        private void testGetArrayStream_GetList(JsonParser parser) {
            parser.next();
            parser.next();
            String key = parser.getString();
            parser.next();
            List<String> values = parser.getArrayStream().map(value -> Objects.toString(value, "null")).collect(Collectors.toList());

            assertThat(values, TestData.ARRAY_STREAM_MATCHER);
            assertEquals("firstElement", key);
        }

        @Test
        void testGetObjectStream_GetOneElementStructure() {
            testWithCreateParserFromObject(TestData.createJsonObject(), this::testGetObjectStream_GetOneElement);
        }

        @Test
        void testGetObjectStream_GetOneElementString() {
            testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_OBJECTS, this::testGetObjectStream_GetOneElement);
        }

        private void testGetObjectStream_GetOneElement(JsonParser parser) {
            parser.next();
            String surname = parser.getObjectStream().filter(e -> e.getKey().equals("firstPerson"))
                    .map(Map.Entry::getValue)
                    .map(JsonValue::asJsonObject)
                    .map(obj -> obj.getString("surname"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Surname not found"));

            assertEquals("Smith", surname);
        }

        @Test
        void testGetObjectStream_GetListStructure() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::testGetObjectStream_GetList);
        }

        @Test
        void testGetObjectStream_GetListString() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::testGetObjectStream_GetList);
        }

        private void testGetObjectStream_GetList(JsonParser parser) {
            parser.next();
            List<String> values = parser.getObjectStream().collect(MAP_TO_LIST_COLLECTOR);

            assertThat(values, TestData.FAMILY_MATCHER_KEYS_WITHOUT_QUOTATION);
        }
    }

    @Nested
    public class JSONPStandardParserTests {
        @Test
        void testStandardStructureParser_getValueStream() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::test_getValueStream);
        }

        @Test
        void testStandardStringParser_getValueStream() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::test_getValueStream);
        }

        private void test_getValueStream(JsonParser parser) {
            List<String> values = parser.getValueStream().map(value -> Objects.toString(value, "null")).collect(Collectors.toList());

            assertThat(values, contains(TestData.JSON_FAMILY_STRING));
        }

        @Test
        void testStandardStructureParser_getArrayStream() {
            testWithCreateParserFromObject(TestData.createObjectWithArrays(), this::test_getArrayStream);
        }

        @Test
        void testStandardStringParser_getArrayStream() {
            testWithCreateParserFromString(TestData.JSON_OBJECT_WITH_ARRAYS, this::test_getArrayStream);
        }

        private void test_getArrayStream(JsonParser parser) {
            parser.next();
            parser.next();
            String key = parser.getString();
            parser.next();
            List<String> values = parser.getArrayStream().map(value -> Objects.toString(value, "null")).collect(Collectors.toList());

            assertThat(values, TestData.ARRAY_STREAM_MATCHER);
            assertEquals("firstElement", key);
        }

        @Test
        void testStandardStructureParser_getObjectStream() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::test_getObjectStream);
        }

        @Test
        void testStandardStringParser_getObjectStream() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::test_getObjectStream);
        }

        private void test_getObjectStream(JsonParser parser) {
            parser.next();
            List<String> values = parser.getObjectStream().collect(MAP_TO_LIST_COLLECTOR);

            assertThat(values, TestData.FAMILY_MATCHER_KEYS_WITHOUT_QUOTATION);
        }

        @Test
        void testStandardStructureParser_getValue() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::test_getValue);
        }

        @Test
        void testStandardStringParser_getValue() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::test_getValue);
        }

        private void test_getValue(JsonParser parser) {
            List<String> values = new ArrayList<>();
            parser.next();
            while (parser.hasNext()) {
                Event event = parser.next();
                if (!NOT_GET_VALUE_EVENT_ENUM_SET.contains(event)) {
                    String strValue = Objects.toString(parser.getValue(), "null");
                    values.add(strValue);
                }
            }

            assertThat(values, TestData.FAMILY_MATCHER_KEYS_WITH_QUOTATION);
        }

        @Test
        void testStandardStructureParser_getString() {
            testWithCreateParserFromObject(TestData.createFamilyPerson(), this::test_getString);
        }

        @Test
        void testStandardStringParser_getString() {
            testWithCreateParserFromString(TestData.JSON_FAMILY_STRING, this::test_getString);
        }

        private void test_getString(JsonParser parser) {
            List<String> values = new ArrayList<>();
            parser.next();
            while (parser.hasNext()) {
                Event event = parser.next();
                if (GET_STRING_EVENT_ENUM_SET.contains(event)) {
                    String strValue = Objects.toString(parser.getString(), "null");
                    values.add(strValue);
                }
            }

            assertThat(values, TestData.FAMILY_MATCHER_WITH_NO_QUOTATION);
        }
    }

    private static class TestData {
        private static final String JSON_OBJECT_WITH_OBJECTS = "{\"firstPerson\":{\"name\":\"John\", \"surname\":\"Smith\"}," +
                "\"secondPerson\":{\"name\":\"Deborah\", \"surname\":\"Harris\"}}";

        private static final String JSON_OBJECT_WITH_ARRAYS = "{\"firstElement\":[\"first\", \"second\"],\"secondElement\":[\"third\", \"fourth\"]}";

        private static final String JSON_FAMILY_STRING = "{\"name\":\"John\",\"surname\":\"Smith\",\"age\":30,\"married\":true," +
                "\"wife\":{\"name\":\"Deborah\",\"surname\":\"Harris\"},\"children\":[\"Jack\",\"Mike\"]}";

        private static final Matcher<Iterable<? extends String>> FAMILY_MATCHER_KEYS_WITHOUT_QUOTATION =
                Matchers.contains("name", "\"John\"", "surname", "\"Smith\"", "age", "30", "married", "true", "wife",
                        "{\"name\":\"Deborah\",\"surname\":\"Harris\"}", "children", "[\"Jack\",\"Mike\"]");

        private static final Matcher<Iterable<? extends String>> FAMILY_MATCHER_KEYS_WITH_QUOTATION =
                Matchers.contains("\"name\"", "\"John\"", "\"surname\"", "\"Smith\"", "\"age\"", "30", "\"married\"", "true",
                        "\"wife\"", "{\"name\":\"Deborah\",\"surname\":\"Harris\"}", "\"children\"", "[\"Jack\",\"Mike\"]");

        private static final Matcher<Iterable<? extends String>> FAMILY_MATCHER_WITH_NO_QUOTATION =
                Matchers.contains("name", "John", "surname", "Smith", "age", "30", "married",
                        "wife", "name", "Deborah", "surname", "Harris", "children", "Jack", "Mike");

        private static final Matcher<Iterable<? extends String>> ARRAY_STREAM_MATCHER = Matchers.contains("\"first\"", "\"second\"");

        private static JsonObject createFamilyPerson() {
            return Json.createObjectBuilder()
                    .add("name", "John")
                    .add("surname", "Smith")
                    .add("age", 30)
                    .add("married", true)
                    .add("wife", createPerson("Deborah", "Harris"))
                    .add("children", createArray("Jack", "Mike"))
                    .build();
        }

        private static JsonObject createObjectWithArrays() {
            return Json.createObjectBuilder()
                    .add("firstElement", createArray("first", "second"))
                    .add("secondElement", createArray("third", "fourth"))
                    .build();
        }

        private static JsonArrayBuilder createArray(String firstElement, String secondElement) {
            return Json.createArrayBuilder().add(firstElement).add(secondElement);
        }

        private static JsonObject createJsonObject() {
            return Json.createObjectBuilder()
                    .add("firstPerson", createPerson("John", "Smith"))
                    .add("secondPerson", createPerson("Deborah", "Harris"))
                    .build();
        }

        private static JsonObjectBuilder createPerson(String name, String surname) {
            return Json.createObjectBuilder()
                    .add("name", name)
                    .add("surname", surname);
        }
    }
}
