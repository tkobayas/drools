/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.core.addon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.rule.Accumulate;
import org.drools.core.rule.Pattern;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.kie.api.definition.rule.Rule;

/**
 * 
 * Reorder based on usage count of alpha constraints
 *
 */
public class CountBasedOrderingStrategy implements AlphaNodeOrderingStrategy {

    private Map<AlphaNodeFieldConstraint, Integer> analyzedAlphaConstraints = new HashMap<>();

    @Override
    public void analyzeAlphaConstraints(Set<Rule> ruleSet) {
        // Pattern
        ruleSet.stream()
               .flatMap(rule -> ((RuleImpl) rule).getLhs().getChildren().stream())
               .filter(Pattern.class::isInstance)
               .map(Pattern.class::cast)
               .flatMap(pattern -> pattern.getConstraints().stream())
               .filter(AlphaNodeFieldConstraint.class::isInstance)
               .map(AlphaNodeFieldConstraint.class::cast)
               .forEach(constraint -> analyzedAlphaConstraints.merge(constraint, 1, (count, newValue) -> count + 1));

        // Accumulate
        ruleSet.stream()
               .flatMap(rule -> ((RuleImpl) rule).getLhs().getChildren().stream())
               .filter(Pattern.class::isInstance)
               .map(Pattern.class::cast)
               .map(Pattern::getSource)
               .filter(Accumulate.class::isInstance)
               .map(Accumulate.class::cast)
               .map(Accumulate::getSource)
               .filter(Pattern.class::isInstance)
               .map(Pattern.class::cast)
               .flatMap(pattern -> pattern.getConstraints().stream())
               .filter(AlphaNodeFieldConstraint.class::isInstance)
               .map(AlphaNodeFieldConstraint.class::cast)
               .forEach(constraint -> analyzedAlphaConstraints.merge(constraint, 1, (count, newValue) -> count + 1));

        // TODO: Other cases

        System.out.println("analyzedAlphaConstraints : " + analyzedAlphaConstraints);
    }

    @Override
    public void reorderAlphaConstraints(List<AlphaNodeFieldConstraint> alphaConstraints) {
        // greater usage count is earlier
        System.out.println("** before alphaConstraints : " + alphaConstraints);
        alphaConstraints.sort((constraint1, constraint2) -> {
            Integer count1 = analyzedAlphaConstraints.getOrDefault(constraint1, 1);
            Integer count2 = analyzedAlphaConstraints.getOrDefault(constraint2, 1);
            return count2.compareTo(count1);
        });
        System.out.println(" ** after alphaConstraints : " + alphaConstraints);
    }

}