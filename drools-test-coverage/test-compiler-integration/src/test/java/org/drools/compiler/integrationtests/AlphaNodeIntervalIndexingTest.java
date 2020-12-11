/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.compiler.integrationtests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.drools.testcoverage.common.model.Person;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieUtil;
import org.drools.testcoverage.common.util.TestParametersUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

@RunWith(Parameterized.class)
public class AlphaNodeIntervalIndexingTest {

    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public AlphaNodeIntervalIndexingTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Parameterized.Parameters(name = "KieBase type={0}")
    public static Collection<Object[]> getParameters() {
        return TestParametersUtil.getKieBaseCloudConfigurations(false);
    }

    @Test
    public void testInteger() {
        final String drl = "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "global java.util.List results;\n" +
                           "rule test1\n when\n" +
                           "   Person( age >= 0 && < 10 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n" +
                           "rule test2\n when\n" +
                           "   Person( age >= 10 && < 20 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n" +
                           "rule test3\n when\n" +
                           "   Person( age >= 20 && < 30 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n" +
                           "rule test4\n when\n" +
                           "   Person( age >= 30 && < 40 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n";

        //System.out.println(drl);

        final KieModule kieModule = KieUtil.getKieModuleFromDrls("indexing-test", kieBaseTestConfiguration, drl);
        final KieContainer kieContainer = KieServices.get().newKieContainer(kieModule.getReleaseId());
        final KieBaseConfiguration kieBaseConfiguration = kieBaseTestConfiguration.getKieBaseConfiguration();
        final KieBase kbase = kieContainer.newKieBase(kieBaseConfiguration);
        final KieSession ksession = kbase.newKieSession();

        List<String> results = new ArrayList<>();
        ksession.setGlobal("results", results);

        //        ReteDumper dumper = new ReteDumper();
        //        dumper.setNodeInfoOnly(true);
        //        dumper.dump(kbase);

        ksession.insert(new Person("John", 18));
        ksession.fireAllRules();
        Assertions.assertThat(results).containsOnly("test2");
        results.clear();

        ksession.insert(new Person("John", 20));
        ksession.fireAllRules();
        Assertions.assertThat(results).containsOnly("test3");
        results.clear();

        ksession.insert(new Person("Paul", 40));
        ksession.fireAllRules();
        Assertions.assertThat(results).isEmpty();
        results.clear();

        ksession.insert(new Person("XXX", -5));
        ksession.fireAllRules();
        Assertions.assertThat(results).isEmpty();
        results.clear();
    }

    @Test
    public void testOverlap() {
        final String drl = "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "global java.util.List results;\n" +
                           "rule test1\n when\n" +
                           "   Person( age >= 0 && < 40 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n" +
                           "rule test2\n when\n" +
                           "   Person( age >= 10 && < 20 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n" +
                           "rule test3\n when\n" +
                           "   Person( age >= 10 && <= 30 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n" +
                           "rule test4\n when\n" +
                           "   Person( age > 30 && <= 50 )\n" +
                           "then\n" +
                           "   results.add(drools.getRule().getName());" +
                           "end\n";

        //System.out.println(drl);

        final KieModule kieModule = KieUtil.getKieModuleFromDrls("indexing-test", kieBaseTestConfiguration, drl);
        final KieContainer kieContainer = KieServices.get().newKieContainer(kieModule.getReleaseId());
        final KieBaseConfiguration kieBaseConfiguration = kieBaseTestConfiguration.getKieBaseConfiguration();
        final KieBase kbase = kieContainer.newKieBase(kieBaseConfiguration);
        final KieSession ksession = kbase.newKieSession();

        List<String> results = new ArrayList<>();
        ksession.setGlobal("results", results);

        //        ReteDumper dumper = new ReteDumper();
        //        dumper.setNodeInfoOnly(true);
        //        dumper.dump(kbase);

        ksession.insert(new Person("John", 18));
        ksession.fireAllRules();
        Assertions.assertThat(results).containsOnly("test1", "test2", "test3");
        results.clear();

        ksession.insert(new Person("Paul", 30));
        ksession.fireAllRules();
        Assertions.assertThat(results).containsOnly("test1", "test3");
        results.clear();

        ksession.insert(new Person("Paul", 50));
        ksession.fireAllRules();
        Assertions.assertThat(results).containsOnly("test4");
        results.clear();

        ksession.insert(new Person("Paul", 51));
        ksession.fireAllRules();
        Assertions.assertThat(results).isEmpty();
        results.clear();

        ksession.insert(new Person("XXX", -5));
        ksession.fireAllRules();
        Assertions.assertThat(results).isEmpty();
        results.clear();
    }
}
