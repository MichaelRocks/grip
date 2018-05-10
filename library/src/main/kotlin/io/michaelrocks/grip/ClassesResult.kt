/*
 * Copyright 2018 Michael Rozumyanskiy
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
import io.michaelrocks.grip.mirrors.Type

interface ClassesResult : Map<Type.Object, ClassMirror> {
  val types: Set<Type.Object>
    get() = keys
  val classes: Collection<ClassMirror>
    get() = values

  fun containsType(type: Type.Object) =
    containsKey(type)
  fun containsClass(classMirror: ClassMirror) =
    containsValue(classMirror)

  class Builder {
    private val classes = LazyMap<Type.Object, ClassMirror>()

    fun addClass(mirror: ClassMirror) = apply {
      val oldMirror = classes.put(mirror.type, mirror)
      require(oldMirror == null) { "Duplicate ClassMirror for type ${mirror.type}" }
    }

    fun build(): ClassesResult = ImmutableClassesResult(this)

    private class ImmutableClassesResult(
        builder: Builder
    ) : ClassesResult, Map<Type.Object, ClassMirror> by builder.classes.detachImmutableCopy()
  }
}

internal inline fun buildClassesResult(body: ClassesResult.Builder.() -> Unit) =
    ClassesResult.Builder().run {
      body()
      build()
    }

val Map.Entry<Type.Object, ClassMirror>.type: Type.Object
  get() = key
val Map.Entry<Type.Object, ClassMirror>.classMirror: ClassMirror
  get() = value
