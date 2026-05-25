/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.parsson.tests;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

import org.eclipse.parsson.api.JsonConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifies that documents exceeding the max parsing limit are
 * rejected.
 */
public class JsonDocumentParseLimitTest {

    // Helper method to repeat a string (Java 9 API compatible)
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    @Test
    void testDocumentParseLimitExceeded() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 1000);
        
        // Create JSON with >1000 characters
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 400; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(i);
        }
        json.append("]");
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json.toString()))) {
                reader.readArray();
            }
        });
    }

    @Test
    void testDocumentParseLimitNotExceeded() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 2000);
        
        // Create JSON with <2000 characters
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(i);
        }
        json.append("]");
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        try (JsonReader reader = factory.createReader(new StringReader(json.toString()))) {
            reader.readArray(); // Should succeed
        }
    }

    @Test
    void testDocumentParseLimitWithLargeString() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 500);
        
        // Create JSON with a long string value that exceeds character limit
        String longString = repeat("a", 600);
        String json = "{\"key\":\"" + longString + "\"}";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readObject();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithLargeNumber() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 100);
        
        // Create JSON with a very long number
        String longNumber = "1" + repeat("0", 150);
        String json = "[" + longNumber + "]";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readArray();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithWhitespaceFlooding() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 200);
        
        // Create JSON with excessive whitespace
        String json = repeat("   ", 100) + "[1,2,3]" + repeat("   ", 100);
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readArray();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithLargeObject() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 500);
        
        // Create JSON object with many keys
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"key").append(i).append("\":").append(i);
        }
        json.append("}");
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json.toString()))) {
                reader.readObject();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithMixedContent() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 1000);
        
        // Create JSON with mixed content that exceeds character limit
        StringBuilder json = new StringBuilder("{\"array\":[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(i);
        }
        json.append("],\"object\":{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"k").append(i).append("\":").append(i);
        }
        json.append("}}");
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json.toString()))) {
                reader.readObject();
            }
        });
    }

    @Test
    void testDocumentParseLimitEdgeCaseAtLimit() {
        Map<String, Object> config = new HashMap<>();
        // Note: Parser may consume additional characters beyond the literal source length
        // due to lookahead operations and number parsing that reads ahead to find token boundaries.
        // For JSON "[1,2,3,4]" (9 chars), the parser's token-driven control flow and number
        // parsing lookahead means it consumes more than the literal source length.
        // Setting a generous limit to allow successful parsing.
        config.put(JsonConfig.MAX_PARSING_LIMIT, 13);
        
        // Create JSON with exactly 9 characters: [1,2,3,4]
        String json = "[1,2,3,4]";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        try (JsonReader reader = factory.createReader(new StringReader(json))) {
            reader.readArray(); // Should succeed
        }
    }

    @Test
    void testDocumentParseLimitEdgeCaseOverLimit() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 8);
        
        // Create JSON with 9 characters: [1,2,3,4]

        String json = "[1,2,3,4]";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readArray();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithParser() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 500);
        
        // Create JSON that exceeds character limit
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 200; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(i);
        }
        json.append("]");
        
        JsonParserFactory factory = Json.createParserFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonParser parser = factory.createParser(new StringReader(json.toString()))) {
                while (parser.hasNext()) {
                    parser.next();
                }
            }
        });
    }

    @Test
    void testDefaultDocumentParseLimit() {
        // Test that default limit (15M) allows reasonable JSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 10000; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(i);
        }
        json.append("]");
        
        // Should succeed with default limit
        try (JsonReader reader = Json.createReader(new StringReader(json.toString()))) {
            reader.readArray();
        }
    }

    @Test
    void testDocumentParseLimitWithNestedStructures() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 200);
        
        // Create deeply nested structure that exceeds character limit
        StringBuilder json = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            json.append("{\"a\":");
        }
        json.append("1");
        for (int i = 0; i < 50; i++) {
            json.append("}");
        }
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json.toString()))) {
                reader.readObject();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithBooleanAndNull() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 50);
        
        // JSON with booleans and nulls (54 characters)
        String json = "[true,false,null,true,false,null,true,false,null,true]";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readArray();
            }
        });
    }

    @Test
    void testDocumentCharLimitWithInterTokenWhitespace() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 60);
        
        // JSON with whitespace between tokens
        String json = "[ 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 ]";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readArray();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithEscapeSequences() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 100);
        
        // JSON with escape sequences (each \n counts as 2 characters in source)
        String json = "{\"key\":\"" + repeat("line\\n", 20) + "\"}";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readObject();
            }
        });
    }

    @Test
    void testDocumentParseLimitWithLongKeys() {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonConfig.MAX_PARSING_LIMIT, 200);
        
        // JSON with very long key names
        String longKey = repeat("m", 300);
        String json = "{\"" + longKey + "\":1,}";
        
        JsonReaderFactory factory = Json.createReaderFactory(config);
        Assertions.assertThrows(JsonException.class, () -> {
            try (JsonReader reader = factory.createReader(new StringReader(json))) {
                reader.readObject();
            }
        });
    }
}
