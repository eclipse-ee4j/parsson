/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lukas
 */
public class JsonBuilderFactoryTest {

    @Test
    void testArrayBuilder() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        Assertions.assertNotNull(builderFactory.createArrayBuilder());
    }

    @Test
    void testArrayBuilderNPE() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
            builderFactory.createArrayBuilder(null);
        });
    }

    @Test
    void testArrayBuilderFromArray() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        JsonArrayBuilder builder = builderFactory.createArrayBuilder(JsonBuilderTest.buildPhone());
        Assertions.assertEquals(JsonBuilderTest.buildPhone(), builder.build());
    }

    @Test
    void testObjectBuilder() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        Assertions.assertNotNull(builderFactory.createObjectBuilder());
    }

    @Test
    void testObjectBuilderNPE() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
            builderFactory.createObjectBuilder((JsonObject) null);
        });
    }

    @Test
    void testObjectBuilderNPE_map() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
            builderFactory.createObjectBuilder((Map<String, Object>) null);
        });
    }

    @Test
    void testObjectBuilderFromObject() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = builderFactory.createObjectBuilder(JsonBuilderTest.buildPerson());
        Assertions.assertEquals(JsonBuilderTest.buildPerson(), builder.build());
    }
}
