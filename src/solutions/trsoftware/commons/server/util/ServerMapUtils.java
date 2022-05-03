/*
 * Copyright 2022 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.callables.Function2;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Sep 30, 2009
 *
 * @author Alex
 */
public class ServerMapUtils extends MapUtils {

  /**
   * Inserts a new instance of the given class into the map for the given key
   * if the map doesn't already contain it.
   *
   * This method is has to be server-side because GWT doesn't allow instantiation by reflection.
   *
   * @param valueClass Must have a 0-arg constructor.
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map.
   *
   * @deprecated Can be replaced with {@link ConcurrentMap#computeIfAbsent(Object, Function)} using a method reference
   * for the class constructor.
   */
  public static <K,V> V getOrInsert(Map<K,V> map, K key, final Class<? extends V> valueClass) {
    return MapUtils.getOrInsert(map, key, (Supplier<V>)() -> {
      try {
        return valueClass.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Inserts a value into the {@link ConcurrentMap} if the map doesn't already contain it.
   * This method is thread-safe but doesn't use locking by taking advantage
   * of {@link ConcurrentMap#putIfAbsent}.
   *
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map.
   *
   * @deprecated Use {@link ConcurrentMap#computeIfAbsent(Object, Function)} instead
   */
  public static <K,V> V getOrInsertConcurrent(ConcurrentMap<K,V> map, K key, Function0<V> valueFactory) {
    // as a performance optimization, don't create the new value when the map already contains one
    V priorValue = map.get(key);
    if (priorValue != null)
      return priorValue;
    return getOrInsertConcurrent(map, key, valueFactory.call());
  }

  /**
   * Inserts a value into the {@link ConcurrentMap} if the map doesn't already contain it.
   * This method is thread-safe but doesn't use locking by taking advantage
   * of {@link ConcurrentMap#putIfAbsent}.
   *
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map.
   *
   * @deprecated Use {@link ConcurrentMap#computeIfAbsent(Object, Function)} instead
   */
  public static <K,V,A> V getOrInsertConcurrent(ConcurrentMap<K,V> map, K key, Function1<A, V> valueFactory, A factoryArg) {
    // as a performance optimization, don't create the new value when the map already contains one
    V priorValue = map.get(key);
    if (priorValue != null)
      return priorValue;
    return getOrInsertConcurrent(map, key, valueFactory.call(factoryArg));
  }

  /**
   * Inserts a value into the ConcurrentMap if the map doesn't already contain it.
   * This method is thread-safe but doesn't use locking by taking advantage
   * of {@link ConcurrentMap#putIfAbsent}.
   *
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map.
   *
   * @deprecated Use {@link ConcurrentMap#computeIfAbsent(Object, Function)} instead
   */
  public static <K,A1,A2,V> V getOrInsertConcurrent(ConcurrentMap<K,V> map, K key, Function2<A1, A2, V> valueFactory, A1 factoryArg1, A2 factoryArg2) {
    // as a performance optimization, don't create the new value when the map already contains one
    V priorValue = map.get(key);
    if (priorValue != null)
      return priorValue;
    return getOrInsertConcurrent(map, key, valueFactory.call(factoryArg1, factoryArg2));
  }

  /**
   * Inserts a value into the ConcurrentMap if the map doesn't already contain it.
   * This method is thread-safe but doesn't use locking by taking advantage
   * of {@link ConcurrentMap#putIfAbsent}.
   *
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map.
   *
   * @deprecated Use {@link ConcurrentMap#putIfAbsent(Object, Object)} instead
   */
  public static <K,V> V getOrInsertConcurrent(ConcurrentMap<K,V> map, K key, V value) {
    // we let threads compete for who gets to insert their value and return
    // the winner's value (most likely our own)
    V putResult = map.putIfAbsent(key, value);
    if (putResult == null)
      return value;  // our value was just inserted (null indicates no previous value existed)
    else
      return putResult; // another thread must have beat us to it, use their value
  }

}
