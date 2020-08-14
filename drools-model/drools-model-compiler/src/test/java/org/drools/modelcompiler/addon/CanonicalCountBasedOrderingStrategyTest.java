package org.drools.modelcompiler.addon;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.drools.core.base.ClassObjectType;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.modelcompiler.domain.Person;
import org.junit.Test;

public class CanonicalCountBasedOrderingStrategyTest {

    @Test
    public void testPrioritizeConstraintBasic() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "address != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintNegate() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "!(address == null)", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintThis() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "this.address != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "getAddress() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintThisGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "this.getAddress() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintNestedProp() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "address.street != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address.street");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintNestedGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "getAddress().getStreet() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address.street");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintIrregular() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "getAddress( ) .getStreet() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).isNull();
        Assertions.assertThat(strategy.getDenyReorderList()).contains(objectType);
    }

    @Test
    public void testPrioritizeConstraintMap() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "myMap[$p] != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).isNull();
        Assertions.assertThat(strategy.getDenyReorderList()).contains(objectType);
    }

    @Test
    public void testPrioritizeConstraintList() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "myMap[1] != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).isNull();
        Assertions.assertThat(strategy.getDenyReorderList()).contains(objectType);
    }

    @Test
    public void testPrioritizeConstraintInverse() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);

        // In actual run, it will be normalized to "address != null" in standard-drl
        // However, LambdaConstriant retains the original expression "null != address"
        MvelConstraint constraint = new MvelConstraint(null, "null != address", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).isNull();
        Assertions.assertThat(strategy.getDenyReorderList()).contains(objectType);
    }

    @Test
    public void testPrioritizeConstraintAnd() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);

        // In actual run, it will be split to 2 constraints "address != null" and "likes != null" in standard-drl
        // However, LambdaConstriant retains the original expression "address != null && likes != null"
        MvelConstraint constraint = new MvelConstraint(null, "address != null && likes != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).isNull();
        Assertions.assertThat(strategy.getDenyReorderList()).contains(objectType);
    }

    @Test
    public void testPrioritizeConstraintOr() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);

        MvelConstraint constraint = new MvelConstraint(null, "address != null || likes != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).isNull();
        Assertions.assertThat(strategy.getDenyReorderList()).contains(objectType);
    }

    @Test
    public void testPrioritizeConstraintInstanceof() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);

        MvelConstraint constraint = new MvelConstraint(null, "address instanceof Address", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintInstanceofNegate() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);

        MvelConstraint constraint = new MvelConstraint(null, "!(address instanceof Address)", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintTrim() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        ClassObjectType objectType = new ClassObjectType(Person.class);
        MvelConstraint constraint = new MvelConstraint(null, "address!=null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, objectType);
        Assertions.assertThat(strategy.getPrioritizedConstraints().get(constraint)).containsExactlyInAnyOrder("address");
        Assertions.assertThat(strategy.getDenyReorderList()).isEmpty();
    }

    @Test
    public void testSplitExpression() {
        List<String> tokens;

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address != null");
        Assertions.assertThat(tokens).containsExactly("address", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address!=null");
        Assertions.assertThat(tokens).containsExactly("address", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("!( address == null )");
        Assertions.assertThat(tokens).containsExactly("!(", "address", "==", "null", ")");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("!(address==null)");
        Assertions.assertThat(tokens).containsExactly("!(", "address", "==", "null", ")");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("! ( address == null )");
        Assertions.assertThat(tokens).containsExactly("!", "(", "address", "==", "null", ")");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("!( likes == null ) || !( address == null )");
        Assertions.assertThat(tokens).containsExactly("!(", "likes", "==", "null", ")", "||", "!(", "address", "==", "null", ")");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("(likes == \"beer\" && address == \"XXX\") || address != null");
        Assertions.assertThat(tokens).containsExactly("(", "likes", "==", "\"beer\"", "&&", "address", "==", "\"XXX\"", ")", "||", "address", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("likes == \"beer\"");
        Assertions.assertThat(tokens).containsExactly("likes", "==", "\"beer\"");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("( ( likes == \"beer\" ) )");
        Assertions.assertThat(tokens).containsExactly("(", "(", "likes", "==", "\"beer\"", ")", ")");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("getAddress() != null");
        Assertions.assertThat(tokens).containsExactly("getAddress()", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("this.getAddress() != null");
        Assertions.assertThat(tokens).containsExactly("this.getAddress()", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address.street != null");
        Assertions.assertThat(tokens).containsExactly("address.street", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("getAddress().getStreet() != null");
        Assertions.assertThat(tokens).containsExactly("getAddress().getStreet()", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("getAddress( ) .getStreet() != null");
        Assertions.assertThat(tokens).containsExactly("getAddress", "(", ")", ".", "getStreet()", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address instanceof Address");
        Assertions.assertThat(tokens).containsExactly("address", "instanceof", "Address");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address instanceof org.example.Address");
        Assertions.assertThat(tokens).containsExactly("address", "instanceof", "org.example.Address");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("!( address instanceof Address )");
        Assertions.assertThat(tokens).containsExactly("!(", "address", "instanceof", "Address", ")");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("myMethod(address) != null");
        Assertions.assertThat(tokens).containsExactly("myMethod", "(", "address", ")", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("myMap.get(\"key\") != null");
        Assertions.assertThat(tokens).containsExactly("myMap.get", "(", "\"key\"", ")", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("myMap[$p] != null");
        Assertions.assertThat(tokens).containsExactly("myMap", "[", "$p", "]", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("myList[1] != null");
        Assertions.assertThat(tokens).containsExactly("myList", "[1]", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address\t != null");
        Assertions.assertThat(tokens).containsExactly("address", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address\n != null");
        Assertions.assertThat(tokens).containsExactly("address", "!=", "null");

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("nullableField != null");
        Assertions.assertThat(tokens).containsExactly("nullableField", "!=", "null");
    }

    @Test
    public void testGetCheckedPropBasic() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        MvelConstraint constraint = new MvelConstraint(null, "age != 50", null, null, null, null, null);
        List<String> checkedProp = strategy.getCheckedProp(constraint, Person.class);
        Assertions.assertThat(checkedProp).containsExactlyInAnyOrder("age");
    }

    @Test
    public void testGetCheckedPropThis() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        MvelConstraint constraint = new MvelConstraint(null, "this.age != 50", null, null, null, null, null);
        List<String> checkedProp = strategy.getCheckedProp(constraint, Person.class);
        Assertions.assertThat(checkedProp).containsExactlyInAnyOrder("age");
    }

    @Test
    public void testGetCheckedPropNestedProp() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        MvelConstraint constraint = new MvelConstraint(null, "address.number != 6", null, null, null, null, null);
        List<String> checkedProp = strategy.getCheckedProp(constraint, Person.class);
        Assertions.assertThat(checkedProp).containsExactlyInAnyOrder("address", "address.number");
    }

    @Test
    public void testGetCheckedPropNestedPropWithMethod() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        MvelConstraint constraint = new MvelConstraint(null, "address.street.length() > 12", null, null, null, null, null);
        List<String> checkedProp = strategy.getCheckedProp(constraint, Person.class);
        Assertions.assertThat(checkedProp).containsExactlyInAnyOrder("address", "address.street", "address.street.length");
    }

    @Test
    public void testGetCheckedPropNestedGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        MvelConstraint constraint = new MvelConstraint(null, "getAddress().getStreet() != null", null, null, null, null, null);
        List<String> checkedProp = strategy.getCheckedProp(constraint, Person.class);
        Assertions.assertThat(checkedProp).containsExactlyInAnyOrder("address", "address.street");
    }
}
