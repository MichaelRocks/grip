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

package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.commons.LazyList
import io.michaelrocks.grip.commons.immutable
import io.michaelrocks.grip.mirrors.GenericTypeListWrapper
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader

interface MethodSignatureMirror {
  val typeParameters: List<TypeParameter>
  val parameterTypes: List<GenericType>
  val returnType: GenericType
  val exceptionTypes: List<GenericType>

  fun toJvmSignature(): String

  class Builder() {
    private val typeParameters = LazyList<TypeParameter.Builder>()
    private val parameterTypes = LazyList<GenericType>()
    private var returnType: GenericType? = null
    private val exceptionTypes = LazyList<GenericType>()

    fun addTypeParameterBuilder(builder: TypeParameter.Builder) = apply {
      typeParameters += builder
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
      override val typeParameters: List<TypeParameter> = builder.typeParameters.map { it.build() }.immutable()
      override val parameterTypes: List<GenericType> = builder.parameterTypes.detachImmutableCopy()
      override val returnType: GenericType = builder.returnType!!
      override val exceptionTypes: List<GenericType> = builder.exceptionTypes.detachImmutableCopy()

      override fun toJvmSignature() = throw UnsupportedOperationException()
    }
  }
}

internal class LazyMethodSignatureMirror(private val signature: String) : MethodSignatureMirror {
  private val delegate by lazy(LazyThreadSafetyMode.PUBLICATION) { readMethodSignature(signature) }

  override val typeParameters: List<TypeParameter>
    get() = delegate.typeParameters
  override val parameterTypes: List<GenericType>
    get() = delegate.parameterTypes
  override val returnType: GenericType
    get() = delegate.returnType
  override val exceptionTypes: List<GenericType>
    get() = delegate.exceptionTypes

  override fun toJvmSignature() = signature
}

internal class EmptyMethodSignatureMirror(type: Type, exceptions: List<Type>) : MethodSignatureMirror {
  override val typeParameters: List<TypeParameter>
    get() = emptyList()
  override val parameterTypes: List<GenericType> =
      type.argumentTypes.run {
        if (isEmpty()) emptyList()
        else GenericTypeListWrapper(toList())
      }
  override val returnType: GenericType =
      GenericType.RawType(type.returnType)
  override val exceptionTypes: List<GenericType> =
      if (exceptions.isEmpty()) emptyList() else GenericTypeListWrapper(exceptions)

  override fun toJvmSignature() = ""
}

internal fun readMethodSignature(signature: String): MethodSignatureMirror =
    MethodSignatureReader().run {
      SignatureReader(signature).accept(this)
      toMethodSignature()
    }
