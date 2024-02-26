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

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;

import org.eclipse.parsson.JsonParserFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lukas
 */
public class JsonParserSkipTest {

    @Test
    void testSkipArrayReader() {
        JsonParserFixture.testWithCreateParserFromString("[[],[[]]]", JsonParserSkipTest::testSkipArray);
    }

    @Test
    void testSkipArrayStructure() {
        JsonParserFixture.testWithCreateParserFromArray(Json.createArrayBuilder()
                .add(Json.createArrayBuilder())
                .add(Json.createArrayBuilder()
                        .add(Json.createArrayBuilder()))
                .build(), JsonParserSkipTest::testSkipArray);
    }

    private static void testSkipArray(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        parser.skipArray();
		Assertions.assertFalse(parser.hasNext());
    }

    @Test
    void testSkipInsideArrayReader() {
        JsonParserFixture.testWithCreateParserFromString("[\"test\"]", JsonParserSkipTest::testSkipInsideArray);
    }

    @Test
    void testSkipInsideArrayStructure() {
        JsonParserFixture.testWithCreateParserFromArray(Json.createArrayBuilder()
                .add("test")
                .build(), JsonParserSkipTest::testSkipInsideArray);
    }

    private static void testSkipInsideArray(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        Assertions.assertEquals(JsonParser.Event.VALUE_STRING, parser.next());
        parser.skipArray();
		Assertions.assertFalse(parser.hasNext());
    }

    @Test
    void testNoSkipArrayReader() {
        JsonParserFixture.testWithCreateParserFromString("{\"key\":\"value\"}", JsonParserSkipTest::testNoSkipArray);
    }

    @Test
    void testNoSkipArrayStructure() {
        JsonParserFixture.testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("key","value")
                .build(), JsonParserSkipTest::testNoSkipArray);
    }

    private static void testNoSkipArray(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        parser.skipArray();
        Assertions.assertEquals(JsonParser.Event.VALUE_STRING, parser.next());
        Assertions.assertEquals(JsonParser.Event.END_OBJECT, parser.next());
        Assertions.assertFalse(parser.hasNext());
    }

    @Test
    void testSkipArrayInObjectReader() {
        JsonParserFixture.testWithCreateParserFromString("{\"array\":[[],[[]]],\"object\":\"value2\"}", JsonParserSkipTest::testSkipArrayInObject);
    }

    @Test
    void testSkipArrayInObjectStructure() {
        JsonParserFixture.testWithCreateParserFromObject(Json.createObjectBuilder().add("array", Json.createArrayBuilder()
                        .add(Json.createArrayBuilder())
                        .add(Json.createArrayBuilder()
                                .add(Json.createArrayBuilder()))
                ).add("object", "value2")
                .build(), JsonParserSkipTest::testSkipArrayInObject);
    }

    private static void testSkipArrayInObject(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        parser.skipArray();
        Assertions.assertTrue(parser.hasNext());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.VALUE_STRING, parser.next());
        Assertions.assertEquals(JsonParser.Event.END_OBJECT, parser.next());
        Assertions.assertFalse(parser.hasNext());
    }

    @Test
    void testSkipObjectReader() {
        JsonParserFixture.testWithCreateParserFromString("{\"array\":[],\"objectToSkip\":{\"huge key\":\"huge value\"},\"simple\":2}", JsonParserSkipTest::testSkipObject);
    }

    @Test
    void testSkipObjectStructure() {
        JsonParserFixture.testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("array", Json.createArrayBuilder().build())
                .add("objectToSkip", Json.createObjectBuilder().add("huge key", "huge value"))
                .add("simple", 2)
                .build(), JsonParserSkipTest::testSkipObject);
    }

    private static void testSkipObject(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        Assertions.assertEquals(JsonParser.Event.END_ARRAY, parser.next());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        parser.skipObject();
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.VALUE_NUMBER, parser.next());
        Assertions.assertEquals(JsonParser.Event.END_OBJECT, parser.next());
		Assertions.assertFalse(parser.hasNext());
    }

    @Test
    void testSkipInsideObjectReader() {
        JsonParserFixture.testWithCreateParserFromString("{\"objectToSkip\":{\"huge key\":\"huge value\"},\"simple\":2}", JsonParserSkipTest::testSkipInsideObject);
    }

    @Test
    void testSkipInsideObjectStructure() {
        JsonParserFixture.testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("objectToSkip", Json.createObjectBuilder().add("huge key", "huge value"))
                .add("simple", 2)
                .build(), JsonParserSkipTest::testSkipInsideObject);
    }

    private static void testSkipInsideObject(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        parser.skipObject();
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        Assertions.assertEquals(JsonParser.Event.VALUE_NUMBER, parser.next());
        Assertions.assertEquals(JsonParser.Event.END_OBJECT, parser.next());
		Assertions.assertFalse(parser.hasNext());
    }

    @Test
    void testNoSkipObjectReader() {
        JsonParserFixture.testWithCreateParserFromString("{\"key\":\"value\"}", JsonParserSkipTest::testNoSkipObject);
    }

    @Test
    void testNoSkipObjectStructure() {
        JsonParserFixture.testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("Key", "value")
                .build(), JsonParserSkipTest::testNoSkipObject);
    }

    private static void testNoSkipObject(JsonParser parser) {
        Assertions.assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        Assertions.assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        parser.skipObject();
		Assertions.assertFalse(parser.hasNext());
    }
}
