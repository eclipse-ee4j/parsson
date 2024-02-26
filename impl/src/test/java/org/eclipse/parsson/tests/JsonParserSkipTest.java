/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved.
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

import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromArray;
import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromObject;
import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;

import org.junit.jupiter.api.Test;

/**
 *
 * @author lukas
 */
public class JsonParserSkipTest {

    @Test
    void testSkipArrayReader() {
        testWithCreateParserFromString("[[],[[]]]", JsonParserSkipTest::testSkipArray);
    }

    @Test
    void testSkipArrayStructure() {
        testWithCreateParserFromArray(Json.createArrayBuilder()
                .add(Json.createArrayBuilder())
                .add(Json.createArrayBuilder()
                        .add(Json.createArrayBuilder()))
                .build(), JsonParserSkipTest::testSkipArray);
    }

    private static void testSkipArray(JsonParser parser) {
        assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        parser.skipArray();
		assertFalse(parser.hasNext());
    }

    @Test
    void testSkipInsideArrayReader() {
        testWithCreateParserFromString("[\"test\"]", JsonParserSkipTest::testSkipInsideArray);
    }

    @Test
    void testSkipInsideArrayStructure() {
        testWithCreateParserFromArray(Json.createArrayBuilder()
                .add("test")
                .build(), JsonParserSkipTest::testSkipInsideArray);
    }

    private static void testSkipInsideArray(JsonParser parser) {
        assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        assertEquals(JsonParser.Event.VALUE_STRING, parser.next());
        parser.skipArray();
		assertFalse(parser.hasNext());
    }

    @Test
    void testNoSkipArrayReader() {
        testWithCreateParserFromString("{\"key\":\"value\"}", JsonParserSkipTest::testNoSkipArray);
    }

    @Test
    void testNoSkipArrayStructure() {
        testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("key","value")
                .build(), JsonParserSkipTest::testNoSkipArray);
    }

    private static void testNoSkipArray(JsonParser parser) {
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        parser.skipArray();
        assertEquals(JsonParser.Event.VALUE_STRING, parser.next());
        assertEquals(JsonParser.Event.END_OBJECT, parser.next());
        assertFalse(parser.hasNext());
    }

    @Test
    void testSkipArrayInObjectReader() {
        testWithCreateParserFromString("{\"array\":[[],[[]]],\"object\":\"value2\"}", JsonParserSkipTest::testSkipArrayInObject);
    }

    @Test
    void testSkipArrayInObjectStructure() {
        testWithCreateParserFromObject(Json.createObjectBuilder().add("array", Json.createArrayBuilder()
                        .add(Json.createArrayBuilder())
                        .add(Json.createArrayBuilder()
                                .add(Json.createArrayBuilder()))
                ).add("object", "value2")
                .build(), JsonParserSkipTest::testSkipArrayInObject);
    }

    private static void testSkipArrayInObject(JsonParser parser) {
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        parser.skipArray();
        assertTrue(parser.hasNext());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.VALUE_STRING, parser.next());
        assertEquals(JsonParser.Event.END_OBJECT, parser.next());
        assertFalse(parser.hasNext());
    }

    @Test
    void testSkipObjectReader() {
        testWithCreateParserFromString("{\"array\":[],\"objectToSkip\":{\"huge key\":\"huge value\"},\"simple\":2}", JsonParserSkipTest::testSkipObject);
    }

    @Test
    void testSkipObjectStructure() {
        testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("array", Json.createArrayBuilder().build())
                .add("objectToSkip", Json.createObjectBuilder().add("huge key", "huge value"))
                .add("simple", 2)
                .build(), JsonParserSkipTest::testSkipObject);
    }

    private static void testSkipObject(JsonParser parser) {
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.START_ARRAY, parser.next());
        assertEquals(JsonParser.Event.END_ARRAY, parser.next());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        parser.skipObject();
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.VALUE_NUMBER, parser.next());
        assertEquals(JsonParser.Event.END_OBJECT, parser.next());
		assertFalse(parser.hasNext());
    }

    @Test
    void testSkipInsideObjectReader() {
        testWithCreateParserFromString("{\"objectToSkip\":{\"huge key\":\"huge value\"},\"simple\":2}", JsonParserSkipTest::testSkipInsideObject);
    }

    @Test
    void testSkipInsideObjectStructure() {
        testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("objectToSkip", Json.createObjectBuilder().add("huge key", "huge value"))
                .add("simple", 2)
                .build(), JsonParserSkipTest::testSkipInsideObject);
    }

    private static void testSkipInsideObject(JsonParser parser) {
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        parser.skipObject();
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        assertEquals(JsonParser.Event.VALUE_NUMBER, parser.next());
        assertEquals(JsonParser.Event.END_OBJECT, parser.next());
		assertFalse(parser.hasNext());
    }

    @Test
    void testNoSkipObjectReader() {
        testWithCreateParserFromString("{\"key\":\"value\"}", JsonParserSkipTest::testNoSkipObject);
    }

    @Test
    void testNoSkipObjectStructure() {
        testWithCreateParserFromObject(Json.createObjectBuilder()
                .add("Key", "value")
                .build(), JsonParserSkipTest::testNoSkipObject);
    }

    private static void testNoSkipObject(JsonParser parser) {
        assertEquals(JsonParser.Event.START_OBJECT, parser.next());
        assertEquals(JsonParser.Event.KEY_NAME, parser.next());
        parser.skipObject();
		assertFalse(parser.hasNext());
    }
}
