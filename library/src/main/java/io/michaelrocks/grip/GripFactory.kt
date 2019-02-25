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
import io.michaelrocks.grip.io.DefaultFileSinkFactory
import io.michaelrocks.grip.io.DefaultFileSourceFactory
import io.michaelrocks.grip.io.EmptyFileSink
import io.michaelrocks.grip.io.FileFormat
import io.michaelrocks.grip.io.FileFormatDetector
import io.michaelrocks.grip.io.FileSink
import io.michaelrocks.grip.io.FileSource
import io.michaelrocks.grip.mirrors.DefaultReflector
import io.michaelrocks.grip.mirrors.Type
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.ArrayList

interface GripFactory<out T : Grip> {
  fun create(file: File, vararg files: File): T = create(file, *files, outputDirectory = null)
  fun create(file: File, vararg files: File, outputDirectory: File?): T

  fun create(files: Iterable<File>): T = create(files, outputDirectory = null)
  fun create(files: Iterable<File>, outputDirectory: File? = null): T

  companion object {
    const val ASM_API_DEFAULT = Opcodes.ASM9

    @JvmStatic
    val INSTANCE: GripFactory<Grip> = immutable(ASM_API_DEFAULT)

    @JvmStatic
    @Deprecated("Use immutable() or mutable() factory methods instead", ReplaceWith("immutable(asmApi)"))
    fun newInstance(asmApi: Int): GripFactory<Grip> {
      return immutable(asmApi)
    }

    fun immutable(asmApi: Int): GripFactory<Grip> {
      return DefaultGripFactory(asmApi)
    }

    fun mutable(asmApi: Int): GripFactory<MutableGrip> {
      return DefaultGripFactory(asmApi)
    }
  }
}

internal class DefaultGripFactory(
  private val asmApi: Int,
) : GripFactory<MutableGrip> {
  override fun create(file: File, vararg files: File, outputDirectory: File?): MutableGrip {
    val allFiles = ArrayList<File>(files.size + 1)
    allFiles.add(file)
    allFiles.addAll(files)
    return createInternal(allFiles, outputDirectory = outputDirectory)
  }

  override fun create(files: Iterable<File>, outputDirectory: File?): MutableGrip {
    return createInternal(files, outputDirectory)
  }

  private fun createInternal(
    files: Iterable<File>,
    outputDirectory: File? = null,
    fileFormatDetector: FileFormatDetector = DefaultFileFormatDetector(),
    fileSourceFactory: FileSource.Factory = DefaultFileSourceFactory(fileFormatDetector),
    fileSinkFactory: FileSink.Factory = DefaultFileSinkFactory(),
  ): MutableGrip {
    val fileRegistry = DefaultFileRegistry(files, fileSourceFactory)
    val reflector = DefaultReflector(asmApi)
    val classRegistry = DefaultClassRegistry(fileRegistry, reflector)
    val outputSink = if (outputDirectory != null) fileSinkFactory.createFileSink(outputDirectory, FileFormat.DIRECTORY) else EmptyFileSink
    val classProducer = DefaultClassProducer(fileRegistry, fileSinkFactory, fileFormatDetector, outputSink)
    val wrappedClassProducer = object : ClassProducerWrapper(classProducer) {
      override fun produceClass(classData: ByteArray, overwrite: Boolean): Type.Object {
        val type = super.produceClass(classData, overwrite)
        if (type in fileRegistry) {
          classRegistry.invalidateType(type)
        }
        return type
      }
    }
    return DefaultMutableGrip(fileRegistry, classRegistry, wrappedClassProducer)
  }

  private abstract class ClassProducerWrapper(delegate: CloseableMutableClassProducer) : CloseableMutableClassProducer by delegate
}
