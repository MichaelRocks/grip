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

package io.michaelrocks.grip

import io.michaelrocks.grip.commons.LazyMap
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.Type

interface FieldsResult : Map<Type.Object, List<FieldMirror>> {
  val types: Set<Type.Object>
    get() = keys

  fun containsType(type: Type.Object) =
      containsKey(type)

  class Builder {
    private val fields = LazyMap<Type.Object, List<FieldMirror>>()

    fun addFields(classMirror: ClassMirror, fieldMirrors: Iterable<FieldMirror>) = apply {
      val oldFields = fields.put(classMirror.type, fieldMirrors.toList())
      require(oldFields == null) { "Fields for class ${classMirror.type} have already been added" }
    }

    fun build(): FieldsResult = ImmutableFieldsResult(this)

    private class ImmutableFieldsResult(
        builder: Builder
    ) : FieldsResult, Map<Type.Object, List<FieldMirror>> by builder.fields.detachImmutableCopy()
  }
}

internal inline fun buildFieldsResult(body: FieldsResult.Builder.() -> Unit) =
    FieldsResult.Builder().run {
      body()
      build()
    }

val Map.Entry<Type.Object, List<FieldMirror>>.type: Type.Object
  get() = key
val Map.Entry<Type.Object, List<FieldMirror>>.fields: List<FieldMirror>
  get() = value
