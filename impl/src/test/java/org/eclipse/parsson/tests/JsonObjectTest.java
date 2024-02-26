/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;

import org.junit.jupiter.api.Test;

/**
 * @author Jitendra Kotamraju
 */
public class JsonObjectTest {

    @Test
    void test() {
    }

    @Test
    void testEmptyObjectEquals() throws Exception {
        JsonObject empty1 = Json.createObjectBuilder()
                .build();

        JsonObject empty2 = Json.createObjectBuilder()
                .build();

        assertEquals(empty1, empty2);
    }

    @Test
    void testPersonObjectEquals() throws Exception {
        JsonObject person1 = JsonBuilderTest.buildPerson();
        JsonObject person2 = JsonReaderTest.readPerson();

        assertEquals(person1, person2);
    }

    @Test
    void testGetStringOrDefault() throws Exception {
        JsonObject object = Json.createObjectBuilder()
                .add("string", "value")
                .add("number", 25)
                .add("boolean", false)
                .build();
        assertEquals("value", object.getString("string", "default"));
        assertEquals("default", object.getString("missing", "default"));
        assertEquals("default", object.getString("number", "default"));
    }

    @Test
    void testGetIntOrDefault() throws Exception {
        JsonObject object = Json.createObjectBuilder()
                .add("string", "value")
                .add("number", 25)
                .add("boolean", false)
                .build();
        assertEquals(25, object.getInt("number", 10));
        assertEquals(10, object.getInt("missing", 10));
        assertEquals(10, object.getInt("string", 10));
    }

    @Test
    void testGetBooleanOrDefault() throws Exception {
        JsonObject object = Json.createObjectBuilder()
                .add("string", "value")
                .add("number", 25)
                .add("boolean", false)
                .build();
        assertFalse(object.getBoolean("boolean", true));
        assertTrue(object.getBoolean("missing", true));
        assertTrue(object.getBoolean("string", true));
    }

    static void testPerson(JsonObject person) {
        assertEquals(5, person.size());
        assertEquals("John", person.getString("firstName"));
        assertEquals("Smith", person.getString("lastName"));
        assertEquals(25, person.getJsonNumber("age").intValue());
        assertEquals(25, person.getInt("age"));

        JsonObject address = person.getJsonObject("address");
        assertEquals(4, address.size());
        assertEquals("21 2nd Street", address.getString("streetAddress"));
        assertEquals("New York", address.getString("city"));
        assertEquals("NY", address.getString("state"));
        assertEquals("10021", address.getString("postalCode"));

        JsonArray phoneNumber = person.getJsonArray("phoneNumber");
        assertEquals(2, phoneNumber.size());
        JsonObject home = phoneNumber.getJsonObject(0);
        assertEquals(2, home.size());
        assertEquals("home", home.getString("type"));
        assertEquals("212 555-1234", home.getString("number"));
        assertEquals("212 555-1234", home.getString("number"));

        JsonObject fax = phoneNumber.getJsonObject(1);
        assertEquals(2, fax.size());
        assertEquals("fax", fax.getString("type"));
        assertEquals("646 555-4567", fax.getString("number"));

        assertEquals("\"646 555-4567\"", fax.getJsonString("number").toString());
    }

    static void testEmpty(JsonObject empty) {
        assertTrue(empty.isEmpty());
    }

    @Test
    void testClassCastException() {
        JsonObject obj = Json.createObjectBuilder()
                .add("foo", JsonValue.FALSE).build();
        try {
            obj.getJsonNumber("foo");
            fail("Expected ClassCastException for casting JsonValue.FALSE to JsonNumber");
        } catch (ClassCastException ce) {
            // Expected
        }
    }

    @Test
    void testPut() {
        JsonObject obj = Json.createObjectBuilder().add("foo", 1).build();
        try {
            obj.put("bar", JsonValue.FALSE);
            fail("JsonObject#put() should throw UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    void testRemove() {
        JsonObject obj = Json.createObjectBuilder().add("foo", 1).build();
        try {
            obj.remove("foo");
            fail("JsonObject#remove() should throw UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    void testObjectBuilderWithVariousValues() {
        JsonObject expected = Json.createObjectBuilder()
                .add("a", JsonValue.TRUE)
                .add("b", JsonValue.FALSE)
                .add("c", JsonValue.NULL)
                .add("d", Integer.MAX_VALUE)
                .add("e", Long.MAX_VALUE)
                .add("f", Double.MAX_VALUE)
                .add("g", Integer.MIN_VALUE)
                .add("h", Long.MIN_VALUE)
                .add("i", Double.MIN_VALUE)
                .add("j", Json.createArrayBuilder().add("abc"))
                .add("k", Json.createObjectBuilder().add("one", 1))
                .build();

        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.writeObject(expected);
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(sw.toString()));
        JsonObject actual = reader.readObject();
        reader.close();

        assertEquals(expected, actual);
    }

    @Test
    void testObjectBuilderWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", JsonValue.TRUE);
        map.put("b", JsonValue.FALSE);
        map.put("c", JsonValue.NULL);
        map.put("d", Integer.MAX_VALUE);
        map.put("e", Long.MAX_VALUE);
        map.put("f", Double.MAX_VALUE);
        map.put("g", Integer.MIN_VALUE);
        map.put("h", Long.MIN_VALUE);
        map.put("i", Double.MIN_VALUE);
        map.put("j", Json.createArrayBuilder().add("abc"));
        map.put("k", Json.createObjectBuilder().add("one", 1));

        JsonObject expected = Json.createObjectBuilder(map).build();

        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.writeObject(expected);
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(sw.toString()));
        JsonObject actual = reader.readObject();
        reader.close();

        assertEquals(expected, actual);
    }

    @Test
    void testObjectBuilderNpe() {
        try {
            JsonObject obj = Json.createObjectBuilder().add(null, 1).build();
            fail("JsonObjectBuilder#add(null, 1) should throw NullPointerException");
        } catch(NullPointerException e) {
            // Expected
        }
    }

    @Test
    void testHashCode() {
        JsonObject object1 = Json.createObjectBuilder().add("a", 1).add("b", 2).add("c", 3).build();
        assertTrue(object1.hashCode() == object1.hashCode()); //1st call compute hashCode, 2nd call returns cached value

        JsonObject object2 = Json.createObjectBuilder().add("a", 1).add("b", 2).add("c", 3).build();
        assertTrue(object1.hashCode() == object2.hashCode());

        JsonObject object3 = Json.createObjectBuilder().build(); //org.eclipse.parsson.JsonArrayBuilderImpl.JsonArrayImpl
        JsonObject object4 = JsonValue.EMPTY_JSON_OBJECT; //jakarta.json.EmptyObject

        assertTrue(object3.equals(object4));
        assertTrue(object3.hashCode() == object4.hashCode()); //equal instances have same hashCode
    }

    @Test
    void testArrays() {
        String[] stringArr = new String[] {"a", "b", "c"};
        boolean[] boolArr = new boolean[] {true, false, true};
        int[] intArr = new int[] {1, 2, 3};
        char[] charArr = new char[] {'a', 'b', 'c'};
        float[] floatArr = new float[] {1.0f, 2.0f, 3.0f};
        Map<String, Object> m = new HashMap<>();
        m.put("stringArray", stringArr);
        m.put("booleanArray", boolArr);
        m.put("intArray", intArr);
        m.put("charArray", charArr);
        m.put("floatArray", floatArr);
        JsonObject object = Json.createObjectBuilder(m).build();
        assertEquals("b", object.get("stringArray").asJsonArray().getString(1));
        assertEquals(false, object.get("booleanArray").asJsonArray().getBoolean(1));
        assertEquals(2, object.get("intArray").asJsonArray().getInt(1));
        assertEquals('b', object.get("charArray").asJsonArray().getInt(1));
        assertEquals(2.0, object.get("floatArray").asJsonArray().getJsonNumber(1).doubleValue());
    }
}
