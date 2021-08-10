/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.drools.model.functions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used to generate a better error message when constraints fail
 */
public class PredicateInformation {

    public static final PredicateInformation EMPTY_PREDICATE_INFORMATION =
            new PredicateInformation("", "", "");


    // Used to generate a significant error message
    private final String stringConstraint;
    private final Map<String, Set<String>> ruleNameMap = new HashMap<>();

    public PredicateInformation(String stringConstraint) {
        this.stringConstraint = defaultToEmptyString(stringConstraint);
    }

    public PredicateInformation(String stringConstraint, String ruleName, String ruleFileName) {
        this.stringConstraint = defaultToEmptyString(stringConstraint);
        ruleName = defaultToEmptyString(ruleName);
        ruleFileName = defaultToEmptyString(ruleFileName);
        ruleNameMap.computeIfAbsent(ruleFileName, k -> new HashSet<>()).add(ruleName);
    }

    private String defaultToEmptyString(String stringConstraint) {
        return Optional.ofNullable(stringConstraint).orElse("");
    }

    public RuntimeException betterErrorMessage(RuntimeException originalException) {
        if("".equals(stringConstraint)) {
            return originalException;
        }

        String errorMessage = String.format(
                "Error evaluating constraint '%s' in %s",
                stringConstraint,
                formatRuleNames());
        return new RuntimeException(errorMessage, originalException);
    }

    private String formatRuleNames() {
        if (ruleNameMap.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        List<String> ruleFileNameList = ruleNameMap.keySet().stream().sorted().collect(Collectors.toList());
        ruleFileNameList.forEach(ruleFileName -> {
            sb.append("[Rule ");
            ruleNameMap.get(ruleFileName).stream().sorted().forEach(ruleName -> {
                sb.append("\"" + ruleName + "\", ");
            });
            sb.delete(sb.length() - 2, sb.length()); // ruleName set is never empty (default to "") so safe to delete the tail
            sb.append(" in " + ruleFileName + "] ");
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String getStringConstraint() {
        return stringConstraint;
    }

    public Map<String, Set<String>> getRuleNameMap() {
        return ruleNameMap;
    }

    public void addRuleName(String ruleName, String ruleFileName) {
        ruleName = defaultToEmptyString(ruleName);
        ruleFileName = defaultToEmptyString(ruleFileName);
        ruleNameMap.computeIfAbsent(ruleFileName, k -> new HashSet<>()).add(ruleName);
    }

    public boolean isEmpty() {
        return EMPTY_PREDICATE_INFORMATION.equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PredicateInformation that = (PredicateInformation) o;
        return Objects.equals(stringConstraint, that.stringConstraint) &&
                Objects.equals(ruleNameMap, that.ruleNameMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringConstraint, ruleNameMap);
    }

    @Override
    public String toString() {
        return "PredicateInformation{" +
                "stringConstraint='" + stringConstraint + '\'' +
                ", ruleNameMap='" + ruleNameMap + '\'' +
                '}';
    }
}
