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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.drools.core.InitialFact;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.CompositeObjectSinkAdapter;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.SingleObjectSinkAdapter;
import org.drools.core.rule.IndexableConstraint;
import org.drools.core.util.index.AlphaNodeInterval;
import org.drools.core.util.index.IndexUtil.ConstraintType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlphaIntervalIndexBuilder {

    private final Logger logger = LoggerFactory.getLogger(AlphaIntervalIndexBuilder.class);

    // TODO: Move to AlphaIntervalIndexThresholdOption
    public static final String INTERVAL_THRESHOLD_SYSTEM_PROPERTY = "drools.alphaNodeIntervalIndexThreshold";
    public static final int INTERVAL_THRESHOLD = Integer.parseInt(System.getProperty(INTERVAL_THRESHOLD_SYSTEM_PROPERTY, "3"));

    private Set<AlphaNode> indexedAlphaNodeSet = new HashSet<>();

    public void buildIndex(Rete rete) {
        logger.debug("buildIntervalIndex");
        List<ObjectTypeNode> objectTypeNodes = objectTypeNodes(rete);
        objectTypeNodes.stream().forEach(this::buildIndex);
    }

    private void buildIndex(ObjectTypeNode otn) {
        ObjectSinkPropagator propagator = otn.getObjectSinkPropagator();
        traversePropagator(propagator);
    }

    private void traversePropagator(ObjectSinkPropagator propagator) {
        if (propagator instanceof CompositeObjectSinkAdapter) {
            CompositeObjectSinkAdapter adaptor = (CompositeObjectSinkAdapter) propagator;
            buildIndex((CompositeObjectSinkAdapter) adaptor);
            ObjectSink[] sinks = adaptor.getSinks();
            for (ObjectSink sink : sinks) {
                if (sink instanceof AlphaNode) { // traverse only AlphaNodes
                    traversePropagator(((AlphaNode) sink).getObjectSinkPropagator());
                }
            }
        } else if (propagator instanceof SingleObjectSinkAdapter) {
            ObjectSink sink = propagator.getSinks()[0];
            if (sink instanceof AlphaNode) { // traverse only AlphaNodes
                traversePropagator(((AlphaNode) sink).getObjectSinkPropagator());
            }
        }
    }

    private void buildIndex(CompositeObjectSinkAdapter adapter) {
        // TODO: Check if intervalIndex is already built (rule update scenario)
        List<AlphaNodeInterval> alphaNodeIntervals = new ArrayList<>();
        ObjectSink[] sinks = adapter.getSinks();
        for (ObjectSink sink : sinks) {
            List<AlphaNodeInterval> alphaNodeIntervalsPerFormerNode = getAlphaNodeIntervals(sink);
            alphaNodeIntervals.addAll(alphaNodeIntervalsPerFormerNode);
        }
    }

    private List<AlphaNodeInterval> getAlphaNodeIntervals(ObjectSink sink) {
        if (!(sink instanceof AlphaNode) || indexedAlphaNodeSet.contains(sink)) {
            return Collections.emptyList();
        }
        AlphaNode alphaNode = (AlphaNode) sink;
        if (!CompositeObjectSinkAdapter.isRangeIndexable(alphaNode)) { // at least, it must be rangeIndexable
            return Collections.emptyList();
        }
        List<AlphaNodeInterval> alphaNodeIntervals = new ArrayList<>();
        ObjectSinkPropagator propagator = alphaNode.getObjectSinkPropagator();
        if (propagator instanceof CompositeObjectSinkAdapter) {
            CompositeObjectSinkAdapter adaptor = (CompositeObjectSinkAdapter) propagator;
            ObjectSink[] subsequentSinks = adaptor.getSinks();
            for (ObjectSink subsequentSink : subsequentSinks) {
                processSinkPair(alphaNode, subsequentSink, alphaNodeIntervals);
            }
        } else if (propagator instanceof SingleObjectSinkAdapter) {
            ObjectSink subsequentSink = propagator.getSinks()[0];
            processSinkPair(alphaNode, subsequentSink, alphaNodeIntervals);
        }

        return alphaNodeIntervals;
    }

    private void processSinkPair(AlphaNode alphaNode, ObjectSink subsequentSink, List<AlphaNodeInterval> alphaNodeIntervals) {
        if (subsequentSink instanceof AlphaNode) {
            AlphaNode subsequentAlphaNode = (AlphaNode) subsequentSink;
            if (areIntervalIndexable(alphaNode, subsequentAlphaNode)) {
                alphaNodeIntervals.add(new AlphaNodeInterval(alphaNode, subsequentAlphaNode));
                indexedAlphaNodeSet.add(alphaNode);
                indexedAlphaNodeSet.add(subsequentAlphaNode);
            }
        }
    }

    private boolean areIntervalIndexable(AlphaNode formerNode, AlphaNode latterNode) {
        if (!CompositeObjectSinkAdapter.isRangeIndexable(latterNode)) { // at least, it must be rangeIndexable
            return false;
        }
        IndexableConstraint formerConstraint = (IndexableConstraint) formerNode;
        ConstraintType formerConstraintType = formerConstraint.getConstraintType();
        IndexableConstraint latterConstraint = (IndexableConstraint) latterNode;
        ConstraintType latterConstraintType = latterConstraint.getConstraintType();

        return (formerConstraintType.isAscending() && latterConstraintType.isDescending() || formerConstraintType.isDescending() && latterConstraintType.isAscending());
    }

    private List<ObjectTypeNode> objectTypeNodes(Rete rete) {
        return rete.getEntryPointNodes().values().stream()
                   .flatMap(ep -> ep.getObjectTypeNodes().values().stream())
                   .filter(f -> !InitialFact.class.isAssignableFrom(f.getObjectType().getClassType()))
                   .collect(Collectors.toList());
    }
}
