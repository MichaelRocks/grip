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

package io.michaelrocks.grip.impl.io

import java.io.File

class DirectoryFileSource(private val directory: File) : FileSource {
  override fun listFiles(callback: (String, FileSource.EntryType) -> Unit) {
    fun File.toEntryType() = when {
      isDirectory -> FileSource.EntryType.DIRECTORY
      name.endsWith(".class", ignoreCase = true) -> FileSource.EntryType.CLASS
      else -> FileSource.EntryType.FILE
    }

    for (file in directory.walkTopDown()) {
      callback(file.relativeTo(directory).path, file.toEntryType())
    }
  }

  override fun readFile(path: String): ByteArray {
    return File(directory, path).readBytes()
  }

  override fun close() {
  }

  override fun toString(): String {
    return "DirectoryFileSource($directory)"
  }
}
