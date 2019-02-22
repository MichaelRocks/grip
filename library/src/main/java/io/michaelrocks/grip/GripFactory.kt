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

import io.michaelrocks.grip.io.DefaultFileFormatDetector
import io.michaelrocks.grip.io.DefaultFileSourceFactory
import io.michaelrocks.grip.io.FileSource
import io.michaelrocks.grip.mirrors.ReflectorImpl
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.ArrayList

interface GripFactory {
  fun create(file: File, vararg files: File): Grip
  fun create(files: Iterable<File>): Grip
  fun create(files: Iterable<File>, fileSourceFactory: FileSource.Factory): Grip

  companion object {
    const val ASM_API_DEFAULT = Opcodes.ASM9

    @JvmStatic
    val INSTANCE = newInstance(ASM_API_DEFAULT)

    @JvmStatic
    fun newInstance(asmApi: Int): GripFactory {
      return GripFactoryImpl(asmApi)
    }
  }
}

internal class GripFactoryImpl(
  private val asmApi: Int,
) : GripFactory {
  override fun create(file: File, vararg files: File): Grip {
    val allFiles = ArrayList<File>(files.size + 1)
    allFiles.add(file)
    allFiles.addAll(files)
    return create(allFiles)
  }

  override fun create(files: Iterable<File>): Grip {
    return create(files, DefaultFileSourceFactory(DefaultFileFormatDetector()))
  }

  override fun create(files: Iterable<File>, fileSourceFactory: FileSource.Factory): Grip {
    val fileRegistry = FileRegistryImpl(files, fileSourceFactory)
    val reflector = ReflectorImpl(asmApi)
    val classRegistry = ClassRegistryImpl(fileRegistry, reflector)
    return GripImpl(fileRegistry, classRegistry)
  }
}
