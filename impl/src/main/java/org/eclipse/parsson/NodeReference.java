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

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * This class is a helper class for JsonPointer implementation,
 * and is not part of the API.
 *
 * This class encapsulates a reference to a JSON node.
 * There are three types of references.
 * <ol><li>a reference to the root of a JSON tree.</li>
 *     <li>a reference to a name/value (possibly non-existing) pair of a JSON object, identified by a name.</li>
 *     <li>a reference to a member value of a JSON array, identified by an index.</li>
 * </ol>
 * Static factory methods are provided for creating these references.
 *
 * <p>A referenced value can be retrieved or replaced.
 * The value of a JSON object or JSON array can be
 * removed.  A new value can be added to a JSON object or
 * inserted into a JSON array</p>
 *
 * <p>Since a {@code JsonObject} or {@code JsonArray} is immutable, these operations
 * must not modify the referenced JSON object or array. The methods {@link #add},
 * {@link #replace}, and {@link #remove} returns a new
 * JSON object or array after the execution of the operation.</p>
 */
abstract class NodeReference {

    /**
     * Return {@code true} if a reference points to a valid value, {@code false} otherwise.
     *
     * @return {@code true} if a reference points to a value
     */
    abstract public boolean contains();

    /**
     * Get the value at the referenced location.
     *
     * @return the JSON value referenced
     * @throws JsonException if the referenced value does not exist
     */
    abstract public JsonValue get();

    /**
     * Add or replace a value at the referenced location.
     * If the reference is the root of a JSON tree, the added value must be
     * a JSON object or array, which becomes the referenced JSON value.
     * If the reference is an index of a JSON array, the value is inserted
     * into the array at the index.  If the index is -1, the value is
     * appended to the array.
     * If the reference is a name of a JSON object, the name/value pair is added
     * to the object, replacing any pair with the same name.
     *
     * @param value the value to be added
     * @return the JsonStructure after the operation
     * @throws JsonException if the index to the array is not -1 or is out of range
     */
    abstract public JsonStructure add(JsonValue value);

    /**
     * Remove the name/value pair from the JSON object, or the value in a JSON array, as specified by the reference
     *
     * @return the JsonStructure after the operation
     * @throws JsonException if the name/value pair of the referenced JSON object
     *    does not exist, or if the index of the referenced JSON array is
     *    out of range, or if the reference is a root reference
     */
    abstract public JsonStructure remove();

    /**
     * Replace the referenced value with the specified value.
     *
     * @param value the JSON value to be stored at the referenced location
     * @return the JsonStructure after the operation
     * @throws JsonException if the name/value pair of the referenced JSON object
     *    does not exist, or if the index of the referenced JSON array is
     *    out of range, or if the reference is a root reference
     */
    abstract public JsonStructure replace(JsonValue value);

    /**
     * Returns a {@code NodeReference} for a {@code JsonStructure}.
     *
     * @param structure the {@code JsonStructure} referenced
     * @return the {@code NodeReference}
     */
    public static NodeReference of(JsonStructure structure) {
        return new RootReference(structure);
    }

    /**
     * Returns a {@code NodeReference} for a name/value pair in a
     * JSON object.
     *
     * @param object the referenced JSON object
     * @param name the name of the name/pair
     * @return the {@code NodeReference}
     */
    public static NodeReference of(JsonObject object, String name) {
        return new ObjectReference(object, name);
    }

    /**
     * Returns a {@code NodeReference} for a member value in a
     * JSON array.
     *
     * @param array the referenced JSON array
     * @param index the index of the member value in the JSON array
     * @return the {@code NodeReference}
     */
    public static NodeReference of(JsonArray array, int index) {
        return new ArrayReference(array, index);
    }

    static class RootReference extends NodeReference {

        private JsonStructure root;

        RootReference(JsonStructure root) {
            this.root = root;
        }

        @Override
        public boolean contains() {
            return root != null;
        }

        @Override
        public JsonValue get() {
            return root;
        }

        @Override
        public JsonStructure add(JsonValue value) {
            switch (value.getValueType() ) {
                case OBJECT:
                case ARRAY:
                    this.root = (JsonStructure) value;
                    break;
                default:
                    throw new JsonException(JsonMessages.NODEREF_VALUE_ADD_ERR());
            }
            return root;
        }

        @Override
        public JsonStructure remove() {
            throw new JsonException(JsonMessages.NODEREF_VALUE_CANNOT_REMOVE());
        }

        @Override
        public JsonStructure replace(JsonValue value) {
            return add(value);
        }
    }

    static class ObjectReference extends NodeReference {

        private final JsonObject object;
        private final String key;

        ObjectReference(JsonObject object, String key) {
            this.object = object;
            this.key = key;
        }

        @Override
        public boolean contains() {
            return object != null && object.containsKey(key);
        }

        @Override
        public JsonValue get() {
            if (!contains()) {
                throw new JsonException(JsonMessages.NODEREF_OBJECT_MISSING(key));
            }
            return object.get(key);
        }

        @Override
        public JsonObject add(JsonValue value) {
            return new JsonObjectBuilderImpl(object, JsonUtil.getInternalBufferPool()).add(key, value).build();
        }

        @Override
        public JsonObject remove() {
            if (!contains()) {
                throw new JsonException(JsonMessages.NODEREF_OBJECT_MISSING(key));
            }
            return new JsonObjectBuilderImpl(object, JsonUtil.getInternalBufferPool()).remove(key).build();
        }

        @Override
        public JsonObject replace(JsonValue value) {
            if (!contains()) {
                throw new JsonException(JsonMessages.NODEREF_OBJECT_MISSING(key));
            }
            return add(value);
        }
    }

    static class ArrayReference extends NodeReference {

        private final JsonArray array;
        private final int index; // -1 means "-" in JSON Pointer

        ArrayReference(JsonArray array, int index) {
            this.array = array;
            this.index = index;
        }

        @Override
        public boolean contains() {
            return array != null && index > -1 && index < array.size();
        }

        @Override
        public JsonValue get() {
            if (!contains()) {
                throw new JsonException(JsonMessages.NODEREF_ARRAY_INDEX_ERR(index, array.size()));
            }
            return array.get(index);
        }

        @Override
        public JsonArray add(JsonValue value) {
            //TODO should we check for arrayoutofbounds?
            // The spec seems to say index = array.size() is allowed. This is handled as append
            JsonArrayBuilder builder = new JsonArrayBuilderImpl(this.array, JsonUtil.getInternalBufferPool());
            if (index == -1 || index == array.size()) {
                builder.add(value);
            } else {
                if(index < array.size()) {
                    builder.add(index, value);
                } else {
                    throw new JsonException(JsonMessages.NODEREF_ARRAY_INDEX_ERR(index, array.size()));
                }
            }
            return builder.build();
        }

        @Override
        public JsonArray remove() {
            if (!contains()) {
                throw new JsonException(JsonMessages.NODEREF_ARRAY_INDEX_ERR(index, array.size()));
            }
            JsonArrayBuilder builder = new JsonArrayBuilderImpl(this.array, JsonUtil.getInternalBufferPool());
            return builder.remove(index).build();
        }

        @Override
        public JsonArray replace(JsonValue value) {
            if (!contains()) {
                throw new JsonException(JsonMessages.NODEREF_ARRAY_INDEX_ERR(index, array.size()));
            }
            JsonArrayBuilder builder = new JsonArrayBuilderImpl(this.array, JsonUtil.getInternalBufferPool());
            return builder.set(index, value).build();
        }
    }
}

