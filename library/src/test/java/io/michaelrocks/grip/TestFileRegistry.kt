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
import io.michaelrocks.grip.mirrors.getObjectType
import java.io.File
import kotlin.reflect.KClass

class TestFileRegistry(vararg classes: KClass<*>) : FileRegistry {
  private val classesByType = classes.associateBy { getObjectType(it) }

  override fun contains(file: File): Boolean = true
  override fun contains(type: Type.Object): Boolean = type in classesByType

  override fun classpath(): Collection<File> = listOf(DEFAULT_FILE)

  override fun readClass(type: Type.Object): ByteArray {
    val classLoader = classesByType[type]!!.java.classLoader
    return classLoader.getResourceAsStream(type.internalName + ".class").use {
      checkNotNull(it).readBytes()
    }
  }

  override fun findTypesForFile(file: File): Collection<Type.Object> = classesByType.keys
  override fun findFileForType(type: Type.Object) = if (contains(type)) DEFAULT_FILE else null

  companion object {
    private val DEFAULT_FILE = File("/")
  }
}
