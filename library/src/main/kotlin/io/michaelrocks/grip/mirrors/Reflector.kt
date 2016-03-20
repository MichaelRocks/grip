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

package io.michaelrocks.grip.mirrors

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.commons.given
import io.michaelrocks.grip.mirrors.annotation.AnnotationInstanceReader
import io.michaelrocks.grip.mirrors.annotation.AnnotationValueReader
import org.objectweb.asm.*

internal interface Reflector {
  fun reflect(data: ByteArray, classRegistry: ClassRegistry, forAnnotation: Boolean): ClassMirror
}

internal class ReflectorImpl : Reflector {
  override fun reflect(data: ByteArray, classRegistry: ClassRegistry, forAnnotation: Boolean): ClassMirror {
    val reader = ClassReader(data)
    if (forAnnotation) {
      return readClassMirror(reader, classRegistry, true)
    } else {
      return LazyClassMirror(reader) { readClassMirror(reader, classRegistry, false) }
    }
  }

  private fun readClassMirror(reader: ClassReader, classRegistry: ClassRegistry, forAnnotation: Boolean): ClassMirror {
    val visitor = ReflectorClassVisitor(classRegistry, forAnnotation)
    reader.accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
    return visitor.toClassMirror()
  }

  private class ReflectorClassVisitor(
      private val classRegistry: ClassRegistry,
      private val forAnnotation: Boolean
  ) : ClassVisitor(Opcodes.ASM5) {

    private val builder = ClassMirror.Builder()

    fun toClassMirror(): ClassMirror = builder.build()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
        interfaces: Array<out String>?) {
      builder.apply {
        version(version)
        access(access)
        name(name)
        signature(signature)
        superName(superName)
        interfaces(interfaces)
      }
    }

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
        given(!forAnnotation) {
          AnnotationInstanceReader(Type.getType(desc), classRegistry) {
            builder.addAnnotation(it)
          }
        }

    override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? =
        given(!forAnnotation) {
          ReflectorFieldVisitor(classRegistry, access, name, desc, signature, value) {
            builder.addField(it)
          }
        }

    override fun visitMethod(access: Int, name: String, desc: String, signature: String?,
        exceptions: Array<out String>?): MethodVisitor {
      return ReflectorMethodVisitor(classRegistry, forAnnotation, access, name, desc, signature, exceptions) {
        if (it.isConstructor()) {
          builder.addConstructor(it)
        } else {
          builder.addMethod(it)
        }
      }
    }

    override fun visitEnd() {
      super.visitEnd()
    }
  }

  private class ReflectorFieldVisitor(
      private val classRegistry: ClassRegistry,
      access: Int,
      name: String,
      desc: String,
      signature: String?,
      value: Any?,
      private val callback: (FieldMirror) -> Unit
  ) : FieldVisitor(Opcodes.ASM5) {

    private val builder = FieldMirror.Builder().apply {
      access(access)
      name(name)
      type(Type.getType(desc))
      signature(signature)
      value(value)
    }

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor =
        AnnotationInstanceReader(Type.getType(desc), classRegistry) {
          builder.addAnnotation(it)
        }

    override fun visitEnd() = callback(builder.build())
  }

  private class ReflectorMethodVisitor(
      private val classRegistry: ClassRegistry,
      private val forAnnotation: Boolean,
      access: Int,
      name: String,
      desc: String,
      signature: String?,
      exceptions: Array<out String>?,
      private val callback: (MethodMirror) -> Unit
  ) : MethodVisitor(Opcodes.ASM5) {

    private val builder = MethodMirror.Builder().apply {
      val type = Type.getType(desc)
      access(access)
      name(name)
      type(type)
      signature(signature)
      exceptions(exceptions)
    }

    override fun visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor? =
        given(visible && !forAnnotation) {
          AnnotationInstanceReader(Type.getType(desc), classRegistry) {
            builder.addParameterAnnotation(parameter, it)
          }
        }

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
        given(visible && !forAnnotation) {
          AnnotationInstanceReader(Type.getType(desc), classRegistry) {
            builder.addAnnotation(it)
          }
        }

    override fun visitAnnotationDefault(): AnnotationVisitor? =
        AnnotationValueReader(classRegistry) {
          builder.defaultValue(it)
        }

    override fun visitEnd() = callback(builder.build())
  }
}
