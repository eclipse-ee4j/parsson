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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jitendra Kotamraju
 */
public class JsonBuilderTest {
    
    @Test
    void testEmptyObject() {
        JsonObject empty = Json.createObjectBuilder()
                .build();

        JsonObjectTest.testEmpty(empty);
    }

    @Test
    void testEmptyArray() {
        JsonArray empty = Json.createArrayBuilder()
                .build();

        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void testObject() {
        JsonObject person = buildPerson();
        JsonObjectTest.testPerson(person);
    }

    @Test
    void testNumber() {
        JsonObject person = buildPerson();
        JsonNumber number = person.getJsonNumber("age");
        Assertions.assertEquals(25, number.intValueExact());
        Assertions.assertEquals(25, number.intValue());
        Assertions.assertTrue(number.isIntegral());
        JsonObjectTest.testPerson(person);
    }

    @Test
    void testJsonObjectCopy() {
        JsonObject person = buildPerson();
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder(person);
        final JsonObject copyPerson = objectBuilder.build();

        JsonNumber number = copyPerson.getJsonNumber("age");
        Assertions.assertEquals(25, number.intValueExact());
        Assertions.assertEquals(25, number.intValue());
        Assertions.assertTrue(number.isIntegral());
        JsonObjectTest.testPerson(copyPerson);

    }

    @Test
    void testJsonObjectMap() {
        Map<String, Object> person = buildPersonAsMap();
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder(person);
        final JsonObject copyPerson = objectBuilder.build();

        JsonNumber number = copyPerson.getJsonNumber("age");
        Assertions.assertEquals(25, number.intValueExact());
        Assertions.assertEquals(25, number.intValue());
        Assertions.assertTrue(number.isIntegral());
        JsonObjectTest.testPerson(copyPerson);
    }

    static Map<String, Object> buildPersonAsMap() {
        Map<String, Object> person = new HashMap<>();
        person.put("firstName", "John");
        person.put("lastName", "Smith");
        person.put("age", 25);

        Map<String, Object> address = new HashMap<>();
        address.put("streetAddress", "21 2nd Street");
        address.put("city", "New York");
        address.put("state", "NY");
        address.put("postalCode", "10021");

        person.put("address", address);
        person.put("mailingAddress", Optional.empty());

        Collection<Map<String, Object>> phones = new ArrayList<>();

        Map<String, Object> phone1 = new HashMap<>();
        phone1.put("type", "home");
        phone1.put("number", "212 555-1234");
        phones.add(phone1);

        Map<String, Object> phone2 = new HashMap<>();
        phone2.put("type", "fax");
        phone2.put("number", "646 555-4567");
        phones.add(phone2);

        person.put("phoneNumber", phones);

        return person;
    }

    static JsonObject buildPerson() {
        return Json.createObjectBuilder()
                .add("firstName", "John")
                .add("lastName", "Smith")
                .add("age", 25)
                .add("address", Json.createObjectBuilder()
                        .add("streetAddress", "21 2nd Street")
                        .add("city", "New York")
                        .add("state", "NY")
                        .add("postalCode", "10021"))
                .add("phoneNumber", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("type", "home")
                                .add("number", "212 555-1234"))
                        .add(Json.createObjectBuilder()
                                .add("type", "fax")
                                .add("number", "646 555-4567")))
                .build();
    }

    static JsonObject buildAddress() {
        return Json.createObjectBuilder()
                .add("streetAddress", "21 2nd Street")
                .add("city", "New York")
                .add("state", "NY")
                .add("postalCode", "10021")
                .build();
    }

    static JsonArray buildPhone() {
        return Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("type", "home")
                        .add("number", "212 555-1234"))
                .add(Json.createObjectBuilder()
                        .add("type", "fax")
                        .add("number", "646 555-4567"))
                .build();
    }

}
