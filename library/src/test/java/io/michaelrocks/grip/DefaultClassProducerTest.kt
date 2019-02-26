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

import io.michaelrocks.grip.impl.CloseableFileRegistry
import io.michaelrocks.grip.impl.DefaultClassProducer
import io.michaelrocks.grip.impl.io.FileFormat
import io.michaelrocks.grip.impl.io.FileFormatDetector
import io.michaelrocks.grip.impl.io.FileSink
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.mockito.eq
import io.michaelrocks.mockito.mock
import io.michaelrocks.mockito.notNull
import io.michaelrocks.mockito.times
import io.michaelrocks.mockito.verify
import io.michaelrocks.mockito.verifyNoMoreInteractions
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File

class DefaultClassProducerTest {
  private val fileFormatDetector = object : FileFormatDetector {
    override fun detectFileFormat(file: File): FileFormat {
      return FileFormat.DIRECTORY
    }
  }

  private lateinit var fileSinkCache: Map<File, FileSink>
  private lateinit var fileSinkFactory: FileSink.Factory
  private lateinit var classProducer: ClassProducer

  @Before
  fun createClassProducer() {
    val cache = HashMap<File, FileSink>()
    fileSinkCache = cache
    fileSinkFactory = object : FileSink.Factory {
      override fun createFileSink(outputFile: File, fileFormat: FileFormat): FileSink {
        return cache.getOrPut(outputFile) { mock() }
      }
    }

    val fileRegistry = createFileRegistry(
      "file1" to listOf("io/michaelrocks/grip/file1/Class1", "io/michaelrocks/grip/file1/Class2"),
      "file2" to listOf("io/michaelrocks/grip/file2/Class3"),
      "file3" to listOf()
    )
    val outputSink = fileSinkFactory.createFileSink(File("file4"), FileFormat.DIRECTORY)
    classProducer = DefaultClassProducer(fileRegistry, fileSinkFactory, fileFormatDetector, outputSink)
  }

  @Test(expected = ClassAlreadyExistsException::class)
  fun produceExistingClassWithoutOverwriteShouldFail() {
    produceClass("io/michaelrocks/grip/file1/Class2", false)
  }

  @Test(expected = ClassAlreadyExistsException::class)
  fun produceClassTwoTimesWithoutOverwriteShouldFail() {
    produceClass("io/michaelrocks/grip/file1/Class3", false)
    produceClass("io/michaelrocks/grip/file1/Class3", false)
  }

  @Test
  fun produceExistingClassWithOverwriteShouldSucceed() {
    produceClass("io/michaelrocks/grip/file1/Class2", true)
    verify(getCachedSink("file1")).createFile(eq("io/michaelrocks/grip/file1/Class2.class"), notNull())
    verifyNoMoreInteractions(getCachedSink("file1"))
  }

  @Test
  fun produceClassTwoTimesWithOverwriteShouldSucceed() {
    produceClass("io/michaelrocks/grip/file1/Class3", false)
    produceClass("io/michaelrocks/grip/file1/Class3", true)
    verify(getCachedSink("file4"), times(2)).createFile(eq("io/michaelrocks/grip/file1/Class3.class"), notNull())
    verifyNoMoreInteractions(getCachedSink("file4"))
  }

  private fun createFileRegistry(vararg entries: Pair<String, List<String>>): CloseableFileRegistry {
    val fileToTypesMap = entries
      .map { Pair(File(it.first), it.second.map(::getObjectTypeByInternalName)) }
      .toMap()
    val typeToFileMap = HashMap<Type.Object, File>()
    fileToTypesMap.forEach { (file, types) ->
      for (type in types) {
        val oldFile = typeToFileMap.put(type, file)
        if (oldFile != null) {
          fail("Type $type added by $oldFile and $file")
        }
      }
    }

    return object : CloseableFileRegistry {
      override fun contains(file: File): Boolean {
        return file in fileToTypesMap
      }

      override fun contains(type: Type.Object): Boolean {
        return type in typeToFileMap
      }

      override fun classpath(): Collection<File> {
        return fileToTypesMap.keys
      }

      override fun readClass(type: Type.Object): ByteArray {
        error("FileRegistry.readClass() mustn't be called")
      }

      override fun findTypesForFile(file: File): Collection<Type.Object> {
        return requireNotNull(fileToTypesMap[file]) { "File $file isn't registered in FileRegistry" }
      }

      override fun findFileForType(type: Type.Object): File? {
        return typeToFileMap[type]
      }

      override fun close() {
        // Do nothing.
      }
    }
  }

  private fun produceClass(internalName: String, overwrite: Boolean) {
    val type = classProducer.produceClass(createClassByteArray(internalName), overwrite)
    assertEquals(internalName, type.internalName)
  }

  private fun createClassByteArray(internalName: String): ByteArray {
    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
    writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null)
    writer.visitEnd()
    return writer.toByteArray()
  }

  private fun getCachedSink(fileName: String): FileSink {
    val file = File(fileName)
    return fileSinkCache.getOrElse(file) {
      error("FileSink for file $fileName hasn't been created")
    }
  }
}