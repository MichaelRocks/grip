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

import io.michaelrocks.grip.io.IoFactory
import io.michaelrocks.grip.mirrors.ReflectorImpl
import java.io.File
import java.util.*

object GripFactory {
  fun create(file: File, vararg files: File): Grip {
    val allFiles = ArrayList<File>(files.size + 1)
    allFiles.add(file)
    allFiles.addAll(files)
    return create(allFiles)
  }

  fun create(files: Iterable<File>): Grip {
    val fileRegistry = FileRegistryImpl(files, IoFactory)
    val reflector = ReflectorImpl()
    val classRegistry = ClassRegistryImpl(fileRegistry, reflector)
    return GripImpl(classRegistry)
  }
}
