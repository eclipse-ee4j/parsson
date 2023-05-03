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
    private final JsonContext emptyContext = new JsonContext(null, bufferPool);

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
        return config == null
                ? new JsonParserFactoryImpl(emptyContext)
                : new JsonParserFactoryImpl(new JsonContext(config, bufferPool, JsonContext.PROPERTY_BUFFER_POOL));
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        return config == null
                ? new JsonGeneratorFactoryImpl(emptyContext)
                : new JsonGeneratorFactoryImpl(
                        new JsonContext(config, bufferPool,
                                        JsonGenerator.PRETTY_PRINTING,
                                        JsonContext.PROPERTY_BUFFER_POOL));
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
        return config == null
                ? new JsonWriterFactoryImpl(emptyContext)
                : new JsonWriterFactoryImpl(
                        new JsonContext(config, bufferPool,
                                        JsonGenerator.PRETTY_PRINTING,
                                        JsonContext.PROPERTY_BUFFER_POOL));
    }

    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        return config == null
                ? new JsonReaderFactoryImpl(emptyContext)
                : new JsonReaderFactoryImpl(
                        new JsonContext(config, bufferPool,
                                        JsonConfig.REJECT_DUPLICATE_KEYS,
                                        JsonContext.PROPERTY_BUFFER_POOL));
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
    public JsonObjectBuilder createObjectBuilder(Map<String, Object> map) {
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
        return config == null
                ? new JsonBuilderFactoryImpl(emptyContext)
                : new JsonBuilderFactoryImpl(
                        new JsonContext(config, bufferPool,
                                        JsonConfig.REJECT_DUPLICATE_KEYS,
                                        JsonContext.PROPERTY_BUFFER_POOL));
    }

}
