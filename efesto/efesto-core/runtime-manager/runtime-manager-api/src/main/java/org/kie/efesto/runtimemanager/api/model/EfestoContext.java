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

package org.kie.efesto.runtimemanager.api.model;

import java.util.Set;

import org.kie.api.runtime.Context;
import org.kie.efesto.runtimemanager.api.listener.EfestoListener;
import org.kie.efesto.runtimemanager.api.listener.EfestoStep;

/**
 * The context of an execution
 */
public interface EfestoContext<T extends EfestoStep, E extends EfestoListener<T>> extends Context {

    /**
     * Add the given <code>EfestoListener</code> to the current <code>Context</code>
     * @param toAdd
     */
    void addEfestoListener(final E toAdd);

    /**
     * Remove the given <code>EfestoListener</code> from the current <code>Context</code>.
     * @param toRemove
     */
    void removeEfestoListener(final E toRemove);

    /**
     * Returns an <b>unmodifiable set</b> of the <code>EfestoListener</code>s registered with the
     * current instance
     */
    Set<E> getEfestoListeners();
}
