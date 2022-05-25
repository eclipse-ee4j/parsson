/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.parsson.JsonMergePatchImpl;
import org.junit.Test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonPatch;

public class JsonMergePatch2Test {

    @Test
    public void testToString() {
        JsonArray jsonArray = Json.createArrayBuilder().add(Json.createValue(1)).build();
        JsonPatch jsonPatch = Json.createPatchBuilder(jsonArray).build();
        assertEquals("[1]", jsonPatch.toString());
        JsonMergePatch jsonMergePatch = Json.createMergePatch(jsonArray);
        assertEquals("[1]", jsonMergePatch.toString());
    }

    @Test
    public void testEquals() {
        JsonMergePatchImpl j1 = new JsonMergePatchImpl(Json.createValue("test"));
        JsonMergePatchImpl j2 = new JsonMergePatchImpl(Json.createValue("test"));
        JsonMergePatchImpl j3 = new JsonMergePatchImpl(j1.toJsonValue());
        JsonMergePatchImpl j4 = new JsonMergePatchImpl(Json.createValue("test2"));
        JsonMergePatchImpl j5 = new JsonMergePatchImpl(null);

        assertTrue(j1.equals(j1));

        assertTrue(j1.equals(j2));
        assertTrue(j2.equals(j1));

        assertTrue(j1.equals(j3));
        assertTrue(j3.equals(j1));

        assertTrue(j2.equals(j3));
        assertTrue(j3.equals(j2));

        assertFalse(j1.equals(j4));
        assertFalse(j1.equals(j5));
        assertFalse(j1.equals(null));
    }
}
