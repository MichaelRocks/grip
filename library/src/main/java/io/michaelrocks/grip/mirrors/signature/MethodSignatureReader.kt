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

import org.objectweb.asm.signature.SignatureVisitor

internal class MethodSignatureReader(
  asmApi: Int,
  classGenericDeclaration: GenericDeclaration
) : SignatureVisitor(asmApi) {

  private val builder = MethodSignatureMirror.Builder()
  private var typeVariableBuilder: TypeVariableBuilder? = null
  private val genericDeclaration = InheritingGenericDeclaration(classGenericDeclaration)

  fun toMethodSignature(): MethodSignatureMirror = builder.build()

  override fun visitFormalTypeParameter(name: String) {
    buildTypeVariable()
    typeVariableBuilder = TypeVariableBuilder(name)
  }

  override fun visitClassBound(): SignatureVisitor {
    return GenericTypeReader(api, genericDeclaration) { typeVariableBuilder!!.classBound(it) }
  }

  override fun visitInterfaceBound(): SignatureVisitor {
    return GenericTypeReader(api, genericDeclaration) { typeVariableBuilder!!.addInterfaceBound(it) }
  }

  override fun visitParameterType(): SignatureVisitor {
    buildTypeVariable()
    return GenericTypeReader(api, genericDeclaration) { builder.addParameterType(it) }
  }

  override fun visitReturnType(): SignatureVisitor {
    buildTypeVariable()
    return GenericTypeReader(api, genericDeclaration) { builder.returnType(it) }
  }

  override fun visitExceptionType(): SignatureVisitor {
    return GenericTypeReader(api, genericDeclaration) { builder.addExceptionType(it) }
  }

  private fun buildTypeVariable() {
    typeVariableBuilder?.let {
      genericDeclaration.typeVariables.add(it.build())
    }
    typeVariableBuilder = null
  }
}
