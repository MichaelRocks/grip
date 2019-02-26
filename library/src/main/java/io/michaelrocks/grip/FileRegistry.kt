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

import io.michaelrocks.grip.mirrors.Type
import java.io.File

interface FileRegistry {
  operator fun contains(file: File): Boolean
  operator fun contains(type: Type.Object): Boolean

  fun classpath(): Collection<File>

  fun readClass(type: Type.Object): ByteArray
  fun findTypesForFile(file: File): Collection<Type.Object>
  fun findFileForType(type: Type.Object): File?
}

interface MutableFileRegistry : FileRegistry {
  fun addFileToClasspath(file: File)
  fun removeFileFromClasspath(file: File)
}
