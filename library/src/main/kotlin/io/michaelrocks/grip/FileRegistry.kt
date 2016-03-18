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

import io.michaelrocks.grip.commons.closeQuietly
import io.michaelrocks.grip.io.FileSource
import org.objectweb.asm.Type
import java.io.Closeable
import java.io.File
import java.util.*

internal interface FileRegistry : Closeable {
  fun add(files: Iterable<File>)
  fun add(file: File)
  fun isAdded(file: File): Boolean

  fun readClass(type: Type): ByteArray
  fun findTypesForFile(file: File): Collection<Type>
}

internal class FileRegistryImpl(private val fileSourceFactory: FileSource.Factory) : FileRegistry {
  private val sources = HashMap<File, FileSource>()
  private val filesByTypes = HashMap<Type, File>()
  private val typesByFiles = HashMap<File, MutableCollection<Type>>()

  override fun add(files: Iterable<File>) {
    require(files.all { it !in sources }) { "Some files already added to registry" }
    files.forEach { add(it) }
  }

  override fun add(file: File) {
    file.canonicalFile.let { file ->
      require(file !in sources) { "File $file already added to registry" }
      val fileSource = fileSourceFactory.createFileSource(file)
      sources.put(file, fileSource)
      fileSource.listFiles { path, fileType ->
        if (fileType == FileSource.EntryType.CLASS) {
          val type = Type.getObjectType(path.substringBeforeLast(".class"))
          filesByTypes.put(type, file)
          typesByFiles.getOrPut(file) { ArrayList() } += type
        }
      }
    }
  }

  override fun isAdded(file: File): Boolean = file.canonicalFile in sources

  override fun readClass(type: Type): ByteArray {
    val file = filesByTypes.getOrElse(type) { throw IllegalArgumentException("Unable to find a file for ${type.internalName}") }
    val fileSource = sources.getOrElse(file) { throw IllegalArgumentException("Unable to find a source for ${type.internalName}") }
    return fileSource.readFile("${type.internalName}.class")
  }

  override fun findTypesForFile(file: File): Collection<Type> =
      typesByFiles.getOrElse(file.canonicalFile) { error("File $file is not added to the registry") }.let {
        Collections.unmodifiableCollection(it)
      }

  override fun close() {
    sources.values.forEach { it.closeQuietly() }
    sources.clear()
    filesByTypes.clear()
  }
}
