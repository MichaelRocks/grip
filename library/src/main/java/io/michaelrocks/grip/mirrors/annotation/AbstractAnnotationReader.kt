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

package io.michaelrocks.grip.mirrors.annotation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.EnumMirror
import io.michaelrocks.grip.mirrors.getObjectType
import io.michaelrocks.grip.mirrors.toType
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type as AsmType

internal abstract class AbstractAnnotationReader<out T> protected constructor(
  asmApi: Int,
  private val classRegistry: ClassRegistry,
  private val callback: (T) -> Unit
) : AnnotationVisitor(asmApi) {

  override fun visit(name: String?, value: Any) {
    addNormalizedValue(this, name, value)
  }

  override fun visitEnum(name: String?, desc: String, value: String) {
    addNormalizedValue(this, name, EnumMirror(desc, value))
  }

  override fun visitAnnotation(name: String?, desc: String): AnnotationVisitor {
    val parent = this
    return AnnotationInstanceReader(api, getObjectType(desc), true, classRegistry) {
      addNormalizedValue(parent, name, it)
    }
  }

  override fun visitArray(name: String?): AnnotationVisitor {
    val parent = this
    return AnnotationArrayReader(api, classRegistry) {
      addNormalizedValue(parent, name, it)
    }
  }

  override fun visitEnd() {
    callback(buildResult())
  }

  private fun addNormalizedValue(reader: AbstractAnnotationReader<*>, name: String?, value: Any) {
    if (value is AsmType) {
      reader.addValue(name, value.toType())
    } else {
      reader.addValue(name, value)
    }
  }

  protected abstract fun addValue(name: String?, value: Any)
  protected abstract fun buildResult(): T
}
