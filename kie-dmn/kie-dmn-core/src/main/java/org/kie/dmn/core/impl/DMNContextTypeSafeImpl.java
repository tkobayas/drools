/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.util.Map;
import java.util.Optional;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMetadata;
import org.kie.dmn.api.core.FEELPropertyAccessible;
import org.kie.dmn.typesafe.DMNTypeSafeException;

public class DMNContextTypeSafeImpl implements DMNContext {

    private FEELPropertyAccessible fpa;

    private DMNContext delegate;

    public DMNContextTypeSafeImpl() {
        this.delegate = new DMNContextImpl();
    }

    public DMNContextTypeSafeImpl(Map<String, Object> entries) {
        this.delegate = new DMNContextImpl(entries);
    }

    public DMNContextTypeSafeImpl(Map<String, Object> entries, Map<String, Object> metadata) {
        this.delegate = new DMNContextImpl(entries, metadata);
    }

    public DMNContextTypeSafeImpl(DMNContext delegate) {
        this.delegate = delegate;
    }

    public FEELPropertyAccessible getFpa(Class<?> clazz) {
        if (fpa != null) {
            return fpa;
        }
        if (!FEELPropertyAccessible.class.isAssignableFrom(clazz)) {
            throw new DMNTypeSafeException(clazz + "is not FEELPropertyAccessible");
        }
        try {
            fpa = (FEELPropertyAccessible) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
                    throw new DMNTypeSafeException("Exception while instantiating " + clazz, e);
        }
        fpa.fromMap(getAll());
        return fpa;
    }

    @Override
    public Object set(String name, Object value) {
        return delegate.set(name, value);
    }

    @Override
    public Object get(String name) {
        return delegate.get(name);
    }

    @Override
    public Map<String, Object> getAll() {
        return delegate.getAll();
    }

    @Override
    public boolean isDefined(String name) {
        return delegate.isDefined(name);
    }

    @Override
    public DMNMetadata getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public void pushScope(String name, String namespace) {
        delegate.pushScope(name, namespace);
    }

    @Override
    public void popScope() {
        delegate.popScope();
    }

    @Override
    public Optional<String> scopeNamespace() {
        return delegate.scopeNamespace();
    }

    @Override
    public DMNContext clone() {
        DMNContext newDelegate = delegate.clone();
        return new DMNContextTypeSafeImpl(newDelegate); // fpa is null
    }
}
