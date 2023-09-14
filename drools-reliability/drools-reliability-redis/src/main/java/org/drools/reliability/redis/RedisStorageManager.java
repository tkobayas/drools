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

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.drools.core.common.ReteEvaluator;
import org.drools.core.common.Storage;
import org.drools.reliability.core.TestableStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.ScanResult;

import static org.drools.reliability.core.StorageManager.createStorageId;
import static org.drools.reliability.core.StorageManagerFactory.DELIMITER;
import static org.drools.reliability.core.StorageManagerFactory.SESSION_STORAGE_PREFIX;
import static org.drools.reliability.core.StorageManagerFactory.SHARED_STORAGE_PREFIX;

public class RedisStorageManager implements TestableStorageManager {

    private static final Logger LOG = LoggerFactory.getLogger(RedisStorageManager.class);

    static final RedisStorageManager INSTANCE = new RedisStorageManager();

    private JedisPool pool;

    private RedisStorageManager() {
    }

    @Override
    public void initStorageManager() {
        LOG.info("Using Redis client");
        pool = new JedisPool("localhost", 6379);
    }

    @Override
    public <K, V> Storage<K, V> internalGetOrCreateStorageForSession(ReteEvaluator reteEvaluator, String storageName, Class<K> keyClass) {
        return new RedisStorage<>(createStorageId(reteEvaluator, storageName), pool, keyClass);
    }

    @Override
    public <K, V> Storage<K, V> getOrCreateSharedStorage(String storageName, Class<K> keyClass) {
        return new RedisStorage<>(SHARED_STORAGE_PREFIX + storageName, pool, keyClass);
    }

    @Override
    public void close() {
        pool.close();
    }

    @Override
    public void removeStorage(String storageName) {
        callJedis(jedis -> {
            // TODO: pagenation for large data
            // https://stackoverflow.com/questions/32603964/scan-vs-keys-performance-in-redis
            ScanResult<String> scanResult = jedis.scan(storageName + "*");
            List<String> keys = scanResult.getResult();
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[keys.size()]));
            }
            return null;
        });
    }

    private Object callJedis(Function<Jedis, Object> function) {
        try (Jedis jedis = pool.getResource()) {
            return function.apply(jedis);
        }
    }

    @Override
    public void removeStoragesBySessionId(String sessionId) {
        callJedis(jedis -> {
            // TODO: pagenation for large data
            ScanResult<String> scanResult = jedis.scan(SESSION_STORAGE_PREFIX + sessionId + DELIMITER + "*");
            List<String> keys = scanResult.getResult();
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[keys.size()]));
            }
            return null;
        });
    }

    @Override
    public void removeAllSessionStorages() {
        callJedis(jedis -> {
            // TODO: pagenation for large data
            ScanResult<String> scanResult = jedis.scan(SESSION_STORAGE_PREFIX + "*");
            List<String> keys = scanResult.getResult();
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[keys.size()]));
            }
            return null;
        });
    }

    @Override
    public Set<String> getStorageNames() {
        // We can implement this by storing all storage names with a specific key
        throw new UnsupportedOperationException("Currently, not supported");
    }

    //--- test purpose

    @Override
    public void restart() {
        // JVM crashed
        pool.close();
        pool = null;

        // Reboot
        initStorageManager();
    }

    @Override
    public void restartWithCleanUp() {
        // JVM crashed
        pool.close();
        pool = null;

        // Reboot
        initStorageManager();

        // Cleanup
        callJedis(jedis -> {
            jedis.flushDB();
            return null;
        });
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
