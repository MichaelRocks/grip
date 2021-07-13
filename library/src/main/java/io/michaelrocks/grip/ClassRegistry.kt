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

import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.EnumMirror
import io.michaelrocks.grip.mirrors.Reflector
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.UnresolvedAnnotationMirror
import io.michaelrocks.grip.mirrors.buildAnnotation
import io.michaelrocks.grip.mirrors.getObjectType
import org.objectweb.asm.Opcodes
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.HashMap

interface ClassRegistry {
  fun getClassMirror(type: Type.Object): ClassMirror
  fun getAnnotationMirror(type: Type.Object): AnnotationMirror
}

internal class ClassRegistryImpl(
    private val fileRegistry: FileRegistry,
    private val reflector: Reflector
) : ClassRegistry {
  private val classesByType = HashMap<Type, ClassMirror>()
  private val annotationsByType = HashMap<Type, AnnotationMirror>()

  override fun getClassMirror(type: Type.Object): ClassMirror =
      classesByType.getOrPut(type) { readClassMirror(type, false) }

  override fun getAnnotationMirror(type: Type.Object): AnnotationMirror =
      annotationsByType.getOrPut(type) {
        if (type !in fileRegistry) {
          return UnresolvedAnnotationMirror(type)
        } else {
          val classMirror = readClassMirror(type, true)
          val visible = isAnnotationVisible(classMirror)
          buildAnnotation(type, visible) {
            check(classMirror.access or Opcodes.ACC_ANNOTATION != 0)
            for (method in classMirror.methods) {
              method.defaultValue?.let { addValue(method.name, it) }
            }
          }
        }
      }

  private fun readClassMirror(type: Type.Object, forAnnotation: Boolean): ClassMirror {
    return try {
      reflector.reflect(fileRegistry.readClass(type), this, forAnnotation)
    } catch (exception: Exception) {
      throw IllegalArgumentException("Unable to read a ClassMirror for ${type.internalName}", exception)
    }
  }

  companion object {
    private val RETENTION_TYPE = getObjectType<Retention>()
    private val RETENTION_POLICY_TYPE = getObjectType<RetentionPolicy>()

    private fun isAnnotationVisible(classMirror: ClassMirror): Boolean {
      val retention = classMirror.annotations[RETENTION_TYPE] ?: return false
      val retentionPolicy = retention.values["value"] as? EnumMirror ?: return false
      check(retentionPolicy.type == RETENTION_POLICY_TYPE) {
        "Class ${classMirror.type} contains @Retention annotation with unexpected value of type ${retentionPolicy.type}"
      }
      return retentionPolicy.value == RetentionPolicy.RUNTIME.name
    }
  }
}
