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

import io.michaelrocks.grip.commons.LazyMap
import java.util.Arrays

interface AnnotationMirror : Typed<Type.Object> {
  val values: Map<String, Any>
  val resolved: Boolean

  class Builder {
    private var type: Type.Object? = null
    private val values = LazyMap<String, Any>()

    fun type(type: Type.Object) = apply {
      this.type = type
    }

    fun addValue(value: Any) = addValue("value", value)

    fun addValue(name: String, value: Any) = apply {
      this.values.put(name, value)
    }

    fun addValues(mirror: AnnotationMirror) = apply {
      this.values.putAll(mirror.values)
    }

    fun build(): AnnotationMirror = ImmutableAnnotationMirror(this)

    private class ImmutableAnnotationMirror(builder: Builder) : AbstractAnnotationMirror() {
      override val type = builder.type!!
      override val values = builder.values.immutableCopy()
      override val resolved: Boolean
        get() = true
    }
  }
}

internal abstract class AbstractAnnotationMirror : AnnotationMirror {
  override fun toString(): String = "AnnotationMirror{type = $type, values = $values}"

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    val that = other as? AnnotationMirror ?: return false
    if (type != that.type || values.size != that.values.size || resolved != that.resolved) {
      return false
    }

    return values.all {
      val value = that.values[it.key] ?: return false
      if (value.javaClass.isArray) {
        when (value) {
          is BooleanArray -> Arrays.equals(value, it.value as? BooleanArray)
          is ByteArray -> Arrays.equals(value, it.value as? ByteArray)
          is CharArray -> Arrays.equals(value, it.value as? CharArray)
          is DoubleArray -> Arrays.equals(value, it.value as? DoubleArray)
          is FloatArray -> Arrays.equals(value, it.value as? FloatArray)
          is IntArray -> Arrays.equals(value, it.value as? IntArray)
          is LongArray -> Arrays.equals(value, it.value as? LongArray)
          is ShortArray -> Arrays.equals(value, it.value as? ShortArray)
          is Array<*> -> Arrays.equals(value, it.value as? Array<*>)
          else -> error("Huh, unknown array type: $value")
        }
      } else {
        it.value == value
      }
    }
  }

  override fun hashCode(): Int {
    val valuesHashCode = values.entries.fold(37) {
      hashCode, entry -> hashCode + (entry.key.hashCode() xor hashCode(entry.value))
    }
    var hashCode = 37;
    hashCode = hashCode * 17 + type.hashCode()
    hashCode = hashCode * 17 + valuesHashCode
    hashCode = hashCode * 17 + resolved.hashCode()
    return hashCode
  }

  private fun hashCode(value: Any?): Int {
    value ?: return 0
    return if (value.javaClass.isArray) {
      when (value) {
        is BooleanArray -> Arrays.hashCode(value)
        is ByteArray -> Arrays.hashCode(value)
        is CharArray -> Arrays.hashCode(value)
        is DoubleArray -> Arrays.hashCode(value)
        is FloatArray -> Arrays.hashCode(value)
        is IntArray -> Arrays.hashCode(value)
        is LongArray -> Arrays.hashCode(value)
        is ShortArray -> Arrays.hashCode(value)
        is Array<*> -> value.fold(37) { current, item -> current * 17 + hashCode(item) }
        else -> error("Huh, unknown array type: $value")
      }
    } else {
      value.hashCode()
    }
  }
}

internal class UnresolvedAnnotationMirror(
    override val type: Type.Object
) : AbstractAnnotationMirror() {
  override val values: Map<String, Any>
    get() = emptyMap()
  override val resolved: Boolean
    get() = false

  override fun toString(): String = "UnresolvedAnnotationMirror{type = $type}"
}

fun buildAnnotation(type: Type.Object): AnnotationMirror =
    AnnotationMirror.Builder().type(type).build()

inline fun buildAnnotation(type: Type.Object, body: AnnotationMirror.Builder.() -> Unit): AnnotationMirror =
    AnnotationMirror.Builder().run {
      type(type)
      body()
      build()
    }
