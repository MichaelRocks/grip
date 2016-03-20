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

package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

fun <T> not(matcher: (Grip, T) -> Boolean) =
    { grip: Grip, value: T -> !matcher(grip, value) }
inline infix fun <reified R, reified T1 : R, reified T2 : R> ((Grip, T1) -> Boolean).and(
    crossinline other: (Grip, T2) -> Boolean
) = { grip: Grip, value: R -> this(grip, value as T1) && other(grip, value as T2) }
inline infix fun <reified R, reified T1 : R, reified T2 : R> ((Grip, T1) -> Boolean).or(
    crossinline other: (Grip, T2) -> Boolean
) = { grip: Grip, value: R -> this(grip, value as T1) || other(grip, value as T2) }
inline infix fun <reified R, reified T1 : R, reified T2 : R> ((Grip, T1) -> Boolean).xor(
    crossinline other: (Grip, T2) -> Boolean
) = { grip: Grip, value: R -> this(grip, value as T1) != other(grip, value as T2) }

inline fun typeMatcher(crossinline predicate: (Grip, Type) -> Boolean) =
    { grip: Grip, type: Type -> predicate(grip, type) }
fun equalsTo(otherType: Type) = typeMatcher { grip, type -> type == otherType }
fun sortEqualsTo(sort: Int) = typeMatcher { grip, type -> type.sort == sort }
fun isPrimitive() = typeMatcher { grip, type ->
  when (type.sort) {
    Type.OBJECT, Type.ARRAY, Type.METHOD -> false
    else -> true
  }
}
fun isArray() = sortEqualsTo(Type.ARRAY)
fun isObject() = sortEqualsTo(Type.OBJECT)
fun isVoid() = sortEqualsTo(Type.VOID)

inline fun returns(crossinline predicate: (Grip, Type) -> Boolean) =
    typeMatcher { grip, type -> predicate(grip, type.returnType) }
fun returns(otherType: Type) = returns { grip, type -> type == otherType }

inline fun stringMatcher(crossinline predicate: (Grip, String) -> Boolean) =
    { grip: Grip, string: String -> predicate(grip, string) }
fun equalsTo(otherString: String) = stringMatcher { grip, string -> string == otherString }
fun matches(regex: Regex) = stringMatcher { grip, string -> regex.matches(string) }
fun startsWith(prefix: String) = stringMatcher { grip, string -> string.startsWith(prefix) }
fun endsWith(suffix: String) = stringMatcher { grip, string -> string.endsWith(suffix) }
fun contains(otherString: String) = stringMatcher { grip, string -> string.contains(otherString) }

inline fun type(crossinline predicate: (Grip, Type) -> Boolean) =
    { grip: Grip, mirror: Typed -> predicate(grip, mirror.type) }

inline fun access(crossinline predicate: (Grip, Int) -> Boolean) =
    { grip: Grip, mirror: Element -> predicate(grip, mirror.access) }
fun access(otherAccess: Int) = access { grip, access -> access == otherAccess }
fun accessHasAllOf(mask: Int) = access { grip, access -> access and mask == mask }
fun accessHasAnyOf(mask: Int) = access { grip, access -> access and mask != 0 }
fun accessHasNoneOf(mask: Int) = access { grip, access -> access and mask == 0 }
fun isPublic() = accessHasAllOf(Opcodes.ACC_PUBLIC)
fun isProtected() = accessHasAllOf(Opcodes.ACC_PROTECTED)
fun isPrivate() = accessHasAllOf(Opcodes.ACC_PRIVATE)
fun isPackagePrivate() = accessHasNoneOf(Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED or Opcodes.ACC_PRIVATE)
fun isStatic() = accessHasAllOf(Opcodes.ACC_STATIC)
fun isFinal() = accessHasAllOf(Opcodes.ACC_FINAL)
fun isInterface() = accessHasAllOf(Opcodes.ACC_INTERFACE)
fun isAbstract() = accessHasAllOf(Opcodes.ACC_ABSTRACT)
fun isAnnotation() = accessHasAllOf(Opcodes.ACC_ANNOTATION)
fun isEnum() = accessHasAllOf(Opcodes.ACC_ENUM)

inline fun name(crossinline predicate: (Grip, String) -> Boolean) =
    { grip: Grip, mirror: Element -> predicate(grip, mirror.name) }

inline fun annotatedWith(crossinline predicate: (Grip, AnnotationMirror) -> Boolean) =
    { grip: Grip, mirror: Annotated -> mirror.annotations.any { predicate(grip, it) } }
inline fun annotatedWith(annotationType: Type, crossinline predicate: (Grip, AnnotationMirror) -> Boolean) =
    annotatedWith { grip, annotation -> annotation.type == annotationType && predicate(grip, annotation) }
fun annotatedWith(annotationType: Type) =
    annotatedWith { grip, annotation -> annotation.type == annotationType }

inline fun version(crossinline predicate: (Grip, Int) -> Boolean) =
    { grip: Grip, mirror: ClassMirror -> predicate(grip, mirror.version) }
fun version(otherVersion: Int) = version { grip, version -> version == otherVersion }
fun versionIsGreater(otherVersion: Int) = version { grip, version -> version > otherVersion }
fun versionIsGreaterOrEqual(otherVersion: Int) = version { grip, version -> version >= otherVersion }
fun versionIsLower(otherVersion: Int) = version { grip, version -> version < otherVersion }
fun versionIsLowerOrEqual(otherVersion: Int) = version { grip, version -> version <= otherVersion }

inline fun superName(crossinline predicate: (Grip, String) -> Boolean) =
    { grip: Grip, mirror: ClassMirror -> mirror.superName?.let { predicate(grip, it) } ?: false }
fun hasSuperName() =
    { grip: Grip, mirror: ClassMirror -> !mirror.superName.isNullOrEmpty() }

inline fun interfaces(crossinline predicate: (Grip, List<Type>) -> Boolean) =
    { grip: Grip, mirror: ClassMirror -> predicate(grip, mirror.interfaces) }
fun interfacesContain(type: Type) = interfaces { grip, interfaces -> interfaces.contains(type) }
fun interfacesAreEmpty() = interfaces { grip, interfaces -> interfaces.isEmpty() }

inline fun withField(crossinline predicate: (Grip, FieldMirror) -> Boolean) =
    { grip: Grip, mirror: ClassMirror -> mirror.fields.any { predicate(grip, it) } }

inline fun withConstructor(crossinline predicate: (Grip, MethodMirror) -> Boolean) =
    { grip: Grip, mirror: ClassMirror -> mirror.constructors.any { predicate(grip, it) } }
inline fun withMethod(crossinline predicate: (Grip, MethodMirror) -> Boolean) =
    { grip: Grip, mirror: ClassMirror -> mirror.methods.any { predicate(grip, it) } }
fun isConstructor() =
    { grip: Grip, mirror: MethodMirror -> mirror.isConstructor() }
fun isDefaultConstructor() =
    { grip: Grip, mirror: MethodMirror -> mirror.isDefaultConstructor() }
fun isStaticInitializer() =
    { grip: Grip, mirror: MethodMirror -> mirror.isStaticInitializer() }

inline fun withParameter(crossinline predicate: (Grip, MethodParameterMirror) -> Boolean) =
    { grip: Grip, mirror: MethodMirror -> mirror.parameters.any { predicate(grip, it) } }

fun existsInClasses(query: Query<ClassesResult>) =
    { grip: Grip, mirror: ClassMirror ->
      query.execute().containsType(mirror.type)
    }
fun existsInFields(query: Query<FieldsResult>) =
    { grip: Grip, mirror: FieldMirror ->
      query.execute().any {
        it.fields.any { it.name == mirror.name && it.type == mirror.type }
      }
    }
fun existsInMethods(query: Query<MethodsResult>) =
    { grip: Grip, mirror: MethodMirror ->
      query.execute().any {
        it.methods.any { it.name == mirror.name && it.type == mirror.type }
      }
    }

inline fun <T> wrap(crossinline matcher: (T) -> Boolean): (Grip, T) -> Boolean =
    { grip: Grip, value: T -> matcher(value) }
