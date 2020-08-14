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
import org.drools.modelcompiler.domain.FactASuper;
import org.drools.modelcompiler.domain.FactBSub;
import org.drools.modelcompiler.domain.FactCSub;
import org.drools.modelcompiler.domain.FactDSubSub;
import org.drools.modelcompiler.domain.Person;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.AlphaNodeOrderingOption;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.assertEquals;

public class AlphaNodeOrderingInstanceofTest extends BaseModelTest {

//        private static final AlphaNodeOrderingOption TEST_OPTION = AlphaNodeOrderingOption.NONE;
    private static final AlphaNodeOrderingOption TEST_OPTION = AlphaNodeOrderingOption.COUNT;

    private static final String EXTERNALISE_LAMBDA = Boolean.TRUE.toString();
    
    // just for quick test
    final static Object[] QUICK_TEST = {
                                      RUN_TYPE.STANDARD_FROM_DRL,
//                                      RUN_TYPE.PATTERN_DSL,
    };

//    @Parameters(name = "{0}")
//    public static Object[] params() {
//        return QUICK_TEST;
//    }

    public AlphaNodeOrderingInstanceofTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testInstanceof() {

        // name != "Paul" has larger usage count.
        // So if you reorder, it results in [name != "Paul", this instanceof FactBSub] for R1
        //   -> FactASuper.getName() throws UnsupportedOperationException 

        String str =
                "import " + FactASuper.class.getCanonicalName() + "\n" +
                     "import " + FactBSub.class.getCanonicalName() + "\n" +
                     "import " + FactCSub.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : FactASuper((this instanceof FactBSub), name != \"Paul\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : FactASuper((this instanceof FactCSub), name != \"Paul\")\n" +
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

        ksession.insert(new FactASuper());
        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testInstanceof2() {

        // name != "Paul" has larger usage count.
        // So if you reorder, it results in [name != "Paul", !(this instanceof FactDSubSub)] for R1
        //   -> FactDSubSub.getName() throws UnsupportedOperationException 

        String str =
                "import " + FactASuper.class.getCanonicalName() + "\n" +
                     "import " + FactBSub.class.getCanonicalName() + "\n" +
                     "import " + FactCSub.class.getCanonicalName() + "\n" +
                     "import " + FactDSubSub.class.getCanonicalName() + "\n" +
                     "rule R1 when\n" +
                     "  $p : FactASuper(!(this instanceof FactDSubSub), name != \"Paul\")\n" +
                     "then\n" +
                     "end\n" +
                     "rule R2 when\n" +
                     "  $p : FactASuper(!(this instanceof FactBSub), name != \"Paul\")\n" +
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

        ksession.insert(new FactDSubSub());
        assertEquals(0, ksession.fireAllRules());
    }
}
