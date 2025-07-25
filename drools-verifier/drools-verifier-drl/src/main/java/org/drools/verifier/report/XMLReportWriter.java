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
package org.drools.verifier.report;

import java.io.IOException;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import org.drools.util.IoUtils;
import org.drools.verifier.components.LiteralRestriction;
import org.drools.verifier.data.VerifierReport;
import org.drools.verifier.report.components.Gap;
import org.drools.verifier.report.components.MissingNumberPattern;
import org.drools.verifier.report.components.VerifierMessage;

import static org.kie.utll.xml.XStreamUtils.createNonTrustingXStream;

public class XMLReportWriter
    implements
    VerifierReportWriter {

    public void writeReport(OutputStream out,
                            VerifierReport result) throws IOException {
        XStream xstream = createNonTrustingXStream();

        xstream.alias( "result",
                       VerifierReport.class );
        xstream.alias( "message",
                       VerifierMessage.class );

        xstream.alias( "Gap",
                       Gap.class );
        xstream.alias( "MissingNumber",
                       MissingNumberPattern.class );

        xstream.alias( "Field",
                       org.drools.verifier.components.Field.class );

        xstream.alias( "LiteralRestriction",
                       LiteralRestriction.class );

        out.write( ("<?xml version=\"1.0\"?>\n" + xstream.toXML( result )).getBytes( IoUtils.UTF8_CHARSET ) );
    }

}
