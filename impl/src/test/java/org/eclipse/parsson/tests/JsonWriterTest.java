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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import junit.framework.TestCase;

/**
 * @author Jitendra Kotamraju
 */
public class JsonWriterTest extends TestCase {
    public JsonWriterTest(String testName) {
        super(testName);
    }

    public void testObject() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeObject(Json.createObjectBuilder().build());
        jsonWriter.close();
        writer.close();

        assertEquals("{}", writer.toString());
    }

    public void testEmptyObject() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.write(JsonValue.EMPTY_JSON_OBJECT);
        jsonWriter.close();
        writer.close();

        assertEquals("{}", writer.toString());
    }

    public void testArray() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeArray(Json.createArrayBuilder().build());
        jsonWriter.close();
        writer.close();

        assertEquals("[]", writer.toString());
    }

    public void testEmptyArray() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.write(JsonValue.EMPTY_JSON_ARRAY);
        jsonWriter.close();
        writer.close();

        assertEquals("[]", writer.toString());
    }

    public void testNumber() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeArray(Json.createArrayBuilder().add(10).build());
        jsonWriter.close();
        writer.close();

        assertEquals("[10]", writer.toString());
    }

    public void testDoubleNumber() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeArray(Json.createArrayBuilder().add(10.5).build());
        jsonWriter.close();
        writer.close();

        assertEquals("[10.5]", writer.toString());
    }

    public void testArrayString() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeArray(Json.createArrayBuilder().add("string").build());
        jsonWriter.close();
        writer.close();

        assertEquals("[\"string\"]", writer.toString());
    }

    public void testObjectAsValue() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.write((JsonValue) (Json.createObjectBuilder().build()));
        jsonWriter.close();
        writer.close();

        assertEquals("{}", writer.toString());
    }

    public void testNullValue() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.write(JsonValue.NULL);
        jsonWriter.close();
        writer.close();

        assertEquals("null", writer.toString());
    }

    public void testTrueValue() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.write(JsonValue.TRUE);
        jsonWriter.close();
        writer.close();

        assertEquals("true", writer.toString());
    }

    public void testFalseValue() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.write(JsonValue.FALSE);
        jsonWriter.close();
        writer.close();

        assertEquals("false", writer.toString());
    }

    public void testIllegalStateExcepton() throws Exception {
        JsonObject obj = Json.createObjectBuilder().build();
        JsonArray array = Json.createArrayBuilder().build();

        JsonWriter writer = Json.createWriter(new StringWriter());
        writer.writeObject(obj);
        try {
            writer.writeObject(obj);
        } catch (IllegalStateException expected) {
            // no-op
        }
        writer.close();

        writer = Json.createWriter(new StringWriter());
        writer.writeArray(array);
        try {
            writer.writeArray(array);
        } catch (IllegalStateException expected) {
            // no-op
        }
        writer.close();

        writer = Json.createWriter(new StringWriter());
        writer.write(array);
        try {
            writer.writeArray(array);
        } catch (IllegalStateException expected) {
            // no-op
        }
        writer.close();
    }

    public void testNoCloseWriteObjectToStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = Json.createWriter(baos);
        writer.write(Json.createObjectBuilder().build());
        // not calling writer.close() intentionally
        assertEquals("{}", baos.toString("UTF-8"));
    }

    public void testNoCloseWriteObjectToWriter() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.write(Json.createObjectBuilder().build());
        // not calling writer.close() intentionally
        assertEquals("{}", sw.toString());
    }

    public void testNoCloseWriteArrayToStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = Json.createWriter(baos);
        writer.write(Json.createArrayBuilder().build());
        // not calling writer.close() intentionally
        assertEquals("[]", baos.toString("UTF-8"));
    }

    public void testNoCloseWriteArrayToWriter() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.write(Json.createArrayBuilder().build());
        // not calling writer.close() intentionally
        assertEquals("[]", sw.toString());
    }

    public void testClose() throws Exception {
        MyByteStream baos = new MyByteStream();
        JsonWriter writer = Json.createWriter(baos);
        writer.write(Json.createObjectBuilder().build());
        writer.close();
        assertEquals("{}", baos.toString("UTF-8"));
        assertTrue(baos.isClosed());
    }

    private String nameValueNanInfinity(JsonWriterFactory writerFactory) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(writer);
        JsonObject jsonObject = Json.createObjectBuilder()
        .add("val1", Double.NaN)
        .add("val2", 1.0)
        .add("val3", 0.0)
        .add("val4", Double.POSITIVE_INFINITY)
        .add("val5", Double.NEGATIVE_INFINITY)
        .add("val6", Json.createValue(Double.NaN))
        .add("val7", Json.createValue(1.0))
        .add("val8", Json.createValue(0.0))
        .add("val9", Json.createValue(Double.POSITIVE_INFINITY))
        .add("val10", Json.createValue(Double.NEGATIVE_INFINITY)).build();
        jsonWriter.writeObject(jsonObject);
        jsonWriter.close();
        return writer.toString();
    }

    private String jsonNumberNanInfinity(JsonWriterFactory writerFactory, double value) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(writer);
        jsonWriter.write(Json.createValue(value));
        jsonWriter.close();
        return writer.toString();
    }

    public void testNanInfinityDefault() {
        Map<String, Object> config = new HashMap<>();
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        assertEquals("{\"val1\":null,\"val2\":1.0,\"val3\":0.0,\"val4\":null,\"val5\":null,\"val6\":null,\"val7\":1.0,\"val8\":0.0,\"val9\":null,\"val10\":null}", nameValueNanInfinity(writerFactory));
        assertEquals("0.0", jsonNumberNanInfinity(writerFactory, 0.0));
        assertEquals("1.0", jsonNumberNanInfinity(writerFactory, 1.0));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.NaN));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.POSITIVE_INFINITY));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.NEGATIVE_INFINITY));
    }

    public void testNanInfinityWriteNanAsNull() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.WRITE_NAN_AS_NULLS, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        assertEquals("{\"val1\":null,\"val2\":1.0,\"val3\":0.0,\"val4\":null,\"val5\":null,\"val6\":null,\"val7\":1.0,\"val8\":0.0,\"val9\":null,\"val10\":null}", nameValueNanInfinity(writerFactory));
        assertEquals("0.0", jsonNumberNanInfinity(writerFactory, 0.0));
        assertEquals("1.0", jsonNumberNanInfinity(writerFactory, 1.0));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.NaN));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.POSITIVE_INFINITY));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.NEGATIVE_INFINITY));
    }

    public void testNanInfinityWriteNanAsString() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.WRITE_NAN_AS_STRINGS, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        assertEquals("{\"val1\":\"NaN\",\"val2\":1.0,\"val3\":0.0,\"val4\":\"+Infinity\",\"val5\":\"-Infinity\",\"val6\":\"NaN\",\"val7\":1.0,\"val8\":0.0,\"val9\":\"+Infinity\",\"val10\":\"-Infinity\"}", nameValueNanInfinity(writerFactory));
        assertEquals("0.0", jsonNumberNanInfinity(writerFactory, 0.0));
        assertEquals("1.0", jsonNumberNanInfinity(writerFactory, 1.0));
        assertEquals("\"NaN\"", jsonNumberNanInfinity(writerFactory, Double.NaN));
        assertEquals("\"+Infinity\"", jsonNumberNanInfinity(writerFactory, Double.POSITIVE_INFINITY));
        assertEquals("\"-Infinity\"", jsonNumberNanInfinity(writerFactory, Double.NEGATIVE_INFINITY));
    }

    public void testNanInfinityBothFalse() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.WRITE_NAN_AS_STRINGS, false);
        config.put(JsonGenerator.WRITE_NAN_AS_NULLS, false);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        try {
            nameValueNanInfinity(writerFactory);
            fail("Expected a failure");
        } catch (NumberFormatException e) {}
        try {
            jsonNumberNanInfinity(writerFactory, Double.NaN);
            fail("Expected a failure");
        } catch (NumberFormatException e) {}
        assertEquals("0.0", jsonNumberNanInfinity(writerFactory, 0.0));
        assertEquals("1.0", jsonNumberNanInfinity(writerFactory, 1.0));
    }

    public void testNanInfinityBothTrue() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.WRITE_NAN_AS_STRINGS, true);
        config.put(JsonGenerator.WRITE_NAN_AS_NULLS, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        assertEquals("{\"val1\":null,\"val2\":1.0,\"val3\":0.0,\"val4\":null,\"val5\":null,\"val6\":null,\"val7\":1.0,\"val8\":0.0,\"val9\":null,\"val10\":null}", nameValueNanInfinity(writerFactory));
        assertEquals("0.0", jsonNumberNanInfinity(writerFactory, 0.0));
        assertEquals("1.0", jsonNumberNanInfinity(writerFactory, 1.0));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.NaN));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.POSITIVE_INFINITY));
        assertEquals("null", jsonNumberNanInfinity(writerFactory, Double.NEGATIVE_INFINITY));
    }
    private static final class MyByteStream extends ByteArrayOutputStream {
        boolean closed;

        boolean isClosed() {
            return closed;
        }

        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }
}
