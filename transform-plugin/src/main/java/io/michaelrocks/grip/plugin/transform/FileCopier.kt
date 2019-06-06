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

import io.michaelrocks.grip.plugin.logging.getLogger
import io.michaelrocks.grip.transform.Changes
import java.io.File

internal interface FileCopier {
  fun copyInputsToOutputs(transformSet: TransformSet)
}

internal class DefaultFileCopier : FileCopier {
  private val logger = getLogger()

  override fun copyInputsToOutputs(transformSet: TransformSet) {
    transformSet.units.forEach { unit ->
      when (unit.format) {
        TransformUnit.FileFormat.DIRECTORY -> copyDirectory(unit.input, unit.output, unit.changes)
        TransformUnit.FileFormat.JAR -> copyJar(unit.input, unit.output, unit.changes)
      }
    }
  }

  private fun copyDirectory(source: File, target: File, changes: Changes) {
    if (!changes.hasFileStatuses) {
      logger.info("Non-incremental directory change: {} -> {}", source, target)
      target.deleteRecursively()
      if (source.exists()) {
        source.copyRecursively(target)
      }
      return
    }

    logger.info("Incremental directory change: {} -> {}", source, target)
    target.mkdirs()
    changes.files.forEach { file ->
      val status = changes.getFileStatus(file)
      val relativePath = file.toRelativeString(source)
      val targetFile = File(target, relativePath)
      applyChanges(file, targetFile, status)
    }
  }

  private fun copyJar(source: File, target: File, changes: Changes) {
    logger.info("Jar change: {} -> {}", source, target)
    applyChanges(source, target, changes.status)
  }

  private fun applyChanges(source: File, target: File, status: Changes.Status) {
    logger.debug("Incremental file change ({}): {} -> {}", status, source, target)
    when (status) {
      Changes.Status.UNCHANGED -> return
      Changes.Status.REMOVED -> target.deleteRecursively()
      Changes.Status.ADDED -> source.replaceRecursively(target)
      Changes.Status.CHANGED -> source.replaceRecursively(target)
      Changes.Status.UNKNOWN -> applyChanges(source, target, computeFileStatus(source))
    }
  }

  private fun File.replaceRecursively(target: File) {
    target.deleteRecursively()
    copyRecursively(target, false)
  }

  private fun computeFileStatus(source: File): Changes.Status {
    return if (source.exists()) Changes.Status.CHANGED else Changes.Status.REMOVED
  }
}