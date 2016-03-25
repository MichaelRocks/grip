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

import org.objectweb.asm.Type

interface FieldSignatureMirror {
  val type: GenericType

  fun toJvmSignature(): String

  class Builder() {
    private var type: GenericType = OBJECT_RAW_TYPE

    fun type(type: GenericType) = apply {
      this.type = type
    }

    fun build(): FieldSignatureMirror = FieldSignatureMirrorImpl(this)

    private class FieldSignatureMirrorImpl(builder: Builder) : FieldSignatureMirror {
      override val type: GenericType = builder.type

      override fun toJvmSignature() = throw UnsupportedOperationException()
    }
  }
}

internal class LazyFieldSignatureMirror(private val signature: String) : FieldSignatureMirror {
  private val delegate by lazy(LazyThreadSafetyMode.PUBLICATION) { readFieldSignature(signature) }

  override val type: GenericType
    get() = delegate.type

  override fun toJvmSignature() = signature
}

internal class EmptyFieldSignatureMirror(type: Type) : FieldSignatureMirror {
  override val type: GenericType = GenericType.Raw(type)

  override fun toJvmSignature() = ""
}

internal fun readFieldSignature(signature: String): FieldSignatureMirror =
    FieldSignatureMirror.Builder()
        .type(readGenericType(signature))
        .build()
