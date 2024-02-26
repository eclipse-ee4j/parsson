/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Objects;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonPointer;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 
 * @author Alex Soto
 *
 */
public class JsonPointerTest {

    private static JsonObject rfc6901Example;

    public static Iterable<Object[]> data() throws Exception {
        rfc6901Example = JsonPointerTest.readRfc6901Example();
        return Arrays.asList(new Object[][] { 
                 {Json.createPointer(""), rfc6901Example, null },
                 {Json.createPointer("/foo"), rfc6901Example.getJsonArray("foo"), null},
                 {Json.createPointer("/foo/0"), rfc6901Example.getJsonArray("foo").get(0), null},
                 {Json.createPointer("/foo/5"), null, JsonException.class},
                 {Json.createPointer("/p/1"), null, JsonException.class},
                 {Json.createPointer("/"), rfc6901Example.getJsonNumber(""), null},
                 {Json.createPointer("/a~1b"), rfc6901Example.getJsonNumber("a/b"), null},
                 {Json.createPointer("/m~0n"), rfc6901Example.getJsonNumber("m~n"), null},
                 {Json.createPointer("/c%d"), rfc6901Example.getJsonNumber("c%d"), null},
                 {Json.createPointer("/e^f"), rfc6901Example.getJsonNumber("e^f"), null},
                 {Json.createPointer("/g|h"), rfc6901Example.getJsonNumber("g|h"), null},
                 {Json.createPointer("/i\\j"), rfc6901Example.getJsonNumber("i\\j"), null},
                 {Json.createPointer("/k\"l"), rfc6901Example.getJsonNumber("k\"l"), null},
                 {Json.createPointer("/ "), rfc6901Example.getJsonNumber(" "), null},
                 {Json.createPointer("/notexists"), null, JsonException.class},
                 {Json.createPointer("/s/t"), null, JsonException.class},
                 {Json.createPointer("/o"), JsonObject.NULL, null}
           });
    }

    @MethodSource("data")
    @ParameterizedTest(name = "{index}: ({0})={1}")
    void shouldEvaluateJsonPointerExpressions(JsonPointer pointer, JsonValue expected, Class<? extends Exception> expectedException) {
        try {
            JsonValue result = pointer.getValue(rfc6901Example);
            assertThat(result, is(expected));
            assertThat(expectedException, nullValue());
        } catch(Exception e) {
            if(expectedException == null) {
                fail(e.getMessage());
            } else {
                assertThat(e, instanceOf(expectedException));
            }
        }
    }

    static JsonObject readRfc6901Example() {
        Reader rfc6901Reader = new InputStreamReader(Objects.requireNonNull(JsonReaderTest.class.getResourceAsStream("/rfc6901.json")));
        JsonReader reader = Json.createReader(rfc6901Reader);
        JsonObject value = reader.readObject();
        reader.close();
        return value;
    }
}
