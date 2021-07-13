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

import io.michaelrocks.mockito.mock
import io.michaelrocks.mockito.only
import io.michaelrocks.mockito.verify
import io.michaelrocks.mockito.verifyNoMoreInteractions
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

val mockGrip = mock<Grip>()

inline fun <T> T.assertMatcher(condition: Boolean, matcher: (Grip, T) -> Boolean) =
    if (condition) assertTrue(matcher(mockGrip, this)) else assertFalse(matcher(mockGrip, this))

inline fun <T : Any> T.assert(condition: Boolean, body: () -> ((Grip, T) -> Boolean)) =
    assertTrue(body()(mockGrip, this) == condition)

inline fun <T : Any> T.assertAndVerify(condition: Boolean, body: () -> ((Grip, T) -> Boolean), verifier: T.() -> Unit) {
  assertTrue(body()(mockGrip, this) == condition)
  verify(this, only()).verifier()
  verifyNoMoreInteractions(this)
}
