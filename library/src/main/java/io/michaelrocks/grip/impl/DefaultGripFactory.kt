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

package io.michaelrocks.grip.impl

import io.michaelrocks.grip.DefaultMutableGrip
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.MutableGrip
import io.michaelrocks.grip.impl.io.DefaultFileFormatDetector
import io.michaelrocks.grip.impl.io.DefaultFileSinkFactory
import io.michaelrocks.grip.impl.io.DefaultFileSourceFactory
import io.michaelrocks.grip.impl.io.FileFormatDetector
import io.michaelrocks.grip.impl.io.FileSink
import io.michaelrocks.grip.impl.io.FileSource
import io.michaelrocks.grip.mirrors.Type
import java.io.File

class DefaultGripFactory(
  private val asmApi: Int,
) : GripFactory {
  override fun create(classpath: Iterable<File>, outputDirectory: File?): Grip {
    return createInternal(classpath, outputDirectory)
  }

  override fun createMutable(classpath: Iterable<File>, outputDirectory: File?): MutableGrip {
    return createInternal(classpath, outputDirectory)
  }

  fun createInternal(
    classpath: Iterable<File>,
    outputDirectory: File? = null,
    fileFormatDetector: FileFormatDetector = DefaultFileFormatDetector(),
    fileSourceFactory: FileSource.Factory = DefaultFileSourceFactory(fileFormatDetector),
    fileSinkFactory: FileSink.Factory = DefaultFileSinkFactory()
  ): MutableGrip {
    val fileRegistry = DefaultFileRegistry(classpath, fileSourceFactory)
    val reflector = DefaultReflector(asmApi)
    val classRegistry = DefaultClassRegistry(fileRegistry, reflector)
    val classProducer = DefaultClassProducer(fileRegistry, fileSinkFactory, fileFormatDetector)
    if (outputDirectory != null) {
      classProducer.setOutputDirectory(outputDirectory)
    }

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
