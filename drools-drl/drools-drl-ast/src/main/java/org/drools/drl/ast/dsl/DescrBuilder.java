/**
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
package org.drools.drl.ast.dsl;

import org.drools.drl.ast.descr.BaseDescr;

/**
 * A super interface for all DescrBuilders.
 */
public interface DescrBuilder<P extends DescrBuilder< ? , ? >, T extends BaseDescr> {

    /**
     * Sets the start location of the corresponding construction
     * in the source file.
     * 
     * @param line
     * @param column
     * 
     * @return itself, in order to be used as a fluent API
     */
    public DescrBuilder<P, T> startLocation( int line,
                                             int column );

    /**
     * Sets the end location of the corresponding construction
     * in the source file.
     * 
     * @param line
     * @param column
     * 
     * @return itself, in order to be used as a fluent API
     */
    public DescrBuilder<P, T> endLocation( int line,
                                           int column );

    /**
     * Sets the offset of the starting character of the 
     * corresponding construction in the source file.
     * 
     * @param offset the offset of the first character of 
     *        this construction inside the source file, relative
     *        to the start.
     * 
     * @return itself, in order to be used as a fluent API
     */
    public DescrBuilder<P, T> startCharacter( int offset );

    /**
     * Sets the offset of the starting character of the 
     * corresponding construction in the source file.
     * 
     * @param offset the offset of the first character of 
     *        this construction inside the source file, relative
     *        to the start.
     * 
     * @return itself, in order to be used as a fluent API
     */
    public DescrBuilder<P, T> endCharacter( int offset );

    /**
     * Returns the descriptor generated by this builder.
     * 
     * @return
     */
    public T getDescr();
    
    /**
     * Returns the parent container of this descr builder.
     * Example: ruleDescrBuilder.end() will return the 
     * PackageDescrBuilder as that is its parent container.
     * 
     * @return
     */
    public P end();
}
