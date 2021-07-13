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
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type

interface MethodsResult : Map<Type.Object, List<MethodMirror>> {
  val types: Set<Type.Object>
    get() = keys

  fun containsType(type: Type.Object) =
      containsKey(type)

  class Builder {
    private val methods = LazyMap<Type.Object, List<MethodMirror>>()

    fun addMethods(classMirror: ClassMirror, methodMirrors: Iterable<MethodMirror>) = apply {
      val oldMethods = methods.put(classMirror.type, methodMirrors.toList())
      require(oldMethods == null) { "Methods for class ${classMirror.type} have already been added" }
    }

    fun build(): MethodsResult = ImmutableMethodsResult(this)

    private class ImmutableMethodsResult(
        builder: Builder
    ) : MethodsResult, Map<Type.Object, List<MethodMirror>> by builder.methods.detachImmutableCopy()
  }
}

internal inline fun buildMethodsResult(body: MethodsResult.Builder.() -> Unit) =
    MethodsResult.Builder().run {
      body()
      build()
    }

val Map.Entry<Type.Object, List<MethodMirror>>.type: Type.Object
  get() = key
val Map.Entry<Type.Object, List<MethodMirror>>.methods: List<MethodMirror>
  get() = value
