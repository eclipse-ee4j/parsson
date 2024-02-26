/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package customprovider.test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

import org.eclipse.parsson.demos.customprovider.TestGenerator;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lukas
 */
public class TestProviderTest {

    @Test
    void hello() {
        try (JsonGenerator generator = Json.createGenerator(System.out)) {
            assertInstanceOf(TestGenerator.class, generator, "TestGenerator is not picked up");
            generator.writeStartArray().writeEnd();
        }
        System.out.println();
        System.out.println("Hurray!!!");
    }
}
