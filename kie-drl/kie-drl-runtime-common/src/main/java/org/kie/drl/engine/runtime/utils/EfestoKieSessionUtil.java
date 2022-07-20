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
package org.kie.drl.engine.runtime.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.drools.model.Model;
import org.drools.modelcompiler.KieBaseBuilder;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.common.api.model.GeneratedExecutableResource;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.utils.GeneratedResourceUtils;

public class EfestoKieSessionUtil {

    private EfestoKieSessionUtil() {
    }

    public static KieSession loadKieSession(FRI fri, EfestoRuntimeContext context) {
        GeneratedExecutableResource finalResource = GeneratedResourceUtils.getGeneratedExecutableResource(fri, "drl")
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        List<Model> models = finalResource.getFullClassNames().stream()
                        .map(className -> loadModel(className, context))
                        .collect(Collectors.toList());
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(models);

        KieSession toReturn = kieBase.newKieSession();
        // TODO find a way to set a unique identifier for the created session -
        return toReturn;
    }

    static Model loadModel(String fullModelResourcesSourceClassName, EfestoRuntimeContext context) {
        try {
            final Class<? extends Model> aClass =
                    (Class<? extends Model>) context.loadClass(fullModelResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }
}