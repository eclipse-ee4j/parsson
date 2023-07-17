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
     * Configuration property to limit maximum absolute value of BigInteger scale.
     * This property limits maximum absolute value of scale to be allowed
     * in {@link jakarta.json.JsonNumber#bigIntegerValue()}
     * and {@link jakarta.json.JsonNumber#bigIntegerValueExact()} implemented methods.
     * Default value is set to {@code 100000}.
     */
    String MAX_BIGINTEGER_SCALE = "org.eclipse.parsson.maxBigIntegerScale";

    /**
     * Configuration property to limit maximum value of BigDecimal length when being parsed.
     * This property limits maximum number of characters of BigDecimal source being parsed.
     * Default value is set to {@code 1100}.
     */
    String MAX_BIGDECIMAL_LEN = "org.eclipse.parsson.maxBigDecimalLength";

    /**
     * Configuration property to limit maximum level of nesting when being parsing JSON string.
     * Default value is set to {@code 1000}.
     */
    String MAX_DEPTH = "org.eclipse.parsson.maxDepth";

    /**
     * Configuration property to reject duplicate keys.
     * The value of the property could be anything.
     */
    String REJECT_DUPLICATE_KEYS = "org.eclipse.parsson.rejectDuplicateKeys";

}
