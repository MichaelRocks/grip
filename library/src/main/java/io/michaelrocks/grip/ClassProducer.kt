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

package io.michaelrocks.grip

import io.michaelrocks.grip.commons.closeQuietly
import io.michaelrocks.grip.io.EmptyFileSink
import io.michaelrocks.grip.io.FileFormat
import io.michaelrocks.grip.io.FileFormatDetector
import io.michaelrocks.grip.io.FileSink
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import org.objectweb.asm.ClassReader
import java.io.File
import java.util.LinkedHashMap

interface ClassProducer {
  fun produceClass(classData: ByteArray, overwrite: Boolean = false): Type.Object
}

interface MutableClassProducer : ClassProducer {
  fun setOutputDirectory(directory: File)
  fun resetOutputDirectory()
}

internal interface CloseableClassProducer : ClassProducer, AutoCloseable
internal interface CloseableMutableClassProducer : CloseableClassProducer, MutableClassProducer

internal class DefaultClassProducer(
  private val fileRegistry: CloseableFileRegistry,
  private val fileSinkFactory: FileSink.Factory,
  private val fileFormatDetector: FileFormatDetector,
  private var outputSink: FileSink = EmptyFileSink
) : CloseableMutableClassProducer {

  private val sinks = LinkedHashMap<File, FileSink>()
  private val producedTypes = HashSet<Type.Object>()

  private var closed = false

  override fun produceClass(classData: ByteArray, overwrite: Boolean): Type.Object {
    checkNotClosed()
    val reader = ClassReader(classData)
    val className = reader.className
    val type = getObjectTypeByInternalName(className)

    val fileSink = getFileSink(type, overwrite)
    fileSink.createFile("$className.class", classData)
    return type
  }

  override fun setOutputDirectory(directory: File) {
    checkNotClosed()
    setOutputFile(directory, FileFormat.DIRECTORY)
  }

  override fun resetOutputDirectory() {
    checkNotClosed()
    setOutputSink(EmptyFileSink)
  }

  override fun close() {
    closed = true
    sinks.values.forEach { it.closeQuietly() }
    sinks.clear()
    fileRegistry.close()
  }

  private fun getFileSink(type: Type.Object, overwrite: Boolean): FileSink {
    val file = fileRegistry.findFileForType(type)
    if (file != null) {
      if (!overwrite) {
        throw ClassAlreadyExistsException(type)
      }

      return sinks.getOrPut(file) {
        fileSinkFactory.createFileSink(file, fileFormatDetector.detectFileFormat(file))
      }
    }

    if (!producedTypes.add(type)) {
      if (!overwrite) {
        throw ClassAlreadyExistsException(type)
      }
    }

    return outputSink
  }

  private fun setOutputFile(file: File, fileFormat: FileFormat) {
    val sink = fileSinkFactory.createFileSink(file, fileFormat)
    setOutputSink(sink)
  }

  private fun setOutputSink(sink: FileSink) {
    if (outputSink !== sink) {
      outputSink.close()
      outputSink = sink
    }
  }

  private fun checkNotClosed() {
    check(!closed) { "$this is closed" }
  }
}
