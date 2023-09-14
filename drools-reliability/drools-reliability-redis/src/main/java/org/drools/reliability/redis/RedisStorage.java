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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.drools.core.common.Storage;
import org.drools.reliability.core.StorageManagerFactory;
import org.drools.util.IoUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class RedisStorage<K, V> implements Storage<K, V> {

    public static final String KEY_DELIMITER = ":";

    private final String storagePrefix;

    private final JedisPool pool;

    private final Class<K> keyClass;

    public RedisStorage(String storageId, JedisPool pool, Class<K> keyClass) {
        // This implementation always assumes that the key is a String or a Long
        // If we use Object as the key (e.g. FullStoreRuntimeEventListener.componentsCache), this storagePrefix strategy should be changed
        this.storagePrefix = storageId + KEY_DELIMITER;
        this.pool = pool;
        this.keyClass = keyClass;
    }

    @Override
    public V get(K key) {
        return getOrDefault(key, null);
    }

    private Object callJedis(Function<Jedis, Object> function) {
        try (Jedis jedis = pool.getResource()) {
            return function.apply(jedis);
        }
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        byte[] serializedValue = (byte[]) callJedis(jedis -> jedis.get((storagePrefix + key.toString()).getBytes()));
        return serializedValue == null ? defaultValue : (V) IoUtils.deserialize(serializedValue);
    }

    @Override
    public V put(K key, V value) {
        byte[] serializedValue = IoUtils.serialize(value);
        callJedis(jedis -> (V) jedis.set((storagePrefix + key.toString()).getBytes(), serializedValue));
        return null; // return null considering performance. There is no usage which requires the previous value. If needed, getset can be used
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> otherMap) {
        callJedis(jedis -> {
            Pipeline pipeline = jedis.pipelined();
            otherMap.forEach((key, value) -> {
                byte[] serializedValue = IoUtils.serialize(value);
                pipeline.set((storagePrefix + key.toString()).getBytes(), serializedValue);
            });
            pipeline.sync();
            return null;
        });
    }

    @Override
    public boolean containsKey(K key) {
        return (boolean) callJedis(jedis -> jedis.exists(storagePrefix + key.toString()));
    }

    @Override
    public V remove(K key) {
        callJedis(jedis -> jedis.del(storagePrefix + key.toString()));
        return null; // return null considering performance. There is no usage which requires the previous value. If needed, getdel can be used
    }

    @Override
    public void clear() {
        callJedis(jedis -> {
            Set<String> keys = jedis.keys(storagePrefix + "*");
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[keys.size()]));
            }
            return null;
        });
    }

    @Override
    public Collection<V> values() {
        return (Collection<V>) callJedis(jedis -> {
            Set<String> keys = jedis.keys(storagePrefix + "*");
            if (!keys.isEmpty()) {
                byte[][] keyBytesArray = keys.stream().map(String::getBytes).toArray(byte[][]::new);
                List<byte[]> valueBytesList = jedis.mget(keyBytesArray);
                return valueBytesList.stream().map(IoUtils::deserialize).collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }

    @Override
    public Set<K> keySet() {
        return (Set<K>) callJedis(jedis -> {
            Set<byte[]> keyBytesSet = jedis.keys((storagePrefix + "*").getBytes());
            if (!keyBytesSet.isEmpty()) {
                return keyBytesSet.stream().map(bytes -> {
                    String fullKey = new String(bytes);
                    String key = fullKey.substring(storagePrefix.length());
                    if (keyClass == String.class) {
                        return (K) key;
                    } else if (keyClass == Long.class) {
                        return (K) Long.valueOf(key);
                    } else {
                        throw new UnsupportedOperationException("Unsupported keyClass: " + keyClass);
                    }
                }).collect(Collectors.toSet());
            }
            return Collections.emptySet();
        });
    }

    @Override
    public int size() {
        return (int) callJedis(jedis -> jedis.keys(storagePrefix + "*").size());
    }

    @Override
    public boolean isEmpty() {
        return (boolean) callJedis(jedis -> jedis.keys(storagePrefix + "*").isEmpty());
    }
}
