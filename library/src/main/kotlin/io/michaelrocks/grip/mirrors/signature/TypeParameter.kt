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

package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.commons.LazyList

interface TypeParameter {
  val name: String
  val classBound: GenericType
  val interfaceBounds: List<GenericType>

  class Builder(val name: String) {
    private var classBound: GenericType = OBJECT_RAW_TYPE
    private val interfaceBounds = LazyList<GenericType>()

    fun classBound(classBound: GenericType) = apply {
      this.classBound = classBound
    }

    fun addInterfaceBound(interfaceBound: GenericType) = apply {
      interfaceBounds += interfaceBound
    }

    fun build(): TypeParameter = TypeParameterImpl(this)

    private data class TypeParameterImpl(
        override val name: String,
        override val classBound: GenericType,
        override val interfaceBounds: List<GenericType>
    ) : TypeParameter {

      constructor(
          builder: Builder
      ) : this(builder.name, builder.classBound, builder.interfaceBounds.detachImmutableCopy())

      override fun toString() = "$name extends ${interfaceBounds.joinToString(" & ", prefix = classBound.toString())}"
    }
  }
}
