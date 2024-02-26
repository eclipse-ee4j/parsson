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

package org.eclipse.parsson.tests;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests JsonParserFactory
 *
 * @author Jitendra Kotamraju
 */
public class JsonParserFactoryTest {

    @Test
    void testParserFactory() {
        JsonParserFactory parserFactory = Json.createParserFactory(null);
        JsonParser parser1 = parserFactory.createParser(new StringReader("[]"));
        parser1.close();
        JsonParser parser2 = parserFactory.createParser(new StringReader("[]"));
        parser2.close();
    }

    @Test
    void testParserFactoryWithConfig() {
        Map<String, ?> config = new HashMap<>();
        JsonParserFactory parserFactory = Json.createParserFactory(config);
        JsonParser parser1 = parserFactory.createParser(new StringReader("[]"));
        parser1.close();
        JsonParser parser2 = parserFactory.createParser(new StringReader("[]"));
        parser2.close();
    }

}
