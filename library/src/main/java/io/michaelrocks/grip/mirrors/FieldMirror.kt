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
import io.michaelrocks.grip.mirrors.signature.EmptyFieldSignatureMirror
import io.michaelrocks.grip.mirrors.signature.FieldSignatureMirror
import io.michaelrocks.grip.mirrors.signature.GenericDeclaration
import io.michaelrocks.grip.mirrors.signature.LazyFieldSignatureMirror

interface FieldMirror : Element<Type>, Annotated {
  val signature: FieldSignatureMirror
  val value: Any?

  class Builder(
    private val asmApi: Int,
    private val enclosingGenericDeclaration: GenericDeclaration
  ) {
    private var access = 0
    private var name: String? = null
    private var type: Type? = null
    private var signature: String? = null
    private var value: Any? = null

    private val annotations = LazyList<AnnotationMirror>()

    fun access(access: Int) = apply {
      this.access = access
    }

    fun name(name: String) = apply {
      this.name = name
      this.type = getTypeByInternalName(name)
    }

    fun type(type: Type) = apply {
      this.type = type
    }

    fun signature(signature: String?) = apply {
      this.signature = signature
    }

    fun value(value: Any?) = apply {
      this.value = value
    }

    fun addAnnotation(mirror: AnnotationMirror) = apply {
      this.annotations += mirror
    }

    fun build(): FieldMirror = ImmutableFieldMirror(this)

    private fun buildSignature(): FieldSignatureMirror =
      signature?.let { LazyFieldSignatureMirror(asmApi, enclosingGenericDeclaration, it) }
        ?: EmptyFieldSignatureMirror(type!!)

    private class ImmutableFieldMirror(builder: Builder) : FieldMirror {
      override val access = builder.access
      override val name = builder.name!!
      override val type = builder.type!!
      override val signature = builder.buildSignature()
      override val value = builder.value
      override val annotations = ImmutableAnnotationCollection(builder.annotations)

      override fun toString() = "FieldMirror{name = $name, type = $type}"
    }
  }
}
