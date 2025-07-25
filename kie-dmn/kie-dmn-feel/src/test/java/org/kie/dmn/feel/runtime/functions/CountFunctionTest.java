/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.feel.runtime.functions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.dmn.feel.runtime.events.InvalidParametersEvent;

class CountFunctionTest {

    private static final CountFunction countFunction = CountFunction.INSTANCE;

    @Test
    void invokeParamListNull() {
        FunctionTestUtil.assertResultError(countFunction.invoke((List) null), InvalidParametersEvent.class);
    }

    @Test
    void invokeParamListEmpty() {
        FunctionTestUtil.assertResult(countFunction.invoke(Collections.emptyList()), BigDecimal.ZERO);
    }

    @Test
    void invokeParamListNonEmpty() {
        FunctionTestUtil.assertResult(countFunction.invoke(Arrays.asList(1, 2, "test")), BigDecimal.valueOf(3));
    }

    @Test
    void invokeParamArrayNull() {
        FunctionTestUtil.assertResultError(countFunction.invoke((Object[]) null), InvalidParametersEvent.class);
    }

    @Test
    void invokeParamArrayEmpty() {
        FunctionTestUtil.assertResult(countFunction.invoke(new Object[]{}), BigDecimal.ZERO);
    }

    @Test
    void invokeParamArrayNonEmpty() {
        FunctionTestUtil.assertResult(countFunction.invoke(new Object[]{1, 2, "test"}), BigDecimal.valueOf(3));
    }
}