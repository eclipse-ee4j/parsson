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

import org.eclipse.parsson.api.BufferPool;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 * Util for transforming a Map to a Json objects.
 *
 * @author asotobu
 */
public final class MapUtil {

    // Configuration property to limit maximum value of BigInteger scale value.
    private final int bigIntegerScaleLimit;

    MapUtil(int bigIntegerScaleLimit) {
        super();
        this.bigIntegerScaleLimit = bigIntegerScaleLimit;
    }

    JsonValue handle(Object value, BufferPool bufferPool) {
        if (value == null) {
            return JsonValue.NULL;
        } else if (value instanceof JsonValue) {
            return (JsonValue) value;
        } else if (value instanceof JsonArrayBuilder) {
            return ((JsonArrayBuilder) value).build();
        } else if (value instanceof JsonObjectBuilder) {
            return ((JsonObjectBuilder) value).build();
        } else if (value instanceof BigDecimal) {
            return JsonNumberImpl.getJsonNumber((BigDecimal) value, bigIntegerScaleLimit);
        } else if (value instanceof BigInteger) {
            return JsonNumberImpl.getJsonNumber((BigInteger) value, bigIntegerScaleLimit);
        } else if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            return b ? JsonValue.TRUE : JsonValue.FALSE;
        } else if (value instanceof Double) {
            return JsonNumberImpl.getJsonNumber((Double) value, bigIntegerScaleLimit);
        } else if (value instanceof Integer) {
            return JsonNumberImpl.getJsonNumber((Integer) value, bigIntegerScaleLimit);
        } else if (value instanceof Long) {
            return JsonNumberImpl.getJsonNumber((Long) value, bigIntegerScaleLimit);
        } else if (value instanceof String) {
            return new JsonStringImpl((String) value);
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<?> collection = (Collection<?>) value;
            JsonArrayBuilder jsonArrayBuilder = new JsonArrayBuilderImpl(collection, bufferPool, bigIntegerScaleLimit);
            return jsonArrayBuilder.build();
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            JsonObjectBuilder object = new JsonObjectBuilderImpl((Map<String, Object>) value, bufferPool, bigIntegerScaleLimit);
            return object.build();
        }

        throw new IllegalArgumentException(String.format("Type %s is not supported.", value.getClass()));
    }

}
