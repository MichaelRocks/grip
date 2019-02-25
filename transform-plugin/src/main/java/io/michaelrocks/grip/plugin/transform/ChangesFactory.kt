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

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import io.michaelrocks.grip.transform.Changes
import java.io.File

internal class ChangesFactory {
  fun create(content: QualifiedContent, incremental: Boolean): Changes {
    return when (content) {
      is DirectoryInput -> DirectoryChanges(content.changedFiles, incremental)
      is JarInput -> JarChanges(content.status, incremental)
      else -> error("Unknown content $content")
    }
  }

  private class DirectoryChanges(
    private val changes: Map<File, Status>,
    private val incremental: Boolean
  ) : Changes {

    override val status: Changes.Status
      get() = if (incremental) Changes.Status.UNKNOWN else Changes.Status.CHANGED

    override val hasFileStatuses: Boolean
      get() = incremental
    override val files: Collection<File>
      get() = changes.keys

    override fun getFileStatus(file: File): Changes.Status {
      return changes[file]?.toTransformUnitStatus() ?: Changes.Status.UNKNOWN
    }
  }

  private class JarChanges(
    private val jarStatus: Status,
    private val incremental: Boolean
  ) : Changes {

    override val status: Changes.Status
      get() = if (incremental) jarStatus.toTransformUnitStatus() else Changes.Status.CHANGED

    override val hasFileStatuses: Boolean
      get() = false
    override val files: Collection<File>
      get() = emptyList()

    override fun getFileStatus(file: File): Changes.Status {
      return Changes.Status.UNKNOWN
    }
  }

  companion object {
    private fun Status.toTransformUnitStatus(): Changes.Status {
      return when (this) {
        Status.NOTCHANGED -> Changes.Status.UNCHANGED
        Status.ADDED -> Changes.Status.ADDED
        Status.CHANGED -> Changes.Status.CHANGED
        Status.REMOVED -> Changes.Status.REMOVED
      }
    }
  }
}
