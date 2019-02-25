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

package io.michaelrocks.grip.plugin.transform

import io.michaelrocks.grip.ClassMirrorSource
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.transform.Scope
import io.michaelrocks.grip.transform.ScopedInput

internal class TransformUnitScopedInput(
  private val grip: Grip,
  private val transformUnit: TransformUnit
) : ScopedInput, ClassMirrorSource {
  override val scopes: Set<Scope> get() = transformUnit.scopes

  override fun createClassMirrorSource(): ClassMirrorSource {
    return this
  }

  override fun getClassMirrors(): Sequence<ClassMirror> {
    return grip.fileRegistry.findTypesForFile(transformUnit.output).asSequence().map { type ->
      grip.classRegistry.getClassMirror(type)
    }
  }
}
