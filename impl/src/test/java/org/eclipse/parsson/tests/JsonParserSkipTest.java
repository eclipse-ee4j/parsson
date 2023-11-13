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

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import junit.framework.TestCase;

import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromArray;
import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromObject;
import static org.eclipse.parsson.JsonParserFixture.testWithCreateParserFromString;

/**
 *
 * @author lukas
 */
public class JsonParserSkipTest extends TestCase {

    public void testSkipArrayReader() {
        testWithCreateParserFromString("[[],[[]]]", JsonParserSkipTest::testSkipArray);
    }

    public void testSkipArrayStructure() {
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

    public void testSkipInsideArrayReader() {
        testWithCreateParserFromString("[\"test\"]", JsonParserSkipTest::testSkipInsideArray);
    }

    public void testSkipInsideArrayStructure() {
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

    public void testNoSkipArrayReader() {
        testWithCreateParserFromString("{\"key\":\"value\"}", JsonParserSkipTest::testNoSkipArray);
    }

    public void testNoSkipArrayStructure() {
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

    public void testSkipArrayInObjectReader() {
        testWithCreateParserFromString("{\"array\":[[],[[]]],\"object\":\"value2\"}", JsonParserSkipTest::testSkipArrayInObject);
    }

    public void testSkipArrayInObjectStructure() {
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

    public void testSkipObjectReader() {
        testWithCreateParserFromString("{\"array\":[],\"objectToSkip\":{\"huge key\":\"huge value\"},\"simple\":2}", JsonParserSkipTest::testSkipObject);
    }

    public void testSkipObjectStructure() {
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

    public void testSkipInsideObjectReader() {
        testWithCreateParserFromString("{\"objectToSkip\":{\"huge key\":\"huge value\"},\"simple\":2}", JsonParserSkipTest::testSkipInsideObject);
    }

    public void testSkipInsideObjectStructure() {
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

    public void testNoSkipObjectReader() {
        testWithCreateParserFromString("{\"key\":\"value\"}", JsonParserSkipTest::testNoSkipObject);
    }

    public void testNoSkipObjectStructure() {
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
