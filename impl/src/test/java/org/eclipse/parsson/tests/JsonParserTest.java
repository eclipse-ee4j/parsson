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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import jakarta.json.stream.JsonParsingException;

import org.eclipse.parsson.api.BufferPool;

import org.junit.jupiter.api.Test;

/**
 * JsonParser Tests
 *
 * @author Jitendra Kotamraju
 */
public class JsonParserTest {
    static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    static final Charset UTF_32BE = Charset.forName("UTF-32BE");

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
}
