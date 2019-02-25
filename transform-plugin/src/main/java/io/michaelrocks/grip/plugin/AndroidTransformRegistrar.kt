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

import com.android.build.gradle.BaseExtension
import io.michaelrocks.grip.plugin.transform.DefaultFileCopier
import io.michaelrocks.grip.transform.Transform

internal class AndroidTransformRegistrar(
  private val android: BaseExtension
) : TransformRegistrar {
  private lateinit var gripTransform: GripTransform

  override fun registerTransform(transform: Transform) {
    if (!::gripTransform.isInitialized) {
      gripTransform = GripTransform(android, DefaultFileCopier())
      android.registerTransform(gripTransform)
    }

    gripTransform.addChildTransform(transform)
  }
}
