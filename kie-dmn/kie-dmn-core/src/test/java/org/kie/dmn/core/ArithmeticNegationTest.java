/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.kie.dmn.feel.lang.types.impl.ComparablePeriod;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArithmeticNegationTest extends BaseInterpretedVsCompiledTest {

    public ArithmeticNegationTest(final boolean useExecModelCompiler) {
        super(useExecModelCompiler);
    }

    @Test
    public void testDecision_001() {
        assertThat(executeTest("decision_001"), is(BigDecimal.valueOf(-10)));
    }

    @Test
    public void testDecision_002() {
        assertThat(executeTest("decision_002"), is(BigDecimal.valueOf(10)));
    }

    @Test
    public void testDecision_003() {
        assertThat(executeTest("decision_003"), is(Duration.parse("-P1D")));
    }

    @Test
    public void testDecision_003_a() {
        assertThat(executeTest("decision_003_a"), is(Duration.parse("P1D")));
    }

    @Test
    public void testDecision_004() {
        assertThat(executeTest("decision_004"), is(ComparablePeriod.parse("-P1Y")));
    }

    @Test
    public void testDecision_004_a() {
        assertThat(executeTest("decision_004_a"), is(ComparablePeriod.parse("P1Y")));
    }

    @Test
    public void testDecision_005() {
        assertThat(executeTest("decision_005"), nullValue());
    }

    @Test
    public void testDecision_006() {
        assertThat(executeTest("decision_006"), nullValue());
    }

    @Test
    public void testDecision_007() {
        assertThat(executeTest("decision_007"), nullValue());
    }

    @Test
    public void testDecision_008() {
        assertThat(executeTest("decision_008"), nullValue());
    }

    @Test
    public void testDecision_009() {
        assertThat(executeTest("decision_009"), nullValue());
    }

    @Test
    public void testDecision_010() {
        assertThat(executeTest("decision_010"), nullValue());
    }

    @Test
    public void testDecision_011() {
        assertThat(executeTest("decision_011"), nullValue());
    }

    @Test
    public void testDecision_012() {
        assertThat(executeTest("decision_012"), is(BigDecimal.valueOf(-10)));
    }

    private Object executeTest(final String decisionName) {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntime("0099-arithmetic-negation.dmn", this.getClass());
        final DMNModel dmnModel = runtime.getModel("http://www.montera.com.au/spec/DMN/0099-arithmetic-negation", "0099-arithmetic-negation");
        assertThat(dmnModel, notNullValue());

        final DMNContext context = DMNFactory.newContext();
        final DMNResult dmnResult = runtime.evaluateByName(dmnModel, context, decisionName);
        final DMNContext result = dmnResult.getContext();
        return result.get(decisionName);
    }
}
