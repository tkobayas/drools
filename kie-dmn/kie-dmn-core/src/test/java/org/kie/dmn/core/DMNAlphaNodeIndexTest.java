/*
* Copyright 2020 Red Hat, Inc. and/or its affiliates.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.kie.dmn.core;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DMNAlphaNodeIndexTest extends BaseInterpretedVsCompiledTest {

    public static final Logger LOG = LoggerFactory.getLogger(DMNAlphaNodeIndexTest.class);

    public DMNAlphaNodeIndexTest(final boolean useExecModelCompiler) {
        super(useExecModelCompiler);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Object[] params() {
        return new Object[]{true};
    }

    @Test
    public void testRangeIndex() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntime("range-index-decision-table.dmn", this.getClass());

        final DMNModel dmnModel = runtime.getModel("https://kiegroup.org/dmn/_F436EBEE-F8F7-4AA3-BEB9-FE9B65617DEF", "range-index-decision-table");
        assertThat(dmnModel, notNullValue());

        final DMNContext context = DMNFactory.newContext();
        Map<String, Object> person = new HashMap<>();
        person.put("name", "John");
        person.put("age", 35);

        context.set("Person", person);

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat(dmnResult.hasErrors(), is(false));

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("Decision-1"), is("20 to 40"));

    }

    @Test
    public void testHashIndex() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntime("hash-index-decision-table.dmn", this.getClass());

        final DMNModel dmnModel = runtime.getModel("https://kiegroup.org/dmn/_F436EBEE-F8F7-4AA3-BEB9-FE9B65617DEF", "hash-index-decision-table");
        assertThat(dmnModel, notNullValue());

        final DMNContext context = DMNFactory.newContext();
        Map<String, Object> person = new HashMap<>();
        person.put("name", "John");
        person.put("age", 35);

        context.set("Person", person);

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat(dmnResult.hasErrors(), is(false));

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("Decision-1"), is("Universe"));

    }
}
