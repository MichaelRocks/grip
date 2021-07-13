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

package io.michaelrocks.grip.mirrors

interface AnnotationCollection : Collection<AnnotationMirror> {
  operator fun contains(type: Type.Object): Boolean
  operator fun get(type: Type.Object): AnnotationMirror?
}

internal class ImmutableAnnotationCollection(collection: Collection<AnnotationMirror>) : AnnotationCollection {
  private val annotationsByType: Map<Type.Object, AnnotationMirror> =
      if (collection.isEmpty()) emptyMap()
      else collection.associateBy { it.type }

  override val size: Int
    get() = annotationsByType.size

  constructor(annotation: AnnotationMirror) : this(listOf(annotation))

  override fun contains(element: AnnotationMirror): Boolean =
      annotationsByType.containsValue(element)

  override fun containsAll(elements: Collection<AnnotationMirror>): Boolean =
      annotationsByType.values.containsAll(elements)

  override fun isEmpty(): Boolean =
      annotationsByType.isEmpty()

  override fun iterator(): Iterator<AnnotationMirror> =
      annotationsByType.values.iterator()

  override fun contains(type: Type.Object): Boolean =
      annotationsByType.containsKey(type)

  override fun get(type: Type.Object): AnnotationMirror? =
      annotationsByType[type]
}
