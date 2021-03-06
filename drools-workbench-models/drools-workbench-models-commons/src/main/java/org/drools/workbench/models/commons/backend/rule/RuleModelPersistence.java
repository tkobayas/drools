/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.drools.workbench.models.commons.backend.rule;

import java.util.Collection;
import java.util.List;

import org.drools.workbench.models.datamodel.rule.RuleModel;
import org.kie.soup.project.datamodel.oracle.PackageDataModelOracle;

public interface RuleModelPersistence {

    String marshal(final RuleModel model);

    RuleModel unmarshal(final String str,
                        final List<String> globals,
                        final PackageDataModelOracle dmo);

    /**
     *
     * @param splitEvals When true, each unresolved DRL will be split to a separate FreeFromLine element.
     *                   When false, the DRL will be placed into FreeFormLine as a blob.
     * @return
     */
    RuleModel unmarshal(final String str,
                        final List<String> globals,
                        final PackageDataModelOracle dmo,
                        final boolean splitEvals);

    RuleModel unmarshal(final String str,
                        final List<String> globals,
                        final PackageDataModelOracle dmo,
                        final Collection<RuleModelIActionPersistenceExtension> extensions);

    RuleModel unmarshalUsingDSL(final String str,
                                final List<String> globals,
                                final PackageDataModelOracle dmo,
                                final String... dsls);

    RuleModel unmarshalUsingDSL(final String str,
                                final List<String> globals,
                                final PackageDataModelOracle dmo,
                                final Collection<RuleModelIActionPersistenceExtension> extensions,
                                final String... dsls);
}
