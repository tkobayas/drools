/*
 * Copyright 2020 JBoss Inc
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

package org.drools.modelcompiler;

import java.util.List;
import java.util.stream.Collectors;

import org.drools.core.reteoo.AlphaNode;
import org.drools.modelcompiler.domain.Address;
import org.drools.modelcompiler.domain.Person;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.AlphaNodeOrderingOption;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.assertEquals;

public class AlphaNodeOrderingNullCheckTest extends BaseModelTest {

            private static final AlphaNodeOrderingOption TEST_OPTION = AlphaNodeOrderingOption.NONE;
//    private static final AlphaNodeOrderingOption TEST_OPTION = AlphaNodeOrderingOption.COUNT;

    private static final String EXTERNALISE_LAMBDA = Boolean.TRUE.toString();

    // just for quick test
    final static Object[] QUICK_TEST = {
                                        RUN_TYPE.STANDARD_FROM_DRL,
            //                                      RUN_TYPE.PATTERN_DSL,
    };

    //        @Parameters(name = "{0}")
    //        public static Object[] params() {
    //            return QUICK_TEST;
    //        }

    public AlphaNodeOrderingNullCheckTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testNullCheckSimple() {
        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address != null, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckInverse() {

        // Normalized thanks to ConstrainUtil address != null

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(null != address, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        assertEquals(4, alphaNodes.size()); // 4 in case of AlphaNodeOrderingOption.NONE

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullSafeDereference() {

        // [STANDARD_FROM_DRL] Generates 2 MvelConstraints address != null, address.street == "ABC street"
        // [PATTERN_DSL] 1 LambdaConstraint address!.street == "ABC street"

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address!.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        if (testRunType.equals(RUN_TYPE.STANDARD_FROM_DRL)) {
            assertEquals(4, alphaNodes.size());
        } else {
            assertEquals(3, alphaNodes.size());
        }
        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckAnd() {

        // [STANDARD_FROM_DRL] Split to 2 MvelConstraints likes != null, address != null
        // [PATTERN_DSL] 1 LambdaConstraint (likes != null && address != null)

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person((likes != null && address != null), address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        ReteDumper.dumpRete(ksession);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        if (testRunType.equals(RUN_TYPE.STANDARD_FROM_DRL)) {
            assertEquals(5, alphaNodes.size());
        } else {
            assertEquals(4, alphaNodes.size());
        }

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOr() {

        // [STANDARD_FROM_DRL] Converted to 2 MvelConstraints !( likes == null ), !( address == null )
        // [PATTERN_DSL] 1 LambdaConstraint !(likes == null || address == null)

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(!(likes == null || address == null), address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        if (testRunType.equals(RUN_TYPE.STANDARD_FROM_DRL)) {
            assertEquals(5, alphaNodes.size());
        } else {
            assertEquals(4, alphaNodes.size());
        }

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOrExtraSpace() {

        // [STANDARD_FROM_DRL] Converted to 2 MvelConstraints !( likes == null ), !( address == null )
        // [PATTERN_DSL] 1 LambdaConstraint ! (likes == null || address == null)

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(! (likes == null || address == null), address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        if (testRunType.equals(RUN_TYPE.STANDARD_FROM_DRL)) {
            assertEquals(5, alphaNodes.size());
        } else {
            assertEquals(4, alphaNodes.size());
        }

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOrInverse() {

        // [STANDARD_FROM_DRL] Converted&Normalized to 2 MvelConstraints !( likes == null ), !( address == null )
        // [PATTERN_DSL] 1 LambdaConstraint !(null == likes || null == address)

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(!(null == likes || null == address), address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        if (testRunType.equals(RUN_TYPE.STANDARD_FROM_DRL)) {
            assertEquals(5, alphaNodes.size());
        } else {
            assertEquals(4, alphaNodes.size());
        }

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOrUnsplit() {

        // Actually, not a right null-check
        // 1 MvelConstraint !( likes == null ) || !( address == null )

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person((!(likes == null) || !(address == null)), address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person( name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOr2() {

        // 1 MvelConstraint (address == null || address.street == \"ABC street\")

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address == null || address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        //ReteDumper.dumpRete(ksession);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(3, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckComplex() {

        // Actually, not a right null-check
        // 1 MvelConstraint (likes == "beer" && address == "XXX") || address != null

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(((likes == \"beer\" && address == \"XXX\") || (address != null)), address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckGetter() {

        // 1 MvelConstraint getAddress() != null

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(getAddress() != null, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckThisGetter() {

        // 1 MvelConstraint this.getAddress() != null

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(this.getAddress() != null, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckNestedProp() {

        // 1 MvelConstraint address.street != null

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address.street != null, address.street.length() == 3)\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street.length() == 3)\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        Person person = new Person("Mario");
        person.setAddress(new Address(null, 1, "XYZ"));
        ksession.insert(person);
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckNestedGetter() {

        // 1 MvelConstraint getAddress().getStreet() != null

        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(getAddress().getStreet() != null, address.street.length() == 3)\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", address.street.length() == 3)\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        Person person = new Person("Mario");
        person.setAddress(new Address(null, 1, "XYZ"));
        ksession.insert(person);
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckSimpleReorder() {
        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address != null, address.street == \"ABC street\", name != \"Mario\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(address != null, name != \"Mario\", age > 20, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        ReteDumper.dumpRete(ksession);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());

        // Reduced!
        if (TEST_OPTION == AlphaNodeOrderingOption.NONE) {
            assertEquals(7, alphaNodes.size());
        } else if (TEST_OPTION == AlphaNodeOrderingOption.COUNT) {
            assertEquals(4, alphaNodes.size());
        }

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckInbetween() {
        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address != null, address.street == \"ABC street\", name != \"Mario\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", age > 20, address != null, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        ReteDumper.dumpRete(ksession);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());

        // Reduced!
        if (TEST_OPTION == AlphaNodeOrderingOption.NONE) {
            assertEquals(7, alphaNodes.size());
        } else if (TEST_OPTION == AlphaNodeOrderingOption.COUNT) {
            assertEquals(4, alphaNodes.size());
        }

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckMap() {
        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(itemsString[\"ABC\"] != null, itemsString[\"ABC\"].length() == 5)\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", itemsString[\"ABC\"].length() == 5)\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        Person person = new Person("Mario");
        person.getItemsString().put("ABC", null);
        ksession.insert(person);
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckList() {
        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(addresses[1] != null, addresses[1].street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name != \"Mario\", addresses[1].street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        Person person = new Person("Mario");
        person.getAddresses().add(new Address("XXX", 0, "ZZZ"));
        person.getAddresses().add(null);
        person.getAddresses().add(new Address("YYY", 2, "ZZZ"));
        ksession.insert(person);
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckTrim() {
        String str =
                "import " + Person.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : Person(address!=null, address.street==\"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : Person(name!=\"Mario\", address.street==\"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name!=\"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             .setConfigurationProperty("drools.externaliseCanonicalModelLambda", EXTERNALISE_LAMBDA)
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(TEST_OPTION)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        // don't reorder
        assertEquals(4, alphaNodes.size());

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }
}
