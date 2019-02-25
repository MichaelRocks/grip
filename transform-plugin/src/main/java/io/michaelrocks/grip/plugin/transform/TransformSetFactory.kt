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

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import io.michaelrocks.grip.transform.Scope
import java.io.File
import java.util.EnumSet

internal class TransformSetFactory(
  private val changesFactory: ChangesFactory
) {
  fun create(invocation: TransformInvocation, bootClasspath: List<File>): TransformSet {
    val units = createTransformUnits(invocation, invocation.inputs)
    val referencedUnits = createTransformUnits(invocation, invocation.referencedInputs)
    return TransformSet(units, referencedUnits, bootClasspath)
  }

  private fun createTransformUnits(invocation: TransformInvocation, inputs: Collection<TransformInput>): List<TransformUnit> {
    return inputs.flatMap { input ->
      val units = ArrayList<TransformUnit>(input.directoryInputs.size + input.jarInputs.size)
      input.directoryInputs.mapTo(units) { directory ->
        createTransformUnit(invocation, directory, Format.DIRECTORY)
      }
      input.jarInputs.mapTo(units) { jar ->
        createTransformUnit(invocation, jar, Format.JAR)
      }
    }
  }

  private fun createTransformUnit(invocation: TransformInvocation, input: QualifiedContent, format: Format): TransformUnit {
    val output = invocation.outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, format)
    val statusProvider = changesFactory.create(input, invocation.isIncremental)
    return TransformUnit(input.file, output, format.toTransformUnitFormat(), getScopes(input), statusProvider)
  }

  private fun Format.toTransformUnitFormat(): TransformUnit.FileFormat {
    return when (this) {
      Format.JAR -> TransformUnit.FileFormat.JAR
      Format.DIRECTORY -> TransformUnit.FileFormat.DIRECTORY
    }
  }

  private fun getScopes(content: QualifiedContent): Set<Scope> {
    return EnumSet.noneOf(Scope::class.java).also { scopes ->
      content.scopes.mapTo(scopes, ::getScope)
    }
  }

  private fun getScope(scope: Any?): Scope {
    return if (scope is QualifiedContent.Scope) {
      @Suppress("DEPRECATION")
      when (scope) {
        QualifiedContent.Scope.PROJECT -> Scope.PROJECT
        QualifiedContent.Scope.SUB_PROJECTS -> Scope.SUB_PROJECTS
        QualifiedContent.Scope.EXTERNAL_LIBRARIES -> Scope.EXTERNAL_LIBRARIES
        QualifiedContent.Scope.TESTED_CODE -> Scope.TESTED_CODE
        QualifiedContent.Scope.PROVIDED_ONLY -> Scope.PROVIDED_ONLY

        QualifiedContent.Scope.PROJECT_LOCAL_DEPS -> Scope.EXTERNAL_LIBRARIES
        QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS -> Scope.EXTERNAL_LIBRARIES

        else -> Scope.UNKNOWN
      }
    } else {
      Scope.UNKNOWN
    }
  }
}
