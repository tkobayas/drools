/*
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
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

package org.drools.core.common.metric;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.SingleBetaConstraints;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.ContextEntry;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.Tuple;
import org.drools.core.util.PerfLogUtils;
import org.drools.core.util.bitmask.BitMask;

public class SingleBetaConstraintsMetric extends SingleBetaConstraints {

    private static final long serialVersionUID = 510l;

    private SingleBetaConstraints delegate;

    public SingleBetaConstraintsMetric() {}

    public SingleBetaConstraintsMetric(SingleBetaConstraints original) {
        this.delegate = original;
    }

    @Override
    public void init(BuildContext context, short betaNodeType) {
        delegate.init(context, betaNodeType);
    }

    @Override
    public void initIndexes(int depth, short betaNodeType) {
        delegate.initIndexes(depth, betaNodeType);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        delegate = (SingleBetaConstraints) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(delegate);
    }

    @Override
    public SingleBetaConstraints cloneIfInUse() {
        SingleBetaConstraints clonedOriginal = delegate.cloneIfInUse();
        if (clonedOriginal == delegate) {
            return this;
        } else {
            return new SingleBetaConstraintsMetric(clonedOriginal);
        }
    }

    @Override
    public ContextEntry[] createContext() {
        return delegate.createContext();
    }

    @Override
    public void updateFromTuple(ContextEntry[] context, InternalWorkingMemory workingMemory, Tuple tuple) {
        delegate.updateFromTuple(context, workingMemory, tuple);
    }

    @Override
    public void updateFromFactHandle(ContextEntry[] context, InternalWorkingMemory workingMemory, InternalFactHandle handle) {
        delegate.updateFromFactHandle(context, workingMemory, handle);
    }

    @Override
    public boolean isAllowedCachedLeft(ContextEntry[] context, InternalFactHandle handle) {
        PerfLogUtils.getInstance().incrementEvalCount();
        return delegate.isAllowedCachedLeft(context, handle);
    }

    @Override
    public boolean isAllowedCachedRight(ContextEntry[] context, Tuple tuple) {
        PerfLogUtils.getInstance().incrementEvalCount();
        return delegate.isAllowedCachedRight(context, tuple);
    }

    @Override
    public boolean isIndexed() {
        return delegate.isIndexed();
    }

    @Override
    public int getIndexCount() {
        return delegate.getIndexCount();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public BetaMemory createBetaMemory(RuleBaseConfiguration config, short nodeType) {
        return delegate.createBetaMemory(config, nodeType);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public BetaNodeFieldConstraint getConstraint() {
        return delegate.getConstraint();
    }

    @Override
    public BetaNodeFieldConstraint[] getConstraints() {
        return delegate.getConstraints();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SingleBetaConstraintsMetric) {
            return delegate.equals(((SingleBetaConstraintsMetric) object).delegate);
        } else {
            return delegate.equals(object);
        }
    }

    @Override
    public void resetFactHandle(ContextEntry[] context) {
        delegate.resetFactHandle(context);
    }

    @Override
    public void resetTuple(ContextEntry[] context) {
        delegate.resetTuple(context);
    }

    @Override
    public BetaConstraints getOriginalConstraint() {
        return delegate.getOriginalConstraint();
    }

    @Override
    public BitMask getListenedPropertyMask(Class modifiedClass, List<String> settableProperties) {
        return delegate.getListenedPropertyMask(modifiedClass, settableProperties);
    }

    @Override
    public boolean isLeftUpdateOptimizationAllowed() {
        return delegate.isLeftUpdateOptimizationAllowed();
    }

    @Override
    public void registerEvaluationContext(BuildContext buildContext) {
        delegate.registerEvaluationContext(buildContext);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
