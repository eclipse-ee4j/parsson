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

import java.io.StringReader;
import java.math.BigDecimal;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import junit.framework.TestCase;
import org.eclipse.parsson.api.JsonConfig;

/**
 * Test maxBigDecimalLength limit set from System property.
 */
public class JsonBigDecimalLengthLimitTest extends TestCase  {

    public JsonBigDecimalLengthLimitTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() {
        System.setProperty(JsonConfig.MAX_BIGDECIMAL_LEN, "500");
    }

    @Override
    protected void tearDown() {
        System.clearProperty(JsonConfig.MAX_BIGDECIMAL_LEN);
    }

    // Test BigDecimal max source characters array length using length equal to system property limit of 500.
    // Parsing shall pass and return value equal to source String.
    public void testLargeBigDecimalBellowLimit() {
        String sourceValue = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706"
                + "7982148086513282306647093844609550582231725359408128481117450284102701938521105559644622948954930381"
                + "9644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412"
                + "7372458700660631558817488152092096282925409171536436789259036001133053054882046652138414695194151160"
                + "9433057270365759591953092186117381932611793105118548074462379962749567351885752724891227938183011949";
        JsonReader reader = Json.createReader(new StringReader(sourceValue));
        JsonNumber check = Json.createValue(new BigDecimal(sourceValue));
        JsonValue value = reader.readValue();
        assertEquals(value.getValueType(), JsonValue.ValueType.NUMBER);
        assertEquals(value, check);
    }

    // Test BigDecimal max source characters array length using length above system property limit of 500.
    // Parsing shall pass and return value equal to source String.
    public void testLargeBigDecimalAboveLimit() {
        String sourceValue = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706"
                + "7982148086513282306647093844609550582231725359408128481117450284102701938521105559644622948954930381"
                + "9644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412"
                + "7372458700660631558817488152092096282925409171536436789259036001133053054882046652138414695194151160"
                + "9433057270365759591953092186117381932611793105118548074462379962749567351885752724891227938183011949"
                + "1";
        JsonReader reader = Json.createReader(new StringReader(sourceValue));
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
