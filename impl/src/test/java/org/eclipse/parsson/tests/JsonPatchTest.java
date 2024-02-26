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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
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
public class JsonPatchTest {

    public static Iterable<Object[]> data() throws Exception {
        List<Object[]> examples = new ArrayList<>();
        JsonArray data = loadData();
        for (JsonValue jsonValue : data) {
            JsonObject test = (JsonObject) jsonValue;
            Object[] testData = new Object[4];
            testData[0] = createPatchArray(test.get("op"));
            testData[1] = test.get("target");
            testData[2] = test.get("expected");
            testData[3] = createExceptionClass((JsonString)test.get("exception"));

            examples.add(testData);
        }

        return examples;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Exception> createExceptionClass(
            JsonString exceptionClassName) throws ClassNotFoundException {
        if (exceptionClassName != null) {
            return (Class<? extends Exception>) Class
                    .forName(exceptionClassName.getString());
        }
        return null;
    }

    private static JsonArray createPatchArray(JsonValue object) {
        return Json.createArrayBuilder().add(object).build();
    }

    private static JsonArray loadData() {
        InputStream testData = JsonPatchTest.class
                .getResourceAsStream("/jsonpatch.json");
        JsonReader reader = Json.createReader(testData);
        return (JsonArray) reader.read();
    }

    @MethodSource("data")
    @ParameterizedTest(name = "{index}: ({0})={1}")
    void shouldExecuteJsonPatchOperationsToJsonDocument(JsonArray arrayPatch, JsonStructure target, JsonValue expected, Class<? extends Exception> expectedException) {
        try {
            JsonPatch patch = Json.createPatchBuilder(arrayPatch).build();
            JsonStructure output = patch.apply(target);
            MatcherAssert.assertThat(output, CoreMatchers.is(expected));
            MatcherAssert.assertThat(expectedException, CoreMatchers.nullValue());
        } catch (Exception e) {
            if (expectedException == null) {
                Assertions.fail(e.getMessage());
            } else {
                MatcherAssert.assertThat(e, CoreMatchers.instanceOf(expectedException));
            }
        }
    }
}
