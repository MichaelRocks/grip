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

package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror

val classes: Projection<ClassMirror, ClassesResult>
  get() = Projection.Classes
val fields: Projection<FieldMirror, FieldsResult>
  get() = Projection.Fields
val methods: Projection<MethodMirror, MethodsResult>
  get() = Projection.Methods

sealed class Projection<M, R> {
  internal abstract fun configurator(classRegistry: ClassRegistry): FromConfigurator<M, R>

  internal object Classes : Projection<ClassMirror, ClassesResult>() {
    override fun configurator(classRegistry: ClassRegistry) =
        ClassesQueryBuilder(classRegistry)
  }

  internal object Fields : Projection<FieldMirror, FieldsResult>() {
    override fun configurator(classRegistry: ClassRegistry) =
        FieldsQueryBuilder(classRegistry)
  }

  internal object Methods : Projection<MethodMirror, MethodsResult>() {
    override fun configurator(classRegistry: ClassRegistry) =
        MethodsQueryBuilder(classRegistry)
  }
}
