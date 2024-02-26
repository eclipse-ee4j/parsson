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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

import org.eclipse.parsson.api.BufferPool;
import org.eclipse.parsson.api.JsonConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author Jitendra Kotamraju
 */
public class JsonReaderTest {

    @Test
    void testObject() {
        JsonObject person = readPerson();
        JsonObjectTest.testPerson(person);
    }

    @Test
    void testEscapedString() {
        // u00ff is escaped once, not escaped once
        JsonReader reader = Json.createReader(new StringReader("[\"\\u0000\\u00ff\u00ff\"]"));
        JsonArray array = reader.readArray();
        reader.close();
        String str = array.getString(0);
        Assertions.assertEquals("\u0000\u00ff\u00ff", str);
    }

    @Test
    void testPrimitiveIntNumbers() {
        String[] borderlineCases = new String[]{
                "214748364",
                Integer.toString(Integer.MAX_VALUE),
                Long.toString(Integer.MAX_VALUE + 1L),
                "-214748364",
                Integer.toString(Integer.MIN_VALUE),
                Long.toString(Integer.MIN_VALUE - 1L)
        };
        for (String num : borderlineCases) {
			try (JsonReader reader = Json.createReader(new StringReader("[" + num + "]"))) {
				JsonArray array = reader.readArray();
				JsonNumber value = (JsonNumber) array.get(0);
				Assertions.assertEquals(new BigInteger(num).longValue(), value.longValue(), "Fails for num=" + num);
			}
        }
    }

    @Test
    void testPrimitiveLongNumbers() {
        String[] borderlineCases = new String[]{
                "922337203685477580",
                Long.toString(Long.MAX_VALUE),
                new BigInteger(Long.toString(Long.MAX_VALUE)).add(BigInteger.ONE).toString(),
                "-922337203685477580",
                Long.toString(Long.MIN_VALUE),
                new BigInteger(Long.toString(Long.MIN_VALUE)).subtract(BigInteger.ONE).toString()
        };
        for (String num : borderlineCases) {
			try (JsonReader reader = Json.createReader(new StringReader("[" + num + "]"))) {
				JsonArray array = reader.readArray();
				JsonNumber value = (JsonNumber) array.get(0);
				Assertions.assertEquals(new BigInteger(num), value.bigIntegerValueExact(), "Fails for num=" + num);
			}
        }
    }

    @Test
    void testUnknownFeature() {
        Map<String, Object> config = new HashMap<>();
        config.put("foo", true);
        JsonReaderFactory factory = Json.createReaderFactory(config);
        factory.createReader(new StringReader("{}"));
        Map<String, ?> config1 = factory.getConfigInUse();
        if (!config1.isEmpty()) {
            Assertions.fail("Shouldn't have any config in use");
        }
    }

    @Test
    void testIllegalStateExcepton() {
        JsonReader reader = Json.createReader(new StringReader("{}"));
        reader.readObject();
        try {
            reader.readObject();
        } catch (IllegalStateException expected) {
            // no-op
        }
        reader.close();

        reader = Json.createReader(new StringReader("[]"));
        reader.readArray();
        try {
            reader.readArray();
        } catch (IllegalStateException expected) {
            // no-op
        }
        reader.close();

        reader = Json.createReader(new StringReader("{}"));
        reader.read();
        try {
            reader.read();
        } catch (IllegalStateException expected) {
            // no-op
        }
        reader.close();
    }

    static JsonObject readPerson() {
        Reader wikiReader = new InputStreamReader(Objects.requireNonNull(JsonReaderTest.class.getResourceAsStream("/wiki.json")));
        JsonReader reader = Json.createReader(wikiReader);
        JsonObject value = reader.readObject();
        reader.close();
        return value;
    }

    // JSONP-23 cached empty string is not reset
    @Test
    void testEmptyStringUsingStandardBuffer() throws Throwable {
        JsonReaderFactory factory = Json.createReaderFactory(null);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < 40000; i++) {
            sb.append('a');
            String name = sb.toString();
            String str = "[1, \"\", \""+name+"\", \"\", \""+name+"\", \"\", 100]";
            try {
                JsonReader reader = factory.createReader(new StringReader(str));
                JsonArray array = reader.readArray();
                Assertions.assertEquals(1, array.getInt(0));
                Assertions.assertEquals("", array.getString(1));
                Assertions.assertEquals(name, array.getString(2));
                Assertions.assertEquals("", array.getString(3));
                Assertions.assertEquals(name, array.getString(4));
                Assertions.assertEquals("", array.getString(5));
                Assertions.assertEquals(100, array.getInt(6));
                reader.close();
            } catch (Throwable t) {
                throw new Throwable("Failed for name length="+i, t);
            }
        }
    }

    @Test
    void testDuplicateKeysDefault() {
        Map<String, Object> config = new HashMap<>();
        JsonReaderFactory factory = Json.createReaderFactory(config);
        String json = "{\"val1\":\"A\",\"val1\":\"B\"}";
        JsonReader reader = factory.createReader(new StringReader(json));
        JsonObject object = reader.readObject();
        reader.close();
        Assertions.assertEquals("B", object.getString("val1"));
    }

    @Test
    void testDuplicateKeysStrict() {
        Map<String, Object> config = new HashMap<>();
        config.put(jakarta.json.JsonConfig.KEY_STRATEGY, jakarta.json.JsonConfig.KeyStrategy.NONE);
        JsonReaderFactory factory = Json.createReaderFactory(config);
        String json = "{\"val1\":\"A\",\"val1\":\"B\"}";
        JsonReader reader = factory.createReader(new StringReader(json));
        try {
            reader.readObject();
            Assertions.fail("It is expected a JsonException");
        } catch (JsonException ignored) {}
    }

    @Test
    void testDuplicateKeysStrictWithParssonConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(jakarta.json.JsonConfig.KEY_STRATEGY, jakarta.json.JsonConfig.KeyStrategy.NONE);
        JsonReaderFactory factory = Json.createReaderFactory(config);
        String json = "{\"val1\":\"A\",\"val1\":\"B\"}";
        JsonReader reader = factory.createReader(new StringReader(json));
        try {
            reader.readObject();
            Assertions.fail("It is expected a JsonException");
        } catch (JsonException ignored) {}
    }

    @Test
    void testDuplicateKeysFirst() {
        Map<String, Object> config = new HashMap<>();
        config.put(jakarta.json.JsonConfig.KEY_STRATEGY, jakarta.json.JsonConfig.KeyStrategy.FIRST);
        JsonReaderFactory factory = Json.createReaderFactory(config);
        String json = "{\"val1\":\"A\",\"val1\":\"B\"}";
        JsonReader reader = factory.createReader(new StringReader(json));
        JsonObject object = reader.readObject();
        reader.close();
        Assertions.assertEquals("A", object.getString("val1"));
    }

    @Test
    void testDuplicateKeysFirstWithParssonConfig() {
        // JsonReader configuration rules over JsonConfig
        Map<String, Object> config = new HashMap<>();
        config.put(jakarta.json.JsonConfig.KEY_STRATEGY, jakarta.json.JsonConfig.KeyStrategy.FIRST);
        config.put(JsonConfig.REJECT_DUPLICATE_KEYS, "anything is valid here");
        JsonReaderFactory factory = Json.createReaderFactory(config);
        String json = "{\"val1\":\"A\",\"val1\":\"B\"}";
        JsonReader reader = factory.createReader(new StringReader(json));
        JsonObject object = reader.readObject();
        reader.close();
        Assertions.assertEquals("A", object.getString("val1"));
    }

    @Test
    void testDuplicateKeysLast() {
        Map<String, Object> config = new HashMap<>();
        config.put(jakarta.json.JsonConfig.KEY_STRATEGY, jakarta.json.JsonConfig.KeyStrategy.LAST);
        JsonReaderFactory factory = Json.createReaderFactory(config);
        String json = "{\"val1\":\"A\",\"val1\":\"B\"}";
        JsonReader reader = factory.createReader(new StringReader(json));
        JsonObject object = reader.readObject();
        reader.close();
        Assertions.assertEquals("B", object.getString("val1"));
    }

    // JSONP-23 cached empty string is not reset
    @Test
    void testEmptyStringUsingBuffers() throws Throwable {
        for(int size=20; size < 500; size++) {
            final JsonParserTest.MyBufferPool bufferPool = new JsonParserTest.MyBufferPool(size);
            Map<String, Object> config = new HashMap<String, Object>() {{
                put(BufferPool.class.getName(), bufferPool);
            }};
            JsonReaderFactory factory = Json.createReaderFactory(config);

            StringBuilder sb = new StringBuilder();
            for(int i=0; i < 1000; i++) {
                sb.append('a');
                String name = sb.toString();
                String str = "[1, \"\", \""+name+"\", \"\", \""+name+"\", \"\", 100]";
                try {
                    JsonReader reader = factory.createReader(new StringReader(str));
                    JsonArray array = reader.readArray();
                    Assertions.assertEquals(1, array.getInt(0));
                    Assertions.assertEquals("", array.getString(1));
                    Assertions.assertEquals(name, array.getString(2));
                    Assertions.assertEquals("", array.getString(3));
                    Assertions.assertEquals(name, array.getString(4));
                    Assertions.assertEquals("", array.getString(5));
                    Assertions.assertEquals(100, array.getInt(6));
                    reader.close();
                } catch (Throwable t) {
                    throw new Throwable("Failed for buffer size="+size+" name length="+i, t);
                }
            }
        }
    }

}
