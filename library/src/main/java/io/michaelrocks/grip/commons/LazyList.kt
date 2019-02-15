/*
 * Copyright 2021 Michael Rozumyanskiy
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

import java.util.ArrayList
import java.util.Collections

internal class LazyList<T>(val factory: () -> MutableList<T> = { ArrayList() }) : MutableList<T> {
  private var delegate: MutableList<T> = emptyMutableList()

  override val size: Int
    get() = delegate.size

  override fun contains(element: T): Boolean =
    delegate.contains(element)

  override fun containsAll(elements: Collection<T>): Boolean =
    delegate.containsAll(elements)

  override fun get(index: Int): T =
    delegate[index]

  override fun indexOf(element: T): Int =
    delegate.indexOf(element)

  override fun isEmpty(): Boolean =
    delegate.isEmpty()

  override fun iterator(): MutableIterator<T> =
    delegate.iterator()

  override fun lastIndexOf(element: T): Int =
    delegate.lastIndexOf(element)

  override fun add(element: T): Boolean =
    mutate().add(element)

  override fun add(index: Int, element: T) =
    mutate().add(index, element)

  override fun addAll(elements: Collection<T>): Boolean =
    mutate().addAll(elements)

  override fun addAll(index: Int, elements: Collection<T>): Boolean =
    mutate().addAll(index, elements)

  override fun clear() =
    delegate.clear()

  override fun listIterator(): MutableListIterator<T> =
    delegate.listIterator()

  override fun listIterator(index: Int): MutableListIterator<T> =
    delegate.listIterator(index)

  override fun remove(element: T): Boolean =
    delegate.remove(element)

  override fun removeAll(elements: Collection<T>): Boolean =
    delegate.removeAll(elements)

  override fun removeAt(index: Int): T =
    delegate.removeAt(index)

  override fun retainAll(elements: Collection<T>): Boolean =
    delegate.retainAll(elements)

  override fun set(index: Int, element: T): T =
    delegate.set(index, element)

  override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
    delegate.subList(fromIndex, toIndex)

  fun immutableCopy(): List<T> =
    if (isEmpty()) listOf()
    else toMutableList().immutable()

  fun detachImmutableCopy(): List<T> =
    delegate.immutable().apply {
      delegate = emptyMutableList()
    }

  @Suppress("UNCHECKED_CAST")
  private fun emptyMutableList() =
    Collections.EMPTY_LIST as MutableList<T>

  private fun mutate(): MutableList<T> {
    if (delegate === Collections.EMPTY_LIST) {
      delegate = factory()
    }
    return delegate
  }
}
