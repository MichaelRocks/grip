/*
 * Copyright 2016 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.grip.commons

import java.util.*

internal class LazyMap<K, V>(private val factory: () -> MutableMap<K, V> = { HashMap() }) : MutableMap<K, V> {
  private var delegate: MutableMap<K, V> = emptyMutableMap()

  override val size: Int
    get() = delegate.size
  override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() = delegate.entries
  override val keys: MutableSet<K>
    get() = delegate.keys
  override val values: MutableCollection<V>
    get() = delegate.values

  override fun clear() = delegate.clear()

  override fun put(key: K, value: V): V? =
      mutate().put(key, value)

  override fun putAll(from: Map<out K, V>) =
      mutate().putAll(from)

  override fun remove(key: K): V? =
      delegate.remove(key)

  override fun containsKey(key: K): Boolean =
      delegate.containsKey(key)

  override fun containsValue(value: V): Boolean =
      delegate.containsValue(value)

  @Suppress("ReplaceGetOrSet")
  override fun get(key: K): V? =
      delegate.get(key)

  override fun isEmpty(): Boolean =
      delegate.isEmpty()

  fun immutableCopy(): Map<K, V> =
      if (isEmpty()) mapOf()
      else HashMap(delegate).immutable()

  fun detachImmutableCopy(): Map<K, V> =
      delegate.immutable().apply {
        delegate = emptyMutableMap()
      }

  @Suppress("UNCHECKED_CAST")
  private fun emptyMutableMap() =
      Collections.EMPTY_MAP as MutableMap<K, V>

  private fun mutate(): MutableMap<K, V> {
    if (delegate === Collections.EMPTY_MAP) {
      delegate = factory()
    }
    return delegate
  }
}
