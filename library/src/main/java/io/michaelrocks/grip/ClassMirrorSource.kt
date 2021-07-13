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

import io.michaelrocks.grip.mirrors.ClassMirror
import java.io.File

interface ClassMirrorSource {
  fun getClassMirrors(): Sequence<ClassMirror>
}

class FunctionClassMirrorSource(
    private val classMirrorsProvider: () -> Sequence<ClassMirror>
) : ClassMirrorSource {
  override fun getClassMirrors(): Sequence<ClassMirror> {
    return classMirrorsProvider()
  }
}

internal class FilesClassMirrorSource(
    private val grip: Grip,
    private val files: Collection<File>
) : ClassMirrorSource {
  override fun getClassMirrors(): Sequence<ClassMirror> {
    return files.asSequence().flatMap { file ->
      grip.fileRegistry.findTypesForFile(file).asSequence().map { type ->
        grip.classRegistry.getClassMirror(type)
      }
    }
  }
}
