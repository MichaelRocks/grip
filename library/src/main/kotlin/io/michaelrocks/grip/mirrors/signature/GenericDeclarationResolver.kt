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

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.Enclosure
import io.michaelrocks.grip.mirrors.Type

private class GenericDeclarationResolver(
    private val classRegistry: ClassRegistry,
    private val type: Type.Object
) {
  operator fun invoke(): GenericDeclaration {
    return resolve(type)
  }

  private fun resolve(type: Type.Object): GenericDeclaration {
    val mirror = type.toClassMirror()
    val enclosure = mirror.enclosure
    return when (enclosure) {
      is Enclosure.Method.Named -> resolve(enclosure)
      else -> mirror.enclosingType?.toClassMirror()?.genericDeclaration ?: EmptyGenericDeclaration
    }
  }

  private fun resolve(enclosure: Enclosure.Method.Named): GenericDeclaration {
    val mirror = classRegistry.getClassMirror(enclosure.enclosingType)
    val method = mirror.methods.first { it.name == enclosure.methodName && it.type == enclosure.methodType }
    return method.genericDeclaration
  }

  private fun Type.Object.toClassMirror() = classRegistry.getClassMirror(this)
}

internal fun ClassRegistry.resolveGenericDeclaration(type: Type.Object): GenericDeclaration {
  val resolver = GenericDeclarationResolver(this, type)
  return resolver()
}

internal fun ClassRegistry.resolveGenericDeclarationLazily(type: Type.Object): GenericDeclaration {
  return LazyGenericDeclaration { resolveGenericDeclaration(type) }
}
