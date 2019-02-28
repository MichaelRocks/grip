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

package io.michaelrocks.grip.plugin.transform

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.MutableGrip
import io.michaelrocks.grip.impl.DefaultGripFactory
import io.michaelrocks.grip.impl.FileCanonicalizer
import io.michaelrocks.grip.impl.io.FileFormat
import io.michaelrocks.grip.impl.io.FileFormatDetector
import io.michaelrocks.grip.impl.io.FileSink
import io.michaelrocks.grip.impl.io.FileSource
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.transform.Changes
import io.michaelrocks.grip.transform.Scope
import io.michaelrocks.grip.transform.Transform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.EnumSet

class DefaultTransformRunnerTest {
  @Test
  fun testDefaultTransformRunner() {
    val inputs = mapOf(
      "input1" to listOf("input1/Class1", "input1/Class2"),
      "input2" to listOf("input2/Class1"),
      "input3" to listOf()
    )
    val references = mapOf(
      "referenced1" to listOf("referenced1/Class1", "referenced1/Class2"),
      "referenced2" to listOf("referenced2/Class1"),
      "referenced3" to listOf()
    )
    val classpath = mapOf(
      "classpath1" to listOf("classpath1/Class1", "classpath1/Class2"),
      "classpath2" to listOf("classpath2/Class1"),
      "classpath3" to listOf()
    )
    val produced = mapOf(
      "transform1" to listOf("input1/Class1", "input1/Class3"),
      "transform2" to listOf("transform2/Class1")
    )

    val allInputs = inputs + references + classpath
    val configuration = TestConfiguration(
      inputs = inputs,
      references = references,
      classpath = classpath,
      produced = produced,
      transforms = listOf(
        TransformConfiguration(
          name = "transform1",
          scopes = EnumSet.of(Scope.PROJECT),
          expectedClasspath = allInputs.toFiles(),
          expectedClasses = allInputs.toClasses(),
          producedClasses = produced["transform1"].orEmpty()
        ),
        TransformConfiguration(
          name = "transform2",
          scopes = EnumSet.of(Scope.GENERATED),
          expectedClasspath = allInputs.toFiles() + listOf("transform1"),
          expectedClasses = allInputs.toClasses() + produced.getClassesForFiles("transform1"),
          producedClasses = produced["transform2"].orEmpty()
        ),
        TransformConfiguration(
          name = "transform3",
          scopes = EnumSet.of(Scope.PROJECT, Scope.EXTERNAL_LIBRARIES),
          expectedClasspath = allInputs.toFiles() + listOf("transform1", "transform2"),
          expectedClasses = allInputs.toClasses() + produced.getClassesForFiles("transform1", "transform2"),
          producedClasses = produced["transform3"].orEmpty()
        ),
        TransformConfiguration(
          name = "transform4",
          scopes = EnumSet.allOf(Scope::class.java),
          expectedClasspath = allInputs.toFiles() + listOf("transform1", "transform2", "transform3"),
          expectedClasses = allInputs.toClasses() + produced.getClassesForFiles("transform1", "transform2", "transform3"),
          producedClasses = produced["transform4"].orEmpty()
        ),
        TransformConfiguration(
          name = "transform5",
          scopes = EnumSet.noneOf(Scope::class.java),
          expectedClasspath = allInputs.toFiles() + listOf("transform1", "transform2", "transform3", "transform4"),
          expectedClasses = allInputs.toClasses() + produced.getClassesForFiles("transform1", "transform2", "transform3", "transform4"),
          producedClasses = produced["transform5"].orEmpty()
        )
      )
    )

    testDefaultTransformRunner(configuration)
  }

  private fun testDefaultTransformRunner(configuration: TestConfiguration) {
    val transformSet = createTransformSet(configuration)
    val transforms = createTransforms(configuration)
    val outputProvider = createOutputProvider()
    val gripFactory = createGripFactory(configuration)
    val runner = DefaultTransformRunner(transforms, outputProvider, gripFactory)
    runner.run(transformSet)
  }

  private fun createTransformSet(configuration: TestConfiguration): TransformSet {
    val inputFiles = configuration.inputs.keys.map(::File)
    val referenceFiles = configuration.references.keys.map(::File)
    val classpathFiles = configuration.classpath.keys.map(::File)

    val inputUnits = inputFiles.map { createTransformUnit(it, Scope.PROJECT) }
    val referenceUnits = referenceFiles.map { createTransformUnit(it, Scope.EXTERNAL_LIBRARIES) }

    return TransformSet(inputUnits, referenceUnits, classpathFiles)
  }

  private fun createTransformUnit(file: File, scope: Scope): TransformUnit {
    return TransformUnit(file, file, TransformUnit.FileFormat.DIRECTORY, EnumSet.of(scope), mock(Changes::class.java))
  }

  private fun createTransforms(configuration: TestConfiguration): List<Transform> {
    return configuration.transforms.map(::createTransform)
  }

  private fun createTransform(configuration: TransformConfiguration): Transform {
    return object : Transform {
      override val name: String get() = configuration.name
      override val scopes: Set<Scope> get() = configuration.scopes

      override fun transform(invocation: Transform.Invocation) {
        assertEquals("Transform '$name': scopes must be equal", configuration.scopes.toHashSet(), invocation.inputs.flatMap { scopes }.toHashSet())

        val fileRegistry = invocation.grip.fileRegistry
        assertEquals(
          "Transform '$name': classpaths must be equal",
          configuration.expectedClasspath.toHashSet(),
          fileRegistry.classpath().mapTo(HashSet()) { it.path }
        )

        for (expectedClass in configuration.expectedClasses) {
          assertTrue("Transform '$name': $expectedClass not found", fileRegistry.contains(getObjectTypeByInternalName(expectedClass)))
        }

        val classProducer = invocation.grip.classProducer
        for (producedClass in configuration.producedClasses) {
          classProducer.produceClass(createClassByteArray(producedClass), overwrite = true)
        }
      }

      private fun createClassByteArray(internalName: String): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null)
        writer.visitEnd()
        return writer.toByteArray()
      }
    }
  }

  private fun createOutputProvider(): DefaultTransformRunner.OutputProvider {
    return object : DefaultTransformRunner.OutputProvider {
      override fun getOutputLocation(name: String): File {
        return File(name)
      }
    }
  }

  private fun createGripFactory(configuration: TestConfiguration): GripFactory {
    return object : GripFactory {
      override fun create(classpath: Iterable<File>, outputDirectory: File?): Grip {
        return createInternal(classpath, outputDirectory)
      }

      override fun createMutable(classpath: Iterable<File>, outputDirectory: File?): MutableGrip {
        return createInternal(classpath, outputDirectory)
      }

      private fun createInternal(classpath: Iterable<File>, outputDirectory: File?): MutableGrip {
        val files = configuration.allInputsAndOutputs

        val fileCanonicalizer = object : FileCanonicalizer {
          override fun canonicalizeFile(file: File): File {
            return file
          }
        }

        val fileFormatDetector = object : FileFormatDetector {
          override fun detectFileFormat(file: File): FileFormat {
            return FileFormat.DIRECTORY
          }
        }

        val fileSourceFactory = object : FileSource.Factory {
          override fun createFileSource(inputFile: File, fileFormat: FileFormat?): FileSource {
            return TestFileSource(files)
          }
        }

        val fileSinkFactory = object : FileSink.Factory {
          override fun createFileSink(outputFile: File, fileFormat: FileFormat): FileSink {
            assertEquals(FileFormat.DIRECTORY, fileFormat)
            return TestFileSink()
          }
        }

        return DefaultGripFactory.createInternal(
          classpath,
          outputDirectory,
          fileCanonicalizer,
          fileFormatDetector,
          fileSourceFactory,
          fileSinkFactory
        )
      }
    }
  }

  private fun Map<String, List<String>>.toFiles(): List<String> {
    return keys.toList()
  }

  private fun Map<String, List<String>>.toClasses(): List<String> {
    return values.flatten()
  }

  private fun Map<String, List<String>>.getClassesForFiles(vararg files: String): List<String> {
    return files.flatMap { this[it].orEmpty() }
  }

  private class TestFileSource(private val files: Map<String, List<String>>) : FileSource {
    override fun listFiles(callback: (name: String, type: FileSource.EntryType) -> Unit) {
      files.forEach { (_, types) ->
        types.forEach { type ->
          callback("$type.class", FileSource.EntryType.CLASS)
        }
      }
    }

    override fun readFile(path: String): ByteArray {
      error("Unexpected invocation of readFile($path)")
    }

    override fun close() {
      // Do nothing.
    }
  }

  private class TestFileSink : FileSink {
    override fun createFile(path: String, data: ByteArray) {
      // Do nothing.
    }

    override fun createDirectory(path: String) {
      error("Unexpected invocation of createDirectory($path)")
    }

    override fun flush() {
      // Do nothing.
    }

    override fun close() {
      // Do nothing.
    }
  }

  private class TestConfiguration(
    val inputs: Map<String, List<String>>,
    val references: Map<String, List<String>>,
    val classpath: Map<String, List<String>>,
    val produced: Map<String, List<String>>,
    val transforms: List<TransformConfiguration>
  ) {
    val allInputs = inputs + references + classpath
    val allInputsAndOutputs = allInputs + produced
  }

  private class TransformConfiguration(
    val name: String,
    val scopes: Set<Scope>,
    val expectedClasspath: Collection<String>,
    val expectedClasses: Collection<String>,
    val producedClasses: Collection<String>
  )
}
