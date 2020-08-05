package org.kie.dmn.core.impl;

import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.FEELPropertyAccessible;
import org.kie.dmn.typesafe.DMNTypeSafeException;

public class DMNResultFPAImpl extends DMNResultImpl {

    private FEELPropertyAccessible outputSet;

    public DMNResultFPAImpl(DMNModel model, FEELPropertyAccessible outputSet) {
        super(model);
        this.outputSet = outputSet;
    }

    public FEELPropertyAccessible getOutputSet() {
        if (outputSet == null) {
            throw new DMNTypeSafeException("outputSet instance is not provided from DMNContextFPAImpl");
        }

        try {
            outputSet.fromMap(getContext().getAll());
        } catch (Exception e) {
            throw new DMNTypeSafeException("Failed while converting to outputSet", e);
        }
        return outputSet;
    }
}