/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jitendra Kotamraju
 */
public class JsonStringTest {

    // tests JsonString#toString()
    @Test
    void testToString() {
        escapedString("");
        escapedString("abc");
        escapedString("abc\f");
        escapedString("abc\na");
        escapedString("abc\tabc");
        escapedString("abc\n\tabc");
        escapedString("abc\n\tabc\r");
        escapedString("\n\tabc\r");
        escapedString("\bab\tb\rc\\\"\ftesting1234");
        escapedString("\f\babcdef\tb\rc\\\"\ftesting1234");
        escapedString("\u0000\u00ff");
        escapedString("abc\"\\/abc");
    }

    @Test
    void testHashCode() {
        String string1 = "a";
        JsonString jsonString1 = Json.createValue(string1);
        Assertions.assertTrue(jsonString1.hashCode() == jsonString1.getString().hashCode());

        String string2 = new String("a");
        JsonString jsonString2 = Json.createValue(string2);

        Assertions.assertTrue(jsonString1.equals(jsonString2));
        Assertions.assertTrue(jsonString1.hashCode() == jsonString2.hashCode());
    }

    void escapedString(String str) {
        JsonArray exp = Json.createArrayBuilder().add(str).build();
        String parseStr = "["+exp.get(0).toString()+"]";
        JsonReader jr = Json.createReader(new StringReader(parseStr));
        JsonArray got = jr.readArray();
        Assertions.assertEquals(exp, got);
        jr.close();
    }

}
