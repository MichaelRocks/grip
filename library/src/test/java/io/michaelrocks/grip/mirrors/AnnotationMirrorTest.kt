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

package io.michaelrocks.grip.mirrors

import io.michaelrocks.grip.annotations.createAnnotationMirror
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AnnotationMirrorTest {
  @Test
  fun testEqualsWithIntArrays() {
    val annotation1 = createAnnotationMirror("EqualsWithIntArrays", intArrayOf(42, 43, 44))
    val annotation2 = createAnnotationMirror("EqualsWithIntArrays", intArrayOf(42, 43, 44))
    assertEquals(1, annotation1.values.size)
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode(), annotation2.hashCode())
  }

  @Test
  fun testEqualsWithAnnotationArrays() {
    val annotation1 = createAnnotationMirror(
      "EqualsWithAnnotationArrays",
      arrayOf(
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(48, 49, 50))
      )
    )
    val annotation2 = createAnnotationMirror(
      "EqualsWithAnnotationArrays",
      arrayOf(
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(48, 49, 50))
      )
    )
    assertEquals(1, annotation1.values.size)
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode(), annotation2.hashCode())
  }

  @Test
  fun testEqualsWithDifferentOrder() {
    val values1 = hashMapOf(
      "intValue" to 42,
      "stringValue" to "42"
    )
    val annotation1 = createAnnotationMirror("EqualsWithAnnotationArrays", values1)
    val values2 = hashMapOf(
      "intValue" to 42,
      "stringValue" to "42"
    )
    val annotation2 = createAnnotationMirror("EqualsWithAnnotationArrays", values2)
    assertEquals(2, annotation1.values.size)
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode(), annotation2.hashCode())
  }

  @Test
  fun testNotEqualsToNull() {
    val annotation = createAnnotationMirror("NotEqualsToNull")
    @Suppress("SENSELESS_COMPARISON")
    assertFalse(annotation == null)
  }

  @Test
  fun testNotEqualsToString() {
    val annotation = createAnnotationMirror("NotEqualsToString")
    assertNotEquals("NotEqualsToString", annotation)
  }

  @Test
  fun testNotEqualsByType() {
    val annotation1 = createAnnotationMirror("NotEqualsByType1")
    val annotation2 = createAnnotationMirror("NotEqualsByType2")
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  fun testNotEqualsByVisibility() {
    val annotation1 = createAnnotationMirror("NotEqualsByVisibility", visible = true)
    val annotation2 = createAnnotationMirror("NotEqualsByVisibility", visible = false)
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  fun testNotEqualsWithStrings() {
    val annotation1 = createAnnotationMirror("NotEqualsWithStrings", "Value1")
    val annotation2 = createAnnotationMirror("NotEqualsWithStrings", "Value2")
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  fun testNotEqualsWithIntArrays() {
    val annotation1 = createAnnotationMirror("NotEqualsWithIntArrays", intArrayOf(42))
    val annotation2 = createAnnotationMirror("NotEqualsWithIntArrays", intArrayOf(-42))
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  fun testNotEqualsWithAnnotationArrays() {
    val annotation1 = createAnnotationMirror(
      "NotEqualsWithAnnotationArrays",
      arrayOf(
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(48, 49, 50)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(1))
      )
    )
    val annotation2 = createAnnotationMirror(
      "NotEqualsWithAnnotationArrays",
      arrayOf(
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(48, 49, 50)),
        createAnnotationMirror("EqualsWithIntArrays", intArrayOf(-1))
      )
    )
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  fun testToString() {
    val nameUuid = UUID.randomUUID()
    val name = "ToString%016x%016x".format(nameUuid.mostSignificantBits, nameUuid.leastSignificantBits)
    val value = UUID.randomUUID().toString()
    val annotation = createAnnotationMirror(name, value)
    val annotationDescription = annotation.toString()
    assertNotNull(annotationDescription)
    assertTrue(annotationDescription.contains(name))
    assertTrue(annotationDescription.contains(value))
  }
}
