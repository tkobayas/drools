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

package org.drools.modelcompiler.addon;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.drools.core.addon.AlphaNodeOrderingStrategy;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.rule.Accumulate;
import org.drools.core.rule.Collect;
import org.drools.core.rule.Forall;
import org.drools.core.rule.GroupElement;
import org.drools.core.rule.Pattern;
import org.drools.core.rule.PatternSource;
import org.drools.core.rule.RuleConditionElement;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.ObjectType;
import org.drools.model.SingleConstraint;
import org.drools.model.constraints.SingleConstraint1;
import org.drools.model.functions.IntrospectableLambda;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.PredicateN;
import org.drools.modelcompiler.constraints.ConstraintEvaluator;
import org.drools.modelcompiler.constraints.LambdaConstraint;
import org.kie.api.definition.rule.Rule;
import org.mvel2.util.PropertyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Reorder based on usage count of alpha constraints
 *
 */
public class CanonicalCountBasedOrderingStrategy implements AlphaNodeOrderingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CanonicalCountBasedOrderingStrategy.class);

    private Map<ObjectType, Map<AlphaNodeFieldConstraint, Integer>> analyzedAlphaConstraints = new HashMap<>();

    private Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();

    private List<ObjectType> denyReorderList = new ArrayList<>();

    @Override
    public void analyzeAlphaConstraints(Map<String, InternalKnowledgePackage> pkgs, Collection<InternalKnowledgePackage> newPkgs) {

        Set<Rule> ruleSet = collectRules(pkgs, newPkgs);

        List<Pattern> patternList = new ArrayList<>();
        ruleSet.stream()
               .forEach(rule -> collectPatterns(((RuleImpl) rule).getLhs(), patternList));
        patternList.removeIf(Objects::isNull);

        // count usage per constraint
        for (Pattern pattern : patternList) {
            ObjectType objectType = pattern.getObjectType();
            Map<AlphaNodeFieldConstraint, Integer> analyzedAlphaConstraintsPerObjectType = analyzedAlphaConstraints.computeIfAbsent(objectType, key -> new HashMap<AlphaNodeFieldConstraint, Integer>());
            pattern.getConstraints()
                   .stream()
                   .filter(AlphaNodeFieldConstraint.class::isInstance)
                   .map(AlphaNodeFieldConstraint.class::cast)
                   .forEach(constraint -> analyzedAlphaConstraintsPerObjectType.merge(constraint, 1, (count, newValue) -> count + 1));
        }

        // store prioritized constraint (e.g. null-check) with affected properties
        for (ObjectType objectType : analyzedAlphaConstraints.keySet()) {
            Map<AlphaNodeFieldConstraint, Integer> analyzedAlphaConstraintsPerObjectType = analyzedAlphaConstraints.get(objectType);
            analyzedAlphaConstraintsPerObjectType.keySet()
                                                 .stream()
                                                 .forEach(alphaConstraint -> prioritizeConstraint(alphaConstraint, objectType));
        }

        logger.trace("analyzedAlphaConstraints : {}", analyzedAlphaConstraints);
        logger.trace(" ** prioritizedConstraints : {}", prioritizedConstraints);
    }

    private Set<Rule> collectRules(Map<String, InternalKnowledgePackage> pkgs, Collection<InternalKnowledgePackage> newPkgs) {
        Set<Rule> ruleSet = new HashSet<>();
        pkgs.forEach((pkgName, pkg) -> ruleSet.addAll(pkg.getRules()));
        newPkgs.forEach(pkg -> ruleSet.addAll(pkg.getRules())); // okay to overwrite
        return ruleSet;
    }

    private void collectPatterns(GroupElement ge, List<Pattern> patternList) {
        List<RuleConditionElement> children = ge.getChildren();
        for (RuleConditionElement child : children) {
            if (child instanceof Pattern) {
                Pattern pattern = (Pattern) child;
                patternList.add(pattern);
                PatternSource source = pattern.getSource();
                if (source instanceof Accumulate) {
                    RuleConditionElement accSource = ((Accumulate) source).getSource();
                    if (accSource instanceof Pattern) {
                        patternList.add((Pattern) accSource);
                    }
                } else if (source instanceof Collect) {
                    patternList.add(((Collect) source).getSourcePattern());
                } else {
                    // do nothing for null, From, EntryPointId, WindowReference
                }
            } else if (child instanceof GroupElement) {
                collectPatterns((GroupElement) child, patternList);
            } else if (child instanceof Forall) {
                Forall forall = (Forall) child;
                patternList.add(forall.getBasePattern());
                patternList.addAll(forall.getRemainingPatterns());
            } else {
                // do nothing for null, EvalCondition, ConditionalBranch, QueryElement, NamedConsequence
            }
        }
    }

    @Override
    public void reorderAlphaConstraints(List<AlphaNodeFieldConstraint> alphaConstraints, ObjectType objectType) {
        // greater usage count is earlier
        logger.trace("** before alphaConstraints : {}", alphaConstraints);
        System.out.println(" ** before alphaConstraints : " + alphaConstraints);

        Map<AlphaNodeFieldConstraint, Integer> analyzedAlphaConstraintsPerObjectType = analyzedAlphaConstraints.get(objectType);
        if (analyzedAlphaConstraintsPerObjectType == null) {
            logger.trace(" ** no analyzedAlphaConstraintsPerObjectType. alphaConstraints : {}", alphaConstraints);
            return;
        }
        if (denyReorderList.contains(objectType)) {
            logger.trace("  ** denyReorderList.contains objectType : {}", objectType);
            System.out.println("  ** denyReorderList.contains objectType : " + objectType);
            return;
        }

        Class<?> clazz = objectType.getClassType();

        alphaConstraints.sort((constraint1, constraint2) -> {

            int compare = compareBasedOnPrioritization(constraint1, constraint2, clazz);
            logger.trace("[{}] [{}] -> {}", constraint1, constraint2, compare);
            if (compare != 0) {
                return compare;
            }

            Integer count1 = analyzedAlphaConstraintsPerObjectType.getOrDefault(constraint1, 1);
            Integer count2 = analyzedAlphaConstraintsPerObjectType.getOrDefault(constraint2, 1);
            return count2.compareTo(count1);
        });
        logger.trace(" ** after alphaConstraints : {}", alphaConstraints);
        System.out.println(" ** after alphaConstraints : " + alphaConstraints);
    }

    private int compareBasedOnPrioritization(AlphaNodeFieldConstraint constraint1, AlphaNodeFieldConstraint constraint2, Class<?> clazz) {
        boolean prioritized1 = prioritizedConstraints.containsKey(constraint1);
        boolean prioritized2 = prioritizedConstraints.containsKey(constraint2);

        if (prioritized1 && prioritized2 || !prioritized1 && !prioritized2) {
            return 0; // use count
        }

        if (prioritized1) {
            List<String> propList1 = prioritizedConstraints.get(constraint1);
            List<String> propList2 = getCheckedProp(constraint2, clazz);
            if (propList1.contains("this") || propList1.isEmpty() || propList2.isEmpty() || !Collections.disjoint(propList1, propList2)) {
                return -1; // constraint1 first
            } else {
                return 0;
            }
        } else {
            List<String> propList2 = prioritizedConstraints.get(constraint2);
            List<String> propList1 = getCheckedProp(constraint1, clazz);
            if (propList2.contains("this") || propList2.isEmpty() || propList1.isEmpty() || !Collections.disjoint(propList2, propList1)) {
                return 1; // constraint2 first
            } else {
                return 0;
            }
        }
    }

    void prioritizeConstraint(AlphaNodeFieldConstraint alphaConstraint, ObjectType objectType) {
        if (denyReorderList.contains(objectType)) {
            // no need to process
            return;
        }
        String expression = getExpression(alphaConstraint);

        if (expression != null) {
            prioritizeConstraintWithExpression(alphaConstraint, expression, objectType);
        } else {
            // e.g. We cannot analyze non-externalized lambda. We shouldn't reorder at all for the objectType
            denyReorderList.add(objectType);
        }
    }

    private String getExpression(AlphaNodeFieldConstraint alphaConstraint) {
        if (alphaConstraint instanceof MvelConstraint) {
            return ((MvelConstraint) alphaConstraint).getExpression();
        } else if (alphaConstraint instanceof LambdaConstraint) {
            return ((LambdaConstraint) alphaConstraint).getOriginalDrlConstraint(); // non-externalized lambda returns null
        } else {
            // return null;
            throw new RuntimeException("DEBUG : alphaConstraint = " + alphaConstraint);
        }
    }

    private void prioritizeConstraintWithExpression(AlphaNodeFieldConstraint alphaConstraint, String expression, ObjectType objectType) {
        Class<?> clazz = objectType.getClassType();
        List<String> tokens = splitExpression(expression);

        if (isPotentialNullcheck(tokens)) {
            List<String> propList = collectPropsAffectedByNullCheck(tokens, clazz);
            if (propList.isEmpty()) {
                denyReorderList.add(objectType);
                return;
            } else {
                prioritizedConstraints.put(alphaConstraint, propList);
            }
        }
        if (isPotentialInstanceof(tokens)) {
            List<String> propList = collectPropsAffectedByInstanceof(tokens, clazz);
            if (propList.isEmpty()) {
                denyReorderList.add(objectType);
                return;
            } else {
                prioritizedConstraints.put(alphaConstraint, propList);
            }
        }
    }

    private boolean isPotentialNullcheck(List<String> tokens) {
        return tokens.contains("null");
    }

    private boolean isPotentialInstanceof(List<String> tokens) {
        return tokens.contains("instanceof");
    }

    private List<String> collectPropsAffectedByNullCheck(List<String> tokens, Class<?> clazz) {
        // Currently, the size of the list is always one at most. However, it could be enhanced
        List<String> propList = new ArrayList<>();
        int size = tokens.size();
        if (size == 3 && tokens.get(1).equals("!=") && tokens.get(2).equals("null")) {
            // address != null
            extractProp(tokens.get(0), clazz).ifPresent(propList::add);
        } else if (size == 5 && tokens.get(0).equals("!(") && tokens.get(2).equals("==") && tokens.get(3).equals("null") && tokens.get(4).equals(")")) {
            // !( address == null )
            extractProp(tokens.get(1), clazz).ifPresent(propList::add);
        } else if (size == 6 && tokens.get(0).equals("!") && tokens.get(1).equals("(") && tokens.get(3).equals("==") && tokens.get(4).equals("null") && tokens.get(5).equals(")")) {
            // ! ( address == null )
            extractProp(tokens.get(2), clazz).ifPresent(propList::add);
        } else {
            // Don't parse other cases
        }
        return propList;
    }

    private List<String> collectPropsAffectedByInstanceof(List<String> tokens, Class<?> clazz) {
        List<String> propList = new ArrayList<>();
        int size = tokens.size();
        if (size == 3 && tokens.get(1).equals("instanceof")) {
            // address instanceof Address
            extractProp(tokens.get(0), clazz).ifPresent(propList::add);
        } else if (size == 5 && tokens.get(0).equals("!(") && tokens.get(2).equals("instanceof") && tokens.get(4).equals(")")) {
            // !( address instanceof Address )
            extractProp(tokens.get(1), clazz).ifPresent(propList::add);
        } else if (size == 6 && tokens.get(0).equals("!") && tokens.get(1).equals("(") && tokens.get(3).equals("instanceof") && tokens.get(5).equals(")")) {
            // ! ( address instanceof Address )
            extractProp(tokens.get(2), clazz).ifPresent(propList::add);
        } else {
            // Don't parse other cases
        }
        return propList;
    }

    private Optional<String> extractProp(String target, Class<?> clazz) {
        if (target.equals("this")) {
            return Optional.of(target);
        }
        if (target.startsWith("this.")) {
            target = target.substring(5);
        }
        if (!target.contains(".")) {
            String prop = MvelConstraint.getPropertyNameFromSimpleExpression(target);
            if (!prop.isEmpty() && PropertyTools.getFieldOrAccessor(clazz, prop) != null) {
                return Optional.of(prop);
            } else {
                return Optional.empty();
            }
        } else {
            StringBuilder normalizedPropExpression = new StringBuilder();
            if (nestedPropExists(clazz, target, normalizedPropExpression)) {
                return Optional.of(normalizedPropExpression.toString());
            } else {
                return Optional.empty();
            }
        }
    }

    private boolean nestedPropExists(Class<?> clazz, String propString, StringBuilder normalizedPropExpression) {
        Class<?> type;
        String firstPropString;
        int firstDot = propString.indexOf('.');
        if (firstDot == -1) {
            firstPropString = propString;
        } else {
            firstPropString = propString.substring(0, firstDot);
        }
        String firstProp = MvelConstraint.getPropertyNameFromSimpleExpression(firstPropString);
        normalizedPropExpression.append(firstProp);
        if (firstProp.isEmpty()) {
            return false;
        }
        Member fieldOrAccessor = PropertyTools.getFieldOrAccessor(clazz, firstProp);
        if (fieldOrAccessor instanceof Field) {
            Field field = (Field) fieldOrAccessor;
            type = field.getType();
        } else if (fieldOrAccessor instanceof Method) {
            Method method = (Method) fieldOrAccessor;
            type = method.getReturnType();
        } else {
            return false;
        }
        if (firstDot == -1) {
            return true;
        } else {
            normalizedPropExpression.append(".");
            return nestedPropExists(type, propString.substring(firstDot + 1), normalizedPropExpression);
        }
    }

    List<String> getCheckedProp(AlphaNodeFieldConstraint alphaConstraint, Class<?> clazz) {
        List<String> propList = new ArrayList<>();
        String expression = getExpression(alphaConstraint);
        List<String> tokens = splitExpression(expression);
        for (String token : tokens) {
            Optional<String> optProp = extractProp(token, clazz);
            if (optProp.isPresent()) {
                addNestedPropChain(optProp.get(), propList);
            }
        }
        return propList;
    }

    private void addNestedPropChain(String propStr, List<String> propList) {
        propList.add(propStr);
        int lastIndex = propStr.lastIndexOf('.');
        if (lastIndex != -1) {
            addNestedPropChain(propStr.substring(0, lastIndex), propList);
        }
    }

    /**
     * Split expression into tokens including java identifiers and non java identifiers. Ignores quoted string
     */
    static List<String> splitExpression(String expression) {
        List<String> tokens = new ArrayList<>();
        int lastStart = 0;
        boolean isQuoted = false;
        boolean isIdentifier = false;
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (!isIdentifier) {
                if (!isQuoted && Character.isJavaIdentifierStart(ch)) {
                    collectTokensUpHere(expression, tokens, lastStart, i);
                    lastStart = i;
                    isIdentifier = true;
                }
            } else if (!Character.isJavaIdentifierPart(ch)) {
                // java identifier
                tokens.add(expression.subSequence(lastStart, i).toString().trim());
                lastStart = i;
                isIdentifier = false;
            }
            if (ch == '"' || ch == '\'') {
                if (i == 0 || expression.charAt(i - 1) != '\\') {
                    isQuoted = !isQuoted;
                }
                if (isQuoted) {
                    collectTokensUpHere(expression, tokens, lastStart, i);
                    lastStart = i;
                } else {
                    // quoted string
                    tokens.add(expression.subSequence(lastStart, i + 1).toString());
                    lastStart = i + 1;
                }
            }
        }
        if (lastStart < expression.length()) {
            collectTokensUpHere(expression, tokens, lastStart, expression.length());
        }

        tokens.removeIf(String::isEmpty);

        // concat java identifiers with "." and "()"
        List<String> newTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (Character.isJavaIdentifierStart(token.charAt(0))) {
                LastToken last = LastToken.ID;
                boolean concatEnd = false;
                int j = i + 1;
                for (; j < tokens.size(); j++) {
                    String nextToken = tokens.get(j);
                    switch (last) {
                        case ID:
                            if (nextToken.equals(".") || nextToken.equals("().")) {
                                token = token.concat(nextToken);
                                last = LastToken.DOT;
                            } else if (nextToken.equals("()")) {
                                token = token.concat(nextToken);
                                last = LastToken.PARENTHESES;
                            } else {
                                concatEnd = true;
                            }
                            break;
                        case DOT:
                            if (Character.isJavaIdentifierStart(nextToken.charAt(0))) {
                                token = token.concat(nextToken);
                                last = LastToken.ID;
                            } else {
                                concatEnd = true;
                            }
                            break;
                        case PARENTHESES:
                            if (nextToken.equals(".")) {
                                token = token.concat(nextToken);
                                last = LastToken.DOT;
                            } else {
                                concatEnd = true;
                            }
                            break;
                    }
                    if (concatEnd) {
                        break;
                    }
                }
                i = j - 1;
            }
            newTokens.add(token);
        }

        return newTokens;
    }

    enum LastToken {
        ID,
        DOT,
        PARENTHESES
    }

    private static void collectTokensUpHere(String expression, List<String> tokens, int lastStart, int here) {
        String inbetweenText = expression.subSequence(lastStart, here).toString().trim();
        String[] inbetweenTokens = inbetweenText.split(" ");
        tokens.addAll(Arrays.asList(inbetweenTokens));
    }

    Map<AlphaNodeFieldConstraint, List<String>> getPrioritizedConstraints() {
        return prioritizedConstraints;
    }

    List<ObjectType> getDenyReorderList() {
        return denyReorderList;
    }
}
