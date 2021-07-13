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

import org.junit.Test

class StringMatchersTest {
  @Test fun testEqualsTrue() = "FinalClass".assertMatcher(true, equalsTo("FinalClass"))
  @Test fun testEqualsFalse() = "FinalClass".assertMatcher(false, equalsTo("AbstractClass"))
  @Test fun testMatchesTrue() = "FinalClass".assertMatcher(true, matches("^[a-zA-Z]+Class$".toRegex()))
  @Test fun testMatchesFalse() = "FinalClass".assertMatcher(false, matches("^[a-z]+Class$".toRegex()))
  @Test fun testStartsWithTrue() = "FinalClass".assertMatcher(true, startsWith("Final"))
  @Test fun testStartsWithFalse() = "FinalClass".assertMatcher(false, startsWith("Abstract"))
  @Test fun testEndsWithTrue() = "FinalClass".assertMatcher(true, endsWith("Class"))
  @Test fun testEndsWithFalse() = "FinalClass".assertMatcher(false, endsWith("Annotation"))
  @Test fun testContainsTrue() = "FinalClass".assertMatcher(true, contains("Cla"))
  @Test fun testContainsFalse() = "FinalClass".assertMatcher(false, contains("bus"))
}
