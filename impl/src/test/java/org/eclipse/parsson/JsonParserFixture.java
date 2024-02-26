/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.parsson;

import java.io.StringReader;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;

/**
 * Class with methods that creates JsonParser with different configuration from different sources and runs the test code with this parser
 */
public class JsonParserFixture {
    /**
     * Runs the test code with JsonParser created from the given JsonObject
     *
     * @param object         JsonObject to create JsonParser from
     * @param parserConsumer test code to run with the created JsonParser
     */
    public static void testWithCreateParserFromObject(JsonObject object, Consumer<JsonParser> parserConsumer) {
        testWithParser(() -> Json.createParserFactory(null).createParser(object), parserConsumer);
    }

    /**
     * Runs the test code with JsonParser created from the given JsonArray
     *
     * @param array          JsonArray to create JsonParser from
     * @param parserConsumer test code to run with the created JsonParser
     */
    public static void testWithCreateParserFromArray(JsonArray array, Consumer<JsonParser> parserConsumer) {
        testWithParser(() -> Json.createParserFactory(null).createParser(array), parserConsumer);
    }

    /**
     * Runs the test code with JsonParser created from the given String
     *
     * @param string         String with JSON to create JsonParser from
     * @param parserConsumer test code to run with the created JsonParser
     */
    public static void testWithCreateParserFromString(String string, Consumer<JsonParser> parserConsumer) {
        testWithParser(() -> Json.createParser(new StringReader(string)), parserConsumer);
    }

    /**
     * Runs the test code with JsonParser created from the given String
     *
     * @param parserSupplier Supplier of JsonParser to create JsonParser from
     * @param parserConsumer test code to run with the created JsonParser
     */
    private static void testWithParser(Supplier<JsonParser> parserSupplier, Consumer<JsonParser> parserConsumer) {
        try (JsonParser parser = parserSupplier.get()) {
            parserConsumer.accept(parser);
        }
    }
}
