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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jitendra Kotamraju
 */
public class JsonObjectTest {

    @Test
    void test() {
    }

    @Test
    void testEmptyObjectEquals() {
        JsonObject empty1 = Json.createObjectBuilder()
                .build();

        JsonObject empty2 = Json.createObjectBuilder()
                .build();

        Assertions.assertEquals(empty1, empty2);
    }

    @Test
    void testPersonObjectEquals() {
        JsonObject person1 = JsonBuilderTest.buildPerson();
        JsonObject person2 = JsonReaderTest.readPerson();

        Assertions.assertEquals(person1, person2);
    }

    @Test
    void testGetStringOrDefault() {
        JsonObject object = Json.createObjectBuilder()
                .add("string", "value")
                .add("number", 25)
                .add("boolean", false)
                .build();
        Assertions.assertEquals("value", object.getString("string", "default"));
        Assertions.assertEquals("default", object.getString("missing", "default"));
        Assertions.assertEquals("default", object.getString("number", "default"));
    }

    @Test
    void testGetIntOrDefault() {
        JsonObject object = Json.createObjectBuilder()
                .add("string", "value")
                .add("number", 25)
                .add("boolean", false)
                .build();
        Assertions.assertEquals(25, object.getInt("number", 10));
        Assertions.assertEquals(10, object.getInt("missing", 10));
        Assertions.assertEquals(10, object.getInt("string", 10));
    }

    @Test
    void testGetBooleanOrDefault() {
        JsonObject object = Json.createObjectBuilder()
                .add("string", "value")
                .add("number", 25)
                .add("boolean", false)
                .build();
        Assertions.assertFalse(object.getBoolean("boolean", true));
        Assertions.assertTrue(object.getBoolean("missing", true));
        Assertions.assertTrue(object.getBoolean("string", true));
    }

    static void testPerson(JsonObject person) {
        Assertions.assertEquals(5, person.size());
        Assertions.assertEquals("John", person.getString("firstName"));
        Assertions.assertEquals("Smith", person.getString("lastName"));
        Assertions.assertEquals(25, person.getJsonNumber("age").intValue());
        Assertions.assertEquals(25, person.getInt("age"));

        JsonObject address = person.getJsonObject("address");
        Assertions.assertEquals(4, address.size());
        Assertions.assertEquals("21 2nd Street", address.getString("streetAddress"));
        Assertions.assertEquals("New York", address.getString("city"));
        Assertions.assertEquals("NY", address.getString("state"));
        Assertions.assertEquals("10021", address.getString("postalCode"));

        JsonArray phoneNumber = person.getJsonArray("phoneNumber");
        Assertions.assertEquals(2, phoneNumber.size());
        JsonObject home = phoneNumber.getJsonObject(0);
        Assertions.assertEquals(2, home.size());
        Assertions.assertEquals("home", home.getString("type"));
        Assertions.assertEquals("212 555-1234", home.getString("number"));
        Assertions.assertEquals("212 555-1234", home.getString("number"));

        JsonObject fax = phoneNumber.getJsonObject(1);
        Assertions.assertEquals(2, fax.size());
        Assertions.assertEquals("fax", fax.getString("type"));
        Assertions.assertEquals("646 555-4567", fax.getString("number"));

        Assertions.assertEquals("\"646 555-4567\"", fax.getJsonString("number").toString());
    }

    static void testEmpty(JsonObject empty) {
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void testClassCastException() {
        JsonObject obj = Json.createObjectBuilder()
                .add("foo", JsonValue.FALSE).build();
        try {
            obj.getJsonNumber("foo");
            Assertions.fail("Expected ClassCastException for casting JsonValue.FALSE to JsonNumber");
        } catch (ClassCastException ce) {
            // Expected
        }
    }

    @Test
    void testPut() {
        JsonObject obj = Json.createObjectBuilder().add("foo", 1).build();
        try {
            obj.put("bar", JsonValue.FALSE);
            Assertions.fail("JsonObject#put() should throw UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    void testRemove() {
        JsonObject obj = Json.createObjectBuilder().add("foo", 1).build();
        try {
            obj.remove("foo");
            Assertions.fail("JsonObject#remove() should throw UnsupportedOperationException");
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

        Assertions.assertEquals(expected, actual);
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

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testObjectBuilderNpe() {
        try {
            Json.createObjectBuilder().add(null, 1).build();
            Assertions.fail("JsonObjectBuilder#add(null, 1) should throw NullPointerException");
        } catch(NullPointerException e) {
            // Expected
        }
    }

    @Test
    void testHashCode() {
        JsonObject object1 = Json.createObjectBuilder().add("a", 1).add("b", 2).add("c", 3).build();
        Assertions.assertTrue(object1.hashCode() == object1.hashCode()); //1st call compute hashCode, 2nd call returns cached value

        JsonObject object2 = Json.createObjectBuilder().add("a", 1).add("b", 2).add("c", 3).build();
        Assertions.assertTrue(object1.hashCode() == object2.hashCode());

        JsonObject object3 = Json.createObjectBuilder().build(); //org.eclipse.parsson.JsonArrayBuilderImpl.JsonArrayImpl
        JsonObject object4 = JsonValue.EMPTY_JSON_OBJECT; //jakarta.json.EmptyObject

        Assertions.assertTrue(object3.equals(object4));
        Assertions.assertTrue(object3.hashCode() == object4.hashCode()); //equal instances have same hashCode
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
        Assertions.assertEquals("b", object.get("stringArray").asJsonArray().getString(1));
		Assertions.assertFalse(object.get("booleanArray").asJsonArray().getBoolean(1));
        Assertions.assertEquals(2, object.get("intArray").asJsonArray().getInt(1));
        Assertions.assertEquals('b', object.get("charArray").asJsonArray().getInt(1));
        Assertions.assertEquals(2.0, object.get("floatArray").asJsonArray().getJsonNumber(1).doubleValue());
    }
}
