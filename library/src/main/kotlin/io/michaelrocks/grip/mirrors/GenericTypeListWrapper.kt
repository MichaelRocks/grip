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

package io.michaelrocks.grip.mirrors

import io.michaelrocks.grip.commons.immutable
import io.michaelrocks.grip.mirrors.signature.GenericType

internal class GenericTypeListWrapper<T : Type>(private val types: List<T>) : List<GenericType> {
  private val genericTypes by lazy(LazyThreadSafetyMode.NONE) {
    types.map { GenericType.Raw(it) }.immutable()
  }

  override val size: Int
    get() = types.size

  override fun contains(element: GenericType): Boolean =
      element is GenericType.Raw && types.contains(element.type)

  override fun containsAll(elements: Collection<GenericType>): Boolean {
    for (element in elements) {
      if (!contains(element)) {
        return false
      }
    }
    return true
  }

  override fun get(index: Int): GenericType =
      genericTypes[index]

  override fun indexOf(element: GenericType): Int =
      if (element is GenericType.Raw) types.indexOf(element.type) else -1

  override fun isEmpty(): Boolean =
      types.isEmpty()

  override fun iterator(): Iterator<GenericType> =
      genericTypes.iterator()

  override fun lastIndexOf(element: GenericType): Int =
      if (element is GenericType.Raw) types.lastIndexOf(element.type) else -1

  override fun listIterator(): ListIterator<GenericType> =
      genericTypes.listIterator()

  override fun listIterator(index: Int): ListIterator<GenericType> =
      genericTypes.listIterator(index)

  override fun subList(fromIndex: Int, toIndex: Int): List<GenericType> =
      genericTypes.subList(fromIndex, toIndex)
}
