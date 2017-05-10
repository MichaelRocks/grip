/*
 * Copyright 2017 Michael Rozumyanskiy
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

import io.michaelrocks.grip.mirrors.CONSTRUCTOR_NAME
import io.michaelrocks.grip.mirrors.DEFAULT_CONSTRUCTOR_TYPE
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.MethodParameterMirror
import io.michaelrocks.grip.mirrors.STATIC_INITIALIZER_NAME
import io.michaelrocks.grip.mirrors.STATIC_INITIALIZER_TYPE
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getMethodType
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test

class MethodMatchersTest {
  private val defaultConstructor = mock<MethodMirror>().apply {
    given(name).thenReturn(CONSTRUCTOR_NAME)
    given(type).thenReturn(DEFAULT_CONSTRUCTOR_TYPE)
  }

  private val constructor = mock<MethodMirror>().apply {
    given(name).thenReturn(CONSTRUCTOR_NAME)
    given(type).thenReturn(getMethodType(Type.Primitive.Void, Type.Primitive.Int))
    given(parameters).thenReturn(listOf(MethodParameterMirror.Builder(0, Type.Primitive.Int).build()))
  }

  private val staticInitializer = mock<MethodMirror>().apply {
    given(name).thenReturn(STATIC_INITIALIZER_NAME)
    given(type).thenReturn(STATIC_INITIALIZER_TYPE)
  }

  @Test fun testIsConstructorTrue() = defaultConstructor.assert(true) { isConstructor() }
  @Test fun testIsConstructorFalse() = staticInitializer.assert(false) { isConstructor() }
  @Test fun testIsDefaultConstructorTrue() = defaultConstructor.assert(true) { isDefaultConstructor() }
  @Test fun testIsDefaultConstructorFalse() = constructor.assert(false) { isDefaultConstructor() }
  @Test fun testIsStaticInitializerTrue() = staticInitializer.assert(true) { isStaticInitializer() }
  @Test fun testIsStaticInitializerFalse() = defaultConstructor.assert(false) { isStaticInitializer() }
  @Test fun testWithParameterTrue() = constructor.testParameters(true) { withParameter { _, _ -> true } }
  @Test fun testWithParameterFalse() = constructor.testParameters(false) { withParameter { _, _ -> false } }
  @Test fun testWithParameterEmpty() = defaultConstructor.testParameters(false) { withParameter { _, _ -> true }
  }

  private inline fun MethodMirror.testParameters(condition: Boolean, body: () -> ((Grip, MethodMirror) -> Boolean)) =
      assertAndVerify(condition, body) { parameters }
}
