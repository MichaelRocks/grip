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

interface Grip : AutoCloseable {
  val fileRegistry: FileRegistry
  val classRegistry: ClassRegistry
  val classProducer: ClassProducer

  infix fun <M, R> select(projection: Projection<M, R>): FromConfigurator<M, R>
}

interface MutableGrip : Grip {
  override val fileRegistry: MutableFileRegistry
  override val classRegistry: MutableClassRegistry
  override val classProducer: MutableClassProducer
}

internal abstract class AbstractGrip : Grip {
  abstract override val fileRegistry: CloseableFileRegistry
  abstract override val classRegistry: CloseableClassRegistry
  abstract override val classProducer: CloseableClassProducer

  private var closed = false

  override fun <M, R> select(projection: Projection<M, R>): FromConfigurator<M, R> {
    checkNotClosed()
    return projection.configurator(this)
  }

  override fun close() {
    closed = true
    classProducer.close()
    classRegistry.close()
    fileRegistry.close()
  }

  private fun checkNotClosed() {
    check(!closed) { "$this is closed" }
  }
}

internal class DefaultGrip(
  override val fileRegistry: CloseableFileRegistry,
  override val classRegistry: CloseableClassRegistry,
  override val classProducer: CloseableClassProducer
) : AbstractGrip()

internal class DefaultMutableGrip(
  override val fileRegistry: CloseableMutableFileRegistry,
  override val classRegistry: CloseableMutableClassRegistry,
  override val classProducer: CloseableMutableClassProducer
) : AbstractGrip(), MutableGrip
