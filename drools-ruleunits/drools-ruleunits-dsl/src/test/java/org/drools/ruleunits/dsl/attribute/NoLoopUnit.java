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

import java.util.ArrayList;
import java.util.List;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.dsl.RuleUnitDefinition;
import org.drools.ruleunits.dsl.RulesFactory;
import org.drools.ruleunits.dsl.domain.Person;

import static org.drools.model.Index.ConstraintType.GREATER_OR_EQUAL;

public class NoLoopUnit implements RuleUnitDefinition {

    private final DataStore<Person> persons;
    private final List<String> results = new ArrayList<>();

    public NoLoopUnit() {
        this(DataSource.createStore());
    }

    public NoLoopUnit(DataStore<Person> persons) {
        this.persons = persons;
    }

    public DataStore<Person> getPersons() {
        return persons;
    }

    public List<String> getResults() {
        return results;
    }

    @Override
    public void defineRules(RulesFactory rulesFactory) {
        rulesFactory.rule("NoLoopRule")
                    .noLoop()
                    .on(persons)
                    .filter("age", p -> p.getAge(), GREATER_OR_EQUAL, 18)
                    .executeOnDataStore(persons, (ps, p) -> {
                        results.add("fired-" + p.getAge());
                        p.setAge(p.getAge() + 1);
                        ps.update(p, "age");
                    });
    }
}
