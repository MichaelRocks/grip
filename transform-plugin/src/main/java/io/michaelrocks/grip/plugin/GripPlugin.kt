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

import com.android.build.gradle.BasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

open class GripPlugin : Plugin<Project> {
  private lateinit var project: Project
  private lateinit var logger: Logger

  private val extension = DefaultGripExtension()

  override fun apply(project: Project) {
    this.project = project
    this.logger = project.logger

    project.extensions.add(GripExtension::class.java, "grip", extension)

    if (!tryBindToPluginEagerly()) {
      bindToPluginDeferred()
    }
  }

  private fun tryBindToPluginEagerly(): Boolean {
    val androidPlugins = project.plugins.filterIsInstance<BasePlugin<*>>()
    if (androidPlugins.isEmpty()) {
      return false
    }

    require(androidPlugins.size == 1) {
      "Multiple Android plugins found: $androidPlugins"
    }

    bindToAndroidPlugin(androidPlugins.single())
    return true
  }

  private fun bindToPluginDeferred() {
    project.plugins.whenPluginAdded { plugin ->
      if (plugin is BasePlugin<*>) {
        bindToAndroidPlugin(plugin)
      }
    }

    project.afterEvaluate {
      if (!extension.isBoundToTransformRegistrar) {
        bindToJavaPlugin()
      }
    }
  }

  private fun bindToAndroidPlugin(plugin: BasePlugin<*>) {
    extension.bindToTransformRegistrar(AndroidTransformRegistrar(plugin.extension))
  }

  private fun bindToJavaPlugin() {
    TODO("Java plugin isn't supported yet")
  }
}
