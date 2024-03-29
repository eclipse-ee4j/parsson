/*
 * Copyright (c) 2016, 2024 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.json.JsonException;
import jakarta.json.JsonPatch.Operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lukas
 */
public class JsonPatchOperationTest {

    private static final String[] opNames = {"add", "remove", "replace", "move", "copy", "test"};

    @Test
    void fromOperationName() {
        for (String op: opNames) {
            Assertions.assertEquals(Operation.valueOf(op.toUpperCase()), Operation.fromOperationName(op));
        }
        for (String op: opNames) {
            Assertions.assertEquals(Operation.valueOf(op.toUpperCase()), Operation.fromOperationName(op.toUpperCase()));
        }
    }

    @Test
    void fromInvalidOperationName() {
        Assertions.assertThrows(JsonException.class, () -> Operation.fromOperationName("undef"));
    }
}
