/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.core.phreak.metric;

import org.drools.core.common.InternalAgenda;
import org.drools.core.common.TupleSets;
import org.drools.core.phreak.PhreakQueryTerminalNode;
import org.drools.core.phreak.StackEntry;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.QueryTerminalNode;
import org.drools.core.util.LinkedList;
import org.drools.core.util.PerfLogUtils;

public class PhreakQueryTerminalNodeMetric extends PhreakQueryTerminalNode {

    @Override
    public void doNode(QueryTerminalNode qtnNode,
                       InternalAgenda agenda,
                       TupleSets<LeftTuple> srcLeftTuples,
                       LinkedList<StackEntry> stack) {

        try {
            PerfLogUtils.getInstance().startMetrics(qtnNode);

            super.doNode(qtnNode, agenda, srcLeftTuples, stack);

        } finally {
            PerfLogUtils.getInstance().logAndEndMetrics();
        }
    }
}
