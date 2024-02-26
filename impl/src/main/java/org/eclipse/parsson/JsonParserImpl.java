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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;

import org.eclipse.parsson.JsonTokenizer.JsonToken;

/**
 * JSON parser implementation. NoneContext, ArrayContext, ObjectContext is used
 * to go to next parser state.
 *
 * @author Jitendra Kotamraju
 * @author Kin-man Chung
 */
public class JsonParserImpl implements JsonParser {

    private Context currentContext = new NoneContext();
    private Event currentEvent;

    private final Stack stack;
    private final JsonTokenizer tokenizer;
    private boolean closed = false;

    private final JsonContext jsonContext;

    private final JsonParserStreamCreator streamCreator = new JsonParserStreamCreator(this, true, () -> currentEvent,
            () -> currentContext instanceof NoneContext);


    JsonParserImpl(Reader reader, JsonContext jsonContext) {
        this.jsonContext = jsonContext;
        stack = new Stack(jsonContext.depthLimit());
        this.tokenizer = new JsonTokenizer(reader, jsonContext);
    }

    JsonParserImpl(InputStream in, JsonContext jsonContext) {
        this.jsonContext = jsonContext;
        stack = new Stack(jsonContext.depthLimit());
        UnicodeDetectingInputStream uin = new UnicodeDetectingInputStream(in);
        this.tokenizer = new JsonTokenizer(new InputStreamReader(uin, uin.getCharset()), jsonContext);
    }

    JsonParserImpl(InputStream in, Charset encoding, JsonContext jsonContext) {
        this.jsonContext = jsonContext;
        stack = new Stack(jsonContext.depthLimit());
        this.tokenizer = new JsonTokenizer(new InputStreamReader(in, encoding), jsonContext);
    }

    @Override
    public String getString() {
        if (currentEvent == Event.KEY_NAME || currentEvent == Event.VALUE_STRING
                || currentEvent == Event.VALUE_NUMBER) {
            return tokenizer.getValue();
        }
        throw new IllegalStateException(
                JsonMessages.PARSER_GETSTRING_ERR(currentEvent));
    }

    private <T> T getNumberValue(Function<JsonTokenizer, T> tokenizerFunction, Function<Event, String> exceptionMessageFunction) {
        if (currentEvent != Event.VALUE_NUMBER) {
            throw new IllegalStateException(
                    exceptionMessageFunction.apply(currentEvent));
        }
        return tokenizerFunction.apply(tokenizer);
    }

    @Override
    public boolean isIntegralNumber() {
        return getNumberValue(JsonTokenizer::isIntegral, JsonMessages::PARSER_ISINTEGRALNUMBER_ERR);
    }

    @Override
    public int getInt() {
        return getNumberValue(JsonTokenizer::getInt, JsonMessages::PARSER_GETINT_ERR);
    }

    boolean isDefinitelyInt() {
        return tokenizer.isDefinitelyInt();
    }

    boolean isDefinitelyLong() {
        return tokenizer.isDefinitelyLong();
    }

    @Override
    public long getLong() {
        return getNumberValue(JsonTokenizer::getLong, JsonMessages::PARSER_GETLONG_ERR);
    }

    @Override
    public BigDecimal getBigDecimal() {
        return getNumberValue(JsonTokenizer::getBigDecimal, JsonMessages::PARSER_GETBIGDECIMAL_ERR);
    }

    @Override
    public JsonArray getArray() {
        if (currentEvent != Event.START_ARRAY) {
            throw new IllegalStateException(
                JsonMessages.PARSER_GETARRAY_ERR(currentEvent));
        }
        return getArray(new JsonArrayBuilderImpl(jsonContext));
    }

    @Override
    public JsonObject getObject() {
        if (currentEvent != Event.START_OBJECT) {
            throw new IllegalStateException(
                JsonMessages.PARSER_GETOBJECT_ERR(currentEvent));
        }
        return getObject(new JsonObjectBuilderImpl(jsonContext));
    }

    @Override
    public JsonValue getValue() {
        switch (currentEvent) {
            case START_ARRAY:
                return getArray(new JsonArrayBuilderImpl(jsonContext));
            case START_OBJECT:
                return getObject(new JsonObjectBuilderImpl(jsonContext));
            case KEY_NAME:
            case VALUE_STRING:
                return new JsonStringImpl(getCharSequence());
            case VALUE_NUMBER:
                if (isDefinitelyInt()) {
                    return JsonNumberImpl.getJsonNumber(getInt(), jsonContext.bigIntegerScaleLimit());
                } else if (isDefinitelyLong()) {
                    return JsonNumberImpl.getJsonNumber(getLong(), jsonContext.bigIntegerScaleLimit());
                }
                return JsonNumberImpl.getJsonNumber(getBigDecimal(), jsonContext.bigIntegerScaleLimit());
            case VALUE_TRUE:
                return JsonValue.TRUE;
            case VALUE_FALSE:
                return JsonValue.FALSE;
            case VALUE_NULL:
                return JsonValue.NULL;
            case END_ARRAY:
            case END_OBJECT:
            default:
                throw new IllegalStateException(JsonMessages.PARSER_GETVALUE_ERR(currentEvent));
        }
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
    public void skipArray() {
        if (currentContext instanceof ArrayContext) {
            currentContext.skip();
            currentContext = stack.pop();
            currentEvent = Event.END_ARRAY;
        }
    }

    @Override
    public void skipObject() {
        if (currentContext instanceof ObjectContext) {
            currentContext.skip();
            currentContext = stack.pop();
            currentEvent = Event.END_OBJECT;
        }
    }

    private JsonArray getArray(JsonArrayBuilder builder) {
        while(hasNext()) {
            JsonParser.Event e = next();
            if (e == JsonParser.Event.END_ARRAY) {
                return builder.build();
            }
            builder.add(getValue());
        }
        throw parsingException(JsonToken.EOF, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL, SQUARECLOSE]");
    }

    private CharSequence getCharSequence() {
      if (currentEvent == Event.KEY_NAME || currentEvent == Event.VALUE_STRING
              || currentEvent == Event.VALUE_NUMBER) {
          return tokenizer.getCharSequence();
      }
      throw new IllegalStateException(JsonMessages.PARSER_GETSTRING_ERR(currentEvent));
  }

    private JsonObject getObject(JsonObjectBuilder builder) {
        while(hasNext()) {
            JsonParser.Event e = next();
            if (e == JsonParser.Event.END_OBJECT) {
                return builder.build();
            }
            String key = getString();
            next();
            builder.add(key, getValue());
        }
        throw parsingException(JsonToken.EOF, "[STRING, CURLYCLOSE]");
    }

    @Override
    public JsonLocation getLocation() {
        return tokenizer.getLocation();
    }

    public JsonLocation getLastCharLocation() {
        return tokenizer.getLastCharLocation();
    }

    @Override
    public boolean hasNext() {
        if (stack.isEmpty() && (currentEvent != null && currentEvent.compareTo(Event.KEY_NAME) > 0)) {
            JsonToken token = tokenizer.nextToken();
            if (token != JsonToken.EOF) {
                throw new JsonParsingException(JsonMessages.PARSER_EXPECTED_EOF(token),
                        getLastCharLocation());
            }
            return false;
        } else if (!stack.isEmpty() && !tokenizer.hasNextToken()) {
            currentEvent = currentContext.getNextEvent();
            return false;
        }
        return true;
    }

    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return currentEvent = currentContext.getNextEvent();
    }

    @Override
    public Event currentEvent() {
        return currentEvent;
    }

    @Override
    public void close() {
        if (!closed) {
            try {
                tokenizer.close();
                closed = true;
            } catch (IOException e) {
                throw new JsonException(JsonMessages.PARSER_TOKENIZER_CLOSE_IO(), e);
            }
        }
    }

    // Using the optimized stack impl as we don't require other things
    // like iterator etc.
    private static final class Stack {
        int size = 0;
        final int limit;
        private Context head;

        Stack(int size) {
            this.limit = size;
        }

        private void push(Context context) {
            if (++size >= limit) {
                throw new RuntimeException(JsonMessages.PARSER_INPUT_NESTED_TOO_DEEP(size));
            }
            context.next = head;
            head = context;
        }

        private Context pop() {
            if (head == null) {
                throw new NoSuchElementException();
            }
            size--;
            Context temp = head;
            head = head.next;
            return temp;
        }

        private Context peek() {
            return head;
        }

        private boolean isEmpty() {
            return head == null;
        }
    }

    private abstract class Context {
        Context next;
        abstract Event getNextEvent();
        abstract void skip();

        protected Event nextEventIfValueOrObjectOrArrayStart(JsonToken token) {
            if (token.isValue()) {
                return token.getEvent();
            } else if (token == JsonToken.CURLYOPEN) {
                stack.push(currentContext);
                currentContext = new ObjectContext();
                return Event.START_OBJECT;
            } else if (token == JsonToken.SQUAREOPEN) {
                stack.push(currentContext);
                currentContext = new ArrayContext();
                return Event.START_ARRAY;
            }
            return null;
        }
    }

    private final class NoneContext extends Context {
        @Override
        public Event getNextEvent() {
            // Handle 1. {   2. [   3. value
            JsonToken token = tokenizer.nextToken();
            Event event = nextEventIfValueOrObjectOrArrayStart(token);
            if (event != null) {
                return event;
            }
            throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
        }

        @Override
        void skip() {
            // no-op
        }
    }

    private JsonParsingException parsingException(JsonToken token, String expectedTokens) {
        JsonLocation location = getLastCharLocation();
        return new JsonParsingException(
                JsonMessages.PARSER_INVALID_TOKEN(token, location, expectedTokens), location);
    }

    private abstract class SkippingContext extends Context {
        private final JsonToken openToken;
        private final JsonToken closeToken;

        private boolean firstValue = true;

        private SkippingContext(JsonToken openToken, JsonToken closeToken) {
            this.openToken = Objects.requireNonNull(openToken);
            this.closeToken = Objects.requireNonNull(closeToken);
        }

        @Override
        void skip() {
            JsonToken token;
            int depth = 1;
            do {
                token = tokenizer.nextToken();
                if (token == closeToken) {
                    depth--;
                }
                if (token == openToken) {
                    depth++;
                }
            } while (!(token == closeToken && depth == 0));
        }

        JsonToken firstValueOrJsonToken(JsonToken token) {
            if (firstValue) {
                firstValue = false;
            } else {
                if (token != JsonToken.COMMA) {
                    throw parsingException(token, "[COMMA]");
                }
                token = tokenizer.nextToken();
            }
            return token;
        }
    }

    private final class ObjectContext extends SkippingContext {
        private ObjectContext() {
            super(JsonToken.CURLYOPEN, JsonToken.CURLYCLOSE);
        }

        /*
         * Some more things could be optimized. For example, instead
         * tokenizer.nextToken(), one could use tokenizer.matchColonToken() to
         * match ':'. That might optimize a bit, but will fragment nextToken().
         * I think the current one is more readable.
         *
         */
        @Override
        public Event getNextEvent() {
            // Handle 1. }   2. name:value   3. ,name:value
            JsonToken token = tokenizer.nextToken();
            if (token == JsonToken.EOF) {
                switch (currentEvent) {
                    case START_OBJECT:
                        throw parsingException(token, "[STRING, CURLYCLOSE]");
                    case KEY_NAME:
                        throw parsingException(token, "[COLON]");
                    default:
                        throw parsingException(token, "[COMMA, CURLYCLOSE]");
                }
            } else if (currentEvent == Event.KEY_NAME) {
                // Handle 1. :value
                if (token != JsonToken.COLON) {
                    throw parsingException(token, "[COLON]");
                }
                token = tokenizer.nextToken();
                Event event = nextEventIfValueOrObjectOrArrayStart(token);
                if (event != null) {
                    return event;
                }
                throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
            } else {
                // Handle 1. }   2. name   3. ,name
                if (token == JsonToken.CURLYCLOSE) {
                    currentContext = stack.pop();
                    return Event.END_OBJECT;
                }

                token = firstValueOrJsonToken(token);
                if (token == JsonToken.STRING) {
                    return Event.KEY_NAME;
                }
                throw parsingException(token, "[STRING]");
            }
        }
    }

    private final class ArrayContext extends SkippingContext {

        private ArrayContext() {
            super(JsonToken.SQUAREOPEN, JsonToken.SQUARECLOSE);
        }

        // Handle 1. ]   2. value   3. ,value
        @Override
        public Event getNextEvent() {
            JsonToken token = tokenizer.nextToken();
            if (token == JsonToken.EOF) {
                throw parsingException(token, (Objects.requireNonNull(currentEvent) == Event.START_ARRAY) ?
                        "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]" : "[COMMA, CURLYCLOSE]");
			}
            if (token == JsonToken.SQUARECLOSE) {
                currentContext = stack.pop();
                return Event.END_ARRAY;
            }
            token = firstValueOrJsonToken(token);

            Event event = nextEventIfValueOrObjectOrArrayStart(token);
            if (event != null) {
                return event;
            }
            throw parsingException(token, "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
        }
    }
}
