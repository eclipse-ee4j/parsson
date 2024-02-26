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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.math.BigDecimal;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import org.eclipse.parsson.api.JsonConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test maxBigDecimalLength limit set from System property.
 */
public class JsonBigDecimalLengthLimitTest  {

    @BeforeEach
    void setUp() {
        System.setProperty(JsonConfig.MAX_BIGDECIMAL_LEN, "500");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(JsonConfig.MAX_BIGDECIMAL_LEN);
    }

    // Test BigDecimal max source characters array length using length equal to system property limit of 500.
    // Parsing shall pass and return value equal to source String.
    @Test
    void testLargeBigDecimalBellowLimit() {
        JsonReader reader = Json.createReader(new StringReader(JsonNumberTest.Π_500));
        JsonNumber check = Json.createValue(new BigDecimal(JsonNumberTest.Π_500));
        JsonValue value = reader.readValue();
        assertEquals(value.getValueType(), JsonValue.ValueType.NUMBER);
        assertEquals(value, check);
    }

    // Test BigDecimal max source characters array length using length above system property limit of 500.
    // Parsing shall pass and return value equal to source String.
    @Test
    void testLargeBigDecimalAboveLimit() {
        JsonReader reader = Json.createReader(new StringReader(JsonNumberTest.Π_501));
        try {
            reader.readValue();
            fail("No exception was thrown from BigDecimal parsing with source characters array length over limit");
        } catch (UnsupportedOperationException e) {
            // UnsupportedOperationException is expected to be thrown
            assertEquals(
                    "Number of BigDecimal source characters 501 exceeded maximal allowed value of 500",
                    e.getMessage());
        }
    }

}
