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
import io.michaelrocks.grip.commons.getType
import io.michaelrocks.grip.commons.immutable
import io.michaelrocks.grip.mirrors.signature.ClassSignatureMirror
import io.michaelrocks.grip.mirrors.signature.EmptyClassSignatureMirror
import io.michaelrocks.grip.mirrors.signature.LazyClassSignatureMirror
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type

private val OBJECT_TYPE = getType<Any>()
private val OBJECT_INTERNAL_NAME = OBJECT_TYPE.internalName

interface ClassMirror : Element, Annotated {
  val version: Int
  val superName: String?
  val superType: Type?
  val signature: ClassSignatureMirror
  val interfaces: List<Type>

  val fields: Collection<FieldMirror>
  val constructors: Collection<MethodMirror>
  val methods: Collection<MethodMirror>

  class Builder {
    private var version: Int = 0
    private var access: Int = 0
    private var name: String? = null
    private var type: Type? = null
    private var superName: String = OBJECT_INTERNAL_NAME
    private var superType: Type = OBJECT_TYPE
    private var signature: String? = null
    private val interfaces = LazyList<Type>()

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
      this.type = Type.getObjectType(name)
    }

    fun superName(superName: String?) = apply {
      this.superName = superName ?: OBJECT_INTERNAL_NAME
      this.superType = superName?.let { Type.getObjectType(it) } ?: OBJECT_TYPE
    }

    fun signature(signature: String?) = apply {
      this.signature = signature
    }

    fun interfaces(interfaces: Array<out String>?) = apply {
      this.interfaces.clear()
      interfaces?.mapTo(this.interfaces) { Type.getObjectType(it) }
    }

    fun addAnnotation(mirror: AnnotationMirror) = apply {
      this.annotations += mirror
    }

    fun addField(mirror: FieldMirror) = apply {
      this.fields += mirror
    }

    fun addConstructor(mirror: MethodMirror) = apply {
      check(mirror.isConstructor()) { "Method $mirror is not a constructor" }
      this.constructors += mirror
    }

    fun addMethod(mirror: MethodMirror) = apply {
      check(!mirror.isConstructor()) { "Method $mirror is a constructor" }
      this.methods += mirror
    }

    fun build(): ClassMirror = ImmutableClassMirror(this)

    private fun buildSignature(): ClassSignatureMirror =
        signature?.let { LazyClassSignatureMirror(it) } ?: EmptyClassSignatureMirror(superType, interfaces)

    private class ImmutableClassMirror(builder: Builder) : ClassMirror {
      override val version = builder.version
      override val access = builder.access
      override val name = builder.name!!.substringAfterLast('/')
      override val type = Type.getObjectType(builder.name)
      override val superName = builder.superName
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
  override val name = classReader.className.substringAfterLast('/')
  override val type = Type.getObjectType(classReader.className)
  override val superName = classReader.superName
  override val superType = Type.getObjectType(classReader.superName)
  override val signature: ClassSignatureMirror
    get() = delegate.signature
  override val interfaces = classReader.interfaces.map { Type.getObjectType(it) }.immutable()
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
