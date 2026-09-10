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
package org.drools.drl.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HasTopLevelTernaryExpressionTest {

    @Test
    void ternaryOperator() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("\"foo\" == \"foo\" ? \"foo\" == flag1 : \"foo\" == flag2")).isTrue();
    }

    @Test
    void simpleTernary() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("x > 0 ? y : z")).isTrue();
    }

    @Test
    void noTernary() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("age > 10")).isFalse();
    }

    @Test
    void equalityExpression() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("length == 4")).isFalse();
    }

    @Test
    void nullSafeOperator() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("address?.city == \"London\"")).isFalse();
    }

    @Test
    void questionMarkInsideStringLiteral() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("name == \"what?\"")).isFalse();
    }

    @Test
    void escapedQuoteBeforeTernary() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("\"val\\\"ue\" == flag ? x : y")).isTrue();
    }

    @Test
    void questionMarkInsideStringWithEscapedQuote() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("name == \"is\\\"this?real\"")).isFalse();
    }

    @Test
    void singleQuotedStringWithQuestionMark() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("name == 'what?'")).isFalse();
    }

    @Test
    void questionMarkWithoutColon() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("x?")).isFalse();
    }

    @Test
    void colonInsideStringAfterQuestionMark() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("x ? \":\"")).isFalse();
    }

    @Test
    void emptyExpression() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("")).isFalse();
    }

    @Test
    void complexTernaryWithLogicalOperators() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("a > 0 && b < 10 ? c : d")).isTrue();
    }

    @Test
    void nullSafeChain() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("person?.address?.city == \"London\"")).isFalse();
    }

    @Test
    void parenthesizedTernaryConsumedByParser() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("(x ? y : z)")).isFalse();
    }

    @Test
    void parenthesizedTernaryInEquality() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("(x ? y : z) == true")).isFalse();
    }

    @Test
    void nestedTernary() {
        assertThat(Drl6ExprParser.hasTopLevelTernaryExpression("x ? y ? a : b : z")).isTrue();
    }
}
