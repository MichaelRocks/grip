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
import java.io.File
import java.util.*
import kotlin.properties.Delegates

internal abstract class AbstractQueryBuilder<M, R>(
    private val grip: Grip
) : FromConfigurator<M ,R>, QueryConfigurator<M, R>, Query<R> {

  private var classMirrorSource by Delegates.notNull<ClassMirrorSource>()
  private var matcher by Delegates.notNull<(M) -> Boolean>()

  private var result = lazy(LazyThreadSafetyMode.NONE) { execute(classMirrorSource, matcher) }

  override fun from(file: File): QueryConfigurator<M, R> =
      from(Collections.singletonList(file))

  override fun from(files: Iterable<File>): QueryConfigurator<M, R> = apply {
    classMirrorSource = FilesClassMirrorSource(grip, files.toList())
  }

  override fun from(query: Query<ClassesResult>): QueryConfigurator<M, R> = apply {
    classMirrorSource = QueryClassMirrorSource(query)
  }

  override fun from(classMirror: ClassMirror): QueryConfigurator<M, R> = apply {
    classMirrorSource = SingletonClassMirrorSource(classMirror)
  }

  override fun from(classpath: Classpath): QueryConfigurator<M, R> =
      from(grip.fileRegistry.classpath())

  override fun where(matcher: (M) -> Boolean): Query<R> = apply {
    this.matcher = matcher
  }

  final override fun execute(): R = result.value

  protected abstract fun execute(source: ClassMirrorSource, matcher: (M) -> Boolean): R
}
