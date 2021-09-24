/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.io.StringReader;
import java.util.Map;

import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import org.eclipse.parsson.api.BufferPool;

/**
 * A utility class
 * 
 * @since 1.1
 */
public final class JsonUtil {

    private static BufferPool internalPool;

    private JsonUtil() {
    }

    static BufferPool getInternalBufferPool() {
        if (internalPool == null) {
            internalPool = new BufferPoolImpl();
        }
        return internalPool;
    }

    /**
     * Reads the input JSON text and returns a JsonValue.
     * <p>For convenience, single quotes as well as double quotes
     * are allowed to delimit JSON strings. If single quotes are
     * used, any quotes, single or double, in the JSON string must be
     * escaped (prepend with a '\').
     *
     * @param jsonString the input JSON data
     * @return the object model for {@code jsonString}
     * @throws JsonParsingException if the input is not legal JSON text
     */
    public static JsonValue toJson(String jsonString) {
        StringBuilder builder = new StringBuilder();
        boolean single_context = false;
        for (int i = 0; i < jsonString.length(); i++) {
            char ch = jsonString.charAt(i);
            if (ch == '\\') {
                i = i + 1;
                if (i < jsonString.length()) {
                    ch = jsonString.charAt(i);
                    if (!(single_context && ch == '\'')) {
                        // unescape ' inside single quotes
                        builder.append('\\');
                    }
                }
            } else if (ch == '\'') {
                // Turn ' into ", for proper JSON string
                ch = '"';
                single_context = ! single_context;
            }
            builder.append(ch);
        }
                   
        JsonReader reader = new JsonReaderImpl(
                                new StringReader(builder.toString()),
                                getInternalBufferPool());
        JsonValue value = reader.readValue();
        reader.close();
        return value;
    }

    static boolean getConfigValue(String key, boolean defaultValue, Map<String, ?> config) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return defaultValue;
    }

    static enum NaNInfinite {
        NAN("\"NaN\""), POSITIVE_INFINITY("\"+Infinity\""), NEGATIVE_INFINITY("\"-Infinity\"");
        
        private final String strVal;

        private NaNInfinite(String strVal) {
            this.strVal = strVal;
        }

        String processValue(boolean writeNanAsNulls, boolean writeNanAsStrings, double value) {
            if (writeNanAsNulls) {
                return null;
            } else if (writeNanAsStrings) {
                return strVal;
            } else {
                throw new NumberFormatException(JsonMessages.GENERATOR_DOUBLE_INFINITE_NAN());
            }
        }
        
        static NaNInfinite get(double value) {
            if (Double.isNaN(value)) {
                return NAN;
            } else if (Double.NEGATIVE_INFINITY == value) {
                return NEGATIVE_INFINITY;
            } else if (Double.POSITIVE_INFINITY == value) {
                return POSITIVE_INFINITY;
            } else {
                return null;
            }
        }

        static NaNInfinite get(String value) {
            for (NaNInfinite item : NaNInfinite.values()) {
                if (item.strVal.equals(value)) {
                    return item;
                }
            }
            return null;
        }
    }
}

