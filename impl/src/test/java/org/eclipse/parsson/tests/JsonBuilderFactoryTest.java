/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 *
 * @author lukas
 */
public class JsonBuilderFactoryTest {
    
    @Test
    public void testArrayBuilder() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        Assert.assertNotNull(builderFactory.createArrayBuilder());
    }
    
    @Test(expected = NullPointerException.class)
    public void testArrayBuilderNPE() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        builderFactory.createArrayBuilder(null);
    }

    @Test
    public void testArrayBuilderFromArray() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        JsonArrayBuilder builder = builderFactory.createArrayBuilder(JsonBuilderTest.buildPhone());
        Assert.assertEquals(JsonBuilderTest.buildPhone(), builder.build());
    }

    @Test
    public void testObjectBuilder() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        Assert.assertNotNull(builderFactory.createObjectBuilder());
    }
    
    @Test(expected = NullPointerException.class)
    public void testObjectBuilderNPE() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        builderFactory.createObjectBuilder((JsonObject) null);
    }

    @Test(expected = NullPointerException.class)
    public void testObjectBuilderNPE_map() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        builderFactory.createObjectBuilder((Map<String, Object>) null);
    }

    @Test
    public void testObjectBuilderFromObject() {
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = builderFactory.createObjectBuilder(JsonBuilderTest.buildPerson());
        Assert.assertEquals(JsonBuilderTest.buildPerson(), builder.build());
    }

    @Test(expected = NullPointerException.class)
    public void testIgnoreIfNullDefault() {
        Map<String, Object> config = new HashMap<>();
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(config);
        builderFactory.createObjectBuilder()
        .add("name", (String) null)
        .add("age", 36)
        .build();
    }

    @Test(expected = NullPointerException.class)
    public void testIgnoreIfNullDisabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonBuilderFactory.IGNORE_ADDING_IF_NULL, false);
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(config);
        builderFactory.createObjectBuilder()
        .add("name", (String) null)
        .add("age", 36)
        .build();
    }

    @Test
    public void testIgnoreIfNullEnabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonBuilderFactory.IGNORE_ADDING_IF_NULL, true);
        JsonBuilderFactory builderFactory = Json.createBuilderFactory(config);
        JsonObject jsonObject = builderFactory.createObjectBuilder()
        .add("name", (String) null)
        .add("age", 36)
        .build();
        assertEquals(jsonObject.toString(), "{\"age\":36}");
    }
}
