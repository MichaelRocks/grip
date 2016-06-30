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

class MethodSignatureReader(
    private val classGenericDeclaration: GenericDeclaration
) : SignatureVisitor(Opcodes.ASM5) {

  private val builder = MethodSignatureMirror.Builder()
  private var typeVariableBuilder: TypeVariableBuilder? = null
  private val genericDeclaration = InheritingGenericDeclaration(classGenericDeclaration)

  fun toMethodSignature(): MethodSignatureMirror = builder.build()

  override fun visitFormalTypeParameter(name: String) {
    buildTypeVariable()
    typeVariableBuilder = TypeVariableBuilder(name)
  }

  override fun visitClassBound(): SignatureVisitor {
    return GenericTypeReader(genericDeclaration) { typeVariableBuilder!!.classBound(it) }
  }

  override fun visitInterfaceBound(): SignatureVisitor {
    return GenericTypeReader(genericDeclaration) { typeVariableBuilder!!.addInterfaceBound(it) }
  }

  override fun visitParameterType(): SignatureVisitor {
    buildTypeVariable()
    return GenericTypeReader(genericDeclaration) { builder.addParameterType(it) }
  }

  override fun visitReturnType(): SignatureVisitor {
    buildTypeVariable()
    return GenericTypeReader(genericDeclaration) { builder.returnType(it) }
  }

  override fun visitExceptionType(): SignatureVisitor {
    return GenericTypeReader(genericDeclaration) { builder.addExceptionType(it) }
  }

  private fun buildTypeVariable() {
    typeVariableBuilder?.let {
      genericDeclaration.typeVariables.add(it.build())
    }
    typeVariableBuilder = null
  }
}
