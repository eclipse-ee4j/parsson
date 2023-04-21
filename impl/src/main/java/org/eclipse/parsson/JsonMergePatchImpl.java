/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Objects;

import jakarta.json.JsonMergePatch;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

/**
 * This class is an implementation of a JSON Merge Patch as specified in
 * <a href="http://tools.ietf.org/html/rfc7396">RFC 7396</a>.
 *
 * @since 1.1
 */

public final class JsonMergePatchImpl implements JsonMergePatch {

    private final JsonValue patch;
    private final JsonContext jsonContext;

    public JsonMergePatchImpl(JsonValue patch, JsonContext jsonContext) {
        this.jsonContext = jsonContext;
        this.patch = patch;
    }

    @Override
    public JsonValue apply(JsonValue target) {
        return mergePatch(target, patch);
    }

    @Override
    public JsonValue toJsonValue() {
        return patch;
    }
    /**
     * Applies the specified patch to the specified target.
     * The target is not modified by the patch.
     *
     * @param target the {@code JsonValue} to apply the patch operations
     * @param patch the patch
     * @return the {@code JsonValue} as the result of applying the patch
     *    operations on the target.
     */
    private JsonValue mergePatch(JsonValue target, JsonValue patch) {

        if (patch.getValueType() != JsonValue.ValueType.OBJECT) {
            return patch;
        }
        if (target.getValueType() != JsonValue.ValueType.OBJECT) {
            target = JsonValue.EMPTY_JSON_OBJECT;
        }
        JsonObject targetJsonObject = target.asJsonObject();
        JsonObjectBuilder builder =
            new JsonObjectBuilderImpl(targetJsonObject, jsonContext);
        patch.asJsonObject().forEach((key, value) -> {
            if (value == JsonValue.NULL) {
                if (targetJsonObject.containsKey(key)) {
                    builder.remove(key);
                }
            } else if (targetJsonObject.containsKey(key)) {
                builder.add(key, mergePatch(targetJsonObject.get(key), value));
            } else {
                builder.add(key, mergePatch(JsonValue.EMPTY_JSON_OBJECT, value));
            }
        });
        return builder.build();
    }

    /**
     * Generate a JSON Merge Patch from the source and target {@code JsonValue}.
     * @param source the source
     * @param target the target
     * @return a JSON Patch which when applied to the source, yields the target
     */
    static JsonValue diff(JsonValue source, JsonValue target, JsonContext jsonContext) {
        if (source.getValueType() != JsonValue.ValueType.OBJECT ||
                target.getValueType() != JsonValue.ValueType.OBJECT) {
            return target;
        }
        JsonObject s = (JsonObject) source;
        JsonObject t = (JsonObject) target;
        JsonObjectBuilder builder = new JsonObjectBuilderImpl(jsonContext);
        // First find members to be replaced or removed
        s.forEach((key, value) -> {
            if (t.containsKey(key)) {
                // key present in both.
                if (! value.equals(t.get(key))) {
                    // If the values are equal, nop, else get diff for the values
                    builder.add(key, diff(value, t.get(key), jsonContext));
                }
            } else {
                builder.addNull(key);
            }
        });
        // Then find members to be added
        t.forEach((key, value) -> {
            if (! s.containsKey(key))
                builder.add(key, value);
        });
        return builder.build();
    }

    /**
     * Compares this {@code JsonMergePatchImpl} with another object.
     * @param obj the object to compare this {@code JsonMergePatchImpl} against
     * @return true if the given object is a {@code JsonMergePatchImpl} with the same
     * reference tokens as this one, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != JsonMergePatchImpl.class) {
            return false;
        } else {
            JsonMergePatchImpl other = (JsonMergePatchImpl) obj;
            return Objects.equals(patch, other.patch);
        }
    }

    /**
     * Returns the hash code value for this {@code JsonMergePatchImpl}.
     *
     * @return the hash code value for this {@code JsonMergePatchImpl} object
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(patch);
    }

    /**
     * Returns the JSON Patch text
     * @return the JSON Patch text
     */
    @Override
    public String toString() {
        return patch.toString();
    }
}

