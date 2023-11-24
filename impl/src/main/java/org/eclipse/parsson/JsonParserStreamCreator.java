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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.json.JsonException;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

class JsonParserStreamCreator {

    private final JsonParser parser;
    private final boolean nextBeforeCreationOfValueStream;
    private final Supplier<Event> currenEventSupplier;
    private final Supplier<Boolean> canProduceValueStream;

    JsonParserStreamCreator(JsonParser parser, boolean nextBeforeCreationOfValueStream, Supplier<Event> currenEventSupplier,
            Supplier<Boolean> canProduceValueStream) {

        this.parser = Objects.requireNonNull(parser);
        this.nextBeforeCreationOfValueStream = nextBeforeCreationOfValueStream;
        this.currenEventSupplier = Objects.requireNonNull(currenEventSupplier);
        this.canProduceValueStream = Objects.requireNonNull(canProduceValueStream);
    }

    /**
     * Creates new {@link Stream} from values from {@link Supplier}. The stream delivers the values as long as supplier delivers non-null values
     *
     * @param supplier supplier of the values
     * @param <T>      type of the values which are delivered by the supplier and the stream
     * @return stream of values from given supplier
     */
    private static <T> Stream<T> streamFromSupplier(Supplier<T> supplier) {
        return StreamCreator.iterate(Objects.requireNonNull(supplier).get(), Objects::nonNull, value -> supplier.get());
    }

    public Stream<JsonValue> getArrayStream() {
        if (currenEventSupplier.get() == Event.START_ARRAY) {
            return streamFromSupplier(() -> (parser.hasNext() && parser.next() != Event.END_ARRAY) ? parser.getValue() : null);
        } else {
            throw new IllegalStateException(JsonMessages.PARSER_GETARRAY_ERR(parser.currentEvent()));
        }
    }

    public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
        if (currenEventSupplier.get() == Event.START_OBJECT) {
            return streamFromSupplier(() -> {
                if (!parser.hasNext()) {
                    return null;
                }
                Event e = parser.next();
                if (e == Event.END_OBJECT) {
                    return null;
                } else if (e != Event.KEY_NAME) {
                    throw new JsonException(JsonMessages.INTERNAL_ERROR());
                } else {
                    String key = parser.getString();
                    if (!parser.hasNext()) {
                        throw new JsonException(JsonMessages.INTERNAL_ERROR());
                    } else {
                        parser.next();
                        return new AbstractMap.SimpleImmutableEntry<>(key, parser.getValue());
                    }
                }
            });
        } else {
            throw new IllegalStateException(JsonMessages.PARSER_GETOBJECT_ERR(parser.currentEvent()));
        }
    }

    public Stream<JsonValue> getValueStream() {
        if (canProduceValueStream.get()) {
            if (nextBeforeCreationOfValueStream) {
                parser.next();
            }

            return streamFromSupplier(() -> {
                if (parser.hasNext()) {
                    return parser.getValue();
                } else {
                    return null;
                }
            });
        } else {
            throw new IllegalStateException(JsonMessages.PARSER_GETVALUESTREAM_ERR());
        }
    }
}
