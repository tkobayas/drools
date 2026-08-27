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

import static org.drools.model.Index.ConstraintType.EQUAL;

public class DateAttributeUnit implements RuleUnitDefinition {

    private final DataStore<String> strings;
    private final List<String> results = new ArrayList<>();

    public DateAttributeUnit() {
        this(DataSource.createStore());
    }

    public DateAttributeUnit(DataStore<String> strings) {
        this.strings = strings;
    }

    public DataStore<String> getStrings() {
        return strings;
    }

    public List<String> getResults() {
        return results;
    }

    @Override
    public void defineRules(RulesFactory rulesFactory) {
        rulesFactory.rule("ExpiredRule")
                    .dateEffective("1-Jan-2020")
                    .dateExpires("1-Jan-2021")
                    .on(strings)
                    .filter(EQUAL, "Hello World")
                    .execute(results, r -> r.add("expired"));

        rulesFactory.rule("ActiveRule")
                    .dateEffective("1-Jan-2020")
                    .dateExpires("1-Jan-2099")
                    .on(strings)
                    .filter(EQUAL, "Hello World")
                    .execute(results, r -> r.add("active"));

        rulesFactory.rule("NotYetEffectiveRule")
                    .dateEffective("1-Jan-2099")
                    .dateExpires("1-Jan-2100")
                    .on(strings)
                    .filter(EQUAL, "Hello World")
                    .execute(results, r -> r.add("notYetEffective"));
    }
}
