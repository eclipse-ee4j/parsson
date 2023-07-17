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

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.json.Json;
import junit.framework.TestCase;
import org.eclipse.parsson.api.JsonConfig;

/**
 * Test maxBigIntegerScale limit set from System property.
 */
public class JsonBigDecimalScaleLimitTest extends TestCase {

    public JsonBigDecimalScaleLimitTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() {
        System.setProperty(JsonConfig.MAX_BIGINTEGER_SCALE, "50000");
    }

    @Override
    protected void tearDown() {
        System.clearProperty(JsonConfig.MAX_BIGINTEGER_SCALE);
    }

    // Test BigInteger scale value limit set from system property using value bellow limit.
    // Call shall return value.
    public void testSystemPropertyBigIntegerScaleBellowLimit() {
        BigDecimal value = new BigDecimal("3.1415926535897932384626433");
        Json.createValue(value).bigIntegerValue();
    }

    // Test BigInteger scale value limit set from system property using value above limit.
    // Call shall throw specific UnsupportedOperationException exception.
    // Default value is 100000 and system property lowered it to 50000 so value with scale 50001
    // test shall fail with exception message matching modified limits.
    public void testSystemPropertyBigIntegerScaleAboveLimit() {
        BigDecimal value = new BigDecimal("3.1415926535897932384626433")
                .setScale(50001, RoundingMode.HALF_UP);
        try {
            Json.createValue(value).bigIntegerValue();
            fail("No exception was thrown from bigIntegerValue with scale over limit");
        } catch (UnsupportedOperationException e) {
            // UnsupportedOperationException is expected to be thrown
            assertEquals(
                    "Scale value 50001 of this BigInteger exceeded maximal allowed absolute value of 50000",
                    e.getMessage());
        }
        System.clearProperty("org.eclipse.parsson.maxBigIntegerScale");
    }

    // Test BigInteger scale value limit set from system property using value above limit.
    // Call shall throw specific UnsupportedOperationException exception.
    // Default value is 100000 and system property lowered it to 50000 so value with scale -50001
    // test shall fail with exception message matching modified limits.
    public void testSystemPropertyBigIntegerNegScaleAboveLimit() {
        BigDecimal value = new BigDecimal("3.1415926535897932384626433")
                .setScale(-50001, RoundingMode.HALF_UP);
        try {
            Json.createValue(value).bigIntegerValue();
            fail("No exception was thrown from bigIntegerValue with scale over limit");
        } catch (UnsupportedOperationException e) {
            // UnsupportedOperationException is expected to be thrown
            assertEquals(
                    "Scale value -50001 of this BigInteger exceeded maximal allowed absolute value of 50000",
                    e.getMessage());
        }
        System.clearProperty("org.eclipse.parsson.maxBigIntegerScale");
    }

}
