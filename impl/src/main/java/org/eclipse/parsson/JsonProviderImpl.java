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
    private final JsonContext emptyContext = new JsonContext(Collections.emptyMap(), bufferPool);

    @Override
    public JsonGenerator createGenerator(Writer writer) {
        return new JsonGeneratorImpl(writer, emptyContext);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        return new JsonGeneratorImpl(out, emptyContext);
    }

    @Override
    public JsonParser createParser(Reader reader) {
        return new JsonParserImpl(reader, emptyContext);
    }

    @Override
    public JsonParser createParser(InputStream in) {
        return new JsonParserImpl(in, emptyContext);
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        return new JsonParserFactoryImpl(new JsonContext(config, bufferPool));
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        if (config == null) {
            return new JsonGeneratorFactoryImpl(false, emptyContext);
        }
        Map<String, Object> providerConfig = new HashMap<>();
        boolean prettyPrinting = JsonProviderImpl.isPrettyPrintingEnabled(config);
        if (prettyPrinting) {
            providerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
        }
        BufferPool pool = (BufferPool) config.get(JsonContext.PROPERTY_BUFFER_POOL);
        if (pool != null) {
            providerConfig.put(JsonContext.PROPERTY_BUFFER_POOL, pool);
        }
        return new JsonGeneratorFactoryImpl(
                prettyPrinting, new JsonContext(Collections.unmodifiableMap(providerConfig), bufferPool));
    }

    @Override
    public JsonReader createReader(Reader reader) {
        return new JsonReaderImpl(reader, emptyContext);
    }

    @Override
    public JsonReader createReader(InputStream in) {
        return new JsonReaderImpl(in, emptyContext);
    }

    @Override
    public JsonWriter createWriter(Writer writer) {
        return new JsonWriterImpl(writer, emptyContext);
    }

    @Override
    public JsonWriter createWriter(OutputStream out) {
        return new JsonWriterImpl(out, emptyContext);
    }

    @Override
    public JsonWriterFactory createWriterFactory(Map<String, ?> config) {
        if (config == null) {
            return new JsonWriterFactoryImpl(false, emptyContext);
        }
        Map<String, Object> providerConfig = new HashMap<>();
        boolean prettyPrinting = JsonProviderImpl.isPrettyPrintingEnabled(config);
        if (prettyPrinting) {
            providerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
        }
        BufferPool pool = (BufferPool) config.get(JsonContext.PROPERTY_BUFFER_POOL);
        if (pool != null) {
            providerConfig.put(JsonContext.PROPERTY_BUFFER_POOL, pool);
        }
        return new JsonWriterFactoryImpl(
                prettyPrinting, new JsonContext(Collections.unmodifiableMap(providerConfig), bufferPool));
    }

    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        if (config == null) {
            return new JsonReaderFactoryImpl(false, emptyContext);
        }
        Map<String, Object> providerConfig = new HashMap<>();
        boolean rejectDuplicateKeys = JsonProviderImpl.isRejectDuplicateKeysEnabled(config);
        if (rejectDuplicateKeys) {
            providerConfig.put(JsonConfig.REJECT_DUPLICATE_KEYS, true);
        }
        addKnowProperty(providerConfig, config, jakarta.json.JsonConfig.KEY_STRATEGY);
        BufferPool pool = (BufferPool) config.get(JsonContext.PROPERTY_BUFFER_POOL);
        if (pool != null) {
            providerConfig.put(JsonContext.PROPERTY_BUFFER_POOL, pool);
        }
        return new JsonReaderFactoryImpl(
                rejectDuplicateKeys, new JsonContext(Collections.unmodifiableMap(providerConfig), bufferPool));
    }

    @Override
    public JsonObjectBuilder createObjectBuilder() {
        return new JsonObjectBuilderImpl(emptyContext);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(JsonObject object) {
        return new JsonObjectBuilderImpl(object, emptyContext);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(Map<String, ?> map) {
        return new JsonObjectBuilderImpl(map, emptyContext);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder() {
        return new JsonArrayBuilderImpl(emptyContext);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(JsonArray array) {
        return new JsonArrayBuilderImpl(array, emptyContext);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(Collection<?> collection) {
        return new JsonArrayBuilderImpl(collection, emptyContext);
    }

    @Override
    public JsonPointer createPointer(String jsonPointer) {
        return new JsonPointerImpl(jsonPointer, emptyContext);
    }

    @Override
    public JsonPatchBuilder createPatchBuilder() {
        return new JsonPatchBuilderImpl(emptyContext);
    }

    @Override
    public JsonPatchBuilder createPatchBuilder(JsonArray array) {
        return new JsonPatchBuilderImpl(array, emptyContext);
    }

    @Override
    public JsonPatch createPatch(JsonArray array) {
        return new JsonPatchImpl(array, emptyContext);
    }

    @Override
    public JsonPatch createDiff(JsonStructure source, JsonStructure target) {
        return new JsonPatchImpl(JsonPatchImpl.diff(source, target, emptyContext), emptyContext);
    }

    @Override
    public JsonMergePatch createMergePatch(JsonValue patch) {
        return new JsonMergePatchImpl(patch, emptyContext);
    }

    @Override
    public JsonMergePatch createMergeDiff(JsonValue source, JsonValue target) {
        return new JsonMergePatchImpl(JsonMergePatchImpl.diff(source, target, emptyContext), emptyContext);
    }

    @Override
    public JsonString createValue(String value) {
        return new JsonStringImpl(value);
    }

    @Override
    public JsonNumber createValue(int value) {
        return JsonNumberImpl.getJsonNumber(value, emptyContext.bigIntegerScaleLimit());
    }

    @Override
    public JsonNumber createValue(long value) {
        return JsonNumberImpl.getJsonNumber(value, emptyContext.bigIntegerScaleLimit());
    }

    @Override
    public JsonNumber createValue(double value) {
        return JsonNumberImpl.getJsonNumber(value, emptyContext.bigIntegerScaleLimit());
    }

    @Override
    public JsonNumber createValue(BigInteger value) {
        return JsonNumberImpl.getJsonNumber(value, emptyContext.bigIntegerScaleLimit());
    }

    @Override
    public JsonNumber createValue(BigDecimal value) {
        return JsonNumberImpl.getJsonNumber(value, emptyContext.bigIntegerScaleLimit());
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
        return new JsonBuilderFactoryImpl(rejectDuplicateKeys, new JsonContext(config, bufferPool));
    }

    @Override
    public JsonNumber createValue(Number value) {
        return JsonNumberImpl.getJsonNumber(value, emptyContext.bigIntegerScaleLimit());
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
