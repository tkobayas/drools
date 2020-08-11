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

    @Override
    public void analyzeAlphaConstraints(Map<String, InternalKnowledgePackage> pkgs, Collection<InternalKnowledgePackage> newPkgs) {

        Set<Rule> ruleSet = collectRules(pkgs, newPkgs);

        List<Pattern> patternList = new ArrayList<>();
        ruleSet.stream()
               .forEach(rule -> collectPatterns(((RuleImpl) rule).getLhs(), patternList));
        patternList.removeIf(Objects::isNull);

        for (Pattern pattern : patternList) {
            ObjectType objectType = pattern.getObjectType();
            Map<AlphaNodeFieldConstraint, Integer> analyzedAlphaConstraintsPerObjectType = analyzedAlphaConstraints.computeIfAbsent(objectType, key -> new HashMap<AlphaNodeFieldConstraint, Integer>());
            pattern.getConstraints()
                   .stream()
                   .filter(AlphaNodeFieldConstraint.class::isInstance)
                   .map(AlphaNodeFieldConstraint.class::cast)
                   .forEach(constraint -> analyzedAlphaConstraintsPerObjectType.merge(constraint, 1, (count, newValue) -> count + 1));
        }

        logger.trace("analyzedAlphaConstraints : {}", analyzedAlphaConstraints);
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
            logger.trace(" ** after alphaConstraints : {}", alphaConstraints);
            return;
        }

        Class<?> clazz = objectType.getClassType();

        // key=prioritizedConstraint, value=affected properties
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();

        for (AlphaNodeFieldConstraint alphaConstraint : alphaConstraints) {
            System.out.println(alphaConstraint);
            prioritizeConstraint(alphaConstraint, prioritizedConstraints, clazz);
        }

        logger.trace(" ** prioritizedConstraints : {}", prioritizedConstraints);

        alphaConstraints.sort((constraint1, constraint2) -> {

            int compare = compareBasedOnPrioritization(constraint1, constraint2, prioritizedConstraints, clazz);
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

    private int compareBasedOnPrioritization(AlphaNodeFieldConstraint constraint1, AlphaNodeFieldConstraint constraint2, Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints, Class<?> clazz) {
        boolean prioritized1 = prioritizedConstraints.containsKey(constraint1);
        boolean prioritized2 = prioritizedConstraints.containsKey(constraint2);

        if (prioritized1 && prioritized2 || !prioritized1 && !prioritized2) {
            return 0;
        }

        if (prioritized1) {
            List<String> propList1 = prioritizedConstraints.get(constraint1);
            List<String> propList2 = getCheckedProp(constraint2, clazz);
            if (propList1.isEmpty() || propList2.isEmpty() || !Collections.disjoint(propList1, propList2)) {
                return -1; // constraint1 first
            } else {
                return 0;
            }
        } else {
            List<String> propList2 = prioritizedConstraints.get(constraint2);
            List<String> propList1 = getCheckedProp(constraint1, clazz);
            if (propList2.isEmpty() || propList1.isEmpty() || !Collections.disjoint(propList2, propList1)) {
                return 1; // constraint2 first
            } else {
                return 0;
            }
        }
    }

    void prioritizeConstraint(AlphaNodeFieldConstraint alphaConstraint, Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints, Class<?> clazz) {
        if (alphaConstraint instanceof MvelConstraint) {
            boolean potentialPriority = false;
            AtomicBoolean propIsAmbiguous = new AtomicBoolean(false);
            List<String> propList = new ArrayList<>();
            MvelConstraint mvelConstraint = (MvelConstraint) alphaConstraint;
            String expression = mvelConstraint.getExpression();
            List<String> tokens = splitExpression(expression);
            if (tokens.contains("null")) {
                potentialPriority = true;
                int size = tokens.size();
                if (size == 3 && tokens.get(1).equals("!=") && tokens.get(2).equals("null")) {
                    // address != null
                    String target = tokens.get(0);
                    addToPropList(target, propList, clazz, propIsAmbiguous);
                } else if ((size == 5 || size == 6) && tokens.get(size - 3).equals("==") && tokens.get(size - 2).equals("null") && tokens.get(size - 1).equals(")")) {
                    // !( address == null )
                    String target;
                    if (tokens.get(0).equals("!") && tokens.get(1).equals("(")) {
                        target = tokens.get(2);
                        addToPropList(target, propList, clazz, propIsAmbiguous);
                    } else if (tokens.get(0).equals("!(")) {
                        target = tokens.get(1);
                        addToPropList(target, propList, clazz, propIsAmbiguous);
                    } else {
                        propIsAmbiguous.set(true);
                    }
                } else {
                    // Don't parse other cases
                    propIsAmbiguous.set(true);
                }
            }
            if (tokens.contains("instanceof")) {
                potentialPriority = true;
                int size = tokens.size();
                if (size == 3 && tokens.get(size - 2).equals("instanceof") && !tokens.get(size - 1).equals(")")) {
                    // address instanceof Address
                    String target = tokens.get(0);
                    addToPropList(target, propList, clazz, propIsAmbiguous);
                } else if ((size == 5 || size == 6) && tokens.get(size - 3).equals("instanceof") && tokens.get(size - 1).equals(")")) {
                    // !( address instanceof Address )
                    String target;
                    if (tokens.get(0).equals("!") && tokens.get(1).equals("(")) {
                        target = tokens.get(2);
                        addToPropList(target, propList, clazz, propIsAmbiguous);
                    } else if (tokens.get(0).equals("!(")) {
                        target = tokens.get(1);
                        addToPropList(target, propList, clazz, propIsAmbiguous);
                    } else {
                        propIsAmbiguous.set(true);
                    }
                } else {
                    // Don't parse other cases
                    propIsAmbiguous.set(true);
                }
            }
            if (potentialPriority) {
                if (propIsAmbiguous.get()) {
                    prioritizedConstraints.put(alphaConstraint, new ArrayList<>());
                } else {
                    prioritizedConstraints.put(alphaConstraint, propList);
                }
            }
        }
    }

    private void addToPropList(String target, List<String> propList, Class<?> clazz, AtomicBoolean propIsAmbiguous) {
        if (target.startsWith("this.")) {
            target = target.substring(5);
        }
        if (!target.contains(".")) {
            String prop = MvelConstraint.getPropertyNameFromSimpleExpression(target);
            if (!prop.isEmpty() && PropertyTools.getFieldOrAccessor(clazz, prop) != null) {
                propList.add(prop);
            } else {
                propIsAmbiguous.set(true);
            }
        } else {
            StringBuilder normalizedPropExpression = new StringBuilder();
            if (nestedPropExists(clazz, target, normalizedPropExpression)) {
                propList.add(normalizedPropExpression.toString());
            } else {
                propIsAmbiguous.set(true);
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
        AtomicBoolean propIsAmbiguous = new AtomicBoolean(false);
        List<String> propList = new ArrayList<>();
        if (alphaConstraint instanceof MvelConstraint) {
            MvelConstraint mvelConstraint = (MvelConstraint) alphaConstraint;
            String expression = mvelConstraint.getExpression();
            List<String> tokens = splitExpression(expression);
            for (String token : tokens) {
                addToPropList(token, propList, clazz, propIsAmbiguous);
            }
        }
        if (propIsAmbiguous.get()) {
            propList = new ArrayList<>();
        }
        return propList;
    }

    /**
     * Split expression into tokens ignoring quoted string
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
}
