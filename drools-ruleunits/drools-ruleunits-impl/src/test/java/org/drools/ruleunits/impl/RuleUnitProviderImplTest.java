/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.drools.ruleunits.impl;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import org.drools.ruleunits.api.DataHandle;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.impl.listener.TestAgendaEventListener;
import org.drools.ruleunits.impl.listener.TestRuleEventListener;
import org.drools.ruleunits.impl.listener.TestRuleRuntimeEventListener;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.junit.jupiter.api.Test;
import org.kie.api.builder.CompilationErrorsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RuleUnitProviderImplTest {

    @Test
    public void testHelloWorldGenerated() {
        HelloWorld unit = new HelloWorld();
        unit.getStrings().add("Hello World");

        try ( RuleUnitInstance<HelloWorld> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit) ) {
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("it worked!");
        }
    }

    @Test
    public void testNotWithAndWithoutSingleQuote() {
        NotTestUnit unit = new NotTestUnit();

        try ( RuleUnitInstance<NotTestUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit) ) {
            assertThat(unitInstance.fire()).isEqualTo(2);
        }
    }

    @Test
    public void testLogicalAdd() {
        // KOGITO-6466
        LogicalAddTestUnit unit = new LogicalAddTestUnit();

        try ( RuleUnitInstance<LogicalAddTestUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit) ) {

            DataHandle dh = unit.getStrings().add("abc");

            assertThat(unitInstance.fire()).isEqualTo(2);
            assertThat(unit.getResults()).containsExactly("exists");

            unit.getResults().clear();

            unit.getStrings().remove(dh);
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("not exists");
        }
    }

    @Test
    public void testUpdate() {
        UpdateTestUnit unit = new UpdateTestUnit();

        try ( RuleUnitInstance<UpdateTestUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit) ) {

            unit.getPersons().add(new Person("Mario", 17));

            assertThat(unitInstance.fire()).isEqualTo(2);
            assertThat(unit.getResults()).containsExactly("ok");
        }
    }

    @Test
    public void testWrongType() {
        try {
            RuleUnitProvider.get().createRuleUnitInstance(new WronglyTypedUnit());
            fail("Compilation should fail");
        } catch (CompilationErrorsException e) {
            assertThat(
                    e.getErrorMessages().stream().map(Objects::toString)
                            .anyMatch( s -> s.contains("The method add(Integer) in the type DataStore<Integer> is not applicable for the arguments (String)"))
            ).isTrue();
        }
    }

    @Test
    public void addEventListeners() {
        List<EventListener> eventListerList = new ArrayList<>();
        TestAgendaEventListener testAgendaEventListener = new TestAgendaEventListener();
        TestRuleRuntimeEventListener testRuleRuntimeEventListener = new TestRuleRuntimeEventListener();
        TestRuleEventListener testRuleEventListener = new TestRuleEventListener();
        eventListerList.add(testAgendaEventListener);
        eventListerList.add(testRuleRuntimeEventListener);
        eventListerList.add(testRuleEventListener);

        HelloWorld unit = new HelloWorld();
        unit.getStrings().add("Hello World");

        try (RuleUnitInstance<HelloWorld> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit, eventListerList)) {
            assertThat(unitInstance.fire()).isEqualTo(1);
            assertThat(unit.getResults()).containsExactly("it worked!");
            assertThat(testAgendaEventListener.getResults()).containsExactly("matchCreated : HelloWorld", "beforeMatchFired : HelloWorld", "afterMatchFired : HelloWorld");
            assertThat(testRuleRuntimeEventListener.getResults()).containsExactly("objectInserted : Hello World");
            assertThat(testRuleEventListener.getResults()).containsExactly("onBeforeMatchFire : HelloWorld", "onAfterMatchFire : HelloWorld");
        }
    }
}
