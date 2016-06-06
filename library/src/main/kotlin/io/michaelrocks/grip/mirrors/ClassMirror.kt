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

import io.michaelrocks.grip.commons.LazyList
import io.michaelrocks.grip.commons.immutable
import io.michaelrocks.grip.mirrors.signature.ClassSignatureMirror
import io.michaelrocks.grip.mirrors.signature.EmptyClassSignatureMirror
import io.michaelrocks.grip.mirrors.signature.LazyClassSignatureMirror
import org.objectweb.asm.ClassReader

private val OBJECT_TYPE = getType<Any>()

interface ClassMirror : Element<Type.Object>, Annotated {
  val version: Int
  val superType: Type.Object?
  val signature: ClassSignatureMirror
  val interfaces: List<Type.Object>

  val fields: Collection<FieldMirror>
  val constructors: Collection<MethodMirror>
  val methods: Collection<MethodMirror>

  class Builder {
    private var version: Int = 0
    private var access: Int = 0
    private var name: String? = null
    private var type: Type.Object? = null
    private var superType: Type.Object? = null
    private var signature: String? = null
    private val interfaces = LazyList<Type.Object>()

    private val annotations = LazyList<AnnotationMirror>()
    private val fields = LazyList<FieldMirror>()
    private val constructors = LazyList<MethodMirror>()
    private val methods = LazyList<MethodMirror>()

    fun version(version: Int) = apply {
      this.version = version
    }

    fun access(access: Int) = apply {
      this.access = access
    }

    fun name(name: String) = apply {
      this.name = name
      this.type = getTypeFromInternalName(name) as Type.Object
    }

    fun superName(superName: String?) = apply {
      this.superType = superName?.let { getTypeByInternalName(it) as Type.Object }
    }

    fun signature(signature: String?) = apply {
      this.signature = signature
    }

    fun interfaces(interfaces: Array<out String>?) = apply {
      this.interfaces.clear()
      interfaces?.mapTo(this.interfaces) { getTypeByInternalName(it) as Type.Object }
    }

    fun addAnnotation(mirror: AnnotationMirror) = apply {
      this.annotations += mirror
    }

    fun addField(mirror: FieldMirror) = apply {
      this.fields += mirror
    }

    fun addConstructor(mirror: MethodMirror) = apply {
      check(mirror.isConstructor) { "Method $mirror is not a constructor" }
      this.constructors += mirror
    }

    fun addMethod(mirror: MethodMirror) = apply {
      check(!mirror.isConstructor) { "Method $mirror is a constructor" }
      this.methods += mirror
    }

    fun build(): ClassMirror = ImmutableClassMirror(this)

    private fun buildSignature(): ClassSignatureMirror =
        signature?.let { LazyClassSignatureMirror(it) } ?:
            EmptyClassSignatureMirror(superType ?: OBJECT_TYPE, interfaces)

    private class ImmutableClassMirror(builder: Builder) : ClassMirror {
      override val version = builder.version
      override val access = builder.access
      override val name = builder.name!!.substringAfterLast('/')
      override val type = getObjectTypeByInternalName(builder.name!!)
      override val superType = builder.superType
      override val signature = builder.buildSignature()
      override val interfaces = builder.interfaces.detachImmutableCopy()
      override val annotations = ImmutableAnnotationCollection(builder.annotations)
      override val fields = builder.fields.detachImmutableCopy()
      override val constructors = builder.constructors.detachImmutableCopy()
      override val methods = builder.methods.detachImmutableCopy()

      override fun toString() = "ClassMirror{type = $type}"
    }
  }
}

internal class LazyClassMirror(
    private val classReader: ClassReader,
    private val builder: () -> ClassMirror
) : ClassMirror {
  private val delegate by lazy { builder() }

  override val version = getClassVersion()
  override val access = classReader.access
  override val name: String = classReader.className.substringAfterLast('/')
  override val type: Type.Object = getObjectTypeByInternalName(classReader.className)
  override val superType = classReader.superName?.let { getObjectTypeByInternalName(it) }
  override val signature: ClassSignatureMirror
    get() = delegate.signature
  override val interfaces = classReader.interfaces.map { getObjectTypeByInternalName(it) }.immutable()
  override val annotations: AnnotationCollection
    get() = delegate.annotations
  override val fields: Collection<FieldMirror>
    get() = delegate.fields
  override val constructors: Collection<MethodMirror>
    get() = delegate.constructors
  override val methods: Collection<MethodMirror>
    get() = delegate.methods

  private fun getClassVersion(): Int =
      classReader.readInt(classReader.getItem(1) - 7)
}
