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

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.Typed
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test

class TypedMatchersTest {
  val typed = mock<Typed<*>>().apply {
    given(type).thenReturn(Type.Primitive.Void)
  }

  @Test fun testTypeTrue() = typed.testType(true) { type { _, _ -> true } }
  @Test fun testTypeFalse() = typed.testType(false) { type { _, _ -> false } }

  private inline fun Typed<*>.testType(condition: Boolean, body: () -> ((Grip, Typed<*>) -> Boolean)) =
      assertAndVerify(condition, body) { type }
}
