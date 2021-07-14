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

import io.michaelrocks.grip.commons.LazyList

interface MethodParameterMirror : Annotated {
  val index: Int
  val type: Type

  class Builder(
    val index: Int,
    val type: Type
  ) {
    private val annotations = LazyList<AnnotationMirror>()

    fun addAnnotation(annotation: AnnotationMirror) = apply {
      annotations += annotation
    }

    fun build(): MethodParameterMirror = ImmutableMethodParameterMirror(this)

    private class ImmutableMethodParameterMirror(builder: Builder) : MethodParameterMirror {
      override val index = builder.index
      override val type = builder.type
      override val annotations: AnnotationCollection = ImmutableAnnotationCollection(builder.annotations)

      override fun toString(): String = "MethodParameterMirror{index = $index, type = $type}"
    }
  }
}
