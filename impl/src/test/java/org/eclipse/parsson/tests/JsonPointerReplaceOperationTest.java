/*
 * Copyright (c) 2015, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonPointer;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 
 * @author Alex Soto
 *
 */
public class JsonPointerReplaceOperationTest {

    public static Iterable<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] { 
                 {buildSimpleReplacePatch(), buildAddress(), buildExpectedAddress(), null},
                 {buildComplexReplacePatch(), buildPerson(), buildExpectedPerson(), null},
                 {buildArrayReplacePatchInPosition(), buildPerson(), buildExpectedPersonConcreteArrayPosition(), null},
                 {buildArrayAddPatchInLastPosition(), buildPerson(), null, JsonException.class},
                 {buildNoneExistingReplacePatch(), buildAddress(), null, JsonException.class}
           });
    }

    @MethodSource("data")
    @ParameterizedTest(name = "{index}: ({0})={1}")
    void shouldReplaceElementsToExistingJsonDocument(JsonObject pathOperation, JsonStructure target, JsonValue expectedResult, Class<? extends Exception> expectedException) {
        try {
        JsonPointer pointer = Json.createPointer(pathOperation.getString("path"));
        JsonObject modified = (JsonObject) pointer.replace(target, pathOperation.get("value"));
        MatcherAssert.assertThat(modified, CoreMatchers.is(expectedResult));
        MatcherAssert.assertThat(expectedException, CoreMatchers.nullValue());
        } catch(Exception e) {
            if(expectedException == null) {
                Assertions.fail(e.getMessage());
            } else {
                MatcherAssert.assertThat(e, CoreMatchers.instanceOf(expectedException));
            }
        }
    }

    static JsonObject buildAddress() {
        return Json.createObjectBuilder()
                .add("streetAddress", "21 2nd Street")
                .add("city", "New York")
                .add("state", "NY")
                .add("postalCode", "10021")
                .build();
    }
    static JsonObject buildComplexReplacePatch() {
        return Json.createObjectBuilder()
                .add("op", "add")
                .add("path", "/address/streetAddress")
                .add("value", "myaddress")
                .build();
    }
    static JsonObject buildSimpleReplacePatch() {
        return Json.createObjectBuilder()
                .add("op", "replace")
                .add("path", "/streetAddress")
                .add("value", "myaddress")
                .build();
    }
    static JsonObject buildNoneExistingReplacePatch() {
        return Json.createObjectBuilder()
                .add("op", "replace")
                .add("path", "/notexists")
                .add("value", "myaddress")
                .build();
    }
    static JsonObject buildArrayReplacePatchInPosition() {
        return Json.createObjectBuilder()
                .add("op", "replace")
                .add("path", "/phoneNumber/0")
                .add("value", Json.createObjectBuilder()
                        .add("type", "home")
                        .add("number", "200 555-1234"))
                .build();
    }
    static JsonObject buildArrayAddPatchInLastPosition() {
        return Json.createObjectBuilder()
                .add("op", "add")
                .add("path", "/phoneNumber/-")
                .add("value", Json.createObjectBuilder()
                        .add("type", "home")
                        .add("number", "200 555-1234"))
                .build();
    }
    static JsonObject buildExpectedAddress() {
        return Json.createObjectBuilder()
                .add("streetAddress", "myaddress")
                .add("city", "New York")
                .add("state", "NY")
                .add("postalCode", "10021")
                .build();
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
    static JsonObject buildExpectedPersonConcreteArrayPosition() {
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
                        .add((Json.createObjectBuilder()
                                .add("type", "home")
                                .add("number", "200 555-1234")))
                        .add(Json.createObjectBuilder()
                                .add("type", "fax")
                                .add("number", "646 555-4567")))
                .build();
    }
    static JsonObject buildExpectedPerson() {
        return Json.createObjectBuilder()
                .add("firstName", "John")
                .add("lastName", "Smith")
                .add("age", 25)
                .add("address", Json.createObjectBuilder()
                        .add("streetAddress", "myaddress")
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
    
}
