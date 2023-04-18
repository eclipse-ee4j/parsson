/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.parsson;

import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JsonNumber impl. Subclasses provide optimized implementations
 * when backed by int, long, BigDecimal
 *
 * @author Jitendra Kotamraju
 */
abstract class JsonNumberImpl implements JsonNumber {

    /**
     * Configuration system property to limit maximum value of BigInteger scale value.
     * This property limits maximum value of scale value to be allowed
     * in {@link jakarta.json.JsonNumber#bigIntegerValue()}
     * and {@link jakarta.json.JsonNumber#bigIntegerValueExact()} implemented methods.
     * Default value is set to {@code 100000} and higher values may be a security risc
     * allowing dDoS attacks.
     */
    static String PROPERTY_MAX_BIGINT_SCALE = "org.eclipse.parsson.maxBigIntegerScale";

    /** Default maximum value of BigInteger scale value limit. */
    static final int DEFAULT_MAX_BIGINT_SCALE = 100000;

    private int hashCode;

    // Configuration property to limit maximum value of BigInteger scale value.
    private final int bigIntegerScaleLimit;

    private JsonNumberImpl(int bigIntegerScaleLimit) {
        this.bigIntegerScaleLimit = bigIntegerScaleLimit;
    }

    static JsonNumber getJsonNumber(int num, int bigIntegerScaleLimit) {
        return new JsonIntNumber(num, bigIntegerScaleLimit);
    }

    static JsonNumber getJsonNumber(long num, int bigIntegerScaleLimit) {
        return new JsonLongNumber(num, bigIntegerScaleLimit);
    }

    static JsonNumber getJsonNumber(BigInteger value, int bigIntegerScaleLimit) {
        if (value == null) {
            throw new NullPointerException("Value is null");
        }
        return new JsonBigDecimalNumber(new BigDecimal(value), bigIntegerScaleLimit);
    }

    static JsonNumber getJsonNumber(double value, int bigIntegerScaleLimit) {
        //bigDecimal = new BigDecimal(value);
        // This is the preferred way to convert double to BigDecimal
        return new JsonBigDecimalNumber(BigDecimal.valueOf(value), bigIntegerScaleLimit);
    }

    static JsonNumber getJsonNumber(BigDecimal value, int bigIntegerScaleLimit) {
        if (value == null) {
            throw new NullPointerException("Value is null");
        }
        return new JsonBigDecimalNumber(value, bigIntegerScaleLimit);
    }

    static JsonNumber getJsonNumber(Number value, int bigIntegerScaleLimit) {
        if (value == null) {
            throw new NullPointerException("Value is null");
        }
        if (value instanceof Integer) {
            return getJsonNumber(value.intValue(), bigIntegerScaleLimit);
        } else if (value instanceof Long) {
            return getJsonNumber(value.longValue(), bigIntegerScaleLimit);
        } else if (value instanceof Double) {
            return getJsonNumber(value.doubleValue(), bigIntegerScaleLimit);
        } else if (value instanceof BigInteger) {
            return getJsonNumber((BigInteger) value, bigIntegerScaleLimit);
        } else if (value instanceof BigDecimal) {
            return getJsonNumber((BigDecimal) value, bigIntegerScaleLimit);
        } else {
            return new JsonNumberNumber(value, bigIntegerScaleLimit);
        }
    }

    private static final class JsonNumberNumber extends JsonNumberImpl {

        private final Number num;
        private BigDecimal bigDecimal;

        JsonNumberNumber(Number num, int bigIntegerScaleLimit) {
            super(bigIntegerScaleLimit);
            this.num = num;
        }

        @Override
        public Number numberValue() {
            return num;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            BigDecimal bd = bigDecimal;
            if (bd == null) {
                bigDecimal = bd = new BigDecimal(num.toString());
            }
            return bd;
        }

    }

    // Optimized JsonNumber impl for int numbers.
    private static final class JsonIntNumber extends JsonNumberImpl {
        private final int num;
        private BigDecimal bigDecimal;  // assigning it lazily on demand

        JsonIntNumber(int num, int bigIntegerScaleLimit) {
            super(bigIntegerScaleLimit);
            this.num = num;
        }

        @Override
        public boolean isIntegral() {
            return true;
        }

        @Override
        public int intValue() {
            return num;
        }

        @Override
        public int intValueExact() {
            return num;
        }

        @Override
        public long longValue() {
            return num;
        }

        @Override
        public long longValueExact() {
            return num;
        }

        @Override
        public double doubleValue() {
            return num;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            // reference assignments are atomic. At the most some more temp
            // BigDecimal objects are created
            BigDecimal bd = bigDecimal;
            if (bd == null) {
                bigDecimal = bd = new BigDecimal(num);
            }
            return bd;
        }

        @Override
        public Number numberValue() {
            return num;
        }

        @Override
        public String toString() {
            return Integer.toString(num);
        }
    }

    // Optimized JsonNumber impl for long numbers.
    private static final class JsonLongNumber extends JsonNumberImpl {
        private final long num;
        private BigDecimal bigDecimal;  // assigning it lazily on demand

        JsonLongNumber(long num, int bigIntegerScaleLimit) {
            super(bigIntegerScaleLimit);
            this.num = num;
        }

        @Override
        public boolean isIntegral() {
            return true;
        }

        @Override
        public int intValue() {
            return (int) num;
        }

        @Override
        public int intValueExact() {
            return Math.toIntExact(num);
        }

        @Override
        public long longValue() {
            return num;
        }

        @Override
        public long longValueExact() {
            return num;
        }

        @Override
        public double doubleValue() {
            return num;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            // reference assignments are atomic. At the most some more temp
            // BigDecimal objects are created
            BigDecimal bd = bigDecimal;
            if (bd == null) {
                bigDecimal = bd = new BigDecimal(num);
            }
            return bd;
        }

        @Override
        public Number numberValue() {
            return num;
        }

        @Override
        public String toString() {
            return Long.toString(num);
        }

    }

    // JsonNumber impl using BigDecimal numbers.
    private static final class JsonBigDecimalNumber extends JsonNumberImpl {
        private final BigDecimal bigDecimal;

        JsonBigDecimalNumber(BigDecimal value, int bigIntegerScaleLimit) {
            super(bigIntegerScaleLimit);
            this.bigDecimal = value;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            return bigDecimal;
        }

        @Override
        public Number numberValue() {
            return bigDecimalValue();
        }

    }

    @Override
    public boolean isIntegral() {
        return bigDecimalValue().scale() == 0;
    }

    @Override
    public int intValue() {
        return bigDecimalValue().intValue();
    }

    @Override
    public int intValueExact() {
        return bigDecimalValue().intValueExact();
    }

    @Override
    public long longValue() {
        return bigDecimalValue().longValue();
    }

    @Override
    public long longValueExact() {
        return bigDecimalValue().longValueExact();
    }

    @Override
    public double doubleValue() {
        return bigDecimalValue().doubleValue();
    }

    @Override
    public BigInteger bigIntegerValue() {
        BigDecimal bd = bigDecimalValue();
        if (bd.scale() <= bigIntegerScaleLimit) {
            return bd.toBigInteger();
        }
        throw new UnsupportedOperationException(
                String.format("Scale of this BigInteger exceeded maximal allowed value of %d", bigIntegerScaleLimit));
    }

    @Override
    public BigInteger bigIntegerValueExact() {
        BigDecimal bd = bigDecimalValue();
        if (bd.scale() <= bigIntegerScaleLimit) {
            return bd.toBigIntegerExact();
        }
        throw new UnsupportedOperationException(
                String.format("Scale of this BigInteger exceeded maximal allowed value of %d", bigIntegerScaleLimit));
    }

    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = bigDecimalValue().hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (!(obj instanceof JsonNumber)) {
            return false;
        }
        JsonNumber other = (JsonNumber)obj;
        return bigDecimalValue().equals(other.bigDecimalValue());
    }

    @Override
    public String toString() {
        return bigDecimalValue().toString();
    }

}

