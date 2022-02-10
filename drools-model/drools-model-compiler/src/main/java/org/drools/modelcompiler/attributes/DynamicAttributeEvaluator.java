/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.modelcompiler.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.drools.core.reteoo.RuleTerminalNode;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Tuple;
import org.drools.model.DynamicValueSupplier;
import org.drools.model.Variable;
import org.kie.api.definition.rule.Rule;

public class DynamicAttributeEvaluator<T> {

    protected final DynamicValueSupplier<T> supplier;

    public static final String ATTR_SCOPE_SUFFIX = "_attr_sCoPe";
    public static final String RULE_ATTR_SCOPE = "rule" + ATTR_SCOPE_SUFFIX;

    public DynamicAttributeEvaluator(DynamicValueSupplier<T> supplier) {
        this.supplier = supplier;
    }

    protected Declaration[] getDeclarations(Tuple tuple, Variable[] variables) {
        Declaration[] declarations = new Declaration[variables.length];
        Declaration[] allDeclarations = ((RuleTerminalNode) tuple.getTupleSink()).getAllDeclarations();
        for (int i = 0; i < variables.length; i++) {
            for (Declaration d : allDeclarations) {
                if (d.getIdentifier().equals(variables[i].getName())) {
                    declarations[i] = d;
                    break;
                }
            }
        }
        return declarations;
    }

    protected int getAttributeScopedRuleVariableIndex() {
        return IntStream.range(0, supplier.getVariables().length)
                .filter(i -> supplier.getVariables()[i].getName().equals(RULE_ATTR_SCOPE))
                .findFirst()
                .orElse(-1);
    }

    protected static Variable[] excludeAttributeScopedRuleVariable(Variable[] variables) {
        return Arrays.stream(variables).filter(v -> !v.getName().equals(RULE_ATTR_SCOPE)).toArray(Variable[]::new);
    }
}
