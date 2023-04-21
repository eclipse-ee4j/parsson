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

package org.eclipse.parsson;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.parsson.api.BufferPool;
import org.eclipse.parsson.api.JsonConfig;

/**
 * Parsson configuration.
 * Values are composed from properties {@code Map}, system properties and default value.
 */
final class JsonContext {

    /** Default maximum value of BigInteger scale value limit. */
    private static final int DEFAULT_MAX_BIGINT_SCALE = 100000;

    /**
     * Custom char[] pool instance property. Can be set in properties {@code Map} only.
     */
    static final String PROPERTY_BUFFER_POOL = BufferPool.class.getName();

    private final Map<String, ?> config;

    // Maximum value of BigInteger scale value
    private final int bigIntegerScaleLimit;

    // Whether JSON pretty printing is enabled
    private final boolean prettyPrinting;

    // Whether duplicate keys in JsonObject shall be rejected.
    private final boolean rejectDuplicateKeys;

    private final BufferPool bufferPool;

    /**
     * Creates an instance of Parsson configuration.
     *
     * @param config a {@code Map} of provider specific properties to configure the JSON parsers
     * @param defaultPool default char[] pool to use when no instance is configured
     */
    JsonContext(Map<String, ?> config, BufferPool defaultPool) {
        this.bigIntegerScaleLimit = getIntConfig(JsonConfig.MAX_BIGINT_SCALE, config, DEFAULT_MAX_BIGINT_SCALE);
        this.prettyPrinting = getBooleanConfig(JsonGenerator.PRETTY_PRINTING, config);
        this.rejectDuplicateKeys = getBooleanConfig(JsonConfig.REJECT_DUPLICATE_KEYS, config);
        this.bufferPool = getBufferPool(config, defaultPool);
        this.config = config != null ? Collections.unmodifiableMap(config) : null;
    }

    /**
     * Creates an instance of Parsson configuration.
     *
     * @param config a map of provider specific properties to configure the JSON parsers
     * @param defaultPool default char[] pool to use when no instance is configured
     * @param properties properties to store in local copy of provider specific properties {@code Map}
     */
    JsonContext(Map<String, ?> config, BufferPool defaultPool, String... properties) {
        this.bigIntegerScaleLimit = getIntConfig(JsonConfig.MAX_BIGINT_SCALE, config, DEFAULT_MAX_BIGINT_SCALE);
        this.prettyPrinting = getBooleanConfig(JsonGenerator.PRETTY_PRINTING, config);
        this.rejectDuplicateKeys = getBooleanConfig(JsonConfig.REJECT_DUPLICATE_KEYS, config);
        this.bufferPool = getBufferPool(config, defaultPool);
        this.config = config != null
                ? Collections.unmodifiableMap(copyPropertiesMap(this, config, properties)) : null;
    }

    Map<String, ?> config() {
        return config;
    }

    Object config(String propertyName) {
        return config != null ? config.get(propertyName) : null;
    }

    int bigIntegerScaleLimit() {
        return bigIntegerScaleLimit;
    }

    boolean prettyPrinting() {
        return prettyPrinting;
    }

    boolean rejectDuplicateKeys() {
        return rejectDuplicateKeys;
    }

    BufferPool bufferPool() {
        return bufferPool;
    }

    private static BufferPool getBufferPool(Map<String, ?> config, BufferPool defaultrPool) {
        BufferPool pool = config != null ? (BufferPool)config.get(PROPERTY_BUFFER_POOL) : null;
        return pool != null ? pool : defaultrPool;
    }

    private static int getIntConfig(String propertyName, Map<String, ?> config, int defaultValue) throws JsonException {
        // Try config Map first
        Integer intConfig = config != null ? getIntProperty(propertyName, config) : null;
        if (intConfig != null) {
            return intConfig;
        }
        // Try system properties as fallback.
        intConfig = getIntSystemProperty(propertyName);
        return intConfig != null ? intConfig : defaultValue;
    }

    private static boolean getBooleanConfig(String propertyName, Map<String, ?> config) throws JsonException {
        // Try config Map first
        Boolean booleanConfig = config != null ? getBooleanProperty(propertyName, config) : null;
        if (booleanConfig != null) {
            return booleanConfig;
        }
        // Try system properties as fallback.
        return getBooleanSystemProperty(propertyName);
    }

    private static Integer getIntProperty(String propertyName, Map<String, ?> config) throws JsonException {
        Object property = config.get(propertyName);
        if (property == null) {
            return null;
        }
        if (property instanceof Number) {
            return ((Number) property).intValue();
        }
        if (property instanceof String) {
            return propertyStringToInt(propertyName, (String) property);
        }
        throw new JsonException(
                String.format("Could not convert %s property of type %s to Integer",
                              propertyName, property.getClass().getName()));
    }

    // Returns true when property exists or null otherwise. Property value is ignored.
    private static Boolean getBooleanProperty(String propertyName, Map<String, ?> config) throws JsonException {
        return config.containsKey(propertyName) ? true : null;
    }


    private static Integer getIntSystemProperty(String propertyName) throws JsonException {
        String systemProperty = getSystemProperty(propertyName);
        if (systemProperty == null) {
            return null;
        }
        return propertyStringToInt(propertyName, systemProperty);
    }

    // Returns true when property exists or false otherwise. Property value is ignored.
    private static boolean getBooleanSystemProperty(String propertyName) throws JsonException {
        return getSystemProperty(propertyName) != null;
    }

    @SuppressWarnings("removal")
    private static String getSystemProperty(String propertyName) throws JsonException {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(
                    (PrivilegedAction<String>) () -> System.getProperty(propertyName));
        } else {
            return System.getProperty(propertyName);
        }
    }

    private static int propertyStringToInt(String propertyName, String propertyValue) throws JsonException {
        try {
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException ex) {
            throw new JsonException(
                    String.format("Value of %s property is not a number", propertyName), ex);
        }
    }

    // Constructor helper: Copy provider specific properties Map. Only specified properties are added.
    // Instance prettyPrinting and rejectDuplicateKeys variables must be initialized before
    // this method is called.
    private static Map<String, ?> copyPropertiesMap(JsonContext instance, Map<String, ?> config, String... properties) {
        Objects.requireNonNull(config, "Map of provider specific properties is null");
        if (properties == null || properties.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> newConfig = new HashMap<>(properties.length);
        for (String propertyName : properties) {
            switch (propertyName) {
                // Some properties need special handling.
                case JsonGenerator.PRETTY_PRINTING:
                    if (instance.prettyPrinting) {
                        newConfig.put(JsonGenerator.PRETTY_PRINTING, true);
                    }
                    break;
                case JsonConfig.REJECT_DUPLICATE_KEYS:
                    if (instance.rejectDuplicateKeys) {
                        newConfig.put(JsonConfig.REJECT_DUPLICATE_KEYS, true);
                    }
                // Rest of properties are copied without changes
                default:
                    if (config.containsKey(propertyName)) {
                        newConfig.put(propertyName, config.get(propertyName));
                    }
            }
        }
        return newConfig;
    }

}
