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
package org.drools.reliability.redis;

import org.drools.reliability.core.StorageManager;
import org.drools.reliability.core.StorageManagerFactory;

public class RedisStorageManagerFactory implements StorageManagerFactory {

    public static final String REDIS_STORAGE_PREFIX = RELIABILITY_STORAGE_PREFIX + ".redis";
    public static final String REDIS_STORAGE_REMOTE_HOST = REDIS_STORAGE_PREFIX + ".remote.host";
    public static final String REDIS_STORAGE_REMOTE_PORT = REDIS_STORAGE_PREFIX + ".remote.port";
    public static final String REDIS_STORAGE_REMOTE_USER = REDIS_STORAGE_PREFIX + ".remote.user";
    public static final String REDIS_STORAGE_REMOTE_PASS = REDIS_STORAGE_PREFIX + ".remote.pass";
    private final StorageManager storageManager;

    public RedisStorageManagerFactory() {
        storageManager = RedisStorageManager.INSTANCE;

        // initStorageManager() is called by StorageManagerFactory.Holder.createInstance()
    }

    @Override
    public StorageManager getStorageManager() {
        return storageManager;
    }

    @Override
    public int servicePriority() {
        return 0;
    }

    @Override
    public String serviceTag() {
        return "redis";
    }
}
