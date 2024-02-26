/*
 * Copyright (c) 2015, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

import jakarta.json.Json;
import jakarta.json.JsonPointer;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * JSON pointer toString tests.
 *
 * @author leadpony
 */
public class JsonPointerToStringTest {

    public static Iterable<Object> data() {
        return Arrays.asList("", "/", "/one/two/3", "/a~1b", "/m~0n");
    }

    @MethodSource("data")
    @ParameterizedTest(name = "{index}: {0}")
    void shouldReturnOriginalEscapedString(String expected) {
        JsonPointer pointer = Json.createPointer(expected);
        MatcherAssert.assertThat(pointer.toString(), CoreMatchers.is(CoreMatchers.equalTo(expected)));
    }
}
