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

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getType

sealed class GenericType {
  data class Raw(val type: Type) : GenericType() {
    override fun toString(): String = type.className
  }

  data class TypeVariable(val name: String) : GenericType() {
    override fun toString(): String = name
  }

  data class Array(val elementType: GenericType) : GenericType() {
    override fun toString(): String = "$elementType[]"
  }

  data class Parameterized(val type: Type.Object, val typeArguments: List<GenericType>) : GenericType() {
    constructor(
        type: Type.Object,
        typeArgument: GenericType,
        vararg typeArguments: GenericType
    ) : this(type, listOf(typeArgument) + typeArguments.asList())

    override fun toString() =
        StringBuilder(type.className).apply { typeArguments.joinTo(this, prefix = "<", postfix = ">") }.toString()
  }

  data class Inner(val type: GenericType, val ownerType: GenericType) : GenericType() {
    override fun toString(): String = "$ownerType.$type"
  }

  data class UpperBounded(val upperBound: GenericType) : GenericType() {
    override fun toString(): String = "? extends $upperBound"
  }

  data class LowerBounded(val lowerBound: GenericType) : GenericType() {
    override fun toString(): String = "? super $lowerBound"
  }
}

internal val OBJECT_RAW_TYPE = GenericType.Raw(getType<Any>())
