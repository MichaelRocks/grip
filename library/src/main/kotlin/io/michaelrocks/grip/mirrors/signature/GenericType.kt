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

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "NOTHING_TO_INLINE")

package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getType
import java.util.Arrays
import java.lang.Boolean as JavaBoolean
import java.lang.Byte as JavaByte
import java.lang.Character as JavaChar
import java.lang.Double as JavaDouble
import java.lang.Float as JavaFloat
import java.lang.Integer as JavaInt
import java.lang.Long as JavaLong
import java.lang.Short as JavaShort

sealed class GenericType {
  class Raw(val type: Type) : GenericType() {
    override fun toString(): String = type.className
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type }
    override fun hashCode(): Int = 17 then type
  }

  class TypeVariable(val name: String) : GenericType() {
    override fun toString(): String = name
    override fun equals(other: Any?): Boolean = equals(other) { name == it.name }
    override fun hashCode(): Int = 17 then name
  }

  class Array(val elementType: GenericType) : GenericType() {
    override fun toString(): String = "$elementType[]"
    override fun equals(other: Any?): Boolean = equals(other) { elementType == it.elementType }
    override fun hashCode(): Int = 17 then elementType
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
    override fun hashCode(): Int = 17 then type then typeArguments
  }

  class Inner(val type: GenericType, val ownerType: GenericType) : GenericType() {
    override fun toString(): String = "$ownerType.$type"
    override fun equals(other: Any?): Boolean = equals(other) { type == it.type && ownerType == it.ownerType }
    override fun hashCode(): Int = 17 then type then ownerType
  }

  class UpperBounded(val upperBound: GenericType) : GenericType() {
    override fun toString(): String = "? extends $upperBound"
    override fun equals(other: Any?): Boolean = equals(other) { upperBound == it.upperBound }
    override fun hashCode(): Int = 17 then upperBound
  }

  class LowerBounded(val lowerBound: GenericType) : GenericType() {
    override fun toString(): String = "? super $lowerBound"
    override fun equals(other: Any?): Boolean = equals(other) { lowerBound == it.lowerBound }
    override fun hashCode(): Int = 17 then lowerBound
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

private inline infix fun Int.then(value: Boolean) = combine(this, JavaBoolean.hashCode(value))
private inline infix fun Int.then(value: Byte) = combine(this, JavaByte.hashCode(value))
private inline infix fun Int.then(value: Char) = combine(this, JavaChar.hashCode(value))
private inline infix fun Int.then(value: Double) = combine(this, JavaDouble.hashCode(value))
private inline infix fun Int.then(value: Float) = combine(this, JavaFloat.hashCode(value))
private inline infix fun Int.then(value: Int) = combine(this, JavaInt.hashCode(value))
private inline infix fun Int.then(value: Long) = combine(this, JavaLong.hashCode(value))
private inline infix fun Int.then(value: Short) = combine(this, JavaShort.hashCode(value))
private inline infix fun Int.then(value: BooleanArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: ByteArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: CharArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: DoubleArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: FloatArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: IntArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: LongArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: ShortArray?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: Array<*>?) = combine(this, value?.let { Arrays.hashCode(it) } ?: 0)
private inline infix fun Int.then(value: Any?) = combine(this, value?.hashCode() ?: 0)
private inline fun combine(current: Int, value: Int) = current * 31 + value
