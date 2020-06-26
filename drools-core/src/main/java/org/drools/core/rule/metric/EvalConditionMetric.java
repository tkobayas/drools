package org.drools.core.rule.metric;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;

import org.drools.core.WorkingMemory;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.EvalCondition;
import org.drools.core.rule.RuleConditionElement;
import org.drools.core.spi.EvalExpression;
import org.drools.core.spi.Tuple;
import org.drools.core.util.PerfLogUtils;

public class EvalConditionMetric extends EvalCondition {

    private static final long serialVersionUID = 510l;

    private EvalCondition delegate;

    public EvalConditionMetric() {}

    public EvalConditionMetric(EvalCondition original) {
        this.delegate = original;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        delegate = (EvalCondition) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(delegate);
    }

    @Override
    public EvalExpression getEvalExpression() {
        return delegate.getEvalExpression();
    }

    @Override
    public void wire(Object object) {
        delegate.wire(object);
    }

    @Override
    public void setEvalExpression(EvalExpression expression) {
        delegate.setEvalExpression(expression);
    }

    @Override
    public Declaration[] getRequiredDeclarations() {
        return delegate.getRequiredDeclarations();
    }

    @Override
    public Object createContext() {
        return delegate.createContext();
    }

    @Override
    public boolean isAllowed(Tuple tuple, WorkingMemory workingMemory, Object context) {
        PerfLogUtils.getInstance().incrementEvalCount();
        return delegate.isAllowed(tuple, workingMemory, context);
    }

    @Override
    public EvalCondition clone() {
        return new EvalConditionMetric(delegate.clone());
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof EvalConditionMetric) {
            return delegate.equals(((EvalConditionMetric) object).delegate);
        } else {
            return delegate.equals(object);
        }
    }

    @Override
    public Map<String, Declaration> getInnerDeclarations() {
        return delegate.getInnerDeclarations();
    }

    @Override
    public Map<String, Declaration> getOuterDeclarations() {
        return delegate.getOuterDeclarations();
    }

    @Override
    public List<? extends RuleConditionElement> getNestedElements() {
        return delegate.getNestedElements();
    }

    @Override
    public boolean isPatternScopeDelimiter() {
        return delegate.isPatternScopeDelimiter();
    }

    @Override
    public Declaration resolveDeclaration(String identifier) {
        return delegate.resolveDeclaration(identifier);
    }

    @Override
    public void replaceDeclaration(Declaration declaration, Declaration resolved) {
        delegate.replaceDeclaration(declaration, resolved);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
