/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.grip.plugin

import io.michaelrocks.grip.transform.Transform

abstract class GripExtension {
  abstract fun registerTransform(transform: Transform)
}

open class DefaultGripExtension : GripExtension() {
  val isBoundToTransformRegistrar: Boolean get() = registrar != null

  private var registrar: TransformRegistrar? = null
  private val pendingTransforms = ArrayList<Transform>()

  override fun registerTransform(transform: Transform) {
    val registrar = registrar
    if (registrar != null) {
      registrar.registerTransform(transform)
    } else {
      pendingTransforms += transform
    }
  }

  internal fun bindToTransformRegistrar(registrar: TransformRegistrar) {
    require(this.registrar == null) { "TransformRegistrar is already set to ${this.registrar}" }
    this.registrar = registrar
    pendingTransforms.forEach(registrar::registerTransform)
    pendingTransforms.clear()
  }
}
