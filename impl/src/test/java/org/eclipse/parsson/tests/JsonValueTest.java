/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Lukas Jungmann
 */
public class JsonValueTest {

    @Test
    void arrayGetJsonObjectIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getJsonObject(0));
    }

    @Test
    void arrayGetJsonArrayIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getJsonArray(0));
    }

    @Test
    void arrayGetJsonNumberIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getJsonNumber(0));
    }

    @Test
    void arrayGetJsonStringIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getJsonString(0));
    }

    @Test
    void arrayGetStringIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getString(0));
    }

    @Test
    void arrayGetIntIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getInt(0));
    }

    @Test
    void arrayGetBooleanIdx() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.getBoolean(0));
    }

    @Test
    void arrayIsNull() {
        assertThrows(IndexOutOfBoundsException.class, () -> JsonValue.EMPTY_JSON_ARRAY.isNull(0));
    }

    @Test
    void arrayMethods() {
        Assertions.assertEquals(JsonValue.ValueType.ARRAY, JsonValue.EMPTY_JSON_ARRAY.getValueType());
        Assertions.assertEquals(Collections.<JsonObject>emptyList(), JsonValue.EMPTY_JSON_ARRAY.getValuesAs(JsonObject.class));
        Assertions.assertEquals(Collections.<String>emptyList(), JsonValue.EMPTY_JSON_ARRAY.getValuesAs(JsonString::getString));
		Assertions.assertTrue(JsonValue.EMPTY_JSON_ARRAY.getBoolean(0, true));
        Assertions.assertEquals(42, JsonValue.EMPTY_JSON_ARRAY.getInt(0, 42));
        Assertions.assertEquals("Sasek", JsonValue.EMPTY_JSON_ARRAY.getString(0, "Sasek"));
    }

    @Test
    void arrayIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> JsonValue.EMPTY_JSON_ARRAY.add(JsonValue.EMPTY_JSON_OBJECT));
    }

    @Test
    void objectGetString() {
        assertThrows(NullPointerException.class, () -> JsonValue.EMPTY_JSON_OBJECT.getString("normalni string"));
    }

    @Test
    void objectGetInt() {
        assertThrows(NullPointerException.class, () -> JsonValue.EMPTY_JSON_OBJECT.getInt("hledej cislo"));
    }

    @Test
    void objectGetBoolean() {
        assertThrows(NullPointerException.class, () -> JsonValue.EMPTY_JSON_OBJECT.getBoolean("booo"));
    }

    @Test
    void objectIsNull() {
        assertThrows(NullPointerException.class, () -> JsonValue.EMPTY_JSON_OBJECT.isNull("???"));
    }

    @Test
    void objectMethods() {
        Assertions.assertNull(JsonValue.EMPTY_JSON_OBJECT.getJsonArray("pole"));
        Assertions.assertNull(JsonValue.EMPTY_JSON_OBJECT.getJsonObject("objekt"));
        Assertions.assertNull(JsonValue.EMPTY_JSON_OBJECT.getJsonNumber("cislo"));
        Assertions.assertNull(JsonValue.EMPTY_JSON_OBJECT.getJsonString("divnej string"));
        
        Assertions.assertEquals("ja jo", JsonValue.EMPTY_JSON_OBJECT.getString("nejsem tu", "ja jo"));
		Assertions.assertFalse(JsonValue.EMPTY_JSON_OBJECT.getBoolean("najdes mne", false));
        Assertions.assertEquals(98, JsonValue.EMPTY_JSON_OBJECT.getInt("spatnej dotaz", 98));
    }


    @Test
    void objectImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> JsonValue.EMPTY_JSON_OBJECT.put("klauni", JsonValue.EMPTY_JSON_ARRAY));
    }

    @Test
    void serialization() {
        byte[] data = serialize(JsonValue.TRUE);
        JsonValue value = deserialize(JsonValue.class, data);
        Assertions.assertEquals(JsonValue.TRUE, value);

        data = serialize(JsonValue.FALSE);
        value = deserialize(JsonValue.class, data);
        Assertions.assertEquals(JsonValue.FALSE, value);

        data = serialize(JsonValue.NULL);
        value = deserialize(JsonValue.class, data);
        Assertions.assertEquals(JsonValue.NULL, value);

        data = serialize(JsonValue.EMPTY_JSON_ARRAY);
        value = deserialize(JsonValue.class, data);
        Assertions.assertEquals(JsonValue.EMPTY_JSON_ARRAY, value);

        data = serialize(JsonValue.EMPTY_JSON_OBJECT);
        value = deserialize(JsonValue.class, data);
        Assertions.assertEquals(JsonValue.EMPTY_JSON_OBJECT, value);
    }

    private byte[] serialize(Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(Class<T> type, byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
