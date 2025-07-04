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
package org.drools.beliefs.bayes;

import org.drools.beliefs.graph.Graph;
import org.drools.beliefs.graph.GraphNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.drools.beliefs.bayes.GraphTest.addNode;
import static org.drools.beliefs.bayes.GraphTest.bitSet;
import static org.drools.beliefs.bayes.JunctionTreeTest.scaleDouble;

public class BayesProjectionTest {

    @Test
    public void testProjection1() {
        // Projects from node1 into sep. A and B are in node1. A and B are in the sep.
        // this is a straight forward projection
        BayesVariable a = new BayesVariable<String>( "A", 0, new String[] {"A1", "A2"}, null);
        BayesVariable b = new BayesVariable<String>( "B", 1, new String[] {"B1", "B2"}, null);

        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode x0 = addNode(graph);
        GraphNode x1 = addNode(graph);

        x0.setContent( a );
        x1.setContent( b );


        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0011") );
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0011")  );
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0011"), graph).createState();

        double v = 0.1;
        for ( int i = 0; i < node1.getPotentials().length; i++ ) {
            node1.getPotentials()[i] = v;
            v += + 0.1;
        }

        BayesVariable[] vars = new BayesVariable[] {a, b};

        BayesVariable[] sepVars = new BayesVariable[] { a, b };
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        double[] projectedSepPotentials = new double[ sep.getPotentials().length];
        BayesProjection p = new BayesProjection(vars, node1.getPotentials(), sepVarPos, sepVarMultipliers, projectedSepPotentials);
        p.project();

        assertThat(scaleDouble(3, projectedSepPotentials)).containsExactly(0.1, 0.2, 0.3, 0.4);
    }

    @Test
    public void testProjection2() {
        // Projects from node1 into sep. A, B and C are in node1. A and B are in the sep.
        // this tests a non separator var, after the vars
        BayesVariable a = new BayesVariable<String>( "A", 0, new String[] {"A1", "A2"},  new double[][] {{0.1, 0.2}});
        BayesVariable b = new BayesVariable<String>( "B", 1, new String[] {"B1", "B2"},  new double[][] {{0.1, 0.2}});
        BayesVariable c = new BayesVariable<String>( "C", 2, new String[] {"C1", "C2"},  new double[][] {{0.1, 0.2}});


        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode x0 = addNode(graph);
        GraphNode x1 = addNode(graph);
        GraphNode x2 = addNode(graph);

        x0.setContent( a );
        x1.setContent( b );
        x2.setContent( c );

        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0111") );
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0011")  );
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0011"), graph).createState();

        double v = 0.1;
        for ( int i = 0; i < node1.getPotentials().length; i++ ) {
            node1.getPotentials()[i] = v;
            v = scaleDouble(3, v + 0.1 );
        }

        BayesVariable[] vars = new BayesVariable[] {a, b, c};

        BayesVariable[] sepVars = new BayesVariable[] { a, b };
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        double[] projectedSepPotentials = new double[ sep.getPotentials().length];
        BayesProjection p = new BayesProjection(vars, node1.getPotentials(), sepVarPos, sepVarMultipliers, projectedSepPotentials);
        p.project();

        // remember it's been normalized, from 0.3, 0.7, 1.1, 1.5
        assertThat(scaleDouble(3, projectedSepPotentials)).containsExactly(0.083, 0.194, 0.306, 0.417);
    }

    @Test
    public void testProjection3() {
        // Projects from node1 into sep. A, B and C are in node1. A and C are in the sep.
        // this tests a non separator var, in the middle of the vars
        BayesVariable a = new BayesVariable<String>( "A", 0, new String[] {"A1", "A2"},  new double[][] {{0.1, 0.2}});
        BayesVariable b = new BayesVariable<String>( "B", 1, new String[] {"B1", "B2"},  new double[][] {{0.1, 0.2}});
        BayesVariable c = new BayesVariable<String>( "C", 2, new String[] {"C1", "C2"},  new double[][] {{0.1, 0.2}});


        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode x0 = addNode(graph);
        GraphNode x1 = addNode(graph);
        GraphNode x2 = addNode(graph);
        GraphNode x3 = addNode(graph);


        x0.setContent( a );
        x1.setContent( b );
        x2.setContent( c );

        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0111") );
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0101")  );
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0101"), graph).createState();

        double v = 0.1;
        for ( int i = 0; i < node1.getPotentials().length; i++ ) {
            node1.getPotentials()[i] = v;
            v = scaleDouble(3, v + 0.1 );
        }

        BayesVariable[] vars = new BayesVariable[] {a, b, c};

        BayesVariable[] sepVars = new BayesVariable[] { a, c };
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        double[] projectedSepPotentials = new double[ sep.getPotentials().length];
        BayesProjection p = new BayesProjection(vars, node1.getPotentials(), sepVarPos, sepVarMultipliers, projectedSepPotentials);
        p.project();

        // remember it's been normalized, from 0.4, 0.6, 1.2, 1.4
        assertThat(scaleDouble(3, projectedSepPotentials)).containsExactly(0.111, 0.167, 0.333, 0.389);
    }

    @Test
    public void testProjection4() {
        // Projects from node1 into sep. A, B and C are in node1. B and C are in the sep.
        // this tests a non separator var, is before the vars
        BayesVariable a = new BayesVariable<String>( "A", 0, new String[] {"A1", "A2"},  new double[][] {{0.1, 0.2}});
        BayesVariable b = new BayesVariable<String>( "B", 1, new String[] {"B1", "B2"},  new double[][] {{0.1, 0.2}});
        BayesVariable c = new BayesVariable<String>( "C", 2, new String[] {"C1", "C2"},  new double[][] {{0.1, 0.2}});


        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode x0 = addNode(graph);
        GraphNode x1 = addNode(graph);
        GraphNode x2 = addNode(graph);
        GraphNode x3 = addNode(graph);


        x0.setContent( a );
        x1.setContent( b );
        x2.setContent( c );

        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0111") );
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0110") );
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0101"), graph).createState();

        double v = 0.1;
        for ( int i = 0; i < node1.getPotentials().length; i++ ) {
            node1.getPotentials()[i] = v;
            v = scaleDouble(3, v + 0.1 );
        }

        BayesVariable[] vars = new BayesVariable[] {a, b, c};

        BayesVariable[] sepVars = new BayesVariable[] { b, c };
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        double[] projectedSepPotentials = new double[ sep.getPotentials().length];
        BayesProjection p = new BayesProjection(vars, node1.getPotentials(), sepVarPos, sepVarMultipliers, projectedSepPotentials);
        p.project();

        // remember it's been normalized, from 0.6 0.8 1.0 1.2
        assertThat(scaleDouble(3, projectedSepPotentials)).containsExactly(0.167, 0.222, 0.278, 0.333);
    }

}