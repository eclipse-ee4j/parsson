/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.parsson.api;

public interface JsonConfig {

    /**
     * Configuration property to limit maximum value of BigInteger scale value.
     * This property limits maximum value of scale value to be allowed
     * in {@link jakarta.json.JsonNumber#bigIntegerValue()}
     * and {@link jakarta.json.JsonNumber#bigIntegerValueExact()} implemented methods.
     * Default value is set to {@code 100000}.
     */
    String MAX_BIGINT_SCALE = "org.eclipse.parsson.maxBigIntegerScale";

    /**
     * Configuration property to reject duplicate keys.
     * The value of the property could be anything.
     *
     * @deprecated in favor of {@link jakarta.json.JsonConfig#KEY_STRATEGY}
     */
    @Deprecated
    String REJECT_DUPLICATE_KEYS = "org.eclipse.parsson.rejectDuplicateKeys";

}
