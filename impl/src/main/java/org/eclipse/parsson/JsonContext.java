/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Map;

import jakarta.json.JsonException;
import org.eclipse.parsson.api.BufferPool;

/**
 * Parsson configuration.
 * Values are composed from properties {@code Map}, system properties and default value.
 */
class JsonContext {

    /**
     * Configuration system property to limit maximum value of BigInteger scale value.
     * This property limits maximum value of scale value to be allowed
     * in {@link jakarta.json.JsonNumber#bigIntegerValue()}
     * and {@link jakarta.json.JsonNumber#bigIntegerValueExact()} implemented methods.
     * Default value is set to {@code 100000} and higher values may be a security risc
     * allowing dDoS attacks.
     */
    private static final String PROPERTY_MAX_BIGINT_SCALE = "org.eclipse.parsson.maxBigIntegerScale";

    /** Default maximum value of BigInteger scale value limit. */
    private static final int DEFAULT_MAX_BIGINT_SCALE = 100000;

    static final String PROPERTY_BUFFER_POOL = BufferPool.class.getName();

    private final Map<String, ?> config;

    // Maximum value of BigInteger scale value
    private final int bigIntegerScaleLimit;

    private final BufferPool bufferPool;

    JsonContext(Map<String, ?> config, BufferPool defaultPool) {
        this.config = config != null ? Collections.unmodifiableMap(config) : null;
        this.bigIntegerScaleLimit = getIntConfig(PROPERTY_MAX_BIGINT_SCALE, config, DEFAULT_MAX_BIGINT_SCALE);
        this.bufferPool = getBufferPool(config, defaultPool);
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
        intConfig = getIntSystemProperty(propertyName);
        return intConfig != null ? intConfig : defaultValue;
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

    private static Integer getIntSystemProperty(String propertyName) throws JsonException {
        String systemProperty = getSystemProperty(propertyName);
        if (systemProperty == null) {
            return null;
        }
        return propertyStringToInt(propertyName, systemProperty);
    }

    @SuppressWarnings("removal")
    private static String getSystemProperty(String propertyName) throws JsonException {
        return AccessController.doPrivileged(
                (PrivilegedAction<String>) () -> System.getProperty(propertyName));
    }

    private static String propertyAction(String propertyName) {
        return System.getProperty(propertyName);
    }

    private static int propertyStringToInt(String propertyName, String propertyValue) throws JsonException {
        try {
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException ex) {
            throw new JsonException(
                    String.format("Value of %s property is not a number", propertyName), ex);
        }
    }

}
