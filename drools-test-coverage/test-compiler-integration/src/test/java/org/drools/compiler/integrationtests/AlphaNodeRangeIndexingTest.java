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

import java.math.BigDecimal;
import java.util.Collection;

import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.CompositeObjectSinkAdapter;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.SingleObjectSinkAdapter;
import org.drools.core.util.DateUtils;
import org.drools.core.util.index.AlphaRangeIndex;
import org.drools.testcoverage.common.model.Address;
import org.drools.testcoverage.common.model.MyComparable;
import org.drools.testcoverage.common.model.MyComparableHolder;
import org.drools.testcoverage.common.model.Order;
import org.drools.testcoverage.common.model.Person;
import org.drools.testcoverage.common.model.Primitives;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.KieUtil;
import org.drools.testcoverage.common.util.TestParametersUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.conf.AlphaRangeIndexThresholdOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class AlphaNodeRangeIndexingTest {

    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    private static final String BASIC_DRL =
            "package org.drools.compiler.test\n" +
                                            "import " + Person.class.getCanonicalName() + "\n" +
                                            "rule test1\n when\n" +
                                            "   Person( age >= 18 )\n" +
                                            "then\n end\n" +
                                            "rule test2\n when\n" +
                                            "   Person( age < 25 )\n" +
                                            "then\n end\n" +
                                            "rule test3\n when\n" +
                                            "   Person( age > 8 )\n" +
                                            "then\n end\n" +
                                            "rule test4\n when\n" +
                                            "   Person( age < 60 )\n" +
                                            "then\n end\n" +
                                            "rule test5\n when\n" +
                                            "   Person( age > 12 )\n" +
                                            "then\n end\n" +
                                            "rule test6\n when\n" +
                                            "   Person( age <= 4 )\n" +
                                            "then\n end\n";

    public AlphaNodeRangeIndexingTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Parameterized.Parameters(name = "KieBase type={0}")
    public static Collection<Object[]> getParameters() {
        return TestParametersUtil.getKieBaseCloudConfigurations(true);
    }

    @Test
    public void testInteger() {
        final String drl = BASIC_DRL;

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        ksession.insert(new Person("John", 18));
        int fired = ksession.fireAllRules();
        assertEquals(5, fired);

        ksession.insert(new Person("Paul", 60));
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }

    @Test
    public void testDouble() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Order.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Order( total >= 18.0 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Order( total < 25.0 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Order( total > 8.0 )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Order( total < 60.0 )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Order( total > 12.0 )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Order( total <= 4.0 )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Order.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        Order o1 = new Order();
        o1.setTotal(18.0);
        ksession.insert(o1);
        int fired = ksession.fireAllRules();
        assertEquals(5, fired);

        Order o2 = new Order();
        o2.setTotal(60.0);
        ksession.insert(o2);
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }

    @Test
    public void testString() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( name >= \"Ann\" )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( name < \"Bob\" )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Person( name > \"Kent\" )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Person( name < \"Steve\" )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Person( name > \"John\" )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Person( name <= \"Paul\" )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        ksession.insert(new Person("John"));
        int fired = ksession.fireAllRules();
        assertEquals(3, fired);

        ksession.insert(new Person("Paul"));
        fired = ksession.fireAllRules();
        assertEquals(5, fired);
    }

    @Test
    public void testBigDecimal() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Primitives.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Primitives( bigDecimal >= 18.0 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Primitives( bigDecimal < 25.0 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Primitives( bigDecimal > 8.0 )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Primitives( bigDecimal < 60.0 )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Primitives( bigDecimal > 12.0 )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Primitives( bigDecimal <= 4.0 )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Primitives.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        Primitives p1 = new Primitives();
        p1.setBigDecimal(new BigDecimal("18.0"));
        ksession.insert(p1);
        int fired = ksession.fireAllRules();
        assertEquals(5, fired);

        Primitives p2 = new Primitives();
        p2.setBigDecimal(new BigDecimal("60.0"));
        ksession.insert(p2);
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }

    @Test
    public void testNull() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Primitives.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Primitives( bigDecimal >= null )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Primitives( bigDecimal < 25.0 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Primitives( bigDecimal > 8.0 )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Primitives( bigDecimal < 60.0 )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Primitives( bigDecimal > 12.0 )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Primitives( bigDecimal <= 4.0 )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Primitives.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(5, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());
        assertEquals(1, sinkAdapter.getOtherSinks().size()); // bigDecimal >= null 

        Primitives p1 = new Primitives();
        p1.setBigDecimal(new BigDecimal("18.0"));
        ksession.insert(p1);
        int fired = ksession.fireAllRules();
        assertEquals(4, fired);

        Primitives p2 = new Primitives();
        p2.setBigDecimal(null);
        ksession.insert(p2);
        fired = ksession.fireAllRules();
        assertEquals(0, fired); // bigDecimal >= null is false
    }

    @Test
    public void testEmpty() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( name >= \"Ann\" )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( name < \"Bob\" )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Person( name > \"\" )\n" + // this is comparable. Smallest
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Person( name < \"Steve\" )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Person( name > \"John\" )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Person( name <= \"Paul\" )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        ksession.insert(new Person("John"));
        int fired = ksession.fireAllRules();
        assertEquals(4, fired);

        ksession.insert(new Person(""));
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }

    @Test
    public void testDate() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Order.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Order( date >= \"01-Oct-2020\" )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Order( date < \"01-Nov-2020\" )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Order( date > \"01-Oct-2010\" )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Order( date < \"01-Oct-2030\" )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Order( date > \"02-Oct-2020\" )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Order( date <= \"02-Apr-2020\" )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Order.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());

        // DROOLS-5712 : Executable model doesn't add index for Date
        if (!kieBaseTestConfiguration.getExecutableModelProjectClass().isPresent()) {
            assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());
        }

        Order o1 = new Order();
        o1.setDate(DateUtils.parseDate("01-Oct-2020"));
        ksession.insert(o1);
        int fired = ksession.fireAllRules();
        assertEquals(4, fired);

        Order o2 = new Order();
        o2.setDate(DateUtils.parseDate("31-Dec-2010"));
        ksession.insert(o2);
        fired = ksession.fireAllRules();
        assertEquals(4, fired);
    }

    @Test
    public void testUnderThreshold() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( age >= 18 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( age < 25 )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(2, sinks.length);
        assertEquals(2, sinkAdapter.size());
        assertEquals(2, sinkAdapter.getRangeIndexableSinks().size()); // under threshold so not yet indexed
        assertNull(sinkAdapter.getRangeIndexMap());

        ksession.insert(new Person("John", 18));
        int fired = ksession.fireAllRules();
        assertEquals(2, fired);

        ksession.insert(new Person("Paul", 60));
        fired = ksession.fireAllRules();
        assertEquals(1, fired);
    }

    @Test
    public void testSurroundingRange() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( age >= 0 && < 20 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( age >= 20 && < 40 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Person( age >= 40 && < 60 )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(3, sinks.length);
        assertEquals(3, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(3, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        AlphaNode alphaNode1 = (AlphaNode) sinks[0];
        ObjectSinkPropagator objectSinkPropagator = alphaNode1.getObjectSinkPropagator();
        assertTrue(objectSinkPropagator instanceof SingleObjectSinkAdapter);
        ObjectSink objectSink = objectSinkPropagator.getSinks()[0];
        assertTrue(objectSink instanceof AlphaNode); // [age < 20] is the next single AlphaNode of [age >= 0]. Cannot be indexed.

        ksession.insert(new Person("John", 18));
        int fired = ksession.fireAllRules();
        assertEquals(1, fired);

        ksession.insert(new Person("Paul", 60));
        fired = ksession.fireAllRules();
        assertEquals(0, fired);
    }

    @Test
    public void testRemoveObjectSink() {
        final String drl = BASIC_DRL;

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession1 = kbase.newKieSession();

        ksession1.insert(new Person("John", 18));
        int fired1 = ksession1.fireAllRules();
        assertEquals(5, fired1);

        ksession1.insert(new Person("Paul", 60));
        fired1 = ksession1.fireAllRules();
        assertEquals(3, fired1);
        ksession1.dispose();

        // remove 2 rules
        kbase.removeRule("org.drools.compiler.test", "test2");
        kbase.removeRule("org.drools.compiler.test", "test3");

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(4, sinks.length);
        assertEquals(4, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(4, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size()); // still above threshold

        final KieSession ksession2 = kbase.newKieSession();

        ksession2.insert(new Person("John", 18));
        int fired2 = ksession2.fireAllRules();
        assertEquals(3, fired2);

        ksession2.insert(new Person("Paul", 60));
        fired2 = ksession2.fireAllRules();
        assertEquals(2, fired2);
        ksession2.dispose();

        // remove 2 more rules
        kbase.removeRule("org.drools.compiler.test", "test4");
        kbase.removeRule("org.drools.compiler.test", "test5");

        final ObjectTypeNode otn2 = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn2);
        final CompositeObjectSinkAdapter sinkAdapter2 = (CompositeObjectSinkAdapter) otn2.getObjectSinkPropagator();
        ObjectSink[] sinks2 = sinkAdapter2.getSinks();
        assertEquals(2, sinks2.length);
        assertEquals(2, sinkAdapter2.size());
        assertEquals(2, sinkAdapter2.getRangeIndexableSinks().size()); // under threshold so put back from rangeIndex
        assertNull(sinkAdapter2.getRangeIndexMap());

        final KieSession ksession3 = kbase.newKieSession();

        ksession3.insert(new Person("John", 18));
        int fired3 = ksession3.fireAllRules();
        assertEquals(1, fired3);

        ksession3.insert(new Person("Paul", 60));
        fired3 = ksession3.fireAllRules();
        assertEquals(1, fired3);
        ksession3.dispose();
    }

    @Test
    public void testCustomComparable() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + MyComparableHolder.class.getCanonicalName() + "\n" +
                           "import " + MyComparable.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   MyComparableHolder( myComparable >= MyComparable.ABC )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   MyComparableHolder( myComparable < MyComparable.DEF )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   MyComparableHolder( myComparable > MyComparable.GHI )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   MyComparableHolder( myComparable < MyComparable.JKL )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   MyComparableHolder( myComparable > MyComparable.MNO )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   MyComparableHolder( myComparable <= MyComparable.PQR )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, MyComparableHolder.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());

        // Doesn't support Object type for range index. See CompositeObjectSinkAdapter.isRangeIndexable()
        assertNull(sinkAdapter.getRangeIndexMap());

        MyComparable abc = new MyComparable("ABC", 1);
        ksession.insert(new MyComparableHolder(abc));
        int fired = ksession.fireAllRules();
        assertEquals(4, fired);

        MyComparable jkl = new MyComparable("JKL", 10);
        ksession.insert(new MyComparableHolder(jkl));
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }

    @Test
    public void testNestedProps() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( address.number >= 18 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( address.number < 25 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Person( address.number > 8 )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Person( address.number < 60 )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Person( address.number > 12 )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Person( address.number <= 4 )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());

        // Doesn't support nested prop for range index. See CompositeObjectSinkAdapter.isRangeIndexable()
        assertNull(sinkAdapter.getRangeIndexMap());

        Person person1 = new Person("John", 18);
        person1.setAddress(new Address("ABC street", 18, "London"));
        ksession.insert(person1);
        int fired = ksession.fireAllRules();
        assertEquals(5, fired);

        Person person2 = new Person("Paul", 60);
        person2.setAddress(new Address("XYZ street", 60, "London"));
        ksession.insert(person2);
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }

    @Test
    public void testMultipleProps() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( age >= 18 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( age < 25 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Person( age > 8 )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Person( age < 60 )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Person( age > 12 )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   Person( age <= 4 )\n" +
                           "then\n end\n" +
                           "rule test7\n when\n" +
                           "   Person( name >= \"Ann\" )\n" +
                           "then\n end\n" +
                           "rule test8\n when\n" +
                           "   Person( name < \"Bob\" )\n" +
                           "then\n end\n" +
                           "rule test9\n when\n" +
                           "   Person( name > \"Kent\" )\n" +
                           "then\n end\n" +
                           "rule test10\n when\n" +
                           "   Person( name < \"Steve\" )\n" +
                           "then\n end\n" +
                           "rule test11\n when\n" +
                           "   Person( name > \"John\" )\n" +
                           "then\n end\n" +
                           "rule test12\n when\n" +
                           "   Person( name <= \"Paul\" )\n" +
                           "then\n end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(12, sinks.length);
        assertEquals(12, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        Collection<AlphaRangeIndex> values = sinkAdapter.getRangeIndexMap().values();
        assertEquals(2, values.size());
        for (AlphaRangeIndex alphaRangeIndex : values) {
            assertEquals(6, alphaRangeIndex.size()); // a tree for "age" has 6 nodes. a tree for "name" has 6 nodes
        }

        ksession.insert(new Person("John", 18));
        int fired = ksession.fireAllRules();
        assertEquals(8, fired);

        ksession.insert(new Person("Paul", 60));
        fired = ksession.fireAllRules();
        assertEquals(8, fired);
    }

    @Test
    public void testModify() {
        final String drl =
                "package org.drools.compiler.test\n" +
                           "import " + Person.class.getCanonicalName() + "\n" +
                           "rule test1\n when\n" +
                           "   Person( age >= 18 )\n" +
                           "then\n end\n" +
                           "rule test2\n when\n" +
                           "   Person( age < 25 )\n" +
                           "then\n end\n" +
                           "rule test3\n when\n" +
                           "   Person( age > 8 )\n" +
                           "then\n end\n" +
                           "rule test4\n when\n" +
                           "   Person( age < 60 )\n" +
                           "then\n end\n" +
                           "rule test5\n when\n" +
                           "   Person( age > 12 )\n" +
                           "then\n end\n" +
                           "rule test6\n when\n" +
                           "   $p : Person( age <= 4 )\n" +
                           "then\n" +
                           "  modify($p) { setAge(90) }\n" +
                           "end\n";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("indexing-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size());

        ksession.insert(new Person("John", 0));
        int fired = ksession.fireAllRules();
        assertEquals(6, fired);

    }

    @Test
    public void testIncrementalCompilation() {

        // 2 rules under threshold
        final String drl1 =
                "package org.drools.compiler.test\n" +
                            "import " + Person.class.getCanonicalName() + "\n" +
                            "rule test1\n when\n" +
                            "   Person( age >= 18 )\n" +
                            "then\n end\n" +
                            "rule test2\n when\n" +
                            "   Person( age < 25 )\n" +
                            "then\n end\n";

        // 6 rules over threshold
        final String drl2 =
                "package org.drools.compiler.test\n" +
                            "import " + Person.class.getCanonicalName() + "\n" +
                            "rule test1\n when\n" +
                            "   Person( age >= 18 )\n" +
                            "then\n end\n" +
                            "rule test2\n when\n" +
                            "   Person( age < 25 )\n" +
                            "then\n end\n" +
                            "rule test3\n when\n" +
                            "   Person( age > 8 )\n" +
                            "then\n end\n" +
                            "rule test4\n when\n" +
                            "   Person( age < 60 )\n" +
                            "then\n end\n" +
                            "rule test5\n when\n" +
                            "   Person( age > 12 )\n" +
                            "then\n end\n" +
                            "rule test6\n when\n" +
                            "   Person( age <= 4 )\n" +
                            "then\n end\n";

        // 2 rules under threshold
        final String drl3 =
                "package org.drools.compiler.test\n" +
                            "import " + Person.class.getCanonicalName() + "\n" +
                            "rule test5\n when\n" +
                            "   Person( age > 12 )\n" +
                            "then\n end\n" +
                            "rule test6\n when\n" +
                            "   Person( age <= 4 )\n" +
                            "then\n end\n";

        final KieServices ks = KieServices.Factory.get();

        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);

        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        KieSession ksession = kc.newKieSession();

        ObjectTypeNode otn = KieUtil.getObjectTypeNode(ksession.getKieBase(), Person.class);
        assertNotNull(otn);
        CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(2, sinks.length);
        assertEquals(2, sinkAdapter.size());
        assertEquals(2, sinkAdapter.getRangeIndexableSinks().size()); // under threshold so not yet indexed
        assertNull(sinkAdapter.getRangeIndexMap());

        ksession.insert(new Person("John", 18));
        int fired = ksession.fireAllRules();
        assertEquals(2, fired);
        ksession.dispose();

        // Create a new jar for version 1.1.0
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);

        // try to update the container to version 1.1.0
        kc.updateToVersion(releaseId2);

        // create and use a new session
        ksession = kc.newKieSession();

        otn = KieUtil.getObjectTypeNode(ksession.getKieBase(), Person.class);
        assertNotNull(otn);
        sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertNull(sinkAdapter.getRangeIndexableSinks());
        assertEquals(6, sinkAdapter.getRangeIndexMap().entrySet().iterator().next().getValue().size()); // now fully indexed

        ksession.insert(new Person("Paul", 18));
        fired = ksession.fireAllRules();
        assertEquals(5, fired);
        ksession.dispose();

        // Create a new jar for version 1.2.0
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.2.0");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration, drl3);

        // try to update the container to version 1.2.0
        kc.updateToVersion(releaseId3);

        // create and use a new session
        ksession = kc.newKieSession();

        otn = KieUtil.getObjectTypeNode(ksession.getKieBase(), Person.class);
        assertNotNull(otn);
        sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        sinks = sinkAdapter.getSinks();
        assertEquals(2, sinks.length);
        assertEquals(2, sinkAdapter.size());
        assertEquals(2, sinkAdapter.getRangeIndexableSinks().size()); // under threshold so back to rangeIndexableSinks
        assertNull(sinkAdapter.getRangeIndexMap());

        ksession.insert(new Person("George", 18));
        fired = ksession.fireAllRules();
        assertEquals(1, fired);
        ksession.dispose();
    }

    @Test
    public void testThresholdOption() {
        final String drl = BASIC_DRL;

        final KieModule kieModule = KieUtil.getKieModuleFromDrls("indexing-test", kieBaseTestConfiguration, drl);
        final KieContainer kieContainer = KieServices.get().newKieContainer(kieModule.getReleaseId());
        final KieBaseConfiguration kieBaseConfiguration = kieBaseTestConfiguration.getKieBaseConfiguration();
        kieBaseConfiguration.setOption(AlphaRangeIndexThresholdOption.get(7));
        final KieBase kbase = kieContainer.newKieBase(kieBaseConfiguration);

        final KieSession ksession = kbase.newKieSession();

        final ObjectTypeNode otn = KieUtil.getObjectTypeNode(kbase, Person.class);
        assertNotNull(otn);
        final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
        ObjectSink[] sinks = sinkAdapter.getSinks();
        assertEquals(6, sinks.length);
        assertEquals(6, sinkAdapter.size());
        assertEquals(6, sinkAdapter.getRangeIndexableSinks().size()); // under threshold so not yet indexed
        assertNull(sinkAdapter.getRangeIndexMap());

        ksession.insert(new Person("John", 18));
        int fired = ksession.fireAllRules();
        assertEquals(5, fired);

        ksession.insert(new Person("Paul", 60));
        fired = ksession.fireAllRules();
        assertEquals(3, fired);
    }
}
