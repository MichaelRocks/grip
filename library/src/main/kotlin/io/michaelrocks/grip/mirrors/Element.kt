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

package io.michaelrocks.grip.mirrors

import org.objectweb.asm.Opcodes

interface Element : Typed {
  val access: Int
  val name: String
}

val Element.isPublic: Boolean
  get() = (access and Opcodes.ACC_PUBLIC) != 0
val Element.isPrivate: Boolean
  get() = (access and Opcodes.ACC_PRIVATE) != 0
val Element.isProtected: Boolean
  get() = (access and Opcodes.ACC_PROTECTED) != 0
val Element.isPackage: Boolean
  get() = (access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED or Opcodes.ACC_PRIVATE)) == 0
val Element.isStatic: Boolean
  get() = (access and Opcodes.ACC_STATIC) != 0
val Element.isFinal: Boolean
  get() = (access and Opcodes.ACC_FINAL) != 0
val Element.isSuper: Boolean
  get() = (access and Opcodes.ACC_SUPER) != 0
val Element.isSynchronized: Boolean
  get() = (access and Opcodes.ACC_SYNCHRONIZED) != 0
val Element.isVolatile: Boolean
  get() = (access and Opcodes.ACC_VOLATILE) != 0
val Element.isBridge: Boolean
  get() = (access and Opcodes.ACC_BRIDGE) != 0
val Element.isVarargs: Boolean
  get() = (access and Opcodes.ACC_VARARGS) != 0
val Element.isTransient: Boolean
  get() = (access and Opcodes.ACC_TRANSIENT) != 0
val Element.isNative: Boolean
  get() = (access and Opcodes.ACC_NATIVE) != 0
val Element.isInterface: Boolean
  get() = (access and Opcodes.ACC_INTERFACE) != 0
val Element.isAbstract: Boolean
  get() = (access and Opcodes.ACC_ABSTRACT) != 0
val Element.isStrict: Boolean
  get() = (access and Opcodes.ACC_STRICT) != 0
val Element.isSynthetic: Boolean
  get() = (access and Opcodes.ACC_SYNTHETIC) != 0
val Element.isAnnotation: Boolean
  get() = (access and Opcodes.ACC_ANNOTATION) != 0
val Element.isEnum: Boolean
  get() = (access and Opcodes.ACC_ENUM) != 0
val Element.isMandated: Boolean
  get() = (access and Opcodes.ACC_MANDATED) != 0
