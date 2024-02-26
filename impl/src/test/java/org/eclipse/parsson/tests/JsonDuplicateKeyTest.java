/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collections;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonConfig;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.stream.JsonParsingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class JsonDuplicateKeyTest {
    @Test
    void testJsonReaderDuplicateKey1() {
        String json = "{\"a\":\"b\",\"a\":\"c\"}";
        JsonReader jsonReader = Json.createReader(new StringReader(json));
        JsonObject jsonObject = jsonReader.readObject();
        Assertions.assertEquals(jsonObject.getString("a"), "c");
    }

    @Test
    void testJsonReaderDuplicateKey2() {
        String json = "{\"a\":\"b\",\"a\":\"c\"}";
        JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(Collections.singletonMap(JsonConfig.KEY_STRATEGY, JsonConfig.KeyStrategy.NONE));
        JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(json));
        try {
            jsonReader.readObject();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof JsonParsingException);
            Assertions.assertEquals("Duplicate key 'a' is not allowed", e.getMessage());
        }
    }

    @Test
    void testJsonReaderDuplicateKey3() {
        String json = "{\"a\":\"b\",\"b\":{\"c\":\"d\",\"c\":\"e\"}}";
        JsonReader jsonReader = Json.createReader(new StringReader(json));
        JsonObject jsonObject = jsonReader.readObject();
        Assertions.assertEquals(jsonObject.getJsonObject("b").getString("c"), "e");
    }

    @Test
    void testJsonReaderDuplicateKey4() {
        String json = "{\"a\":\"b\",\"b\":{\"c\":\"d\",\"c\":\"e\"}}";
        JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(Collections.singletonMap(JsonConfig.KEY_STRATEGY, JsonConfig.KeyStrategy.NONE));
        JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(json));
        try {
            jsonReader.readObject();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof JsonParsingException);
            Assertions.assertEquals("Duplicate key 'c' is not allowed", e.getMessage());
        }
    }

    @Test
    void testJsonObjectBuilderDuplcateKey1() {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonObject jsonObject = objectBuilder.add("a", "b").add("a", "c").build();
        Assertions.assertEquals(jsonObject.getString("a"), "c");
    }

    @Test
    void testJsonObjectBuilderDuplcateKey2() {
        JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(Collections.singletonMap(JsonConfig.KEY_STRATEGY, JsonConfig.KeyStrategy.NONE));
        JsonObjectBuilder objectBuilder = jsonBuilderFactory.createObjectBuilder();
        try {
            objectBuilder.add("a", "b").add("a", "c").build();
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof IllegalStateException);
            Assertions.assertEquals("Duplicate key 'a' is not allowed", e.getMessage());
        }
    }
}
