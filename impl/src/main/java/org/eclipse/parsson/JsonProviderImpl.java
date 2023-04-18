/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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
import org.eclipse.parsson.api.JsonConfig;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;
import jakarta.json.spi.JsonProvider;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Jitendra Kotamraju
 * @author Kin-man Chung
 * @author Alex Soto
 */
public class JsonProviderImpl extends JsonProvider {
    private final BufferPool bufferPool = new BufferPoolImpl();

    // Configuration property to limit maximum value of BigInteger scale value.
    private final int bigIntegerScaleLimit;

    /**
     * Creates an instance of Parsson implementation of {@link JsonProvider} service provider interface.
     */
    public JsonProviderImpl() {
        bigIntegerScaleLimit = JsonUtil.initMaxBigIntegerScale();
    }

    @Override
    public JsonGenerator createGenerator(Writer writer) {
        return new JsonGeneratorImpl(writer, bufferPool);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        return new JsonGeneratorImpl(out, bufferPool);
    }

    @Override
    public JsonParser createParser(Reader reader) {
        return new JsonParserImpl(reader, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonParser createParser(InputStream in) {
        return new JsonParserImpl(in, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        BufferPool pool = null;
        if (config != null && config.containsKey(BufferPool.class.getName())) {
            pool = (BufferPool)config.get(BufferPool.class.getName());
        }
        if (pool == null) {
            pool = bufferPool;
        }
        return new JsonParserFactoryImpl(pool, bigIntegerScaleLimit);
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        Map<String, Object> providerConfig;
        boolean prettyPrinting;
        BufferPool pool;
        if (config == null) {
            providerConfig = Collections.emptyMap();
            prettyPrinting = false;
            pool = bufferPool;
        } else {
            providerConfig = new HashMap<>();
            if (prettyPrinting=JsonProviderImpl.isPrettyPrintingEnabled(config)) {
                providerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
            }
            pool = (BufferPool)config.get(BufferPool.class.getName());
            if (pool != null) {
                providerConfig.put(BufferPool.class.getName(), pool);
            } else {
                pool = bufferPool;
            }
            providerConfig = Collections.unmodifiableMap(providerConfig);
        }

        return new JsonGeneratorFactoryImpl(providerConfig, prettyPrinting, pool);
    }

    @Override
    public JsonReader createReader(Reader reader) {
        return new JsonReaderImpl(reader, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonReader createReader(InputStream in) {
        return new JsonReaderImpl(in, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonWriter createWriter(Writer writer) {
        return new JsonWriterImpl(writer, bufferPool);
    }

    @Override
    public JsonWriter createWriter(OutputStream out) {
        return new JsonWriterImpl(out, bufferPool);
    }

    @Override
    public JsonWriterFactory createWriterFactory(Map<String, ?> config) {
        Map<String, Object> providerConfig;
        boolean prettyPrinting;
        BufferPool pool;
        if (config == null) {
            providerConfig = Collections.emptyMap();
            prettyPrinting = false;
            pool = bufferPool;
        } else {
            providerConfig = new HashMap<>();
            if (prettyPrinting=JsonProviderImpl.isPrettyPrintingEnabled(config)) {
                providerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
            }
            pool = (BufferPool)config.get(BufferPool.class.getName());
            if (pool != null) {
                providerConfig.put(BufferPool.class.getName(), pool);
            } else {
                pool = bufferPool;
            }
            providerConfig = Collections.unmodifiableMap(providerConfig);
        }
        return new JsonWriterFactoryImpl(providerConfig, prettyPrinting, pool);
    }

    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        Map<String, Object> providerConfig;
        boolean rejectDuplicateKeys;
        BufferPool pool;
        if (config == null) {
            providerConfig = Collections.emptyMap();
            rejectDuplicateKeys = false;
            pool = bufferPool;
        } else {
            providerConfig = new HashMap<>();
            addKnowProperty(providerConfig, config, jakarta.json.JsonConfig.KEY_STRATEGY);
            if (rejectDuplicateKeys = JsonProviderImpl.isRejectDuplicateKeysEnabled(config)) {
                providerConfig.put(JsonConfig.REJECT_DUPLICATE_KEYS, true);
            }
            pool = (BufferPool) config.get(BufferPool.class.getName());
            if (pool != null) {
                providerConfig.put(BufferPool.class.getName(), pool);
            } else {
                pool = bufferPool;
            }
            providerConfig = Collections.unmodifiableMap(providerConfig);
        }
        return new JsonReaderFactoryImpl(providerConfig, pool, rejectDuplicateKeys, bigIntegerScaleLimit);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder() {
        return new JsonObjectBuilderImpl(bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(JsonObject object) {
        return new JsonObjectBuilderImpl(object, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(Map<String, ?> map) {
        return new JsonObjectBuilderImpl(map, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder() {
        return new JsonArrayBuilderImpl(bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(JsonArray array) {
        return new JsonArrayBuilderImpl(array, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(Collection<?> collection) {
        return new JsonArrayBuilderImpl(collection, bufferPool, bigIntegerScaleLimit);
    }

    @Override
    public JsonPointer createPointer(String jsonPointer) {
        return new JsonPointerImpl(jsonPointer, bigIntegerScaleLimit);
    }

    @Override
    public JsonPatchBuilder createPatchBuilder() {
        return new JsonPatchBuilderImpl(bigIntegerScaleLimit);
    }

    @Override
    public JsonPatchBuilder createPatchBuilder(JsonArray array) {
        return new JsonPatchBuilderImpl(array, bigIntegerScaleLimit);
    }

    @Override
    public JsonPatch createPatch(JsonArray array) {
        return new JsonPatchImpl(array, bigIntegerScaleLimit);
    }

    @Override
    public JsonPatch createDiff(JsonStructure source, JsonStructure target) {
        return new JsonPatchImpl(JsonPatchImpl.diff(source, target, bigIntegerScaleLimit), bigIntegerScaleLimit);
    }

    @Override
    public JsonMergePatch createMergePatch(JsonValue patch) {
        return new JsonMergePatchImpl(patch, bigIntegerScaleLimit);
    }

    @Override
    public JsonMergePatch createMergeDiff(JsonValue source, JsonValue target) {
        return new JsonMergePatchImpl(JsonMergePatchImpl.diff(source, target, bigIntegerScaleLimit), bigIntegerScaleLimit);
    }

    @Override
    public JsonString createValue(String value) {
        return new JsonStringImpl(value);
    }

    @Override
    public JsonNumber createValue(int value) {
        return JsonNumberImpl.getJsonNumber(value, bigIntegerScaleLimit);
    }

    @Override
    public JsonNumber createValue(long value) {
        return JsonNumberImpl.getJsonNumber(value, bigIntegerScaleLimit);
    }

    @Override
    public JsonNumber createValue(double value) {
        return JsonNumberImpl.getJsonNumber(value, bigIntegerScaleLimit);
    }

    @Override
    public JsonNumber createValue(BigInteger value) {
        return JsonNumberImpl.getJsonNumber(value, bigIntegerScaleLimit);
    }

    @Override
    public JsonNumber createValue(BigDecimal value) {
        return JsonNumberImpl.getJsonNumber(value, bigIntegerScaleLimit);
    }

    @Override
    public JsonBuilderFactory createBuilderFactory(Map<String, ?> config) {
    	BufferPool pool = bufferPool;
    	boolean rejectDuplicateKeys = false;
    	if (config != null) {
    		if (config.containsKey(BufferPool.class.getName())) {
    			pool = (BufferPool) config.get(BufferPool.class.getName());
    		}
    		rejectDuplicateKeys = JsonProviderImpl.isRejectDuplicateKeysEnabled(config);
    	}
        return new JsonBuilderFactoryImpl(pool, rejectDuplicateKeys, bigIntegerScaleLimit);
    }

    @Override
    public JsonNumber createValue(Number value) {
        return JsonNumberImpl.getJsonNumber(value, bigIntegerScaleLimit);
    }

    private void addKnowProperty(Map<String, Object> providerConfig, Map<String, ?> config, String property) {
        if (config.containsKey(property)) {
            providerConfig.put(property, config.get(property));
        }
    }

    static boolean isPrettyPrintingEnabled(Map<String, ?> config) {
        return config.containsKey(JsonGenerator.PRETTY_PRINTING);
    }

    static boolean isRejectDuplicateKeysEnabled(Map<String, ?> config) {
        return config.containsKey(JsonConfig.REJECT_DUPLICATE_KEYS);
    }

}
