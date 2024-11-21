/*
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerationException;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

import org.eclipse.parsson.api.BufferPool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * {@link JsonGenerator} tests
 *
 * @author Jitendra Kotamraju
 */
public class JsonGeneratorTest {

    @Test
    void testObjectWriter() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        testObject(generator);
        generator.close();
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(writer.toString()));
        JsonObject person = reader.readObject();
        JsonObjectTest.testPerson(person);
    }

    @Test
    void testObjectStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator generator = Json.createGenerator(out);
        testObject(generator);
        generator.close();
        out.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        JsonReader reader = Json.createReader(in);
        JsonObject person = reader.readObject();
        JsonObjectTest.testPerson(person);
        reader.close();
        in.close();
    }

    static void testObject(JsonGenerator generator) {
        generator
                .writeStartObject()
                .write("firstName", "John")
                .write("lastName", "Smith")
                .write("age", 25)
                .writeStartObject("address")
                .write("streetAddress", "21 2nd Street")
                .write("city", "New York")
                .write("state", "NY")
                .write("postalCode", "10021")
                .writeEnd()
                .writeStartArray("phoneNumber")
                .writeStartObject()
                .write("type", "home")
                .write("number", "212 555-1234")
                .writeEnd()
                .writeStartObject()
                .write("type", "fax")
                .write("number", "646 555-4567")
                .writeEnd()
                .writeEnd()
                .writeEnd();
    }

    @Test
    void testArray() {
        Writer sw = new StringWriter();
        JsonGenerator generator = Json.createGenerator(sw);
        generator
                .writeStartArray()
                .writeStartObject()
                .write("type", "home")
                .write("number", "212 555-1234")
                .writeEnd()
                .writeStartObject()
                .write("type", "fax")
                .write("number", "646 555-4567")
                .writeEnd()
                .writeEnd();
        generator.close();
    }

    // tests JsonGenerator when JsonValue is used for generation
    @Test
    void testJsonValue() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator
                .writeStartObject()
                .write("firstName", "John")
                .write("lastName", "Smith")
                .write("age", 25)
                .write("address", JsonBuilderTest.buildAddress())
                .write("phoneNumber", JsonBuilderTest.buildPhone())
                .writeEnd();
        generator.close();
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(writer.toString()));
        JsonObject person = reader.readObject();
        JsonObjectTest.testPerson(person);
    }

    @Test
    void testArrayString() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartArray().write("string").writeEnd();
        generator.close();
        writer.close();

        Assertions.assertEquals("[\"string\"]", writer.toString());
    }

    @Test
    void testEscapedString() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartArray().write("\u0000").writeEnd();
        generator.close();
        writer.close();

        Assertions.assertEquals("[\"\\u0000\"]", writer.toString());
    }

    @Test
    void testEscapedString1() throws Exception {
        String expected = "\u0000\u00ff";
        StringWriter sw = new StringWriter();
        JsonGenerator generator = Json.createGenerator(sw);
        generator.writeStartArray().write("\u0000\u00ff").writeEnd();
        generator.close();
        sw.close();

        JsonReader jr = Json.createReader(new StringReader(sw.toString()));
        JsonArray array = jr.readArray();
        String got = array.getString(0);
        jr.close();

        Assertions.assertEquals(expected, got);
    }

    @Test
    void testGeneratorEquals() {
        StringWriter sw = new StringWriter();
        JsonGenerator generator = Json.createGenerator(sw);
        generator.writeStartArray()
                .write(JsonValue.TRUE)
                .write(JsonValue.FALSE)
                .write(JsonValue.NULL)
                .write(Integer.MAX_VALUE)
                .write(Long.MAX_VALUE)
                .write(Double.MAX_VALUE)
                .write(Integer.MIN_VALUE)
                .write(Long.MIN_VALUE)
                .write(Double.MIN_VALUE)
                .writeEnd();
        generator.close();

        JsonReader reader = Json.createReader(new StringReader(sw.toString()));
        JsonArray expected = reader.readArray();
        reader.close();

        JsonArray actual = Json.createArrayBuilder()
                .add(JsonValue.TRUE)
                .add(JsonValue.FALSE)
                .add(JsonValue.NULL)
                .add(Integer.MAX_VALUE)
                .add(Long.MAX_VALUE)
                .add(Double.MAX_VALUE)
                .add(Integer.MIN_VALUE)
                .add(Long.MIN_VALUE)
                .add(Double.MIN_VALUE)
                .build();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testPrettyObjectWriter() throws Exception {
        StringWriter writer = new StringWriter();
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGenerator generator = Json.createGeneratorFactory(config)
                .createGenerator(writer);
        testObject(generator);
        generator.close();
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(writer.toString()));
        JsonObject person = reader.readObject();
        JsonObjectTest.testPerson(person);
    }

    @Test
    void testPrettyPrinting() throws Exception {
        String[][] lines = {{"firstName", "John"}, {"lastName", "Smith"}};
        StringWriter writer = new StringWriter();
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGenerator generator = Json.createGeneratorFactory(config)
                .createGenerator(writer);
        generator.writeStartObject()
                .write("firstName", "John")
                .write("lastName", "Smith")
                .writeEnd();
        generator.close();
        writer.close();

        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        int numberOfLines = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            numberOfLines++;
            if (numberOfLines > 1 && numberOfLines < 4) {
                Assertions.assertTrue(line.contains("\"" + lines[numberOfLines - 2][0] + "\": \"" + lines[numberOfLines - 2][1] + "\""));
            }
        }
        Assertions.assertEquals(4, numberOfLines);
    }

    @Test
    void testPrettyObjectStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGenerator generator = Json.createGeneratorFactory(config)
                .createGenerator(out);
        testObject(generator);
        generator.close();
        out.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        JsonReader reader = Json.createReader(in);
        JsonObject person = reader.readObject();
        JsonObjectTest.testPerson(person);
        reader.close();
        in.close();
    }

    @Test
    void testGenerationException1() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject();
        Assertions.assertThrows(JsonGenerationException.class, generator::writeStartObject,
            "Expected JsonGenerationException, writeStartObject() cannot be called more than once");
    }

    @Test
    void testGenerationException2() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject();
        Assertions.assertThrows(JsonGenerationException.class, generator::writeStartArray,
            "Expected JsonGenerationException, writeStartArray() is valid in no context");
    }

    @Test
    void testGenerationException3() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        Assertions.assertThrows(JsonGenerationException.class, generator::close,
            "Expected JsonGenerationException, no JSON is generated");
    }

    @Test
    void testGenerationException4() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartArray();
        Assertions.assertThrows(JsonGenerationException.class, generator::close,
            "Expected JsonGenerationException, writeEnd() is not called");
    }

    @Test
    void testGenerationException5() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject();
        Assertions.assertThrows(JsonGenerationException.class, generator::close,
            "Expected JsonGenerationException, writeEnd() is not called");
    }

    @Test
    void testGenerationException6() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject().writeEnd();
        Assertions.assertThrows(JsonGenerationException.class, generator::writeStartObject,
            "Expected JsonGenerationException, cannot generate one more JSON text");
    }

    @Test
    void testGenerationException7() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartArray().writeEnd();
        Assertions.assertThrows(JsonGenerationException.class, generator::writeStartArray,
            "Expected JsonGenerationException, cannot generate one more JSON text");
    }


    @Test
    void testGenerationException8() {
        StringWriter sWriter = new StringWriter();
        JsonGenerator generator = Json.createGenerator(sWriter);
        generator.writeStartObject();
        Assertions.assertThrows(JsonGenerationException.class, () -> generator.write(JsonValue.TRUE),
            "Expected JsonGenerationException, cannot generate one more JSON text");
    }

    @Test
    void testGenerationException9() {
        StringWriter sWriter = new StringWriter();
        JsonGenerator generator = Json.createGenerator(sWriter);
        generator.writeStartObject();
        Assertions.assertThrows(JsonGenerationException.class, () -> generator.write("name"),
            "Expected JsonGenerationException, cannot generate one more JSON text");
    }

    @Test
    void testGeneratorArrayDouble() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartArray();
        Assertions.assertThrows(NumberFormatException.class, () -> generator.write(Double.NaN),
            "JsonGenerator.write(Double.NaN) should produce NumberFormatException");

        Assertions.assertThrows(NumberFormatException.class, () -> generator.write(Double.POSITIVE_INFINITY),
            "JsonGenerator.write(Double.POSITIVE_INIFINITY) should produce NumberFormatException");

        Assertions.assertThrows(NumberFormatException.class, () -> generator.write(Double.NEGATIVE_INFINITY),
            "JsonGenerator.write(Double.NEGATIVE_INIFINITY) should produce NumberFormatException");

        generator.writeEnd();
        generator.close();
    }

    @Test
    void testGeneratorObjectDouble() {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject();

        Assertions.assertThrows(NumberFormatException.class, () -> generator.write("foo", Double.NaN),
            "JsonGenerator.write(String, Double.NaN) should produce NumberFormatException");

        Assertions.assertThrows(NumberFormatException.class, () -> generator.write("foo", Double.POSITIVE_INFINITY),
            "JsonGenerator.write(String, Double.POSITIVE_INIFINITY) should produce NumberFormatException");

        Assertions.assertThrows(NumberFormatException.class, () -> generator.write("foo", Double.NEGATIVE_INFINITY),
            "JsonGenerator.write(String, Double.NEGATIVE_INIFINITY) should produce NumberFormatException");

        generator.writeEnd();
        generator.close();
    }

    @Test
    void testIntGenerator() {
        Random r = new Random(System.currentTimeMillis());
        JsonGeneratorFactory gf = Json.createGeneratorFactory(null);
        JsonReaderFactory rf = Json.createReaderFactory(null);
        JsonBuilderFactory bf = Json.createBuilderFactory(null);
        for(int i=0; i < 100000; i++) {
            int num = r.nextInt();
            StringWriter sw = new StringWriter();
            JsonGenerator generator = gf.createGenerator(sw);
            generator.writeStartArray().write(num).writeEnd().close();

            JsonReader reader = rf.createReader(new StringReader(sw.toString()));
            JsonArray got = reader.readArray();
            reader.close();

            JsonArray expected = bf.createArrayBuilder().add(num).build();

            Assertions.assertEquals(expected, got);
        }
    }

    @Test
    void testGeneratorBuf() {
        JsonGeneratorFactory gf = Json.createGeneratorFactory(null);
        JsonReaderFactory rf = Json.createReaderFactory(null);
        JsonBuilderFactory bf = Json.createBuilderFactory(null);
        StringBuilder sb = new StringBuilder();
        int value = 10;
        for(int i=0; i < 25000; i++) {
            sb.append('a');
            String name = sb.toString();
            StringWriter sw = new StringWriter();
            JsonGenerator generator = gf.createGenerator(sw);
            generator.writeStartObject().write(name, value).writeEnd().close();

            JsonReader reader = rf.createReader(new StringReader(sw.toString()));
            JsonObject got = reader.readObject();
            reader.close();

            JsonObject expected = bf.createObjectBuilder().add(name, value).build();

            Assertions.assertEquals(expected, got);
        }
    }

    @Test
    void testBufferPoolFeature() {
        final JsonParserTest.MyBufferPool bufferPool = new JsonParserTest.MyBufferPool(1024);
        Map<String, Object> config = Map.of(BufferPool.class.getName(), bufferPool);

        JsonGeneratorFactory factory = Json.createGeneratorFactory(config);
        JsonGenerator generator = factory.createGenerator(new StringWriter());
        generator.writeStartArray();
        generator.writeEnd();
        generator.close();
        Assertions.assertTrue(bufferPool.isTakeCalled());
        Assertions.assertTrue(bufferPool.isRecycleCalled());
    }

    @Test
    void testBufferSizes() {
        JsonReaderFactory rf = Json.createReaderFactory(null);
        JsonBuilderFactory bf = Json.createBuilderFactory(null);
        for(int size=10; size < 1000; size++) {
            final JsonParserTest.MyBufferPool bufferPool = new JsonParserTest.MyBufferPool(size);
            Map<String, Object> config = Map.of(BufferPool.class.getName(), bufferPool);
            JsonGeneratorFactory gf = Json.createGeneratorFactory(config);

            StringBuilder sb = new StringBuilder();
            int value = 10;
            for(int i=0; i < 1500; i++) {
                sb.append('a');
                String name = sb.toString();
                StringWriter sw = new StringWriter();
                JsonGenerator generator = gf.createGenerator(sw);
                generator.writeStartObject().write(name, value).writeEnd().close();

                JsonReader reader = rf.createReader(new StringReader(sw.toString()));
                JsonObject got = reader.readObject();
                reader.close();

                JsonObject expected = bf.createObjectBuilder().add(name, value).build();

                Assertions.assertEquals(expected, got);
            }

        }
    }

    @Test
    void testString() throws Exception {
        escapedString("");
        escapedString("abc");
        escapedString("abc\f");
        escapedString("abc\na");
        escapedString("abc\tabc");
        escapedString("abc\n\tabc");
        escapedString("abc\n\tabc\r");
        escapedString("\n\tabc\r");
        escapedString("\bab\tb\rc\\\"\ftesting1234");
        escapedString("\f\babcdef\tb\rc\\\"\ftesting1234");
    }

    void escapedString(String expected) throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator generator = Json.createGenerator(sw);
        generator.writeStartArray().write(expected).writeEnd();
        generator.close();
        sw.close();

        JsonReader jr = Json.createReader(new StringReader(sw.toString()));
        JsonArray array = jr.readArray();
        String got = array.getString(0);
        jr.close();

        Assertions.assertEquals(expected, got);
    }

    @Test
    void testFlush() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator gen = Json.createGenerator(baos);
        gen.writeStartObject().writeEnd();
        gen.flush();

        Assertions.assertEquals("{}", baos.toString("UTF-8"));
    }

    @Test
    void testClose() {
        StringWriter sw = new StringWriter();
        JsonGeneratorFactory factory = Json.createGeneratorFactory(Collections.emptyMap());
        try (JsonGenerator generator = factory.createGenerator(sw)) {
            generator.writeStartObject();
            generator.writeEnd();
            // Unnecessary close()
            generator.close();
            Assertions.assertEquals("{}", sw.toString());
        } 
        StringWriter sw1 = new StringWriter();
        StringWriter sw2 = new StringWriter();
        try (JsonGenerator generator1 = factory.createGenerator(sw1);
                JsonGenerator generator2 = factory.createGenerator(sw2)) {
            generator1.writeStartObject();
            generator1.write("key", "value");
            
            generator2.writeStartArray();
            generator2.write("item");
            generator2.write("item2");
            
            generator1.write("key2", "value2");
            
            generator2.writeEnd();
            
            generator1.writeEnd();
        }
        Assertions.assertEquals("{\"key\":\"value\",\"key2\":\"value2\"}", sw1.toString());
        Assertions.assertEquals("[\"item\",\"item2\"]", sw2.toString());
    }
}
