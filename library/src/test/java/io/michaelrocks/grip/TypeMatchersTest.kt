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

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getMethodType
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.mirrors.getType
import org.junit.Test

class TypeMatchersTest {
  private val void = Type.Primitive.Void
  private val string = getType<String>()
  private val array = getType("[I")
  private val method = getMethodType(Type.Primitive.Int, getType<Any>())

  @Test
  fun testTypeEqualsTrue() = string.assertMatcher(true, equalsTo(getObjectTypeByInternalName("java/lang/String")))

  @Test
  fun testTypeEqualsFalse() = string.assertMatcher(false, equalsTo(getObjectTypeByInternalName("java/lang/Object")))

  @Test
  fun testIsPrimitiveTrue() = void.assertMatcher(true, isPrimitive())

  @Test
  fun testIsPrimitiveFalse() = string.assertMatcher(false, isPrimitive())

  @Test
  fun testIsArrayTrue() = array.assertMatcher(true, isArray())

  @Test
  fun testIsArrayFalse() = void.assertMatcher(false, isArray())

  @Test
  fun testIsObjectTrue() = string.assertMatcher(true, isObject())

  @Test
  fun testIsObjectFalse() = void.assertMatcher(false, isObject())

  @Test
  fun testIsVoidTrue() = void.assertMatcher(true, isVoid())

  @Test
  fun testIsVoidFalse() = array.assertMatcher(false, isVoid())

  @Test
  fun testReturnsTrue() = method.assertMatcher(true, returns(Type.Primitive.Int))

  @Test
  fun testReturnsFalse() = method.assertMatcher(false, returns(Type.Primitive.Void))
}
