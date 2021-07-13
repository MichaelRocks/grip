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

package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.commons.LazyList
import io.michaelrocks.grip.mirrors.GenericTypeListWrapper
import io.michaelrocks.grip.mirrors.Type
import org.objectweb.asm.signature.SignatureReader

interface MethodSignatureMirror {
  val typeVariables: List<GenericType.TypeVariable>
  val parameterTypes: List<GenericType>
  val returnType: GenericType
  val exceptionTypes: List<GenericType>

  fun toJvmSignature(): String

  class Builder {
    private val typeVariables = LazyList<GenericType.TypeVariable>()
    private val parameterTypes = LazyList<GenericType>()
    private var returnType: GenericType? = null
    private val exceptionTypes = LazyList<GenericType>()

    fun addTypeVariable(typeVariable: GenericType.TypeVariable) = apply {
      typeVariables += typeVariable
    }

    fun addParameterType(parameterType: GenericType) = apply {
      parameterTypes += parameterType
    }

    fun returnType(returnType: GenericType) = apply {
      this.returnType = returnType
    }

    fun addExceptionType(exceptionType: GenericType) = apply {
      exceptionTypes += exceptionType
    }

    fun build(): MethodSignatureMirror = MethodSignatureMirrorImpl(this)

    private class MethodSignatureMirrorImpl(builder: Builder) : MethodSignatureMirror {
      override val typeVariables: List<GenericType.TypeVariable> = builder.typeVariables.detachImmutableCopy()
      override val parameterTypes: List<GenericType> = builder.parameterTypes.detachImmutableCopy()
      override val returnType: GenericType = builder.returnType!!
      override val exceptionTypes: List<GenericType> = builder.exceptionTypes.detachImmutableCopy()

      override fun toJvmSignature() = throw UnsupportedOperationException()
    }
  }
}

internal class LazyMethodSignatureMirror(
    private val signature: String,
    classGenericDeclaration: GenericDeclaration
) : MethodSignatureMirror {

  private val delegate by lazy(LazyThreadSafetyMode.PUBLICATION) {
    readMethodSignature(signature, classGenericDeclaration)
  }

  override val typeVariables: List<GenericType.TypeVariable>
    get() = delegate.typeVariables
  override val parameterTypes: List<GenericType>
    get() = delegate.parameterTypes
  override val returnType: GenericType
    get() = delegate.returnType
  override val exceptionTypes: List<GenericType>
    get() = delegate.exceptionTypes

  override fun toJvmSignature() = signature
}

internal class EmptyMethodSignatureMirror(type: Type.Method, exceptions: List<Type.Object>) : MethodSignatureMirror {
  override val typeVariables: List<GenericType.TypeVariable>
    get() = emptyList()
  override val parameterTypes: List<GenericType> =
      type.argumentTypes.run {
        if (isEmpty()) emptyList()
        else GenericTypeListWrapper(toList())
      }
  override val returnType: GenericType =
      GenericType.Raw(type.returnType)
  override val exceptionTypes: List<GenericType> =
      if (exceptions.isEmpty()) emptyList() else GenericTypeListWrapper(exceptions)

  override fun toJvmSignature() = ""
}

internal fun readMethodSignature(signature: String, genericDeclaration: GenericDeclaration): MethodSignatureMirror =
    MethodSignatureReader(genericDeclaration).run {
      SignatureReader(signature).accept(this)
      toMethodSignature()
    }

internal fun MethodSignatureMirror.asGenericDeclaration(): GenericDeclaration {
  return GenericDeclaration(typeVariables)
}

internal fun MethodSignatureMirror.asLazyGenericDeclaration(): GenericDeclaration {
  return LazyGenericDeclaration { asGenericDeclaration() }
}
