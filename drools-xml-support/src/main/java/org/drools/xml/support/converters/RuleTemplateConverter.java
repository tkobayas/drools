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
package org.drools.xml.support.converters;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.drools.compiler.kproject.models.RuleTemplateModelImpl;

public class RuleTemplateConverter extends AbstractXStreamConverter {

    public RuleTemplateConverter() {
        super(RuleTemplateModelImpl.class);
    }

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        RuleTemplateModelImpl rtm = (RuleTemplateModelImpl) value;
        writer.addAttribute( "dtable", rtm.getDtable() );
        writer.addAttribute( "template", rtm.getTemplate() );
        writer.addAttribute( "row", "" + rtm.getRow() );
        writer.addAttribute( "col", "" + rtm.getCol() );
    }

    public Object unmarshal(HierarchicalStreamReader reader, final UnmarshallingContext context) {
        RuleTemplateModelImpl rtm = new RuleTemplateModelImpl();
        rtm.setDtable(reader.getAttribute("dtable"));
        rtm.setTemplate(reader.getAttribute("template"));
        rtm.setRow(Integer.parseInt( reader.getAttribute("row")) );
        rtm.setCol(Integer.parseInt( reader.getAttribute( "col" ) ) );
        return rtm;
    }
}