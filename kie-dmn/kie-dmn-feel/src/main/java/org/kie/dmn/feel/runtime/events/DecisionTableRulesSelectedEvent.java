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
package org.kie.dmn.feel.runtime.events;

import java.util.List;

import org.kie.dmn.api.feel.runtime.events.FEELEvent;

/**
 * An event class to report all rules selected as a result for a decision table.
 *
 * For a unique hit decision table, the same rule will be matched and selected,
 * but for decision tables with different hit policies, the rules actually
 * selected might be a subset of the rules matched.
 * 
 * In some Multiple hit policies, the aggregated result may not correspond to 
 * a unique index; in this case, despite the tables evaluating to an actual result,
 * the {@link #getFired()} may be unspecified or an empty list.
 */
public class DecisionTableRulesSelectedEvent
        extends FEELEventBase
        implements FEELEvent {

    private final String        nodeName;
    private final String        dtName;
    private final List<Integer> fired;
    private final List<String> firedIds;

    public DecisionTableRulesSelectedEvent(Severity severity, String msg, String nodeName, String dtName, List<Integer> fired, List<String> firedIds) {
        super( severity, msg, null );
        this.nodeName = nodeName;
        this.dtName = dtName;
        this.fired = fired;
        this.firedIds = firedIds;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getDecisionTableName() { return dtName; }

    public List<Integer> getFired() {
        return fired;
    }

    public List<String> getFiredIds() {
        return firedIds;
    }

    @Override
    public String toString() {
        return "DecisionTableRulesMatchedEvent{" +
               "severity=" + getSeverity() +
               ", message='" + getMessage() + '\'' +
               ", nodeName='" + nodeName + '\'' +
               ", dtName='" + dtName + '\'' +
               ", fired='" + fired + '\'' +
               ", firedIds='" + firedIds + '\'' +
               '}';
    }
}
