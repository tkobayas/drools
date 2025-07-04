/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.efesto.compiler.model;

import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.identifiers.DmnIdFactory;
import org.kie.dmn.api.identifiers.KieDmnComponentRoot;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.compilationmanager.api.model.EfestoCallableOutputModelContainer;

import java.util.Collections;
import java.util.List;

public class EfestoCallableOutputDMN extends EfestoCallableOutputModelContainer<DMNModel, List<DMNMessage>> {

    public EfestoCallableOutputDMN(String fileName, String modelName, String modelSource, DMNModel dmnModel, List<DMNMessage> validationMessages) {
        this(new EfestoAppRoot()
                .get(KieDmnComponentRoot.class)
                .get(DmnIdFactory.class)
                .get(fileName, modelName),
              modelSource,
              dmnModel, validationMessages);

    }

    public EfestoCallableOutputDMN(ModelLocalUriId modelLocalUriId, String modelSource, DMNModel dmnModel, List<DMNMessage> validationMessages) {
        super(modelLocalUriId,
              modelSource,
              dmnModel,
              validationMessages);
    }

    @Override
    public List<String> getFullClassNames() {
        return Collections.emptyList();
    }

}
