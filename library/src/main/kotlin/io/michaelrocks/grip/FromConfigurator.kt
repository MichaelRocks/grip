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

import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type
import java.io.File
import java.util.ArrayList

interface FromConfigurator<M, R> {
  infix fun from(classMirrorSource: ClassMirrorSource): QueryConfigurator<M, R>
  infix fun from(files: Iterable<File>): QueryConfigurator<M, R>
  infix fun from(classpath: Classpath): QueryConfigurator<M, R>
}

infix fun <M, R> FromConfigurator<M, R>.from(file: File): QueryConfigurator<M, R> {
  return from(listOf(file))
}

infix fun <M, R> FromConfigurator<M, R>.from(provider: () -> Sequence<ClassMirror>): QueryConfigurator<M, R> {
  return from(FunctionClassMirrorSource(provider))
}

infix fun <M, R> FromConfigurator<M, R>.from(query: Query<ClassesResult>): QueryConfigurator<M, R> {
  return from { query.execute().classes.asSequence() }
}

infix fun <M, R> FromConfigurator<M, R>.from(classMirror: ClassMirror): QueryConfigurator<M, R> {
  return from { sequenceOf(classMirror) }
}

infix fun <M, R> FromConfigurator<M, R>.from(classMirrors: Iterable<ClassMirror>): QueryConfigurator<M, R> {
  return from { classMirrors.asSequence() }
}

infix fun <M, R> FromConfigurator<M, R>.from(classMirrors: Sequence<ClassMirror>): QueryConfigurator<M, R> {
  return from { classMirrors }
}

fun Iterable<Type.Object>.asClassMirrors(classRegistry: ClassRegistry): Sequence<ClassMirror> {
  return asSequence().asClassMirrors(classRegistry)
}

fun Sequence<Type.Object>.asClassMirrors(classRegistry: ClassRegistry): Sequence<ClassMirror> {
  return map { classRegistry.getClassMirror(it) }
}

val classpath = Classpath()

fun files(file1: File, file2: File, vararg files: File): List<File> =
    ArrayList<File>(files.size + 2).apply {
      add(file1)
      add(file2)
      addAll(files)
    }

interface QueryConfigurator<M, R> {
  infix fun where(matcher: (M) -> Boolean): Query<R> =
      where(wrap(matcher))
  infix fun where(matcher: (Grip, M) -> Boolean): Query<R>
}

interface Query<R> {
  fun execute(): R
}

class Classpath internal constructor()
