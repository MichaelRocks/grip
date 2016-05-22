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
  class Raw(val type: Type) : GenericType() {
    override fun toString(): String = type.className
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type }
    override fun hashCode(): Int = 31 + 17 * type.hashCode()
  }

  class TypeVariable(val name: String) : GenericType() {
    override fun toString(): String = name
    override fun equals(other: Any?): Boolean = equals(other) { name == it.name }
    override fun hashCode(): Int = 31 + 17 * name.hashCode()
  }

  class Array(val elementType: GenericType) : GenericType() {
    override fun toString(): String = "$elementType[]"
    override fun equals(other: Any?): Boolean = equals(other) { elementType == it.elementType }
    override fun hashCode(): Int = 31 + 17 * elementType.hashCode()
  }

  class Parameterized(val type: Type.Object, val typeArguments: List<GenericType>) : GenericType() {

    constructor(
        type: Type.Object,
        typeArgument: GenericType,
        vararg typeArguments: GenericType
    ) : this(type, listOf(typeArgument) + typeArguments.asList())

    override fun toString() =
        StringBuilder("${type.className}").apply { typeArguments.joinTo(this, prefix = "<", postfix = ">") }.toString()

    override fun equals(other: Any?): Boolean = equals(other) { type == it.type && typeArguments == it.typeArguments }
    override fun hashCode(): Int = 17 * (31 + 17 * type.hashCode()) + 17 * typeArguments.hashCode()
  }

  class Inner(val type: GenericType, val ownerType: GenericType) : GenericType() {
    override fun toString(): String = "$ownerType.$type"
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type && ownerType == it.ownerType }
    override fun hashCode(): Int = 17 * (31 + 17 * type.hashCode()) + 17 * ownerType.hashCode()
  }

  class UpperBounded(val upperBound: GenericType) : GenericType() {
    override fun toString(): String = "? extends $upperBound"
    override fun equals(other: Any?): Boolean = equals(other) { upperBound == it.upperBound }
    override fun hashCode(): Int = 31 + 17 * upperBound.hashCode()
  }

  class LowerBounded(val lowerBound: GenericType) : GenericType() {
    override fun toString(): String = "? super $lowerBound"
    override fun equals(other: Any?): Boolean = equals(other) { lowerBound == it.lowerBound }
    override fun hashCode(): Int = 31 + 17 * lowerBound.hashCode()
  }
}

internal val OBJECT_RAW_TYPE = GenericType.Raw(getType<Any>())

private inline fun <reified T : Any> T.equals(other: Any?, body: (T) -> Boolean): Boolean {
  if (this === other) {
    return true
  }

  val that = other as? T ?: return false
  return body(that)
}
