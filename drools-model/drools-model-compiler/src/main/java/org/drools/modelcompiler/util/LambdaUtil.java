/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.modelcompiler.util;

import java.lang.reflect.Field;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalScope;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import org.drools.model.SingleConstraint;
import org.drools.model.constraints.SingleConstraint1;
import org.drools.model.functions.IntrospectableLambda;
import org.drools.model.functions.Predicate1;
import org.drools.modelcompiler.builder.generator.DrlxParseUtil;
import org.drools.modelcompiler.util.lambdareplace.MaterializedLambdaPredicate;

public class LambdaUtil {

    private LambdaUtil() {

    }

    public static Expression appendNewLambdaToOld(LambdaExpr l1, LambdaExpr l2) {
        ExpressionStmt l1ExprStmt = (ExpressionStmt) l1.getBody();
        ExpressionStmt l2ExprStmt = (ExpressionStmt) l2.getBody();

        DrlxParseUtil.RemoveRootNodeResult removeRootNodeResult = DrlxParseUtil.removeRootNode(l2ExprStmt.getExpression());

        NodeWithOptionalScope<?> newExpr = (NodeWithOptionalScope<?>) removeRootNodeResult.getFirstChild();

        newExpr.setScope(l1ExprStmt.getExpression());
        l1.setBody(new ExpressionStmt(removeRootNodeResult.getWithoutRootNode()));
        return l1;
    }

    public static String getOriginalDrlConstraint(SingleConstraint constraint) {
        // Only externalised lambda has originalDrlConstraint
        if (constraint instanceof SingleConstraint1) {
            SingleConstraint1 constraint1 = (SingleConstraint1) constraint;
            Predicate1 predicate1 = constraint1.getPredicate1();
            if (predicate1 instanceof IntrospectableLambda) {
                IntrospectableLambda introspectableLambda = (IntrospectableLambda)predicate1;
                Object lambda = introspectableLambda.getLambda();
                Field originalDrlConstraint;
                try {
                    originalDrlConstraint = lambda.getClass().getDeclaredField(MaterializedLambdaPredicate.ORIGINAL_DRL_CONSTRAINT);
                    return (String) originalDrlConstraint.get(lambda);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
