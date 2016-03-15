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

fun <T> not(matcher: (T) -> Boolean) =
    { value: T -> !matcher(value) }
inline infix fun <reified R, reified T1 : R, reified T2 : R> ((T1) -> Boolean).and(crossinline other: (T2) -> Boolean) =
    { value: R -> this(value as T1) && other(value as T2) }
inline infix fun <reified R, reified T1 : R, reified T2 : R> ((T1) -> Boolean).or(crossinline other: (T2) -> Boolean) =
    { value: R -> this(value as T1) || other(value as T2) }
inline infix fun <reified R, reified T1 : R, reified T2 : R> ((T1) -> Boolean).xor(crossinline other: (T2) -> Boolean) =
    { value: R -> this(value as T1) != other(value as T2) }

inline fun typeMatcher(crossinline predicate: (Type) -> Boolean) =
    { type: Type -> predicate(type) }
fun equalsTo(type: Type) = typeMatcher { it == type }
fun sortEqualsTo(sort: Int) = typeMatcher { it.sort == sort }
fun isPrimitive() = typeMatcher { when (it.sort) {
  Type.OBJECT, Type.ARRAY, Type.METHOD -> false
  else -> true
}}
fun isArray() = sortEqualsTo(Type.ARRAY)
fun isObject() = sortEqualsTo(Type.OBJECT)
fun isVoid() = sortEqualsTo(Type.VOID)

inline fun returns(crossinline predicate: (Type) -> Boolean) =
    typeMatcher { predicate(it.returnType) }
fun returns(type: Type) = returns { it == type }

inline fun stringMatcher(crossinline predicate: (String) -> Boolean) =
    { string: String -> predicate(string) }
fun equalsTo(string: String) = stringMatcher { it == string }
fun matches(regex: Regex) = stringMatcher { regex.matches(it) }
fun startsWith(prefix: String) = stringMatcher { it.startsWith(prefix) }
fun endsWith(suffix: String) = stringMatcher { it.endsWith(suffix) }
fun contains(string: String) = stringMatcher { it.contains(string) }

inline fun type(crossinline predicate: (Type) -> Boolean) =
    { mirror: Typed -> predicate(mirror.type) }

inline fun access(crossinline predicate: (Int) -> Boolean) =
    { mirror: Element -> predicate(mirror.access) }
fun access(access: Int) = access { it == access }
fun accessHasAllOf(mask: Int) = access { it and mask == mask }
fun accessHasAnyOf(mask: Int) = access { it and mask != 0 }
fun accessHasNoneOf(mask: Int) = access { it and mask == 0 }
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

inline fun name(crossinline predicate: (String) -> Boolean) =
    { mirror: Element -> predicate(mirror.name) }

inline fun annotatedWith(crossinline predicate: (AnnotationMirror) -> Boolean) =
    { mirror: Annotated -> mirror.annotations.any { predicate(it) } }
inline fun annotatedWith(annotationType: Type, crossinline predicate: (AnnotationMirror) -> Boolean) =
    annotatedWith { it.type == annotationType && predicate(it) }
fun annotatedWith(annotationType: Type) =
    annotatedWith { it.type == annotationType }

inline fun version(crossinline predicate: (Int) -> Boolean) =
    { mirror: ClassMirror -> predicate(mirror.version) }
fun version(version: Int) = version { it == version }
fun versionIsGreater(version: Int) = version { it > version }
fun versionIsGreaterOrEqual(version: Int) = version { it >= version }
fun versionIsLower(version: Int) = version { it < version }
fun versionIsLowerOrEqual(version: Int) = version { it <= version }

inline fun superName(crossinline predicate: (String) -> Boolean) =
    { mirror: ClassMirror -> mirror.superName?.let { predicate(it) } ?: false }
fun hasSuperName() =
    { mirror: ClassMirror -> !mirror.superName.isNullOrEmpty() }

inline fun interfaces(crossinline predicate: (List<Type>) -> Boolean) =
    { mirror: ClassMirror -> predicate(mirror.interfaces) }
fun interfacesContain(type: Type) = interfaces { it.contains(type) }
fun interfacesAreEmpty() = interfaces { it.isEmpty() }

inline fun withField(crossinline predicate: (FieldMirror) -> Boolean) =
    { mirror: ClassMirror -> mirror.fields.any { predicate(it) } }

inline fun withConstructor(crossinline predicate: (MethodMirror) -> Boolean) =
    { mirror: ClassMirror -> mirror.constructors.any { predicate(it) } }
inline fun withMethod(crossinline predicate: (MethodMirror) -> Boolean) =
    { mirror: ClassMirror -> mirror.methods.any { predicate(it) } }
fun isConstructor() =
    { mirror: MethodMirror -> mirror.isConstructor() }
fun isDefaultConstructor() =
    { mirror: MethodMirror -> mirror.isDefaultConstructor() }
fun isStaticInitializer() =
    { mirror: MethodMirror -> mirror.isStaticInitializer() }

inline fun withParameter(crossinline predicate: (MethodParameterMirror) -> Boolean) =
    { mirror: MethodMirror -> mirror.parameters.any { predicate(it) } }

fun existsInClasses(query: Query<ClassesResult>) =
    { mirror: ClassMirror ->
      query.execute().containsType(mirror.type)
    }
fun existsInFields(query: Query<FieldsResult>) =
    { mirror: FieldMirror ->
      query.execute().any {
        it.fields.any { it.name == mirror.name && it.type == mirror.type }
      }
    }
fun existsInMethods(query: Query<MethodsResult>) =
    { mirror: MethodMirror ->
      query.execute().any {
        it.methods.any { it.name == mirror.name && it.type == mirror.type }
      }
    }
