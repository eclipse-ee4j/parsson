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
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jitendra Kotamraju
 */
public class JsonArrayTest {
    @Test
    void testArrayEquals() {
        JsonArray expected = Json.createArrayBuilder()
                .add(JsonValue.TRUE)
                .add(JsonValue.FALSE)
                .add(JsonValue.NULL)
                .add(Integer.MAX_VALUE)
                .add(Long.MAX_VALUE)
                .add(Double.MAX_VALUE)
                .add(Integer.MIN_VALUE)
                .add(Long.MIN_VALUE)
                .add(Double.MIN_VALUE)
                .add(Json.createArrayBuilder().add("abc"))
                .add(Json.createObjectBuilder().add("one", 1))
                .build();

        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.writeArray(expected);
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(sw.toString()));
        JsonArray actual = reader.readArray();
        reader.close();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testArrayEqualsUsingCollection() {
        List<Object> list = new ArrayList<>();
        list.add(JsonValue.TRUE);
        list.add(JsonValue.FALSE);
        list.add(JsonValue.NULL);
        list.add(Integer.MAX_VALUE);
        list.add(Long.MAX_VALUE);
        list.add(Double.MAX_VALUE);
        list.add(Integer.MIN_VALUE);
        list.add(Long.MIN_VALUE);
        list.add(Double.MIN_VALUE);
        list.add(Json.createArrayBuilder().add("abc"));
        list.add(Json.createObjectBuilder().add("one", 1));

        JsonArray expected = Json.createArrayBuilder(list).build();

        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.writeArray(expected);
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(sw.toString()));
        JsonArray actual = reader.readArray();
        reader.close();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testStringValue() {
        JsonArray array = Json.createArrayBuilder()
                .add("John")
                .build();
        Assertions.assertEquals("John", array.getString(0));
    }

    @Test
    void testIntValue() {
        JsonArray array = Json.createArrayBuilder()
                .add(20)
                .build();
        Assertions.assertEquals(20, array.getInt(0));
    }

    @Test
    void testAdd() {
        JsonArray array = Json.createArrayBuilder().build();
        try {
            array.add(JsonValue.FALSE);
            Assertions.fail("JsonArray#add() should throw UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    void testRemove() {
        JsonArray array = Json.createArrayBuilder().build();
        try {
            array.remove(0);
            Assertions.fail("JsonArray#remove() should throw UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    void testNumberView() {
        JsonArray array = Json.createArrayBuilder().add(20).add(10).build();

        List<JsonNumber> numberList = array.getValuesAs(JsonNumber.class);
        for(JsonNumber num : numberList) {
            num.intValue();
        }

        Assertions.assertEquals(20, array.getInt(0));
        Assertions.assertEquals(10, array.getInt(1));
    }

    @Test
    void testArrayBuilderNpe() {
        try {
            Json.createArrayBuilder().add((JsonValue)null).build();
            Assertions.fail("JsonArrayBuilder#add(null) should throw NullPointerException");
        } catch(NullPointerException e) {
            // Expected
        }
    }

    @Test
    void testHashCode() {
        JsonArray array1 = Json.createArrayBuilder().add(1).add(2).add(3).build();
        Assertions.assertTrue(array1.hashCode() == array1.hashCode()); //1st call compute hashCode, 2nd call returns cached value

        JsonArray array2 = Json.createArrayBuilder().add(1).add(2).add(3).build();
        Assertions.assertTrue(array1.hashCode() == array2.hashCode());

        JsonArray array3 = Json.createArrayBuilder().build(); //org.eclipse.parsson.JsonArrayBuilderImpl.JsonArrayImpl
        JsonArray array4 = JsonValue.EMPTY_JSON_ARRAY; //jakarta.json.EmptyArray

        Assertions.assertTrue(array3.equals(array4));
        Assertions.assertTrue(array3.hashCode() == array4.hashCode()); //equal instances have same hashCode
    }

}
