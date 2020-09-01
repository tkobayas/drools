/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.dmn.core.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMetadata;
import org.kie.dmn.api.core.FEELPropertyAccessible;
import org.kie.dmn.core.impl.DMNContextImpl.ScopeReference;
import org.kie.dmn.feel.util.EvalHelper.PropertyValueResult;
import org.kie.dmn.typesafe.DMNTypeSafeException;

public class DMNContextFPAImpl implements DMNContext {

    private final FEELPropertyAccessible fpa;
    private Deque<ScopeReference> stack = new LinkedList<>();
    private DMNMetadataImpl metadata;

    private Map<String, Object> fallbackMap; // store runtime variables which are not defined in strongly typed fpa

    public DMNContextFPAImpl(FEELPropertyAccessible bean) {
        this.fpa = bean;
        this.metadata = new DMNMetadataImpl();
        this.fallbackMap = new LinkedHashMap<>();
    }

    public DMNContextFPAImpl(FEELPropertyAccessible bean, Map<String, Object> metadata) {
        this.fpa = bean;
        this.metadata = new DMNMetadataImpl(metadata);
        this.fallbackMap = new LinkedHashMap<>();
    }

    public DMNContextFPAImpl(FEELPropertyAccessible bean, Map<String, Object> metadata, Map<String, Object> fallbackMap) {
        this.fpa = bean;
        this.metadata = new DMNMetadataImpl(metadata);
        this.fallbackMap = new LinkedHashMap<>(fallbackMap);
    }

    @Override
    public Object set(String name, Object value) {
        if (stack.isEmpty()) {
            PropertyValueResult propValueResult = (PropertyValueResult) this.fpa.getFEELProperty(name);
            if (propValueResult.isDefined()) {
                if (!this.fpa.getFEELProperty(name).toOptional().isPresent()) {
                    // fromMap can handle Map/Collection rather than setFEELProperty
                    Map<String, Object> compositeValue = new HashMap<>();
                    compositeValue.put(name, value);
                    fpa.fromMap(compositeValue);
                }
                return this.fpa.getFEELProperty(name).toOptional().orElse(null);
            } else {
                return fallbackMap.put(name, value);
            }
        } else {
            return stack.peek().getRef().put(name, value);
        }
    }

    @Override
    public Object get(String name) {
        if (stack.isEmpty()) {
            PropertyValueResult propValueResult = (PropertyValueResult)this.fpa.getFEELProperty(name);
            if (propValueResult.isDefined()) {
                return fpa.getFEELProperty(name).toOptional().orElse(null);
            } else {
                return fallbackMap.get(name);
            }
        } else {
            return stack.peek().getRef().get(name);
        }
    }

    /**
     * Internal utility method
     * 
     * @return FEELPropertyAccessible which represents strongly typed context
     */
    public FEELPropertyAccessible getFpa() {
        return fpa;
    }

    private Map<String, Object> getCurrentEntries() {
        if (stack.isEmpty()) {
            Map<String, Object> mergedMap = new HashMap<>(fpa.allFEELProperties());
            mergedMap.putAll(fallbackMap);
            return mergedMap;
        } else {
            return stack.peek().getRef(); // Intentional, symbol resolution in scope should limit at the top of the stack (for DMN semantic).
        }
    }

    @Override
    public void pushScope(String name, String namespace) {
        Map<String, Object> scopeRef = (Map<String, Object>) fallbackMap.computeIfAbsent(name, s -> new LinkedHashMap<String, Object>());
        stack.push(new ScopeReference(name, namespace, scopeRef));
    }

    @Override
    public void popScope() {
        stack.pop();
    }

    @Override
    public Optional<String> scopeNamespace() {
        if (stack.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(stack.peek().getNamespace());
        }
    }

    @Override
    public Map<String, Object> getAll() {
        return getCurrentEntries();
    }

    @Override
    public boolean isDefined(String name) {
        if (stack.isEmpty()) {
            // Cannot use propValueResult.isDefined() for fpa null field because it's defined=true
            return getCurrentEntries().get(name) != null;
        } else {
            return getCurrentEntries().containsKey(name);
        }
    }

    @Override
    public DMNMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public DMNContext clone() {
        FEELPropertyAccessible newFpa;
        try {
            newFpa = fpa.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
                    throw new DMNTypeSafeException("Exception while instantiating " + fpa.getClass(), e);
        }
        newFpa.fromMap(fpa.allFEELProperties());
        DMNContextFPAImpl newCtx = new DMNContextFPAImpl(newFpa, metadata.asMap(), fallbackMap.clone());
        for (ScopeReference e : stack) {
            newCtx.pushScope(e.getName(), e.getNamespace());
        }
        return newCtx;
    }
}
