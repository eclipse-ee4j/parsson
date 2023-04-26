/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import junit.framework.TestCase;
import org.eclipse.parsson.api.JsonConfig;

/**
 * @author Jitendra Kotamraju
 */
public class JsonNumberTest extends TestCase {

    // π as JsonNumber with 500 source characters
    static final String Π_500
            = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706"
            + "7982148086513282306647093844609550582231725359408128481117450284102701938521105559644622948954930381"
            + "9644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412"
            + "7372458700660631558817488152092096282925409171536436789259036001133053054882046652138414695194151160"
            + "9433057270365759591953092186117381932611793105118548074462379962749567351885752724891227938183011949";

    // π as JsonNumber with 501 source characters
    static final String Π_501 = Π_500 + "1";

    // π as JsonNumber with 1100 source characters
    private static final String Π_1100 = Π_500
            + "1298336733624406566430860213949463952247371907021798609437027705392171762931767523846748184676694051"
            + "3200056812714526356082778577134275778960917363717872146844090122495343014654958537105079227968925892"
            + "3542019956112129021960864034418159813629774771309960518707211349999998372978049951059731732816096318"
            + "5950244594553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473"
            + "0359825349042875546873115956286388235378759375195778185778053217122680661300192787661119590921642019"
            + "8938095257201065485863278865936153381827968230301952035301852968995773622599413891249721775283479131";

    // π as JsonNumber with 1100 source characters
    private static final String Π_1101 = Π_1100 + "5";

    public JsonNumberTest(String testName) {
        super(testName);
    }

    public void testFloating() throws Exception {
        JsonArray array1 = Json.createArrayBuilder().add(10.4).build();
        JsonReader reader = Json.createReader(new StringReader("[10.4]"));
        JsonArray array2 = reader.readArray();

        assertEquals(array1.get(0), array2.get(0));
        assertEquals(array1, array2);
    }

    public void testBigDecimal() throws Exception {
        JsonArray array1 = Json.createArrayBuilder().add(new BigDecimal("10.4")).build();
        JsonReader reader = Json.createReader(new StringReader("[10.4]"));
        JsonArray array2 = reader.readArray();

        assertEquals(array1.get(0), array2.get(0));
        assertEquals(array1, array2);
    }

    public void testIntNumberType() throws Exception {
        JsonArray array1 = Json.createArrayBuilder()
                .add(Integer.MIN_VALUE)
                .add(Integer.MAX_VALUE)
                .add(Integer.MIN_VALUE + 1)
                .add(Integer.MAX_VALUE - 1)
                .add(12)
                .add(12l)
                .add(new BigInteger("0"))
                .build();
        testNumberType(array1, true);

        StringReader sr = new StringReader("[" +
                "-2147483648, " +
                "2147483647, " +
                "-2147483647, " +
                "2147483646, " +
                "12, " +
                "12, " +
                "0 " +
                "]");
        JsonReader reader = Json.createReader(sr);
        JsonArray array2 = reader.readArray();
        reader.close();
        testNumberType(array2, true);

        assertEquals(array1, array2);
    }

    private void testNumberType(JsonArray array, boolean integral) {
        for (JsonValue value : array) {
            assertEquals(integral, ((JsonNumber) value).isIntegral());
        }
    }

    public void testLongNumberType() throws Exception {
        JsonArray array1 = Json.createArrayBuilder()
                .add(Long.MIN_VALUE)
                .add(Long.MAX_VALUE)
                .add(Long.MIN_VALUE + 1)
                .add(Long.MAX_VALUE - 1)
                .add((long) Integer.MIN_VALUE - 1)
                .add((long) Integer.MAX_VALUE + 1)
                .build();
        testNumberType(array1, true);

        StringReader sr = new StringReader("[" +
                "-9223372036854775808, " +
                "9223372036854775807, " +
                "-9223372036854775807, " +
                "9223372036854775806, " +
                "-2147483649, " +
                "2147483648 " +
                "]");
        JsonReader reader = Json.createReader(sr);
        JsonArray array2 = reader.readArray();
        reader.close();
        testNumberType(array2, true);

        assertEquals(array1, array2);
    }


//    public void testBigIntegerNumberType() throws Exception {
//        JsonArray array1 = new JsonBuilder()
//            .startArray()
//                .add(new BigInteger("-9223372036854775809"))
//                .add(new BigInteger("9223372036854775808"))
//                .add(new BigInteger("012345678901234567890"))
//            .end()
//        .build();
//        testNumberType(array1, JsonNumber.NumberType.BIG_INTEGER);
//
//        StringReader sr = new StringReader("[" +
//            "-9223372036854775809, " +
//            "9223372036854775808, " +
//            "12345678901234567890 " +
//        "]");
//        JsonReader reader = new JsonReader(sr);
//        JsonArray array2 = reader.readArray();
//        reader.close();
//        testNumberType(array2, JsonNumber.NumberType.BIG_INTEGER);
//
//        assertEquals(array1, array2);
//    }

    public void testBigDecimalNumberType() throws Exception {
        JsonArray array1 = Json.createArrayBuilder()
                .add(12d)
                .add(12.0d)
                .add(12.1d)
                .add(Double.MIN_VALUE)
                .add(Double.MAX_VALUE)
                .build();
        testNumberType(array1, false);

        StringReader sr = new StringReader("[" +
                "12.0, " +
                "12.0, " +
                "12.1, " +
                "4.9E-324, " +
                "1.7976931348623157E+308 " +
                "]");
        JsonReader reader = Json.createReader(sr);
        JsonArray array2 = reader.readArray();
        reader.close();
        testNumberType(array2, false);

        assertEquals(array1, array2);
    }

    public void testMinMax() throws Exception {
        JsonArray expected = Json.createArrayBuilder()
                .add(Integer.MIN_VALUE)
                .add(Integer.MAX_VALUE)
                .add(Long.MIN_VALUE)
                .add(Long.MAX_VALUE)
                .add(Double.MIN_VALUE)
                .add(Double.MAX_VALUE)
                .build();

        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.writeArray(expected);
        writer.close();

        JsonReader reader = Json.createReader(new StringReader(sw.toString()));
        JsonArray actual = reader.readArray();
        reader.close();

        assertEquals(expected, actual);
    }

    public void testLeadingZeroes() {
        JsonArray array = Json.createArrayBuilder()
                .add(0012.1d)
                .build();

        StringWriter sw = new StringWriter();
        JsonWriter jw = Json.createWriter(sw);
        jw.write(array);
        jw.close();

        assertEquals("[12.1]", sw.toString());
    }

    public void testBigIntegerExact() {
        try {
            JsonArray array = Json.createArrayBuilder().add(12345.12345).build();
            array.getJsonNumber(0).bigIntegerValueExact();
            fail("Expected Arithmetic exception");
        } catch (ArithmeticException expected) {
            // no-op
        }
    }

    public void testHashCode() {
        JsonNumber jsonNumber1 = Json.createValue(1);
        assertTrue(jsonNumber1.hashCode() == jsonNumber1.bigDecimalValue().hashCode());

        JsonNumber jsonNumber2 = Json.createValue(1);

        assertTrue(jsonNumber1.equals(jsonNumber2));
        assertTrue(jsonNumber1.hashCode() == jsonNumber2.hashCode());
    }

    public void testNumber() {
        assertEquals(Json.createValue(1), Json.createValue(Byte.valueOf((byte) 1)));
        assertEquals(Json.createValue(1).toString(), Json.createValue(Byte.valueOf((byte) 1)).toString());
        assertEquals(Json.createValue(1), Json.createValue(Short.valueOf((short) 1)));
        assertEquals(Json.createValue(1).toString(), Json.createValue(Short.valueOf((short) 1)).toString());
        assertEquals(Json.createValue(1), Json.createValue(Integer.valueOf(1)));
        assertEquals(Json.createValue(1).toString(), Json.createValue(Integer.valueOf(1)).toString());
        assertEquals(Json.createValue(1L), Json.createValue(Long.valueOf(1)));
        assertEquals(Json.createValue(1L).toString(), Json.createValue(Long.valueOf(1)).toString());
        assertEquals(Json.createValue(1D), Json.createValue(Float.valueOf(1)));
        assertEquals(Json.createValue(1D).toString(), Json.createValue(Float.valueOf(1)).toString());
        assertEquals(Json.createValue(1D), Json.createValue(Double.valueOf(1)));
        assertEquals(Json.createValue(1D).toString(), Json.createValue(Double.valueOf(1)).toString());
        assertEquals(Json.createValue(1), Json.createValue(new CustomNumber(1)));
        assertEquals(Json.createValue(1).toString(), Json.createValue(new CustomNumber(1)).toString());
    }

    // Test default BigInteger scale value limit using value bellow limit.
    // Call shall return value.
    public void testDefaultBigIntegerScaleBellowLimit() {
        BigDecimal value = new BigDecimal("3.1415926535897932384626433");
        Json.createValue(value).bigIntegerValue();
    }

    // Test default BigInteger scale value limit using value above limit.
    // Call shall throw specific UnsupportedOperationException exception.
    public void testDefaultBigIntegerScaleAboveLimit() {
        BigDecimal value = new BigDecimal("3.1415926535897932384626433")
                .setScale(100001, RoundingMode.HALF_UP);
        try {
            Json.createValue(value).bigIntegerValue();
            fail("No exception was thrown from bigIntegerValue with scale over limit");
        } catch (UnsupportedOperationException e) {
            // UnsupportedOperationException is expected to be thrown
            assertEquals(
                    "Scale value 100001 of this BigInteger exceeded maximal allowed value of 100000",
                    e.getMessage());
        }
    }

    // Test BigInteger scale value limit set from config Map using value above limit.
    // Call shall throw specific UnsupportedOperationException exception.
    // Config Map limit is stored in target JsonObject and shall be present for later value manipulation.
    // Default value is 100000 and config Map property lowered it to 50000 so value with scale 50001
    // test shall fail with exception message matching modified limits.
    public void testConfigBigIntegerScaleAboveLimit() {
        BigDecimal value = new BigDecimal("3.1415926535897932384626433")
                .setScale(50001, RoundingMode.HALF_UP);
        Map<String, ?> config = Map.of(JsonConfig.MAX_BIGINTEGER_SCALE, "50000");
        try {
            JsonObject jsonObject = Json.createBuilderFactory(config)
                    .createObjectBuilder()
                    .add("bigDecimal", value)
                    .build();
            jsonObject.getJsonNumber("bigDecimal").bigIntegerValue();
            fail("No exception was thrown from bigIntegerValue with scale over limit");
        } catch (UnsupportedOperationException e) {
            // UnsupportedOperationException is expected to be thrown
            assertEquals(
                    "Scale value 50001 of this BigInteger exceeded maximal allowed value of 50000",
                    e.getMessage());
        }
    }

    // Test BigDecimal max source characters array length using length equal to default limit of 1100.
    // Parsing shall pass and return value equal to source String.
    public void testLargeBigDecimalBellowLimit() {
        JsonReader reader = Json.createReader(new StringReader(Π_1100));
        JsonNumber check = Json.createValue(new BigDecimal(Π_1100));
        JsonValue value = reader.readValue();
        assertEquals(value.getValueType(), JsonValue.ValueType.NUMBER);
        assertEquals(value, check);
    }

    // Test BigDecimal max source characters array length using length above default limit of 1100.
    // Parsing shall throw specific UnsupportedOperationException exception.
    public void testLargeBigDecimalAboveLimit() {
        JsonReader reader = Json.createReader(new StringReader(Π_1101));
        try {
            reader.readValue();
            fail("No exception was thrown from BigDecimal parsing with source characters array length over limit");
        } catch (UnsupportedOperationException e) {
            // UnsupportedOperationException is expected to be thrown
            assertEquals(
                    "Number of BigDecimal source characters 1101 exceeded maximal allowed value of 1100",
                    e.getMessage());
        }
    }

    // Test BigDecimal max source characters array length using length equal to custom limit of 500.
    // Parsing shall pass and return value equal to source String.
    public void testLargeBigDecimalBellowCustomLimit() {
        Map<String, ?> config = Map.of(JsonConfig.MAX_BIGDECIMAL_LEN, "500");
        JsonReader reader = Json.createReaderFactory(config).createReader(new StringReader(Π_500));
        JsonNumber check = Json.createValue(new BigDecimal(Π_500));
        JsonValue value = reader.readValue();
        assertEquals(value.getValueType(), JsonValue.ValueType.NUMBER);
        assertEquals(value, check);
    }

    // Test BigDecimal max source characters array length using length equal to custom limit of 200.
    // Parsing shall pass and return value equal to source String.
    public void testLargeBigDecimalAboveCustomLimit() {
        Map<String, ?> config = Map.of(JsonConfig.MAX_BIGDECIMAL_LEN, "500");
        JsonReader reader = Json.createReaderFactory(config).createReader(new StringReader(Π_501));
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

    private static class CustomNumber extends Number {

        private static final long serialVersionUID = 1L;
        private final int num;

        private CustomNumber(int num) {
            this.num = num;
        }

        @Override
        public int intValue() {
            return num;
        }

        @Override
        public long longValue() {
            return num;
        }

        @Override
        public float floatValue() {
            return num;
        }

        @Override
        public double doubleValue() {
            return num;
        }

        @Override
        public String toString() {
            return Integer.toString(num);
        }
        
    }
}
