/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.util;

import org.apache.ignite.*;
import org.jdk8.backport.*;

import java.util.concurrent.*;

import static org.apache.ignite.IgniteSystemProperties.*;

/**
 * Concurrent map factory.
 */
public class GridConcurrentFactory {
    /** Default concurrency level. */
    private static final int CONCURRENCY_LEVEL = IgniteSystemProperties.getInteger(IGNITE_MAP_CONCURRENCY_LEVEL, 256);

    /**
     * Ensure singleton.
     */
    private GridConcurrentFactory() {
        // No-op.
    }

    /**
     * Creates concurrent map with default concurrency level.
     *
     * @return New concurrent map.
     */
    public static <K, V> ConcurrentMap<K, V> newMap() {
        return new ConcurrentHashMap8<>(16 * CONCURRENCY_LEVEL, 0.75f, CONCURRENCY_LEVEL);
    }

    /**
     * Creates concurrent map with default concurrency level and given {@code initCap}.
     *
     * @param initCap Initial capacity.
     * @return New concurrent map.
     */
    public static <K, V> ConcurrentMap<K, V> newMap(int initCap) {
        return new ConcurrentHashMap8<>(initialSize(initCap, CONCURRENCY_LEVEL), 0.75f, CONCURRENCY_LEVEL);
    }

    /**
     * Creates concurrent map with given concurrency level and initCap.
     *
     * @param initCap Initial capacity.
     * @param concurrencyLevel Concurrency level.
     * @return New concurrent map.
     */
    public static <K, V> ConcurrentMap<K, V> newMap(int initCap, int concurrencyLevel) {
        return new ConcurrentHashMap8<>(initialSize(initCap, concurrencyLevel), 0.75f, concurrencyLevel);
    }

    /**
     * Creates concurrent set with default concurrency level.
     *
     * @return New concurrent map.
     */
    public static <V> GridConcurrentHashSet<V> newSet() {
        return new GridConcurrentHashSet<>(16 * CONCURRENCY_LEVEL, 0.75f, CONCURRENCY_LEVEL);
    }

    /**
     * Creates concurrent set with default concurrency level and given {@code initCap}.
     *
     * @param initCap Initial capacity.
     * @return New concurrent map.
     */
    public static <V> GridConcurrentHashSet<V> newSet(int initCap) {
        return new GridConcurrentHashSet<>(initialSize(initCap, CONCURRENCY_LEVEL), 0.75f, CONCURRENCY_LEVEL);
    }

    /**
     * Creates concurrent set with given concurrency level and initCap.
     *
     * @param initCap Initial capacity.
     * @param concurrencyLevel Concurrency level.
     * @return New concurrent map.
     */
    public static <V> GridConcurrentHashSet<V> newSet(int initCap, int concurrencyLevel) {
        return new GridConcurrentHashSet<>(initialSize(initCap, concurrencyLevel), 0.75f, concurrencyLevel);
    }

    /**
     * @param cap Capacity.
     * @param concurrencyLevel Concurrency level.
     * @return Calculated size.
     */
    private static int initialSize(int cap, int concurrencyLevel) {
        return cap / concurrencyLevel < 16 ? 16 * concurrencyLevel : cap;
    }
}
