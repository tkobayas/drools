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
package org.drools.ruleunits.dsl.attribute;

import org.drools.core.common.ReteEvaluator;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.dsl.domain.Person;
import org.drools.ruleunits.impl.AbstractRuleUnitInstance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleUnitsAttributesTest {

    @Test
    public void timer() {
        // incubator-kie#6911: RuleUnit DSL does not apply RuleConfig clock type, so pseudo clock cannot be used here.
        // When fixed, use pseudo clock to advance time and verify the timer rule fires after the delay.
        TimerUnit unit = new TimerUnit();
        unit.getStrings().add("Hello Timer");

        try (RuleUnitInstance<TimerUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            assertThat(unitInstance.fire()).isZero();
            assertThat(unit.getResults()).isEmpty();
        }
    }

    @Test
    public void calendars() {
        // incubator-kie#6911: RuleUnit DSL does not apply RuleConfig clock type, so pseudo clock cannot be used here.
        // When fixed, use pseudo clock to also test calendar + timer combination.
        CalendarsUnit unit = new CalendarsUnit();

        try (RuleUnitInstance<CalendarsUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            ReteEvaluator evaluator = (ReteEvaluator) ((AbstractRuleUnitInstance) unitInstance).getEvaluator();
            evaluator.getCalendars().set("myCalendar", timestamp -> false);

            unit.getStrings().add("Hello World");
            assertThat(unitInstance.fire()).isZero();
            assertThat(unit.getResults()).isEmpty();
        }
    }

    @Test
    public void dateEffectiveAndDateExpires() {
        DateAttributeUnit unit = new DateAttributeUnit();
        unit.getStrings().add("Hello World");

        try (RuleUnitInstance<DateAttributeUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("active");
        }
    }

    @Test
    public void activationGroup() {
        ActivationGroupUnit unit = new ActivationGroupUnit();
        unit.getStrings().add("Hello World");

        try (RuleUnitInstance<ActivationGroupUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("groupHigh");
        }
    }

    @Test
    public void enabled() {
        EnabledUnit unit = new EnabledUnit();
        unit.getStrings().add("Hello World");

        try (RuleUnitInstance<EnabledUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("enabled");
        }
    }

    @Test
    public void noLoop() {
        NoLoopUnit unit = new NoLoopUnit();
        unit.getPersons().add(new Person("Mario", 40));

        try (RuleUnitInstance<NoLoopUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("fired-40");
        }
    }

    @Test
    public void salience() {
        SalienceUnit unit = new SalienceUnit();
        unit.getStrings().add("Hello World");

        try (RuleUnitInstance<SalienceUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            assertThat(unitInstance.fire()).isEqualTo(2);
            assertThat(unit.getResults()).containsExactly("high", "low");
        }
    }
}
