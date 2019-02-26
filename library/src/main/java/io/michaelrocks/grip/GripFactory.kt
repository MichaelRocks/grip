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

import io.michaelrocks.grip.impl.DefaultGripFactory
import org.objectweb.asm.Opcodes
import java.io.File

interface GripFactory {
  fun create(classpath: Iterable<File>, outputDirectory: File? = null): Grip
  fun createMutable(classpath: Iterable<File>, outputDirectory: File? = null): MutableGrip

  companion object {
    const val ASM_API_DEFAULT = Opcodes.ASM9

    @JvmStatic
    val INSTANCE: GripFactory = newInstance(ASM_API_DEFAULT)

    @JvmStatic
    fun newInstance(asmApi: Int): GripFactory {
      return DefaultGripFactory(asmApi)
    }
  }
}
