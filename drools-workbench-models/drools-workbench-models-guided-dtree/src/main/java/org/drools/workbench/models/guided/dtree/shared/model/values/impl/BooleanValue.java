/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.drools.workbench.models.guided.dtree.shared.model.values.impl;

import org.drools.workbench.models.guided.dtree.shared.model.values.Value;
import org.kie.soup.commons.validation.PortablePreconditions;

public class BooleanValue implements Value<Boolean> {

    private Boolean value;

    public BooleanValue() {
        //Errai marshalling
    }

    public BooleanValue(final Boolean value) {
        setValue(value);
    }

    public BooleanValue(final BooleanValue value) {
        setValue(value.getValue());
    }

    @Override
    public void setValue(final String value) {
        setValue(Boolean.valueOf(value));
    }

    @Override
    public void setValue(final Boolean value) {
        this.value = PortablePreconditions.checkNotNull("value",
                                                        value);
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanValue)) {
            return false;
        }

        BooleanValue that = (BooleanValue) o;

        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
