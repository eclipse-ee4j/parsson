/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.stream.JsonParser;

import org.junit.jupiter.api.Test;

/**
 * JsonParser tests for sample files
 *
 * @author Jitendra Kotamraju
 */
public class JsonSamplesParsingTest {

    @Test
    void testSampleFiles() {
        String[] fileNames = {
                "facebook.json", "facebook1.json", "facebook2.json",
                "twitter.json"
        };
        for(String fileName: fileNames) {
            try {
                testSampleFile(fileName);
            } catch(Exception e) {
                throw new JsonException("Exception while parsing "+fileName, e);
            }
        }
    }

    private void testSampleFile(String fileName) {
        Reader reader = new InputStreamReader(
				Objects.requireNonNull(JsonSamplesParsingTest.class.getResourceAsStream("/" + fileName)), StandardCharsets.UTF_8);
		try (JsonParser parser = Json.createParser(reader)) {
			while (parser.hasNext()) {
				parser.next();
			}
		}
    }

}
