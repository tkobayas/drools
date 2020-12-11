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

package org.drools.compiler.kie.builder.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.drools.core.InitialFact;
import org.drools.core.reteoo.CompositeObjectSinkAdapter;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieBaseUpdaterAlphaIndex implements KieBaseUpdater {



    private final KieBaseUpdatersContext ctx;

    public KieBaseUpdaterAlphaIndex(KieBaseUpdatersContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        new AlphaIntervalIndexBuilder().buildIndex(ctx.getRete());
    }


}
