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

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor

internal class ClassSignatureReader : SignatureVisitor(Opcodes.ASM5) {
  private val builder = ClassSignatureMirror.Builder()
  private var typeParameterBuilder: TypeParameter.Builder? = null

  fun toClassSignature(): ClassSignatureMirror = builder.build()

  override fun visitFormalTypeParameter(name: String) {
    typeParameterBuilder = TypeParameter.Builder(name).apply {
      builder.addTypeParameterBuilder(this)
    }
  }

  override fun visitClassBound(): SignatureVisitor =
      GenericTypeReader {
        typeParameterBuilder!!.classBound(it)
      }

  override fun visitInterfaceBound(): SignatureVisitor =
      GenericTypeReader {
        typeParameterBuilder!!.addInterfaceBound(it)
      }

  override fun visitSuperclass(): SignatureVisitor =
      GenericTypeReader {
        builder.superType(it)
      }

  override fun visitInterface(): SignatureVisitor =
      GenericTypeReader {
        builder.addInterface(it)
      }
}
