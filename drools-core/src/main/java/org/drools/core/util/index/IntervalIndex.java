/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.drools.core.util.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class IntervalIndex<K extends Comparable, V extends Interval<K>> implements Serializable {

    private final TreeMap<IndexKey<K>, Bucket<V>> map = new TreeMap<>();

    private final Collection<V> allIntervals = new HashSet<>();

    public void addInterval(V interval) {
        if (!interval.isValid()) {
            // TODO: Preliminary check is required for AlphaNodeInterval. Just don't add such invalid intervals to the index. (No need for Exception)
            throw new IllegalStateException("Interval is invalid : " + interval);
        }
        if (!allIntervals.add(interval)) {
            return; // Ensure that the same instance(same hash) is not added. The duplicate intervals are okay (Should not happen with AlphaNode though)
        }

        // leftBucket stores the interval
        IndexKey<K> leftKey = new IndexKey<>(interval.isStartInclusive() ? IndexType.INCLUSIVE : IndexType.EXCLUSIVE_RIGHT, interval.getStart());
        Bucket<V> leftBucket = map.computeIfAbsent(leftKey, k -> {
            Bucket<V> newBucket = new Bucket<>();
            Entry<IndexKey<K>, Bucket<V>> leftFloorEntry = map.floorEntry(leftKey);
            if (leftFloorEntry != null) {
                newBucket.addAll(leftFloorEntry.getValue().values); // add all intervals from the floor bucket
            }
            return newBucket;
        });

        // rightBucket does not store the interval
        IndexKey<K> rightKey = new IndexKey<>(interval.isEndInclusive() ? IndexType.EXCLUSIVE_RIGHT : IndexType.INCLUSIVE, interval.getEnd());
        Bucket<V> rightBucket = map.computeIfAbsent(rightKey, k -> {
            Bucket<V> newBucket = new Bucket<>();
            Entry<IndexKey<K>, Bucket<V>> rightFloorEntry = map.floorEntry(rightKey);
            if (rightFloorEntry != null) {
                newBucket.addAll(rightFloorEntry.getValue().values); // add all intervals from the floor bucket
            }
            return newBucket;
        });

        leftBucket.add(interval); // Add the interval after creating the rightBucket (because floorEntry may pick the leftBucket)
        leftBucket.referenceCount++;
        rightBucket.referenceCount++; // Just increment referenceCount to note that the bucket is in use

        // Maintain in-between buckets
        Collection<Bucket<V>> buckets = map.subMap(leftKey, false, rightKey, false).values();
        buckets.forEach(bucket -> bucket.add(interval));
    }

    public void removeInterval(V interval) {
        if (!allIntervals.remove(interval)) {
            throw new NoSuchElementException("The interval " + interval + " doesn't exist in the index");
        }

        // leftBucket
        IndexKey<K> leftKey = new IndexKey<>(interval.isStartInclusive() ? IndexType.INCLUSIVE : IndexType.EXCLUSIVE_RIGHT, interval.getStart());
        Bucket<V> leftBucket = map.get(leftKey);
        leftBucket.remove(interval);
        leftBucket.referenceCount--;
        removeBucketIfPossible(leftKey, leftBucket);

        // rightBucket
        IndexKey<K> rightKey = new IndexKey<>(interval.isEndInclusive() ? IndexType.EXCLUSIVE_RIGHT : IndexType.INCLUSIVE, interval.getEnd());
        Bucket<V> rightBucket = map.get(rightKey);
        rightBucket.referenceCount--;
        removeBucketIfPossible(rightKey, rightBucket);

        // Maintain in-between buckets
        Collection<Bucket<V>> buckets = map.subMap(leftKey, false, rightKey, false).values();
        buckets.forEach(bucket -> {
            if (!bucket.remove(interval)) {
                throw new NoSuchElementException("The interval " + interval + " doesn't exist in the bucket"); // TODO: remove this check after testing
            }
        });
    }

    private void removeBucketIfPossible(IndexKey<K> key, Bucket<V> bucket) {
        if (bucket.referenceCount == 0) {
            map.remove(key); // if this bucket is no longer used, remove it
        }
    }

    /**
     * Get intervals from floorEntry bucket which should have all matching intervals
     */
    public Collection<V> getIntervals(K key) {
        Entry<IndexKey<K>, Bucket<V>> entry = map.floorEntry(new IndexKey<>(IndexType.INCLUSIVE, key));
        return entry == null ? Collections.emptySet() : entry.getValue().values;
    }

    public Collection<V> getAllIntervals() {
        return allIntervals;
    }

    /**
     * for test
     */
    int getMapSize() {
        return map.size();
    }

    @Override
    public String toString() {
        return "IntervalIndex [map=" + map + "]";
    }

    public enum IndexType {

        INCLUSIVE,
        EXCLUSIVE_RIGHT;
    }

    private static class IndexKey<K extends Comparable> implements Comparable<IndexKey<K>>, Serializable {

        private final IndexType indexType;
        private final K key;

        public IndexKey(IndexType indexType, K key) {
            this.indexType = indexType;
            this.key = key;
        }

        @Override
        public int compareTo(IndexKey<K> o) {
            int orderDiff = key.compareTo(o.key);
            return orderDiff != 0 ? orderDiff : indexType.compareTo(o.indexType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((indexType == null) ? 0 : indexType.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            IndexKey<K> other = (IndexKey<K>) obj;
            if (indexType != other.indexType)
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "IndexKey [key=" + key + ", indexType=" + indexType + "]";
        }
    }

    private static class Bucket<V> {

        private int referenceCount = 0; // how many times this bucket is used for left/right key (not for in-between bucket)
        private Collection<V> values = new HashSet<>();

        public Bucket() {}

        public boolean add(V value) {
            return values.add(value);
        }

        public boolean addAll(Collection<V> newValues) {
            return values.addAll(newValues);
        }

        public boolean remove(V value) {
            return values.remove(value);
        }

        @Override
        public String toString() {
            return "Bucket [referenceCount=" + referenceCount + ", values=" + values + "]";
        }
    }
}
