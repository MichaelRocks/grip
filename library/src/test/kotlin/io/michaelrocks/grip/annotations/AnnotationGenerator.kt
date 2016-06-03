/*
 * Copyright 2016 Michael Rozumyanskiy
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

package io.michaelrocks.grip.annotations

import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.EnumMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getMethodType
import io.michaelrocks.grip.mirrors.getType
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_ABSTRACT
import org.objectweb.asm.Opcodes.ACC_ANNOTATION
import org.objectweb.asm.Opcodes.ACC_INTERFACE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.V1_6

class AnnotationGenerator private constructor(private val classVisitor: ClassVisitor) {
  companion object {
    fun create(classVisitor: ClassVisitor, annotationType: Type.Object): AnnotationGenerator {
      val generator = AnnotationGenerator(classVisitor)
      classVisitor.visit(
          V1_6,
          ACC_PUBLIC or ACC_ANNOTATION or ACC_ABSTRACT or ACC_INTERFACE,
          annotationType.internalName,
          null,
          getType<Any>().internalName,
          arrayOf(getType<Annotation>().internalName))
      return generator
    }
  }

  fun addMethod(name: String, type: Type): AnnotationGenerator {
    addMethod(name, type, null)
    return this
  }

  fun addMethod(name: String, type: Type, defaultValue: Any?): AnnotationGenerator {
    val methodVisitor = classVisitor.visitMethod(
        ACC_PUBLIC or ACC_ABSTRACT,
        name,
        getMethodType(type).descriptor,
        null,
        null)
    if (defaultValue != null) {
      val annotationVisitor = methodVisitor.visitAnnotationDefault()
      addValue(annotationVisitor, null, defaultValue)
      annotationVisitor.visitEnd()
    }
    methodVisitor.visitEnd()

    return this
  }

  private fun addValue(annotationVisitor: AnnotationVisitor, name: String?, defaultValue: Any) {
    if (defaultValue is EnumMirror) {
      annotationVisitor.visitEnum(name, defaultValue.type.descriptor, defaultValue.value)
    } else if (defaultValue is AnnotationMirror) {
      val innerAnnotationVisitor = annotationVisitor.visitAnnotation(name, defaultValue.type.descriptor)
      for (entry in defaultValue.values.entries) {
        addValue(innerAnnotationVisitor, entry.key, entry.value)
      }
      innerAnnotationVisitor.visitEnd()
    } else if (defaultValue is Array<*>) {
      when (defaultValue.javaClass.componentType) {
        EnumMirror::class.java,
        AnnotationMirror::class.java,
        String::class.java,
        Type::class.java -> {
          val innerAnnotationVisitor = annotationVisitor.visitArray(name)
          defaultValue.forEach { value -> addValue(innerAnnotationVisitor, null, value!!) }
          innerAnnotationVisitor.visitEnd()
        }
        else -> annotationVisitor.visit(name, defaultValue)
      }
    } else {
      annotationVisitor.visit(name, defaultValue)
    }
  }

  fun generate() {
    classVisitor.visitEnd()
  }
}
