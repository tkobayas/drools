/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.verifier.core.relations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OperatorTest {

    @Test
    void testOperators() throws Exception {
        assertThat(Operator.resolve("==")).isEqualTo(Operator.EQUALS);
        assertThat(Operator.resolve(">")).isEqualTo(Operator.GREATER_THAN);
        assertThat(Operator.resolve("<")).isEqualTo(Operator.LESS_THAN);
        assertThat(Operator.resolve(">=")).isEqualTo(Operator.GREATER_OR_EQUAL);
        assertThat(Operator.resolve("<=")).isEqualTo(Operator.LESS_OR_EQUAL);
        assertThat(Operator.resolve("!=")).isEqualTo(Operator.NOT_EQUALS);
        assertThat(Operator.resolve("in")).isEqualTo(Operator.IN);
        assertThat(Operator.resolve("not in")).isEqualTo(Operator.NOT_IN);
        assertThat(Operator.resolve("after")).isEqualTo(Operator.AFTER);
        assertThat(Operator.resolve("before")).isEqualTo(Operator.BEFORE);
        assertThat(Operator.resolve("coincides")).isEqualTo(Operator.COINCIDES);
        assertThat(Operator.resolve("matches")).isEqualTo(Operator.MATCHES);
        assertThat(Operator.resolve("soundslike")).isEqualTo(Operator.SOUNDSLIKE);

    }
}