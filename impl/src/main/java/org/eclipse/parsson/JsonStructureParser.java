/*
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.json.*;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * {@link JsonParser} implementation on top of JsonArray/JsonObject
 *
 * @author Jitendra Kotamraju
 */
class JsonStructureParser implements JsonParser {

    private Scope<?> current;
    private Event state;
    private final Deque<Scope<?>> scopeStack = new ArrayDeque<>();

    //JsonParserImpl delivers the whole object - so we have to call next() before creation of the stream
    private final JsonParserStreamCreator streamCreator = new JsonParserStreamCreator(this, true, () -> state, scopeStack::isEmpty);


    JsonStructureParser(JsonArray array) {
        current = new ArrayScope(array);
    }

    JsonStructureParser(JsonObject object) {
        current = new ObjectScope(object);
    }

    @Override
    public String getString() {
        switch (state) {
            case KEY_NAME:
                return ((ObjectScope)current).key;
            case VALUE_STRING:
                return ((JsonString)current.getJsonValue()).getString();
            case VALUE_NUMBER:
                return current.getJsonValue().toString();
            default:
                throw new IllegalStateException(JsonMessages.PARSER_GETSTRING_ERR(state));
        }
    }

    private <T> T getNumberValue(Function<JsonNumber, T> numberFunction, Function<Event, String> exceptionMessageFunction) {
        if (state == Event.VALUE_NUMBER) {
            return numberFunction.apply((JsonNumber)current.getJsonValue());
        }
        throw new IllegalStateException(exceptionMessageFunction.apply(state));
    }

    @Override
    public boolean isIntegralNumber() {
        return getNumberValue(JsonNumber::isIntegral, JsonMessages::PARSER_ISINTEGRALNUMBER_ERR);
    }

    @Override
    public int getInt() {
        return getNumberValue(JsonNumber::intValue, JsonMessages::PARSER_GETINT_ERR);
    }

    @Override
    public long getLong() {
        return getNumberValue(JsonNumber::longValue, JsonMessages::PARSER_GETLONG_ERR);
    }

    @Override
    public BigDecimal getBigDecimal() {
        return getNumberValue(JsonNumber::bigDecimalValue, JsonMessages::PARSER_GETBIGDECIMAL_ERR);
    }

    @Override
    public JsonLocation getLocation() {
        return JsonLocationImpl.UNKNOWN;
    }

    @Override
    public JsonObject getObject() {
        if (state != Event.START_OBJECT) {
            throw new IllegalStateException(
                    JsonMessages.PARSER_GETOBJECT_ERR(state));
        }
        if (current == null) {
            throw new NoSuchElementException(JsonMessages.INTERNAL_ERROR());
        }
        state = Event.END_OBJECT;
        return current.getJsonValue().asJsonObject();

    }

    @Override
    public JsonValue getValue() {
        if (current == null) {
            throw new IllegalStateException(JsonMessages.INTERNAL_ERROR());
        }

        switch (state) {
            case START_OBJECT:
                return getObject();
            case START_ARRAY:
                return getArray();
            case KEY_NAME:
                return Json.createValue(((ObjectScope)current).key);
            case END_OBJECT:
            case END_ARRAY:
                throw new IllegalStateException(JsonMessages.INTERNAL_ERROR());
            default:
                return current.getJsonValue();
        }
    }

    @Override
    public JsonArray getArray() {
        if (state != Event.START_ARRAY) {
            throw new IllegalStateException(
                    JsonMessages.PARSER_GETARRAY_ERR(state));
        }
        Scope<?> topOfTheStack = scopeStack.isEmpty() ? current : scopeStack.pop();
        if (topOfTheStack == null) {
            throw new NoSuchElementException(JsonMessages.INTERNAL_ERROR());
        }
        state = Event.END_ARRAY;
        return topOfTheStack.getJsonValue().asJsonArray();
    }

    @Override
    public boolean hasNext() {
        return !((state == Event.END_OBJECT || state == Event.END_ARRAY) && scopeStack.isEmpty());
    }

    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        transition();
        return state;
    }

    private void transition() {
        if (state == null) {
            state = current instanceof ArrayScope ? Event.START_ARRAY : Event.START_OBJECT;
        } else {
            if (state == Event.END_OBJECT || state == Event.END_ARRAY) {
                current = scopeStack.pop();
            }
            if (current instanceof ArrayScope) {
                if (current.hasNext()) {
                    current.next();
                    nextStateAndEndOfTheObjectOrArray();
                } else {
                    state = Event.END_ARRAY;
                }
            } else {
                // ObjectScope
                if (state == Event.KEY_NAME) {
                    nextStateAndEndOfTheObjectOrArray();
                } else {
                    if (current.hasNext()) {
                        current.next();
                        state = Event.KEY_NAME;
                    } else {
                        state = Event.END_OBJECT;
                    }
                }
            }
        }
    }

    private void nextStateAndEndOfTheObjectOrArray() {
        state = getState(current.getJsonValue());
        if (state == Event.START_ARRAY || state == Event.START_OBJECT) {
            scopeStack.push(current);
            current = Scope.createScope(current.getJsonValue());
        }
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public Event currentEvent() {
        return state;
    }

    @Override
    public Stream<JsonValue> getArrayStream() {
        return streamCreator.getArrayStream();
    }

    @Override
    public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
        return streamCreator.getObjectStream();
    }

    @Override
    public Stream<JsonValue> getValueStream() {
        return streamCreator.getValueStream();
    }

    @Override
    public void skipObject() {
        if (current instanceof ObjectScope) {
            state = Event.END_OBJECT;
        }
    }

    @Override
    public void skipArray() {
        if (current instanceof ArrayScope) {
            state = Event.END_ARRAY;
        }
    }

    private static Event getState(JsonValue value) {
        switch (value.getValueType()) {
            case ARRAY:
                return Event.START_ARRAY;
            case OBJECT:
                return Event.START_OBJECT;
            case STRING:
                return Event.VALUE_STRING;
            case NUMBER:
                return Event.VALUE_NUMBER;
            case TRUE:
                return Event.VALUE_TRUE;
            case FALSE:
                return Event.VALUE_FALSE;
            case NULL:
                return Event.VALUE_NULL;
            default:
                throw new JsonException(JsonMessages.PARSER_STATE_ERR(value.getValueType()));
        }
    }

    private static abstract class Scope<T> implements Iterator<T> {
        @Override
        public final void remove() {throw new UnsupportedOperationException(); }

        abstract JsonValue getJsonValue();

        static Scope<?> createScope(JsonValue value) {
            if (value instanceof JsonArray) {
                return new ArrayScope((JsonArray)value);
            } else if (value instanceof JsonObject) {
                return new ObjectScope((JsonObject)value);
            }
            throw new JsonException(JsonMessages.PARSER_SCOPE_ERR(value));
        }
    }

    private static class ArrayScope extends Scope<JsonValue> {
        private final Iterator<JsonValue> it;
        private JsonValue value;

        ArrayScope(JsonArray array) {
            this.it = array.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public JsonValue next() {
            value = it.next();
            return value;
        }

        @Override
        JsonValue getJsonValue() {
            return value;
        }

    }

    private static class ObjectScope extends Scope<Map.Entry<String, JsonValue>> {
        private final JsonObject object;
        private final Iterator<Map.Entry<String, JsonValue>> it;
        private JsonValue value;
        private String key;

        ObjectScope(JsonObject object) {
            this.object = object;
            this.it = object.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Map.Entry<String, JsonValue> next() {
            Map.Entry<String, JsonValue> next = it.next();
            this.key = next.getKey();
            this.value = next.getValue();
            return next;
        }

        @Override
        JsonValue getJsonValue() {
            return value == null ? object : value;
        }

    }

}
