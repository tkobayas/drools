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
package org.drools.core;

/**
 * This enum represents all engine supported belief systems
 */
public enum BeliefSystemType {

    SIMPLE("simple"),
    JTMS("jtms"),
    DEFEASIBLE("defeasible");

    private String string;
    BeliefSystemType( String string ) {
        this.string = string;
    }
    
    public String toExternalForm() {
        return this.string;
    }
    
    public String toString() {
        return this.string;
    }
    
    public String getId() {
        return this.string;
    }
    
    public static BeliefSystemType resolveBeliefSystemType(String id) {
        if( SIMPLE.getId().equalsIgnoreCase( id ) ) {
            return SIMPLE;
        } else if( JTMS.getId().equalsIgnoreCase( id ) ) {
            return JTMS;
        } else if( DEFEASIBLE.getId().equalsIgnoreCase( id ) ) {
            return DEFEASIBLE;
        }
        throw new IllegalArgumentException( "Illegal enum value '" + id + "' for BeliefSystem" );
    }

}
