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
package org.drools.mvel.evaluators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.base.base.ValueResolver;
import org.drools.base.base.ValueType;
import org.drools.compiler.rule.builder.EvaluatorDefinition;
import org.drools.drl.parser.impl.Operator;
import org.drools.mvel.evaluators.VariableRestriction.ObjectVariableContextEntry;
import org.drools.mvel.evaluators.VariableRestriction.VariableContextEntry;
import org.drools.base.rule.accessor.Evaluator;
import org.drools.base.rule.accessor.FieldValue;
import org.drools.base.rule.accessor.ReadAccessor;
import org.kie.api.runtime.rule.FactHandle;

/**
 * This class defines all the set built in evaluators like contains, memberOf,
 * etc.
 */
public class SetEvaluatorsDefinition
    implements
        EvaluatorDefinition {

    protected static final String containsOp = Operator.BuiltInOperator.CONTAINS.getSymbol();
    protected static final String excludesOp = Operator.BuiltInOperator.EXCLUDES.getSymbol();
    protected static final String memberOfOp = Operator.BuiltInOperator.MEMBEROF.getSymbol();

    public static final Operator CONTAINS = Operator.determineOperator( containsOp, false );
    public static final Operator NOT_CONTAINS = Operator.determineOperator( containsOp, true );
    public static final Operator EXCLUDES = Operator.determineOperator( excludesOp, false );
    public static final Operator NOT_EXCLUDES = Operator.determineOperator( excludesOp, true );
    public static final Operator MEMBEROF = Operator.determineOperator( memberOfOp, false );
    public static final Operator NOT_MEMBEROF = Operator.determineOperator( memberOfOp, true );

    private static String[] SUPPORTED_IDS = new String[] { memberOfOp, containsOp, excludesOp };

    private EvaluatorCache evaluators    = new EvaluatorCache() {
                                                    private static final long serialVersionUID = 510l;
                                                    {
                                                        addEvaluator( ValueType.ARRAY_TYPE,
                                                                      CONTAINS,
                                                                      ArrayContainsEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.ARRAY_TYPE,
                                                                      NOT_CONTAINS,
                                                                      ArrayExcludesEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.ARRAY_TYPE,
                                                                      EXCLUDES,
                                                                      ArrayExcludesEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.ARRAY_TYPE,
                                                                      NOT_EXCLUDES,
                                                                      ArrayContainsEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BIG_DECIMAL_TYPE,
                                                                      MEMBEROF,
                                                                      BigDecimalMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BIG_DECIMAL_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      BigDecimalNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BIG_INTEGER_TYPE,
                                                                      MEMBEROF,
                                                                      BigIntegerMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BIG_INTEGER_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      BigIntegerNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BOOLEAN_TYPE,
                                                                      MEMBEROF,
                                                                      BooleanMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BOOLEAN_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      BooleanNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PBOOLEAN_TYPE,
                                                                      MEMBEROF,
                                                                      BooleanMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PBOOLEAN_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      BooleanNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BYTE_TYPE,
                                                                      MEMBEROF,
                                                                      ByteMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.BYTE_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      ByteNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PBYTE_TYPE,
                                                                      MEMBEROF,
                                                                      ByteMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PBYTE_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      ByteNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.CHAR_TYPE,
                                                                      MEMBEROF,
                                                                      CharacterMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.CHAR_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      CharacterNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PCHAR_TYPE,
                                                                      MEMBEROF,
                                                                      CharacterMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PCHAR_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      CharacterNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.DATE_TYPE,
                                                                      MEMBEROF,
                                                                      DateMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.DATE_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      DateNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.DOUBLE_TYPE,
                                                                      MEMBEROF,
                                                                      DoubleMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.DOUBLE_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      DoubleNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PDOUBLE_TYPE,
                                                                      MEMBEROF,
                                                                      DoubleMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PDOUBLE_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      DoubleNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.FLOAT_TYPE,
                                                                      MEMBEROF,
                                                                      FloatMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.FLOAT_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      FloatNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PFLOAT_TYPE,
                                                                      MEMBEROF,
                                                                      FloatMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PFLOAT_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      FloatNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.INTEGER_TYPE,
                                                                      MEMBEROF,
                                                                      IntegerMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.INTEGER_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      IntegerNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PINTEGER_TYPE,
                                                                      MEMBEROF,
                                                                      IntegerMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PINTEGER_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      IntegerNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.LONG_TYPE,
                                                                      MEMBEROF,
                                                                      LongMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.LONG_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      LongNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PLONG_TYPE,
                                                                      MEMBEROF,
                                                                      LongMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PLONG_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      LongNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.OBJECT_TYPE,
                                                                      CONTAINS,
                                                                      ObjectContainsEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.OBJECT_TYPE,
                                                                      NOT_CONTAINS,
                                                                      ObjectExcludesEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.OBJECT_TYPE,
                                                                      EXCLUDES,
                                                                      ObjectExcludesEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.OBJECT_TYPE,
                                                                      NOT_EXCLUDES,
                                                                      ObjectContainsEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.OBJECT_TYPE,
                                                                      MEMBEROF,
                                                                      ObjectMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.OBJECT_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      ObjectNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.SHORT_TYPE,
                                                                      MEMBEROF,
                                                                      ShortMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.SHORT_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      ShortNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PSHORT_TYPE,
                                                                      MEMBEROF,
                                                                      ShortMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.PSHORT_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      ShortNotMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.STRING_TYPE,
                                                                      MEMBEROF,
                                                                      StringMemberOfEvaluator.INSTANCE );
                                                        addEvaluator( ValueType.STRING_TYPE,
                                                                      NOT_MEMBEROF,
                                                                      StringNotMemberOfEvaluator.INSTANCE );
                                                    }
                                                };

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        evaluators = (EvaluatorCache) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( evaluators );
    }

    /**
     * @inheridDoc
     */
    public Evaluator getEvaluator(ValueType type,
                                  Operator operator) {
        return this.evaluators.getEvaluator( type,
                                             operator );
    }

    /**
     * @inheridDoc
     */
    public Evaluator getEvaluator(ValueType type,
                                  Operator operator,
                                  String parameterText) {
        return this.evaluators.getEvaluator( type,
                                             operator );
    }

    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText) {
        return this.getEvaluator( type,
                                  operatorId,
                                  isNegated,
                                  parameterText,
                                  Target.FACT,
                                  Target.FACT );

    }

    /**
     * @inheritDoc
     */
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right) {
        return this.evaluators.getEvaluator( type,
                                             Operator.determineOperator( operatorId,
                                                                         isNegated ) );
    }

    public String[] getEvaluatorIds() {
        return SUPPORTED_IDS;
    }

    public boolean isNegatable() {
        return true;
    }

    public Target getTarget() {
        return Target.FACT;
    }

    public boolean supportsType(ValueType type) {
        return this.evaluators.supportsType( type );
    }

    private static ObjectArrayContainsEvaluator objectArrayEvaluator    = new ObjectArrayContainsEvaluator();

    private static Map<Class, ArrayContains>    primitiveArrayEvaluator = new HashMap() {
                                                                            {
                                                                                put( boolean[].class,
                                                                                     new BooleanArrayContainsEvaluator() );
                                                                                put( byte[].class,
                                                                                     new ByteArrayContainsEvaluator() );
                                                                                put( short[].class,
                                                                                     new ShortArrayContainsEvaluator() );
                                                                                put( char[].class,
                                                                                     new CharArrayContainsEvaluator() );
                                                                                put( int[].class,
                                                                                     new IntegerArrayContainsEvaluator() );
                                                                                put( long[].class,
                                                                                     new LongArrayContainsEvaluator() );
                                                                                put( float[].class,
                                                                                     new FloatArrayContainsEvaluator() );
                                                                                put( double[].class,
                                                                                     new DoubleArrayContainsEvaluator() );
                                                                            }
                                                                        };

    public static ArrayContains getArrayContains(Class cls) {
        ArrayContains eval = primitiveArrayEvaluator.get( cls );
        if ( eval == null ) {
            eval = objectArrayEvaluator;
        }
        return eval;
    }

    /*
     * Evaluator Implementations
     */
    public static class ArrayContainsEvaluator extends BaseEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ArrayContainsEvaluator();

        public ArrayContainsEvaluator() {
            super( ValueType.ARRAY_TYPE,
                   CONTAINS );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor,
                                final FactHandle handle1,
                                final FieldValue fieldValue) {
            final Object array = extractor.getValue( valueResolver,
                                                     handle1.getObject() );

            if ( array == null ) return false;

            return getArrayContains( array.getClass() ).contains( array,
                                                                  fieldValue );
        }

        public boolean evaluateCachedRight(final ValueResolver valueResolver,
                                           final VariableContextEntry context,
                                           final FactHandle left) {
            final Object array = ((ObjectVariableContextEntry) context).right;
            if ( array == null ) return false;

            return getArrayContains( array.getClass() ).contains( array,
                                                                  valueResolver,
                                                                  context.declaration.getExtractor(),
                                                                  left.getObject() );
        }

        public boolean evaluateCachedLeft(final ValueResolver valueResolver,
                                          final VariableContextEntry context,
                                          final FactHandle right) {
            final Object array = context.extractor.getValue( valueResolver,
                                                             right.getObject() );

            if ( array == null ) return false;

            // the original object is passed, as the extractor
            // is used to cast to the correct type for the array comparison
            return getArrayContains( array.getClass() ).contains( array,
                                                                  valueResolver,
                                                                  context.declaration.getExtractor(),
                                                                  context.tuple.getObject( context.declaration ) );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor1,
                                final FactHandle handle1,
                                final ReadAccessor extractor2,
                                final FactHandle handle2) {
            final Object array = extractor1.getValue( valueResolver,
                                                      handle1.getObject() );

            if ( array == null ) return false;

            return getArrayContains( array.getClass() ).contains( array,
                                                                  valueResolver,
                                                                  extractor2,
                                                                  handle2.getObject() );
        }

        public String toString() {
            return "Array contains";
        }
    }

    public static class ArrayExcludesEvaluator extends BaseEvaluator {
        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ArrayExcludesEvaluator();

        public ArrayExcludesEvaluator() {
            super( ValueType.ARRAY_TYPE,
                   EXCLUDES );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor,
                                final FactHandle handle1,
                                final FieldValue fieldValue) {
            final Object array = extractor.getValue( valueResolver,
                                                     handle1.getObject() );

            if ( array == null ) return false;

            return !getArrayContains( array.getClass() ).contains( array,
                                                                   fieldValue );
        }

        public boolean evaluateCachedRight(final ValueResolver valueResolver,
                                           final VariableContextEntry context,
                                           final FactHandle left) {
            final Object array = ((ObjectVariableContextEntry) context).right;
            if ( array == null ) return false;

            return !getArrayContains( array.getClass() ).contains( array,
                                                                   valueResolver,
                                                                   context.declaration.getExtractor(),
                                                                   left.getObject() );
        }

        public boolean evaluateCachedLeft(final ValueResolver valueResolver,
                                          final VariableContextEntry context,
                                          final FactHandle right) {
            final Object array = context.extractor.getValue( valueResolver,
                                                             right.getObject() );

            if ( array == null ) return false;

            return !getArrayContains( array.getClass() ).contains( array,
                                                                   valueResolver,
                                                                   context.declaration.getExtractor(),
                                                                   context.getTuple().getObject( context.getVariableDeclaration() ) );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor1,
                                final FactHandle handle1,
                                final ReadAccessor extractor2,
                                final FactHandle handle2) {
            final Object array = extractor1.getValue( valueResolver,
                                                      handle1 .getObject());

            if ( array == null ) return false;

            return !getArrayContains( array.getClass() ).contains( array,
                                                                   valueResolver,
                                                                   extractor2,
                                                                   handle2.getObject() );
        }

        public String toString() {
            return "Array not contains";
        }
    }


    private static boolean contains(Object[] array,
                                    Object value) {
        for ( int i = 0; i < array.length; i++ ) {
            if ( array[i] == null && value == null || array[i] != null && array[i].equals( value ) ) {
                return true;
            }
        }
        return false;
    }

    public static abstract class BaseMemberOfEvaluator extends BaseEvaluator {

        private static final long serialVersionUID = 510l;

        public BaseMemberOfEvaluator() {
            super( null,
                   null );
        }

        public BaseMemberOfEvaluator(ValueType type,
                                     Operator operator) {
            super( type,
                   operator );
        }

        public ValueType getCoercedValueType() {
            // during evaluation, always coerce to object
            return ValueType.OBJECT_TYPE;
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor,
                                final FactHandle handle1,
                                final FieldValue fieldValue) {
            if ( fieldValue.isNull() ) {
                return false;
            } else if ( fieldValue.isCollectionField() ) {
                final Collection col = (Collection) fieldValue.getValue();
                final Object value = extractor.getValue( valueResolver,
                                                         handle1.getObject() );
                return col.contains( value );
            } else if ( fieldValue.getValue().getClass().isArray() ) {
                return getArrayContains( fieldValue.getValue().getClass() ).contains( fieldValue.getValue(),
                                                                                      valueResolver,
                                                                                      extractor,
                                                                                      handle1.getObject() );
            } else {
                throw new ClassCastException( "Can't check if an attribute is member of an object of class " + fieldValue.getValue().getClass() );
            }
        }

        public boolean evaluateCachedRight(final ValueResolver valueResolver,
                                           final VariableContextEntry context,
                                           final FactHandle left) {
            final Object object = context.declaration.getExtractor().getValue( valueResolver,
                                                                               left.getObject() );
            if ( object == null ) {
                return false;
            } else if ( object instanceof Collection ) {
                final Collection col = (Collection) object;
                final Object value = ((ObjectVariableContextEntry) context).right;
                return col.contains( value );
            } else if ( object.getClass().isArray() ) {
                return getArrayContains( object.getClass() ).contains( object,
                                                                       valueResolver,
                                                                       context.extractor,
                                                                       context.object );
            } else {
                throw new ClassCastException( "Can't check if an attribute is member of an object of class " + object.getClass() );
            }
        }

        public boolean evaluateCachedLeft(final ValueResolver valueResolver,
                                          final VariableContextEntry context,
                                          final FactHandle right) {
            final Object object = ((ObjectVariableContextEntry) context).left;
            if ( object == null ) {
                return false;
            } else if ( object instanceof Collection ) {
                final Collection col = (Collection) object;
                final Object value = context.extractor.getValue( valueResolver,
                                                                 right.getObject() );
                return col.contains( value );
            } else if ( object.getClass().isArray() ) {
                return getArrayContains( object.getClass() ).contains( object,
                                                                       valueResolver,
                                                                       context.extractor,
                                                                       right.getObject() );
            } else {
                throw new ClassCastException( "Can't check if an attribute is member of an object of class " + object.getClass() );
            }
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor1,
                                final FactHandle handle1,
                                final ReadAccessor extractor2,
                                final FactHandle handle2) {
            final Object object = extractor2.getValue( valueResolver,
                                                       handle2.getObject() );
            if ( object == null ) {
                return false;
            } else if ( object instanceof Collection ) {
                final Collection col = (Collection) object;
                final Object value = extractor1.getValue( valueResolver,
                                                          handle1.getObject() );
                return col.contains( value );
            } else if ( object.getClass().isArray() ) {
                return getArrayContains( object.getClass() ).contains( object,
                                                                       valueResolver,
                                                                       extractor1,
                                                                       handle1.getObject() );
            } else {
                throw new ClassCastException( "Can't check if an attribute is member of an object of class " + object.getClass() );
            }
        }

        public abstract String toString();

    }

    public static abstract class BaseNotMemberOfEvaluator extends BaseEvaluator {

        private static final long serialVersionUID = 510l;

        public BaseNotMemberOfEvaluator(ValueType type,
                                        Operator operator) {
            super( type,
                   operator );
        }

        public BaseNotMemberOfEvaluator() {
        }

        public ValueType getCoercedValueType() {
            // during evaluation, always coerce to object
            return ValueType.OBJECT_TYPE;
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor,
                                final FactHandle handle1,
                                final FieldValue fieldValue) {
            if ( fieldValue.isNull() ) {
                return false;
            } else if ( fieldValue.isCollectionField() ) {
                final Collection col = (Collection) fieldValue.getValue();
                final Object value = extractor.getValue( valueResolver,
                                                         handle1.getObject() );
                return !col.contains( value );
            } else if ( fieldValue.getValue().getClass().isArray() ) {
                return !getArrayContains( fieldValue.getValue().getClass() ).contains( fieldValue.getValue(),
                                                                                       valueResolver,
                                                                                       extractor,
                                                                                       handle1.getObject() );
            } else {
                throw new ClassCastException( "Can't check if an attribute is not member of an object of class " + fieldValue.getValue().getClass() );
            }
        }

        public boolean evaluateCachedRight(final ValueResolver valueResolver,
                                           final VariableContextEntry context,
                                           final FactHandle left) {
            final Object object = context.declaration.getExtractor().getValue( valueResolver,
                                                                               left.getObject() );
            if ( object == null ) {
                return false;
            } else if ( object instanceof Collection ) {
                final Collection col = (Collection) object;
                final Object value = ((ObjectVariableContextEntry) context).right;
                return !col.contains( value );
            } else if ( object.getClass().isArray() ) {
                // the original object is passed, as the extractor
                // is used to cast to the correct type for the array comparison
                return !getArrayContains( object.getClass() ).contains( object,
                                                                        valueResolver,
                                                                        context.extractor,
                                                                         context.object );
                  
            } else {
                throw new ClassCastException( "Can't check if an attribute is not member of an object of class " + object.getClass() );
            }
        }

        public boolean evaluateCachedLeft(final ValueResolver valueResolver,
                                          final VariableContextEntry context,
                                          final FactHandle right) {
            final Object object = ((ObjectVariableContextEntry) context).left;
            if ( object == null ) {
                return false;
            } else if ( object instanceof Collection ) {
                final Collection col = (Collection) object;
                final Object value = context.extractor.getValue( valueResolver,
                                                                 right.getObject() );
                return !col.contains( value );
            } else if ( object.getClass().isArray() ) {
                return !getArrayContains( object.getClass() ).contains( object,
                                                                        valueResolver,
                                                                        context.extractor,
                                                                        right.getObject() );
            } else {
                throw new ClassCastException( "Can't check if an attribute is not member of an object of class " + object.getClass() );
            }
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor1,
                                final FactHandle handle1,
                                final ReadAccessor extractor2,
                                final FactHandle handle2) {
            final Object object = extractor2.getValue( valueResolver,
                                                       handle2.getObject() );
            if ( object == null ) {
                return false;
            } else if ( object instanceof Collection ) {
                final Collection col = (Collection) object;
                final Object value = extractor1.getValue( valueResolver,
                                                          handle1.getObject() );
                return !col.contains( value );
            } else if ( object.getClass().isArray() ) {
                return !getArrayContains( object.getClass() ).contains( object,
                                                                        valueResolver,
                                                                        extractor1,
                                                                        handle1.getObject() );
            } else {
                throw new ClassCastException( "Can't check if an attribute is not member of an object of class " + object.getClass() );
            }
           
        }

        public abstract String toString();
    }

    public static class BigDecimalMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new BigDecimalMemberOfEvaluator();

        public BigDecimalMemberOfEvaluator() {
            super( ValueType.BIG_DECIMAL_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "BigDecimal memberOf";
        }
    }

    public static class BigDecimalNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new BigDecimalNotMemberOfEvaluator();

        public BigDecimalNotMemberOfEvaluator() {
            super( ValueType.BIG_DECIMAL_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "BigDecimal not memberOf";
        }
    }

    public static class BigIntegerMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new BigIntegerMemberOfEvaluator();

        public BigIntegerMemberOfEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "BigInteger memberOf";
        }
    }

    public static class BigIntegerNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {
        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new BigIntegerNotMemberOfEvaluator();

        public BigIntegerNotMemberOfEvaluator() {
            super( ValueType.BIG_INTEGER_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "BigInteger not memberOf";
        }
    }

    public static class BooleanMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new BooleanMemberOfEvaluator();

        public BooleanMemberOfEvaluator() {
            super( ValueType.PBOOLEAN_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Boolean memberOf";
        }
    }

    public static class BooleanNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new BooleanNotMemberOfEvaluator();

        public BooleanNotMemberOfEvaluator() {
            super( ValueType.PBOOLEAN_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Boolean not memberOf";
        }
    }

    public static class ByteMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ByteMemberOfEvaluator();

        public ByteMemberOfEvaluator() {
            super( ValueType.PBYTE_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Byte memberOf";
        }
    }

    public static class ByteNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ByteNotMemberOfEvaluator();

        public ByteNotMemberOfEvaluator() {
            super( ValueType.PBYTE_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Byte not memberOf";
        }
    }

    public static class CharacterMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new CharacterMemberOfEvaluator();

        public CharacterMemberOfEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Character memberOf";
        }
    }

    public static class CharacterNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new CharacterNotMemberOfEvaluator();

        public CharacterNotMemberOfEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Character not memberOf";
        }
    }

    public static class DateMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new DateMemberOfEvaluator();

        public DateMemberOfEvaluator() {
            super( ValueType.DATE_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Date memberOf";
        }
    }

    public static class DateNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new DateNotMemberOfEvaluator();

        public DateNotMemberOfEvaluator() {
            super( ValueType.DATE_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Date not memberOf";
        }
    }

    public static class DoubleMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new DoubleMemberOfEvaluator();

        public DoubleMemberOfEvaluator() {
            super( ValueType.PDOUBLE_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Double memberOf";
        }
    }

    public static class DoubleNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new DoubleNotMemberOfEvaluator();

        public DoubleNotMemberOfEvaluator() {
            super( ValueType.PDOUBLE_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Double not memberOf";
        }
    }

    public static class FloatMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new FloatMemberOfEvaluator();

        public FloatMemberOfEvaluator() {
            super( ValueType.PFLOAT_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Float memberOf";
        }
    }

    public static class FloatNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new FloatNotMemberOfEvaluator();

        public FloatNotMemberOfEvaluator() {
            super( ValueType.PFLOAT_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Float not memberOf";
        }
    }

    public static class IntegerMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new IntegerMemberOfEvaluator();

        public IntegerMemberOfEvaluator() {
            super( ValueType.PINTEGER_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Integer memberOf";
        }
    }

    public static class IntegerNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new IntegerNotMemberOfEvaluator();

        public IntegerNotMemberOfEvaluator() {
            super( ValueType.PINTEGER_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Integer not memberOf";
        }
    }

    public static class LongMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new LongMemberOfEvaluator();

        public LongMemberOfEvaluator() {
            super( ValueType.PLONG_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Long memberOf";
        }
    }

    public static class LongNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new LongNotMemberOfEvaluator();

        public LongNotMemberOfEvaluator() {
            super( ValueType.PLONG_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Long not memberOf";
        }
    }

    public static class ObjectContainsEvaluator extends BaseEvaluator {
        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ObjectContainsEvaluator();

        public ObjectContainsEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   CONTAINS );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor,
                                final FactHandle handle1,
                                final FieldValue fieldValue) {
            final Object value = fieldValue.getValue();
            final Collection col = (Collection) extractor.getValue( valueResolver,
                                                                    handle1.getObject() );
            return (col == null) ? false : col.contains( value );
        }

        public boolean evaluateCachedRight(final ValueResolver valueResolver,
                                           final VariableContextEntry context,
                                           final FactHandle left) {
            final Object value = context.declaration.getExtractor().getValue( valueResolver,
                                                                              left.getObject() );
            final Collection col = (Collection) ((ObjectVariableContextEntry) context).right;
            return (col == null) ? false : col.contains( value );
        }

        public boolean evaluateCachedLeft(final ValueResolver valueResolver,
                                          final VariableContextEntry context,
                                          final FactHandle right) {
            final Object value = ((ObjectVariableContextEntry) context).left;
            final Collection col = (Collection) context.extractor.getValue( valueResolver,
                                                                            right.getObject() );
            return (col == null) ? false : col.contains( value );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor1,
                                final FactHandle handle1,
                                final ReadAccessor extractor2,
                                final FactHandle handle2) {
            final Object value = extractor2.getValue( valueResolver,
                                                      handle2.getObject() );
            final Collection col = (Collection) extractor1.getValue( valueResolver,
                                                                     handle1.getObject() );
            return (col == null) ? false : col.contains( value );
        }

        public String toString() {
            return "Object contains";
        }
    }

    public static class ObjectExcludesEvaluator extends BaseEvaluator {
        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ObjectExcludesEvaluator();

        public ObjectExcludesEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   EXCLUDES );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor,
                                final FactHandle handle1,
                                final FieldValue fieldValue) {
            final Object value = fieldValue.getValue();
            final Collection col = (Collection) extractor.getValue( valueResolver,
                                                                    handle1.getObject() );
            return (col == null) ? true : !col.contains( value );
        }

        public boolean evaluateCachedRight(final ValueResolver valueResolver,
                                           final VariableContextEntry context,
                                           final FactHandle left) {
            final Object value = context.declaration.getExtractor().getValue( valueResolver,
                                                                              left.getObject() );
            final Collection col = (Collection) ((ObjectVariableContextEntry) context).right;
            return (col == null) ? true : !col.contains( value );
        }

        public boolean evaluateCachedLeft(final ValueResolver valueResolver,
                                          final VariableContextEntry context,
                                          final FactHandle right) {
            final Object value = ((ObjectVariableContextEntry) context).left;
            final Collection col = (Collection) context.extractor.getValue( valueResolver,
                                                                            right.getObject() );
            return (col == null) ? true : !col.contains( value );
        }

        public boolean evaluate(final ValueResolver valueResolver,
                                final ReadAccessor extractor1,
                                final FactHandle handle1,
                                final ReadAccessor extractor2,
                                final FactHandle handle2) {
            final Object value = extractor2.getValue( valueResolver,
                                                      handle2.getObject() );
            final Collection col = (Collection) extractor1.getValue( valueResolver,
                                                                     handle1.getObject() );
            return (col == null) ? true : !col.contains( value );
        }

        public String toString() {
            return "Object excludes";
        }
    }

    public static class ObjectMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ObjectMemberOfEvaluator();

        public ObjectMemberOfEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Object memberOf";
        }
    }

    public static class ObjectNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ObjectNotMemberOfEvaluator();

        public ObjectNotMemberOfEvaluator() {
            super( ValueType.OBJECT_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Object not memberOf";
        }
    }

    public static class ShortMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ShortMemberOfEvaluator();

        public ShortMemberOfEvaluator() {
            super( ValueType.PSHORT_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "Short memberOf";
        }
    }

    public static class ShortNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new ShortNotMemberOfEvaluator();

        public ShortNotMemberOfEvaluator() {
            super( ValueType.PSHORT_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "Short not memberOf";
        }
    }

    public static class StringMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new StringMemberOfEvaluator();

        public StringMemberOfEvaluator() {
            super( ValueType.STRING_TYPE,
                   MEMBEROF );
        }

        public String toString() {
            return "String memberOf";
        }
    }

    public static class StringNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 510l;
        public final static Evaluator INSTANCE         = new StringNotMemberOfEvaluator();

        public StringNotMemberOfEvaluator() {
            super( ValueType.STRING_TYPE,
                   NOT_MEMBEROF );
        }

        public String toString() {
            return "String not memberOf";
        }
    }

    public interface ArrayContains {
        Class getArrayType();

        boolean contains(Object array,
                         FieldValue value);

        boolean contains(Object array,
                         ValueResolver valueResolver,
                         ReadAccessor accessor,
                         Object object);
        //        
        // boolean member(FieldValue array,
        // Object object);
        //        
        // boolean member(Object object,
        // ReteEvaluator reteEvaluator,
        // ReadAccessor accessor,
        // Object array);
    }

    public static class BooleanArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            boolean[] boolArray = (boolean[]) array;

            for ( int i = 0, length = boolArray.length; i < length; i++ ) {
                if ( boolArray[i] == value.getBooleanValue() ) {
                    return true;
                }
            }

            return false;
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            boolean[] boolArray = (boolean[]) array;

            for ( int i = 0, length = boolArray.length; i < length; i++ ) {
                if ( boolArray[i] == accessor.getBooleanValue( valueResolver,
                                                               object ) ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return boolean[].class;
        }

    }

    public static class ByteArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            return contains( (byte[]) array,
                             value.getByteValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (byte[]) array,
                             accessor.getByteValue( valueResolver,
                                                    object ) );
        }

        private boolean contains(byte[] array,
                                 byte value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return byte[].class;
        }

    }

    public static class ShortArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            return contains( (short[]) array,
                             value.getShortValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (short[]) array,
                             accessor.getShortValue( valueResolver,
                                                     object ) );
        }

        private boolean contains(short[] array,
                                 short value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return short[].class;
        }
    }

    public static class CharArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            return contains( (char[]) array,
                             value.getCharValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (char[]) array,
                             accessor.getCharValue( valueResolver,
                                                    object ) );
        }

        private boolean contains(char[] array,
                                 char value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return char[].class;
        }

    }

    public static class IntegerArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            return contains( (int[]) array,
                             value.getIntValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (int[]) array,
                             accessor.getIntValue( valueResolver,
                                                   object ) );
        }

        private boolean contains(int[] array,
                                 int value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return int[].class;
        }

    }

    public static class LongArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            return contains( (long[]) array,
                             value.getLongValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (long[]) array,
                             accessor.getLongValue( valueResolver,
                                                    object ) );
        }

        private boolean contains(long[] array,
                                 long value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return long[].class;
        }

    }

    public static class FloatArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue value) {
            return contains( (float[]) array,
                             value.getFloatValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (float[]) array,
                             accessor.getFloatValue( valueResolver,
                                                     object ) );
        }

        private boolean contains(float[] array,
                                 float value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return float[].class;
        }
    }

    public static class DoubleArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue fieldValue) {
            return contains( (double[]) array,
                             fieldValue.getDoubleValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (double[]) array,
                             accessor.getDoubleValue( valueResolver,
                                                      object ) );
        }

        // public boolean member(FieldValue array,
        // Object object) {
        // return contains( (double[]) array.getValue(),
        // object );
        // }
        //
        // public boolean member(Object object,
        // ReteEvaluator reteEvaluator,
        // ReadAccessor accessor,
        // Object array) {
        // return contains( (double[]) accessor.getValue( reteEvaluator,
        // object ),
        // object );
        // }

        private boolean contains(double[] array,
                                 double value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i] == value ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return double[].class;
        }
    }

    public static class ObjectArrayContainsEvaluator
        implements
        ArrayContains {

        public boolean contains(Object array,
                                FieldValue fieldValue) {
            return contains( (Object[]) array,
                             fieldValue.getValue() );
        }

        public boolean contains(Object array,
                                ValueResolver valueResolver,
                                ReadAccessor accessor,
                                Object object) {
            return contains( (Object[]) array,
                             accessor.getValue( valueResolver,
                                                object ) );
        }

        private boolean contains(Object[] array,
                                 Object value) {
            for ( int i = 0, length = array.length; i < length; i++ ) {
                if ( array[i].equals( value ) ) {
                    return true;
                }
            }

            return false;
        }

        public Class getArrayType() {
            return Object[].class;
        }

        // public boolean member(FieldValue array,
        // Object object) {
        // return contains( (Object[]) array.getValue(),
        // object );
        // }
        //
        // public boolean member(Object object,
        // ReteEvaluator reteEvaluator,
        // ReadAccessor accessor,
        // Object array) {
        // return contains( (Object[]) accessor.getValue( reteEvaluator,
        // object ),
        // object );
        // }
    }
}
