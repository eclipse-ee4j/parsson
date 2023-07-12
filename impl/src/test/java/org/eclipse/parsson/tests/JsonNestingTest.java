/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
import org.junit.Test;

import java.io.StringReader;

public class JsonNestingTest {

    @Test(expected = RuntimeException.class)
    public void testArrayNestingException() {
        String json = createDeepNestedDoc(500);
        try (JsonParser parser = Json.createParser(new StringReader(json))) {
            while (parser.hasNext()) {
                JsonParser.Event ev = parser.next();
                if (JsonParser.Event.START_ARRAY == ev) {
                    parser.getArray();
                }
            }
        }
    }

    @Test
    public void testArrayNesting() {
        String json = createDeepNestedDoc(499);
        try (JsonParser parser = Json.createParser(new StringReader(json))) {
            while (parser.hasNext()) {
                JsonParser.Event ev = parser.next();
                if (JsonParser.Event.START_ARRAY == ev) {
                    parser.getArray();
                }
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void testObjectNestingException() {
        String json = createDeepNestedDoc(500);
        try (JsonParser parser = Json.createParser(new StringReader(json))) {
            while (parser.hasNext()) {
                JsonParser.Event ev = parser.next();
                if (JsonParser.Event.START_OBJECT == ev) {
                    parser.getObject();
                }
            }
        }
    }

    @Test
    public void testObjectNesting() {
        String json = createDeepNestedDoc(499);
        try (JsonParser parser = Json.createParser(new StringReader(json))) {
            while (parser.hasNext()) {
                JsonParser.Event ev = parser.next();
                if (JsonParser.Event.START_OBJECT == ev) {
                    parser.getObject();
                }
            }
        }
    }

    private static String createDeepNestedDoc(final int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < depth; i++) {
            sb.append("{ \"a\": [");
        }
        sb.append(" \"val\" ");
        for (int i = 0; i < depth; i++) {
            sb.append("]}");
        }
        sb.append("]");
        return sb.toString();
    }

}
