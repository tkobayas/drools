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
package org.drools.mvel.integrationtests.waltz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import static org.assertj.core.api.Assertions.fail;

/**
 * This is a sample file to launch a rule package from a rule source file.
 */
public abstract class Waltz {

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    @Timeout(60000 )
    public void testWaltz(KieBaseTestConfiguration kieBaseTestConfiguration) {
        try {
            //load up the rulebase
            final KieBase kBase = readKnowledegBase(kieBaseTestConfiguration);
            for ( int i = 0; i < 50; i++ ) {
                KieSession kSession = kBase.newKieSession();
    
    //            workingMemory.setGlobal( "sysout",
    //                                     System.out );
    
                //            DebugWorkingMemoryEventListener wmListener = new DebugWorkingMemoryEventListener();
                //            DebugAgendaEventListener agendaListener = new DebugAgendaEventListener();
                //            workingMemory.addEventListener( wmListener );
                //            workingMemory.addEventListener( agendaListener );
    
                //go !     
                this.loadLines( kSession,
                                "waltz50.dat" );
    
                //final Stage stage = new Stage( Stage.START );
                //workingMemory.assertObject( stage );
    
                final long start = System.currentTimeMillis();
    
                final Stage stage = new Stage( Stage.DUPLICATE );
                kSession.insert( stage );
                kSession.fireAllRules();
                kSession.dispose();
                final long end = System.currentTimeMillis();
                System.out.println( end - start );
            }
        } catch ( final Throwable t ) {
            t.printStackTrace();
            fail( t.getMessage() );
        }
    }

    public KieBase readKnowledegBase(KieBaseTestConfiguration kieBaseTestConfiguration) {
        return KieBaseUtil.getKieBaseFromClasspathResources(getClass(), kieBaseTestConfiguration, "waltz.drl");
    }

    private void loadLines(final KieSession kSession,
                           final String filename) throws IOException {
        final BufferedReader reader = new BufferedReader( new InputStreamReader( Waltz.class.getResourceAsStream( filename ) ) );
        final Pattern pat = Pattern.compile( ".*make line \\^p1 ([0-9]*) \\^p2 ([0-9]*).*" );
        String line = reader.readLine();
        while ( line != null ) {
            final Matcher m = pat.matcher( line );
            if ( m.matches() ) {
                final Line l = new Line( Integer.parseInt( m.group( 1 ) ),
                                         Integer.parseInt( m.group( 2 ) ) );
                kSession.insert( l );
            }
            line = reader.readLine();
        }
        reader.close();
    }

}
