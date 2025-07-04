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
package org.kie.api.time;

import java.util.concurrent.TimeUnit;


/**
 * A clock interface for the implementation of pseudo clocks,
 * that are clocks where the user have control over the actual
 * clock working.
 *  
 * Pseudo clocks are particularly useful for simulations, 
 * "what if" scenario runs, and for tests.
 */
public interface SessionPseudoClock extends SessionClock {

    /**
     * Advances the clock time in the specified unit amount. 
     * 
     * @param amount the amount of units to advance in the clock
     * @param unit the used time unit
     * @return the current absolute timestamp
     */
    long advanceTime( long amount, TimeUnit unit );

}
