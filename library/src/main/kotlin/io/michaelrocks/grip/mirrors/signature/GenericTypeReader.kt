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
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor

private val OBJECT_UPPER_BOUNDED_TYPE = GenericType.UpperBoundedType(OBJECT_RAW_TYPE)

internal class GenericTypeReader(
    private val callback: (GenericType) -> Unit
) : SignatureVisitor(Opcodes.ASM5) {
  private var genericType: GenericType? = null
  private var classType: Type? = null
  private val typeArguments = LazyList<GenericType>()
  private var arrayDimensions = 0

  override fun visitBaseType(descriptor: Char) {
    genericType = GenericType.RawType(Type.getType(descriptor.toString()))
    visitEnd()
  }

  override fun visitTypeVariable(name: String) {
    genericType = GenericType.TypeVariable(name)
    visitEnd()
  }

  override fun visitArrayType(): SignatureVisitor {
    ++arrayDimensions
    return this
  }

  override fun visitClassType(name: String) {
    classType = Type.getObjectType(name)
    typeArguments.clear()
  }

  override fun visitInnerClassType(name: String) {
    buildGenericType()
    classType = Type.getObjectType(name)
    typeArguments.clear()
  }

  override fun visitTypeArgument() {
    typeArguments.add(OBJECT_UPPER_BOUNDED_TYPE)
  }

  override fun visitTypeArgument(name: Char): SignatureVisitor {
    return GenericTypeReader {
      typeArguments.add(
          when (name) {
            SignatureVisitor.EXTENDS -> GenericType.UpperBoundedType(it)
            SignatureVisitor.SUPER -> GenericType.LowerBoundedType(it)
            SignatureVisitor.INSTANCEOF -> it
            else -> error("Unknown wildcard type: $name")
          }
      )
    }
  }

  override fun visitEnd() {
    buildGenericType()
    callback(genericType!!)
  }

  private fun buildGenericType() {
    if (classType != null) {
      val innerType =
          if (typeArguments.isEmpty()) {
            GenericType.RawType(classType!!)
          } else {
            GenericType.ParameterizedType(classType!!, typeArguments.toList())
          }
      genericType = genericType?.let { GenericType.InnerType(innerType, it) } ?: innerType
    }

    while (arrayDimensions > 0) {
      genericType = GenericType.GenericArrayType(genericType!!)
      --arrayDimensions
    }
  }
}

internal fun readGenericType(signature: String): GenericType {
  var genericType: GenericType? = null
  SignatureReader(signature).acceptType(
      GenericTypeReader {
        genericType = it
      }
  )
  return genericType!!
}
