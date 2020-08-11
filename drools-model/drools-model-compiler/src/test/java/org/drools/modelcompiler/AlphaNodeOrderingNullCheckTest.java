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
import org.drools.modelcompiler.BaseModelTest.RUN_TYPE;
import org.drools.modelcompiler.domain.Address;
import org.drools.modelcompiler.domain.Person;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.AlphaNodeOrderingOption;
import org.kie.api.runtime.KieSession;

import static org.drools.modelcompiler.BaseModelTest.RUN_TYPE.FLOW_DSL;
import static org.drools.modelcompiler.BaseModelTest.RUN_TYPE.PATTERN_DSL;
import static org.junit.Assert.assertEquals;

public class AlphaNodeOrderingNullCheckTest extends BaseModelTest {

    // just for quick test
    final static Object[] STANDARD = {
                                      RUN_TYPE.STANDARD_FROM_DRL,
    };

    @Parameters(name = "{0}")
    public static Object[] params() {
        return STANDARD;
    }

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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

        // Generates 2 MvelConstraints address != null, address.street == "ABC street"

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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
    public void testNullCheckAnd() {

        // Split to 2 MvelConstraints likes != null, address != null

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        assertEquals(5, alphaNodes.size()); // 5 in case of AlphaNodeOrderingOption.NONE

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOr() {

        // Converted to 2 MvelConstraints !( likes == null ), !( address == null )

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        assertEquals(5, alphaNodes.size()); // 5 in case of AlphaNodeOrderingOption.NONE

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testNullCheckOrInverse() {

        // Converted&Normalized to 2 MvelConstraints !( likes == null ), !( address == null )

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        assertEquals(5, alphaNodes.size()); // 5 in case of AlphaNodeOrderingOption.NONE

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        assertEquals(4, alphaNodes.size()); // 4 in case of AlphaNodeOrderingOption.NONE

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
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());
        assertEquals(4, alphaNodes.size()); // 4 in case of AlphaNodeOrderingOption.NONE

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
                     "  $p : Person(name != \"Mario\", age > 20, address.street == \"ABC street\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R3 when\n" +
                     "  $p : Person(name != \"Mario\")\n" +
                     "then\n" +
                     "end\n";

        KieModuleModel model = KieServices.get().newKieModuleModel();
        model
             //.setConfigurationProperty("drools.externaliseCanonicalModelLambda", Boolean.FALSE.toString())
             .newKieBaseModel("kb")
             .setDefault(true)
             .setAlphaNodeOrdering(AlphaNodeOrderingOption.COUNT)
             .newKieSessionModel("ks")
             .setDefault(true);

        KieSession ksession = getKieSession(model, str);

        List<AlphaNode> alphaNodes = ReteDumper.collectNodes(ksession)
                                               .stream()
                                               .filter(AlphaNode.class::isInstance)
                                               .map(node -> (AlphaNode) node)
                                               .collect(Collectors.toList());

        ReteDumper.dumpRete(ksession);
        
        System.out.println(alphaNodes.size());

        // Reduced!
        assertEquals(5, alphaNodes.size()); // 6 in case of AlphaNodeOrderingOption.NONE

        ksession.insert(new Person("Mario"));
        assertEquals(0, ksession.fireAllRules());
    }
}
