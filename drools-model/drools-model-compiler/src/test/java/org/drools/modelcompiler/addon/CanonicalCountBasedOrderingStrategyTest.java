package org.drools.modelcompiler.addon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.modelcompiler.domain.Person;
import org.junit.Test;

public class CanonicalCountBasedOrderingStrategyTest {

    @Test
    public void testPrioritizeConstraintBasic() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "address != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
    }

    @Test
    public void testPrioritizeConstraintNegate() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "!(address == null)", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
    }

    @Test
    public void testPrioritizeConstraintThis() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "this.address != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
    }

    @Test
    public void testPrioritizeConstraintGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "getAddress() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
    }

    @Test
    public void testPrioritizeConstraintThisGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "this.getAddress() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
    }

    @Test
    public void testPrioritizeConstraintNestedProp() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "address.street != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address.street");
    }

    @Test
    public void testPrioritizeConstraintNestedGetter() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "getAddress().getStreet() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address.street");
    }

    @Test
    public void testPrioritizeConstraintIrregular() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "getAddress( ) .getStreet() != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintMap() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "myMap[$p] != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintInverse() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        // In actual run, it will be normalized to "address != null"
        MvelConstraint constraint = new MvelConstraint(null, "null != address", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintAnd() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        // In actual run, it will be split to 2 constraints "address != null" and "likes != null"
        MvelConstraint constraint = new MvelConstraint(null, "address != null && likes != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintOr() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "address != null || likes != null", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).isEmpty();
    }

    @Test
    public void testPrioritizeConstraintInstanceof() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "address instanceof Address", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
    }

    @Test
    public void testPrioritizeConstraintInstanceofNegate() {
        CanonicalCountBasedOrderingStrategy strategy = new CanonicalCountBasedOrderingStrategy();
        Map<AlphaNodeFieldConstraint, List<String>> prioritizedConstraints = new HashMap<>();
        Class<?> clazz = Person.class;

        MvelConstraint constraint = new MvelConstraint(null, "!(address instanceof Address)", null, null, null, null, null);
        strategy.prioritizeConstraint(constraint, prioritizedConstraints, clazz);
        Assertions.assertThat(prioritizedConstraints.get(constraint)).containsExactlyInAnyOrder("address");
        ;
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

        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address\t != null");
        Assertions.assertThat(tokens).containsExactly("address", "!=", "null");
        
        tokens = CanonicalCountBasedOrderingStrategy.splitExpression("address\n != null");
        Assertions.assertThat(tokens).containsExactly("address", "!=", "null");
    }
}
