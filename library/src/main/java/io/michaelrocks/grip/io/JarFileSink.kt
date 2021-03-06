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

package io.michaelrocks.grip.io

import io.michaelrocks.grip.commons.closeQuietly
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class JarFileSink(private val jarFile: File) : FileSink {
  private val stream = createJarOutputStream(jarFile)

  override fun createFile(path: String, data: ByteArray) {
    val entry = JarEntry(path)
    stream.putNextEntry(entry)
    stream.write(data)
    stream.closeEntry()
  }

  override fun createDirectory(path: String) {
    val directoryPath = if (path.endsWith("/")) path else "$path/"
    val entry = JarEntry(directoryPath)
    stream.putNextEntry(entry)
    stream.closeEntry()
  }

  override fun flush() {
    stream.flush()
  }

  override fun close() {
    stream.closeQuietly()
  }

  override fun toString(): String {
    return "JarFileSink($jarFile)"
  }

  private fun createJarOutputStream(jarFile: File): JarOutputStream {
    jarFile.parentFile?.mkdirs()
    return JarOutputStream(jarFile.outputStream().buffered())
  }
}
