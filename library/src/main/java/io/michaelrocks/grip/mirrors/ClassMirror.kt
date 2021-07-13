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

package io.michaelrocks.grip.mirrors

import io.michaelrocks.grip.commons.LazyList
import io.michaelrocks.grip.commons.immutable
import io.michaelrocks.grip.mirrors.signature.ClassSignatureMirror
import io.michaelrocks.grip.mirrors.signature.EmptyClassSignatureMirror
import io.michaelrocks.grip.mirrors.signature.EmptyGenericDeclaration
import io.michaelrocks.grip.mirrors.signature.GenericDeclaration
import org.objectweb.asm.ClassReader

interface ClassMirror : Element<Type.Object>, Annotated {
  val version: Int
  val superType: Type.Object?
  val signature: ClassSignatureMirror
  val interfaces: List<Type.Object>

  val declaringType: Type.Object?
  val enclosingType: Type.Object?
    get() {
      val enclosure = enclosure
      return when (enclosure) {
        is Enclosure.Method -> enclosure.enclosingType
        is Enclosure.None -> declaringType
      }
    }

  val simpleName: String
  val types: Collection<Type.Object>
  val enclosure: Enclosure

  val source: String?
  val debug: String?

  val fields: Collection<FieldMirror>
  val constructors: Collection<MethodMirror>
  val methods: Collection<MethodMirror>

  val genericDeclaration: GenericDeclaration

  class Builder {
    private var version: Int = 0
    private var access: Int = 0
    private var name: String? = null
    private var type: Type.Object? = null
    private var superType: Type.Object? = null
    private var signature: ClassSignatureMirror? = null
    private val interfaces = LazyList<Type.Object>()

    private val innerClasses = LazyList<InnerClass>()
    private var enclosure: Enclosure = Enclosure.None

    private var source: String? = null
    private var debug: String? = null

    private val annotations = LazyList<AnnotationMirror>()
    private val fields = LazyList<FieldMirror>()
    private val constructors = LazyList<MethodMirror>()
    private val methods = LazyList<MethodMirror>()

    private var genericDeclaration: GenericDeclaration = EmptyGenericDeclaration

    fun version(version: Int) = apply {
      this.version = version
    }

    fun access(access: Int) = apply {
      this.access = access
    }

    fun name(name: String) = apply {
      this.name = name
      this.type = getObjectTypeByInternalName(name)
    }

    fun superName(superName: String?) = apply {
      this.superType = superName?.let { getObjectTypeByInternalName(it) }
    }

    fun signature(signature: ClassSignatureMirror?) = apply {
      this.signature = signature
    }

    fun interfaces(interfaces: Array<out String>?) = apply {
      this.interfaces.clear()
      interfaces?.mapTo(this.interfaces) { getObjectTypeByInternalName(it) }
    }

    fun addInnerClass(innerClass: InnerClass) = apply {
      this.innerClasses += innerClass
    }

    fun enclosure(enclosure: Enclosure) = apply {
      this.enclosure = enclosure
    }

    fun source(source: String?) = apply {
      this.source = source
    }

    fun debug(debug: String?) = apply {
      this.debug = debug
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

    fun genericDeclaration(genericDeclaration: GenericDeclaration) {
      this.genericDeclaration = genericDeclaration
    }

    fun build(): ClassMirror = ImmutableClassMirror(this)

    private fun buildSignature(): ClassSignatureMirror =
      signature ?: EmptyClassSignatureMirror(superType, interfaces)

    private fun buildDeclaringClass(): Type.Object? {
      val innerClass = innerClasses.firstOrNull { it.type == type }
      return innerClass?.outerType
    }

    private fun buildName(): String {
      fun buildName(type: Type, innerClassesByOuterType: Map<Type.Object, InnerClass>): String {
        val innerClass = innerClassesByOuterType[type] ?: return type.className
        innerClass.outerType ?: return type.className
        innerClass.innerName ?: return type.className
        return "${buildName(innerClass.outerType, innerClassesByOuterType)}.${innerClass.innerName}"
      }

      if (innerClasses.isEmpty()) {
        return name!!.replace('/', '.')
      }
      val innerClassesByOuterType = innerClasses.associateBy { it.type }
      return buildName(type!!, innerClassesByOuterType)
    }

    private fun buildSimpleName(): String {
      val type = type!!
      val innerClass = innerClasses.firstOrNull { it.type == type }
      val enclosure = enclosure as? Enclosure.Method ?: return innerClass?.innerName ?: type.internalName.substringAfterLast('/')
      return type.internalName
        .removePrefix(enclosure.enclosingType.internalName)
        .trimStart { it == '$' || it in '0'..'9' }
    }

    private fun buildTypes(): Collection<Type.Object> {
      val outerInternalName = type!!.internalName
      return innerClasses
        .filter {
          val internalName = it.type.internalName
          internalName.length > outerInternalName.length && internalName.startsWith(outerInternalName)
        }
        .map { it.type }
    }

    private class ImmutableClassMirror(builder: Builder) : ClassMirror {
      override val version = builder.version
      override val access = builder.access
      override val name = builder.buildName()
      override val type = getObjectTypeByInternalName(builder.name!!)
      override val superType = builder.superType
      override val signature = builder.buildSignature()
      override val interfaces = builder.interfaces.detachImmutableCopy()
      override val annotations = ImmutableAnnotationCollection(builder.annotations)
      override val declaringType: Type.Object? = builder.buildDeclaringClass()
      override val simpleName = builder.buildSimpleName()
      override val types = builder.buildTypes()
      override val enclosure = builder.enclosure
      override val source = builder.source
      override val debug = builder.debug
      override val fields = builder.fields.detachImmutableCopy()
      override val constructors = builder.constructors.detachImmutableCopy()
      override val methods = builder.methods.detachImmutableCopy()
      override val genericDeclaration = builder.genericDeclaration

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
  override val name: String = delegate.name
  override val type: Type.Object = getObjectTypeByInternalName(classReader.className)
  override val superType = classReader.superName?.let { getObjectTypeByInternalName(it) }
  override val signature: ClassSignatureMirror
    get() = delegate.signature
  override val interfaces = classReader.interfaces.map { getObjectTypeByInternalName(it) }.immutable()
  override val annotations: AnnotationCollection
    get() = delegate.annotations
  override val declaringType: Type.Object?
    get() = delegate.declaringType
  override val enclosingType: Type.Object?
    get() = delegate.enclosingType
  override val simpleName: String
    get() = delegate.simpleName
  override val types: Collection<Type.Object>
    get() = delegate.types
  override val enclosure: Enclosure
    get() = delegate.enclosure
  override val source: String?
    get() = delegate.source
  override val debug: String?
    get() = delegate.debug
  override val fields: Collection<FieldMirror>
    get() = delegate.fields
  override val constructors: Collection<MethodMirror>
    get() = delegate.constructors
  override val methods: Collection<MethodMirror>
    get() = delegate.methods
  override val genericDeclaration: GenericDeclaration
    get() = delegate.genericDeclaration

  private fun getClassVersion(): Int =
    classReader.readInt(classReader.getItem(1) - 7)
}
