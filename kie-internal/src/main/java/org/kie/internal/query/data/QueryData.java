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
package org.kie.internal.query.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.kie.internal.query.QueryContext;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryData extends QueryParameters {

    @XmlElement
    private QueryContext queryContext = new QueryContext();

    public QueryData() {
        // JAXB constructor
    }

    public QueryData(QueryData queryData) {
       super(queryData);
       this.queryContext = new QueryContext(queryData.getQueryContext());
    }

    public QueryContext getQueryContext() {
        return queryContext;
    }

    public void setQueryContext( QueryContext queryContext ) {
        this.queryContext = queryContext;
    }

    public void clear() {
        super.clear();
        this.queryContext.clear();
    }

}
