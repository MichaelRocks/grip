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
import io.michaelrocks.grip.mirrors.signature.EmptyGenericDeclaration
import io.michaelrocks.grip.mirrors.signature.EmptyMethodSignatureMirror
import io.michaelrocks.grip.mirrors.signature.GenericDeclaration
import io.michaelrocks.grip.mirrors.signature.MethodSignatureMirror

interface MethodMirror : Element<Type.Method>, Annotated {
  val signature: MethodSignatureMirror
  val defaultValue: Any?
  val exceptions: List<Type.Object>
  val parameters: List<MethodParameterMirror>
  val genericDeclaration: GenericDeclaration

  class Builder {
    private var access: Int = 0
    private var name: String? = null
    private var type: Type.Method? = null
    private var signature: MethodSignatureMirror? = null
    private var defaultValue: Any? = null
    private val exceptions = LazyList<Type.Object>()
    private val parameters = LazyList<MethodParameterMirror.Builder>()
    private var genericDeclaration: GenericDeclaration = EmptyGenericDeclaration

    private val annotations = LazyList<AnnotationMirror>()

    fun access(access: Int) = apply {
      this.access = access
    }

    fun name(name: String) = apply {
      this.name = name
    }

    fun type(type: Type.Method) = apply {
      this.type = type
      type.argumentTypes.forEach { parameters += MethodParameterMirror.Builder(parameters.size, it) }
    }

    fun signature(signature: MethodSignatureMirror?) = apply {
      this.signature = signature
    }

    fun defaultValue(defaultValue: Any?) = apply {
      this.defaultValue = defaultValue
    }

    fun exceptions(exceptions: Array<out String>?) = apply {
      this.exceptions.clear()
      exceptions?.mapTo(this.exceptions) { getObjectTypeByInternalName(it) }
    }

    fun addParameterAnnotation(index: Int, annotation: AnnotationMirror) = apply {
      parameters[index].addAnnotation(annotation)
    }

    fun genericDeclaration(genericDeclaration: GenericDeclaration) = apply {
      this.genericDeclaration = genericDeclaration
    }

    fun addAnnotation(annotation: AnnotationMirror) = apply {
      annotations += annotation
    }

    fun build(): MethodMirror = ImmutableMethodMirror(this)

    private fun buildSignature(): MethodSignatureMirror =
        signature ?: EmptyMethodSignatureMirror(type!!, exceptions)

    class ImmutableMethodMirror(builder: Builder) : MethodMirror {
      override val access = builder.access
      override val name = builder.name!!
      override val type = builder.type!!
      override val signature = builder.buildSignature()
      override val defaultValue = builder.defaultValue
      override val exceptions = builder.exceptions.detachImmutableCopy()
      override val annotations: AnnotationCollection = ImmutableAnnotationCollection(builder.annotations)
      override val parameters =
          if (builder.parameters.isEmpty()) listOf()
          else builder.parameters.map { it.build() }.immutable()
      override val genericDeclaration = builder.genericDeclaration

      override fun toString() = "MethodMirror{name = $name, type = $type}"
    }
  }
}

const val CONSTRUCTOR_NAME = "<init>"
const val STATIC_INITIALIZER_NAME = "<clinit>"
val DEFAULT_CONSTRUCTOR_TYPE = getMethodType(Type.Primitive.Void)
val STATIC_INITIALIZER_TYPE = getMethodType(Type.Primitive.Void)

fun isConstructor(methodName: String): Boolean =
    CONSTRUCTOR_NAME == methodName

fun isDefaultConstructor(methodName: String, methodType: Type): Boolean =
    isConstructor(methodName) && methodType == DEFAULT_CONSTRUCTOR_TYPE

fun isStaticInitializer(methodName: String): Boolean =
    methodName == STATIC_INITIALIZER_NAME

val MethodMirror.isConstructor: Boolean
  get() = isConstructor(name)
val MethodMirror.isDefaultConstructor: Boolean
  get() = isDefaultConstructor(name, type)
val MethodMirror.isStaticInitializer: Boolean
  get() = isStaticInitializer(name)

