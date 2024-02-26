/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStream;
import java.net.URL;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.junit.jupiter.api.Test;

/**
 * JsonParser Tests using twitter search API
 *
 * @author Jitendra Kotamraju
 */
public class TwitterSearchTest {

    @Test
    void test() {
        // dummy test so that junit doesn't complain
    }

    void xtestStreamTwitter() throws Exception {
        URL url = new URL("http://search.twitter.com/search.json?q=%23java&rpp=100");
        InputStream is = url.openStream();
        JsonParser parser = Json.createParser(is);

        while(parser.hasNext()) {
            Event e = parser.next();
            if (e == Event.KEY_NAME) {
                if (parser.getString().equals("from_user")) {
                    parser.next();
                    System.out.print(parser.getString());
                    System.out.print(": ");
                } else if (parser.getString().equals("text")) {
                    parser.next();
                    System.out.println(parser.getString());
                    System.out.println("---------");
                }
            }
        }
        parser.close();
	}

    void xtestObjectTwitter() throws Exception {
        URL url = new URL("http://search.twitter.com/search.json?q=%23java&rpp=100");
        InputStream is = url.openStream();
        JsonReader rdr = Json.createReader(is);
        JsonObject obj = rdr.readObject();
        JsonArray results = obj.getJsonArray("results");
        for(JsonObject result : results.getValuesAs(JsonObject.class)) {
            System.out.print(result.get("from_user"));
            System.out.print(": ");
            System.out.println(result.get("text"));
            System.out.println("-----------");
        }
        rdr.close();
    }

}
