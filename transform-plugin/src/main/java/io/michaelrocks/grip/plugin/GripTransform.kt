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

package io.michaelrocks.grip.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.BaseExtension
import io.michaelrocks.grip.plugin.transform.ChangesFactory
import io.michaelrocks.grip.plugin.transform.DefaultTransformRunner
import io.michaelrocks.grip.plugin.transform.FileCopier
import io.michaelrocks.grip.plugin.transform.TransformSetFactory
import io.michaelrocks.grip.transform.Scope
import io.michaelrocks.grip.transform.Transform
import java.io.File
import java.util.EnumSet
import com.android.build.api.transform.Transform as AndroidTransform

internal class GripTransform(
  private val android: BaseExtension,
  private val fileCopier: FileCopier
) : AndroidTransform() {

  private val transforms = ArrayList<Transform>()

  private val configuration by lazy {
    ImmutableConfiguration(transforms)
  }

  fun addChildTransform(transform: Transform) {
    // TODO: Lock mutation when configuration is computed.
    transforms += transform
  }

  override fun transform(invocation: TransformInvocation) {
    if (!invocation.isIncremental) {
      invocation.outputProvider.deleteAll()
    }

    val transformSetFactory = TransformSetFactory(ChangesFactory())
    val transformSet = transformSetFactory.create(invocation, android.bootClasspath)
    fileCopier.copyInputsToOutputs(transformSet)

    val runner = DefaultTransformRunner(configuration.transforms, createOutputProvider(invocation))
    runner.run(transformSet)
  }

  override fun getName(): String {
    return configuration.name
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return EnumSet.copyOf(getQualifiedContentScopes(configuration.scopes))
  }

  override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
    return EnumSet.copyOf(getQualifiedContentScopes(configuration.referencedScopes))
  }

  override fun isIncremental(): Boolean {
    // TODO: Support incremental transforms.
    return false
  }

  private class ImmutableConfiguration(transforms: Collection<Transform>) {
    val transforms: List<Transform> = transforms.map(::ImmutableTransform)
    val name: String = transforms.joinToString(separator = "And", postfix = "Grip") { it.name.capitalize() }
    val scopes: Set<Scope> = transforms.flatMapTo(EnumSet.noneOf(Scope::class.java)) { it.scopes }
    val referencedScopes: Set<Scope> = EnumSet.allOf(Scope::class.java).also { it.removeAll(scopes) }
  }

  private class ImmutableTransform(private val transform: Transform) : Transform {
    override val name: String = transform.name
    override val scopes: Set<Scope> = transform.scopes

    override fun transform(invocation: Transform.Invocation) {
      transform.transform(invocation)
    }
  }

  companion object {
    private fun createOutputProvider(invocation: TransformInvocation): DefaultTransformRunner.OutputProvider {
      return object : DefaultTransformRunner.OutputProvider {
        override fun getOutputLocation(name: String): File {
          return invocation.outputProvider.getGeneratedOutputLocation(name)
        }
      }
    }

    private fun TransformOutputProvider.getGeneratedOutputLocation(name: String): File {
      val contentTypes = setOf(QualifiedContent.DefaultContentType.CLASSES)
      val scopes = EnumSet.of(QualifiedContent.Scope.PROJECT)
      return getContentLocation(name, contentTypes, scopes, Format.DIRECTORY)
    }

    private fun getQualifiedContentScopes(scopes: Iterable<Scope>): Set<QualifiedContent.Scope> {
      return scopes.mapNotNullTo(EnumSet.noneOf(QualifiedContent.Scope::class.java)) { scope ->
        when (scope) {
          Scope.PROJECT -> QualifiedContent.Scope.PROJECT
          Scope.SUB_PROJECTS -> QualifiedContent.Scope.SUB_PROJECTS
          Scope.EXTERNAL_LIBRARIES -> QualifiedContent.Scope.EXTERNAL_LIBRARIES
          Scope.TESTED_CODE -> QualifiedContent.Scope.TESTED_CODE
          Scope.PROVIDED_ONLY -> QualifiedContent.Scope.PROVIDED_ONLY
          Scope.GENERATED -> null
          Scope.UNKNOWN -> null
        }
      }
    }
  }
}
