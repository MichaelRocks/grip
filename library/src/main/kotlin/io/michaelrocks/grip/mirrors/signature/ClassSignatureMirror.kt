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
import java.util.*

interface ClassSignatureMirror {
  val typeParameters: List<TypeParameter>
  val superType: GenericType
  val interfaces: List<GenericType>

  fun toJvmSignature(): String

  class Builder() {
    private val typeParameters = LazyList<TypeParameter.Builder>()
    private var superType: GenericType = OBJECT_RAW_TYPE
    private val interfaces = LazyList<GenericType>()

    fun addTypeParameterBuilder(builder: TypeParameter.Builder) = apply {
      typeParameters += builder
    }

    fun superType(superType: GenericType) = apply {
      this.superType = superType
    }

    fun addInterface(interfaceType: GenericType) = apply {
      interfaces += interfaceType
    }

    fun build(): ClassSignatureMirror = ClassSignatureMirrorImpl(this)

    private class ClassSignatureMirrorImpl(builder: Builder) : ClassSignatureMirror {
      override val typeParameters: List<TypeParameter> =
          builder.typeParameters.map { it.build() }.immutable()
      override val superType: GenericType = builder.superType
      override val interfaces: List<GenericType> = builder.interfaces.detachImmutableCopy()

      override fun toJvmSignature() = throw UnsupportedOperationException()
    }
  }
}

internal class LazyClassSignatureMirror(private val signature: String) : ClassSignatureMirror {
  private val delegate by lazy(LazyThreadSafetyMode.PUBLICATION) { readClassSignature(signature) }

  override val typeParameters: List<TypeParameter>
    get() = delegate.typeParameters
  override val superType: GenericType
    get() = delegate.superType
  override val interfaces: List<GenericType>
    get() = delegate.interfaces

  override fun toJvmSignature() = signature
}

internal class EmptyClassSignatureMirror(superType: Type, interfaces: List<Type>) : ClassSignatureMirror {
  override val typeParameters: List<TypeParameter>
    get() = Collections.emptyList()
  override val superType =
      GenericType.RawType(superType)
  override val interfaces: List<GenericType> =
      if (interfaces.isEmpty()) emptyList() else GenericTypeListWrapper(interfaces)

  override fun toJvmSignature() = ""
}

internal fun readClassSignature(signature: String): ClassSignatureMirror =
    ClassSignatureReader().run {
      SignatureReader(signature).accept(this)
      toClassSignature()
    }
