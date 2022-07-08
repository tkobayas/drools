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
package org.kie.pmml.compiler.model;

import java.util.List;

import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.compilationmanager.api.model.AbstractEfestoCallableCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;

public class EfestoRedirectOutputPMML extends AbstractEfestoCallableCompilationOutput implements EfestoResource<String> {

    private final String targetEngine;

    /**
     * This is the <b>payload</b> to forward to the target compilation-engine
     */
    private final String content;

    public EfestoRedirectOutputPMML(FRI fri, String modelFile) {
        super(fri, (List<String>) null);
        this.targetEngine = "drl";
        this.content = modelFile;
    }

    public String getTargetEngine() {
        return targetEngine;
    }

    @Override
    public String getContent() {
        return content;
    }

}
