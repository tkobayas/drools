/**
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
package org.kie.dmn.backend.marshalling.v1_5.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.kie.dmn.model.api.DMNModelInstrumentedBase;
import org.kie.dmn.model.api.TypedChildExpression;
import org.kie.dmn.model.v1_5.TTypedChildExpression;

public class TypedChildExpressionConverter extends ChildExpressionConverter {

    public static final String TYPE_REF = "typeRef";

    public TypedChildExpressionConverter(XStream xstream) {
        super( xstream );
    }

    @Override
    protected void assignAttributes(HierarchicalStreamReader reader, Object parent) {
        super.assignAttributes( reader, parent );
        String typeRef = reader.getAttribute( TYPE_REF );

        if (typeRef != null) {
            ((TypedChildExpression) parent).setTypeRef(typeRef);
        }
    }

    @Override
    protected void writeAttributes(HierarchicalStreamWriter writer, Object parent) {
        super.writeAttributes(writer, parent);
        TypedChildExpression e = (TypedChildExpression) parent;
        
        if (e.getTypeRef() != null) {
            writer.addAttribute(TYPE_REF, e.getTypeRef());
        }
    }

	@Override
	protected DMNModelInstrumentedBase createModelObject() {
		return new TTypedChildExpression();
	}

	@Override
	public boolean canConvert(Class type) {
		return type.equals(TTypedChildExpression.class);
	}
}
