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

package io.michaelrocks.grip

import java.io.File
import kotlin.properties.Delegates

internal abstract class AbstractQueryBuilder<M, R>(
  val grip: Grip
) : FromConfigurator<M, R>, QueryConfigurator<M, R>, Query<R> {

  private var classMirrorSource by Delegates.notNull<ClassMirrorSource>()
  private var matcher by Delegates.notNull<(Grip, M) -> Boolean>()

  private var result = lazy(LazyThreadSafetyMode.NONE) { execute(classMirrorSource, matcher) }

  override fun from(classMirrorSource: ClassMirrorSource): QueryConfigurator<M, R> = apply {
    this.classMirrorSource = classMirrorSource
  }

  override fun from(files: Iterable<File>): QueryConfigurator<M, R> = apply {
    classMirrorSource = FilesClassMirrorSource(grip, files.toList())
  }

  override fun from(classpath: Classpath): QueryConfigurator<M, R> {
    return from(grip.fileRegistry.classpath())
  }

  override fun where(matcher: (Grip, M) -> Boolean): Query<R> = apply {
    this.matcher = matcher
  }

  final override fun execute(): R = result.value

  protected abstract fun execute(source: ClassMirrorSource, matcher: (Grip, M) -> Boolean): R
}
