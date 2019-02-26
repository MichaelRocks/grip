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

import io.michaelrocks.grip.ClassMirrorSource
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.plugin.logging.getLogger
import io.michaelrocks.grip.transform.Scope
import io.michaelrocks.grip.transform.ScopedInput
import io.michaelrocks.grip.transform.Transform
import java.io.File

internal interface TransformRunner {
  fun run(transformSet: TransformSet)
}

internal class DefaultTransformRunner(
  private val transforms: List<Transform>,
  private val outputProvider: OutputProvider,
  private val gripFactory: GripFactory = GripFactory.INSTANCE
) : TransformRunner {
  private val logger = getLogger()

  override fun run(transformSet: TransformSet) {
    gripFactory.createMutable(transformSet.getClasspath()).use { grip ->
      val generatedInputs = ArrayList<ScopedInput>(transforms.size)

      for (transform in transforms) {
        logger.info("Invoking \"{}\" transform", transform.name)

        val scopes = transform.scopes
        val output = outputProvider.getOutputLocation(transform.name)
        val inputs = transformSet.units.mapNotNullTo(ArrayList<ScopedInput>()) { unit ->
          if (unit.scopes.any { it in scopes }) TransformUnitScopedInput(grip, unit) else null
        }
        inputs += generatedInputs

        grip.classProducer.setOutputDirectory(output)
        val transformInvocation = ImmutableInvocation(grip, inputs)
        transform.transform(transformInvocation)
        grip.classProducer.resetOutputDirectory()

        grip.fileRegistry.addFileToClasspath(output)
        generatedInputs += SimpleScopedInput(
          Scope.GENERATED,
          FileClassMirrorSource(grip, output)
        )
      }
    }
  }

  private data class ImmutableInvocation(
    override val grip: Grip,
    override val inputs: Collection<ScopedInput>
  ) : Transform.Invocation

  private class SimpleScopedInput(
    override val scopes: Set<Scope>,
    private val classMirrorSource: ClassMirrorSource
  ) : ScopedInput {
    constructor(scope: Scope, classMirrorSource: ClassMirrorSource) : this(setOf(scope), classMirrorSource)

    override fun createClassMirrorSource(): ClassMirrorSource {
      return classMirrorSource
    }
  }

  private class FileClassMirrorSource(
    private val grip: Grip,
    private val file: File
  ) : ClassMirrorSource {
    override fun getClassMirrors(): Sequence<ClassMirror> {
      return grip.fileRegistry.findTypesForFile(file).asSequence().map { type ->
        grip.classRegistry.getClassMirror(type)
      }
    }
  }

  interface OutputProvider {
    fun getOutputLocation(name: String): File
  }

  companion object {
    private fun TransformSet.getClasspath(): List<File> {
      val classpath = ArrayList<File>(units.size + referencedUnits.size + bootClasspath.size)
      units.mapTo(classpath) { it.input }
      referencedUnits.mapTo(classpath) { it.input }
      classpath += bootClasspath
      return classpath
    }
  }
}
