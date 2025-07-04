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
package org.drools.model.codegen.execmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.runtime.KieSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TypeObjectCoercionTest extends BaseModelTest {

    private KieSession getKieSessionForJoinObjectToString(RUN_TYPE runType) {

        // NOTE: If we write a test with IntegerHolder instead of ObjectHolder, standard-drl fails with a compilation error
        //    text=Unable to Analyse Expression value >= $i:
        // [Error: Comparison operation requires compatible types. Found class java.lang.String and class java.lang.Integer]
        //        [Near : {... value >= $i ....}]

        final String drl1 =
                "import " + ObjectHolder.class.getCanonicalName() + ";\n" +
                            "import " + StringHolder.class.getCanonicalName() + ";\n" +
                            "rule R when\n" +
                            "    ObjectHolder($o : value)\n" +
                            "    StringHolder( value > $o )\n" + // Left is String. Right is Object
                            "then\n" +
                            "end\n";

        return getKieSession(runType, drl1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToString1(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToString(runType);

        // String "10" > Integer 5
        ksession.insert(new ObjectHolder(Integer.valueOf(5)));
        ksession.insert(new StringHolder("10"));
        assertThat(ksession.fireAllRules()).isEqualTo(1); // standard-drl : 10 > 5  (Number comparison)
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToString2(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToString(runType);

        // String "10" > String "5"
        ksession.insert(new ObjectHolder("5"));
        ksession.insert(new StringHolder("10"));
        assertThat(ksession.fireAllRules()).isEqualTo(0); // standard-drl : "10" < "5"  (String comparison)
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToString3(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToString(runType);

        // String "ABC" > Integer 5
        ksession.insert(new ObjectHolder(Integer.valueOf(5)));
        ksession.insert(new StringHolder("ABC"));
        
        assertThatThrownBy(()->ksession.fireAllRules()).isInstanceOf(RuntimeException.class); // standard-drl : ClassCastException: java.lang.Integer cannot be cast to java.lang.String
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToString4(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToString(runType);

        // String "10" > String "ABC"
        ksession.insert(new ObjectHolder("ABC"));
        ksession.insert(new StringHolder("10"));
        assertThat(ksession.fireAllRules()).isEqualTo(0); // standard-drl : "10" < "ABC" (String comparison)
        ksession.dispose();
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToString5(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToString(runType);

        // String "ABC" > String "DEF"
        ksession.insert(new ObjectHolder("DEF"));
        ksession.insert(new StringHolder("ABC"));
        assertThat(ksession.fireAllRules()).isEqualTo(0); // standard-drl : "ABC" < "DEF" (String comparison)
        ksession.dispose();
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToStringNonComparable(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToString(runType);

        // String "10" > Object
        ksession.insert(new ObjectHolder(new Object())); // not Comparable
        ksession.insert(new StringHolder("10"));
            // in case of standard-drl, MathProcessor.doOperationNonNumeric() throws ClassCastException when the right operand is not comparable to the left operand
            // Caused by ClassCastException: class java.lang.Object cannot be cast to class java.lang.String
        assertThatThrownBy(()->ksession.fireAllRules()).isInstanceOf(RuntimeException.class);
        
    }

    private KieSession getKieSessionForJoinStringToObject(RUN_TYPE runType) {

        final String drl1 =
                "import " + ObjectHolder.class.getCanonicalName() + ";\n" +
                            "import " + StringHolder.class.getCanonicalName() + ";\n" +
                            "rule R when\n" +
                            "    StringHolder($s : value)\n" +
                            "    ObjectHolder( value > $s )\n" + // Left is Object. Right is String
                            "then\n" +
                            "end\n";

        return getKieSession(runType, drl1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinStringToObject1(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinStringToObject(runType);

        // Integer 5 > String "10"
        ksession.insert(new StringHolder("10"));
        ksession.insert(new ObjectHolder(Integer.valueOf(5)));
        assertThat(ksession.fireAllRules()).isEqualTo(0); // standard-drl : 5 < 10  (Number comparison)
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinStringToObject2(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinStringToObject(runType);

        // String "5" > String "10"
        ksession.insert(new StringHolder("10"));
        ksession.insert(new ObjectHolder("5"));
        assertThat(ksession.fireAllRules()).isEqualTo(1); // standard-drl : "5" > "10"  (String comparison)
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinStringToObject3(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinStringToObject(runType);

        // Integer 5 > String "ABC"
        ksession.insert(new StringHolder("ABC"));
        ksession.insert(new ObjectHolder(Integer.valueOf(5)));

       
        assertThatThrownBy(()->ksession.fireAllRules()).isInstanceOf(RuntimeException.class); // standard-drl : Caused by ClassCastException: java.lang.String cannot be cast to java.lang.Integer

    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinStringToObject4(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinStringToObject(runType);

        // String "ABC" > String "10"
        ksession.insert(new StringHolder("10"));
        ksession.insert(new ObjectHolder("ABC"));
        assertThat(ksession.fireAllRules()).isEqualTo(1); // standard-drl : "ABC" > "10" (String comparison)
        ksession.dispose();
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinStringToObject5(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinStringToObject(runType);

        // String "DEF" > String "ABC"
        ksession.insert(new StringHolder("ABC"));
        ksession.insert(new ObjectHolder("DEF"));
        assertThat(ksession.fireAllRules()).isEqualTo(1); // standard-drl : "DEF" < "ABC" (String comparison)
        ksession.dispose();
    }

    private KieSession getKieSessionForJoinIntegerToObject(RUN_TYPE runType) {

        final String drl1 =
                "import " + ObjectHolder.class.getCanonicalName() + ";\n" +
                            "import " + IntegerHolder.class.getCanonicalName() + ";\n" +
                            "rule R when\n" +
                            "    IntegerHolder($i : value)\n" +
                            "    ObjectHolder( value > $i )\n" + // Left is Object. Right is Integer
                            "then\n" +
                            "end\n";

        return getKieSession(runType, drl1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinIntegerToObject1(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinIntegerToObject(runType);

        // Integer 10 > Integer 5
        ksession.insert(new IntegerHolder(Integer.valueOf(5)));
        ksession.insert(new ObjectHolder(Integer.valueOf(10)));
        assertThat(ksession.fireAllRules()).isEqualTo(1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinIntegerToObject2(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinIntegerToObject(runType);

        // String "10" > Integer 5
        ksession.insert(new IntegerHolder(Integer.valueOf(5)));
        ksession.insert(new ObjectHolder(new String("10")));
        assertThat(ksession.fireAllRules()).isEqualTo(1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinIntegerToObject3(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinIntegerToObject(runType);

        // String "ABC" > Integer 5
        ksession.insert(new IntegerHolder(Integer.valueOf(5)));
        ksession.insert(new ObjectHolder(new String("ABC")));

    	assertThatThrownBy(()->ksession.fireAllRules()).isInstanceOf(RuntimeException.class);  // standard-drl : Caused by ClassCastException: java.lang.String cannot be cast to java.lang.Integer
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinIntegerToObjectNonComparable(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinIntegerToObject(runType);

        // Object > Integer 5
        ksession.insert(new IntegerHolder(Integer.valueOf(5)));
        ksession.insert(new ObjectHolder(new Object())); // not Comparable
        assertThat(ksession.fireAllRules()).isEqualTo(0); // in case of standard-drl, MathProcessor.doOperationNonNumeric() returns false when the left operand is not Comparable
    }

    private KieSession getKieSessionForJoinObjectToInteger(RUN_TYPE runType) {

        final String drl1 =
                "import " + ObjectHolder.class.getCanonicalName() + ";\n" +
                            "import " + IntegerHolder.class.getCanonicalName() + ";\n" +
                            "rule R when\n" +
                            "    ObjectHolder($o : value)\n" +
                            "    IntegerHolder( value > $o )\n" + // Left is Integer. Right is Object
                            "then\n" +
                            "end\n";

        return getKieSession(runType, drl1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToInteger1(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToInteger(runType);

        // Integer 10 > Integer 5
        ksession.insert(new ObjectHolder(Integer.valueOf(5)));
        ksession.insert(new IntegerHolder(Integer.valueOf(10)));
        assertThat(ksession.fireAllRules()).isEqualTo(1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToInteger2(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToInteger(runType);

        // Integer 10 > String "5"
        ksession.insert(new ObjectHolder(new String("5")));
        ksession.insert(new IntegerHolder(Integer.valueOf(10)));
        assertThat(ksession.fireAllRules()).isEqualTo(1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToInteger3(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToInteger(runType);

        // Integer 10 > String "ABC"
        ksession.insert(new ObjectHolder(new String("ABC")));
        ksession.insert(new IntegerHolder(Integer.valueOf(10)));

        assertThatThrownBy(()->ksession.fireAllRules()).isInstanceOf(RuntimeException.class); // standard-drl : Caused by ClassCastException: java.lang.String cannot be cast to java.lang.Integer
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToIntegerNonComparable(RUN_TYPE runType) {

        KieSession ksession = getKieSessionForJoinObjectToInteger(runType);

        // Integer 10 > Object
        ksession.insert(new ObjectHolder(new Object())); // not Comparable
        ksession.insert(new IntegerHolder(Integer.valueOf(10)));
            // in case of standard-drl, MathProcessor.doOperationNonNumeric() throws ClassCastException when the right operand is not comparable to the left operand
            // Caused by ClassCastException: class java.lang.Object cannot be cast to class java.lang.Integer 
        assertThatThrownBy(()->ksession.fireAllRules()).isInstanceOf(RuntimeException.class); 
    }

    public static class ObjectHolder {

        private Object value;

        public ObjectHolder(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class StringHolder {

        private String value;

        public StringHolder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class IntegerHolder {

        private Integer value;

        public IntegerHolder(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testJoinObjectToStringWithMap(RUN_TYPE runType) {

        // This rule mimics JittingTest#testJitMapCoercion()

        final String drl1 =
                "import " + Map.class.getCanonicalName() + ";\n" +
                            "import " + StringHolder.class.getCanonicalName() + ";\n" +
                            "rule R when\n" +
                            "    $map : Map()\n" +
                            "    StringHolder( value > $map.get(\"key\") )\n" +
                            "then\n" +
                            "end\n";

        KieSession ksession = getKieSession(runType, drl1);

        Map<String, Object> map = new HashMap<>();
        map.put("key", 5);
        ksession.insert(map);
        ksession.insert(new StringHolder("10"));
        assertThat(ksession.fireAllRules()).isEqualTo(1);
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testCoercionStringVsObjectIntegerWithMap(RUN_TYPE runType) {
        final String drl = "package org.drools.compiler.integrationtests;\n" +
                           "import " + Map.class.getCanonicalName() + ";\n" +
                           "import " + StringHolder.class.getCanonicalName() + ";\n" +
                           "global java.util.List list;\n" +
                           "rule R\n" +
                           "    when\n" +
                           "        $map : Map()" +
                           "        $holder : StringHolder(value < $map.get(\"key\"))\n" +
                           "    then\n" +
                           "        list.add( $holder );\n" +
                           "end";

        // String is coerced to Integer (thus, Number comparison)

        KieSession ksession =getKieSession(runType, drl);
        try {
            final List<StringHolder> list = new ArrayList<>();
            ksession.setGlobal("list", list);

            final StringHolder holder1 = new StringHolder("1");
            ksession.insert(holder1);

            final StringHolder holder5 = new StringHolder("5");
            ksession.insert(holder5);

            final StringHolder holder10 = new StringHolder("10");
            ksession.insert(holder10);

            Map<String, Object> map = new HashMap<>();
            map.put("key", 5);
            ksession.insert(map);

            ksession.fireAllRules();

            assertThat(list).containsExactly(holder1); // If we do String comparison, cheese10 is also contained
        } finally {
            ksession.dispose();
        }
    }

    @ParameterizedTest
	@MethodSource("parameters")
    public void testCoercionStringVsExplicitIntegerWithMap(RUN_TYPE runType) {
        final String drl = "package org.drools.compiler.integrationtests;\n" +
                           "import " + Map.class.getCanonicalName() + ";\n" +
                           "import " + StringHolder.class.getCanonicalName() + ";\n" +
                           "global java.util.List list;\n" +
                           "rule R\n" +
                           "    when\n" +
                           "        $map : Map()" +
                           "        $holder : StringHolder(value < $map.get(\"key\"))\n" +
                           "    then\n" +
                           "        list.add( $holder );\n" +
                           "end";

        // String is coerced to Integer (thus, Number comparison)

        KieSession ksession =getKieSession(runType, drl);
        try {
            final List<StringHolder> list = new ArrayList<>();
            ksession.setGlobal("list", list);

            final StringHolder holder1 = new StringHolder("1");
            ksession.insert(holder1);

            final StringHolder holder5 = new StringHolder("5");
            ksession.insert(holder5);

            final StringHolder holder10 = new StringHolder("10");
            ksession.insert(holder10);

            Map<String, Integer> map = new HashMap<>();
            map.put("key", 5);
            ksession.insert(map);

            ksession.fireAllRules();

            assertThat(list).containsExactly(holder1); // If we do String comparison, cheese10 is also contained
        } finally {
            ksession.dispose();
        }
    }
}
