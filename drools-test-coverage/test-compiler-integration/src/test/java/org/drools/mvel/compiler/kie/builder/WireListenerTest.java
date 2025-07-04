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
package org.drools.mvel.compiler.kie.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieUtil;
import org.drools.testcoverage.common.util.TestParametersUtil2;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.ListenerModel;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.generatePomXml;

public class WireListenerTest {

    public static Stream<KieBaseTestConfiguration> parameters() {
        return TestParametersUtil2.getKieBaseCloudConfigurations(true).stream();
    }

    private static final List<ObjectInsertedEvent> insertEvents = new ArrayList<ObjectInsertedEvent>();
    private static final List<ObjectUpdatedEvent> updateEvents = new ArrayList<ObjectUpdatedEvent>();
    private static final List<ObjectDeletedEvent> retractEvents = new ArrayList<ObjectDeletedEvent>();

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testWireListener(KieBaseTestConfiguration kieBaseTestConfiguration) throws Exception {
        insertEvents.clear();
        updateEvents.clear();
        retractEvents.clear();

        KieServices ks = KieServices.Factory.get();

        ReleaseId releaseId = ks.newReleaseId("org.kie", "listener-test", "1.0");
        build(kieBaseTestConfiguration, ks, releaseId);
        KieContainer kieContainer = ks.newKieContainer(releaseId);

        KieSession ksession = kieContainer.newKieSession();
        ksession.fireAllRules();

        assertThat(insertEvents.size()).isEqualTo(1);
        assertThat(updateEvents.size()).isEqualTo(1);
        assertThat(retractEvents.size()).isEqualTo(1);
    }

    private void build(KieBaseTestConfiguration kieBaseTestConfiguration, KieServices ks, ReleaseId releaseId) throws IOException {
        KieModuleModel kproj = ks.newKieModuleModel();

        KieSessionModel ksession1 = kproj.newKieBaseModel("KBase1").newKieSessionModel("KSession1").setDefault(true);

        ksession1.newListenerModel(RecordingWorkingMemoryEventListener.class.getName(), ListenerModel.Kind.RULE_RUNTIME_EVENT_LISTENER);

        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kproj.toXML())
           .writePomXML( generatePomXml(releaseId) )
           .write("src/main/resources/KBase1/rules.drl", createDRL());

        final KieBuilder kieBuilder = KieUtil.getKieBuilderFromKieFileSystem(kieBaseTestConfiguration, kfs, false);
        assertThat(kieBuilder.getResults().getMessages().isEmpty()).isTrue();
    }

    private String createDRL() {
        return "package org.kie.test\n" +
                "declare Account\n" +
                "    balance : int\n" +
                "end\n" +
                "rule OpenAccount when\n" +
                "then\n" +
                "    insert( new Account(100) );\n" +
                "end\n" +
                "rule PayTaxes when\n" +
                "    $account : Account( $balance : balance > 0 ) \n" +
                "then\n" +
                "    modify( $account ) { setBalance( $balance - 200 ) }\n" +
                "end\n" +
                "rule CloseAccountWithNegeativeBalance when\n" +
                "    $account : Account( balance < 0 ) \n" +
                "then\n" +
                "    retract( $account );\n" +
                "end\n";
    }

    public static class RecordingWorkingMemoryEventListener implements RuleRuntimeEventListener {

        @Override
        public void objectInserted(ObjectInsertedEvent event) {
            insertEvents.add(event);
        }

        @Override
        public void objectUpdated(ObjectUpdatedEvent event) {
            updateEvents.add(event);
        }

        @Override
        public void objectDeleted(ObjectDeletedEvent event) {
            retractEvents.add(event);
        }
    }
}
