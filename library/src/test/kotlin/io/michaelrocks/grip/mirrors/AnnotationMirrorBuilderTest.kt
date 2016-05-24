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

package io.michaelrocks.grip.mirrors

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnotationMirrorBuilderTest {
  @Test
  fun testEmptyAnnotation() {
    val annotation = newAnnotationBuilder("EmptyAnnotation").build()
    assertEquals("EmptyAnnotation", annotation.type.internalName)
    assertTrue(annotation.values.isEmpty())
  }

  @Test
  fun testDefaultValueAnnotation() {
    val annotation = newAnnotationBuilder("DefaultValueAnnotation").addValue("DefaultValue").build()
    assertEquals("DefaultValueAnnotation", annotation.type.internalName)
    assertEquals(1, annotation.values.size.toLong())
    assertEquals("DefaultValue", annotation.values["value"])
  }

  @Test
  fun testNamedValueAnnotation() {
    val annotation = newAnnotationBuilder("NamedValueAnnotation").addValue("namedValue", "NamedValue").build()
    assertEquals("NamedValueAnnotation", annotation.type.internalName)
    assertEquals(1, annotation.values.size.toLong())
    assertEquals("NamedValue", annotation.values["namedValue"])
  }

  @Test
  fun testVariousValuesAnnotation() {
    val innerAnnotation = newAnnotationBuilder("InnerAnnotation").build()
    val annotation = newAnnotationBuilder("VariousValuesAnnotation").run {
      addValue("booleanValue", true)
      addValue("byteValue", 42.toByte())
      addValue("charValue", 'x')
      addValue("floatValue", Math.E.toFloat())
      addValue("doubleValue", Math.PI)
      addValue("intValue", 42)
      addValue("longValue", 42L)
      addValue("shortValue", 42.toShort())
      addValue("stringValue", "x")
      addValue("annotationValue", innerAnnotation)
      addValue("booleanArrayValue", booleanArrayOf(true))
      addValue("byteArrayValue", byteArrayOf(42.toByte()))
      addValue("charArrayValue", charArrayOf('x'))
      addValue("floatArrayValue", floatArrayOf(Math.E.toFloat()))
      addValue("doubleArrayValue", doubleArrayOf(Math.PI))
      addValue("intArrayValue", intArrayOf(42))
      addValue("longArrayValue", longArrayOf(42L))
      addValue("shortArrayValue", shortArrayOf(42.toShort()))
      addValue("stringArrayValue", arrayOf("x"))
      addValue("annotationArrayValue", arrayOf(innerAnnotation)).build()
    }
    assertEquals("VariousValuesAnnotation", annotation.type.internalName)
    assertEquals(20, annotation.values.size.toLong())
    assertEquals(true, annotation.values["booleanValue"])
    assertEquals(42.toByte(), annotation.values["byteValue"])
    assertEquals('x', annotation.values["charValue"])
    assertEquals(Math.E.toFloat(), annotation.values["floatValue"])
    assertEquals(Math.PI, annotation.values["doubleValue"])
    assertEquals(42, annotation.values["intValue"])
    assertEquals(42L, annotation.values["longValue"])
    assertEquals(42.toShort(), annotation.values["shortValue"])
    assertEquals("x", annotation.values["stringValue"])
    assertEquals(innerAnnotation, annotation.values["annotationValue"])
    assertArrayEquals(booleanArrayOf(true), annotation.values["booleanArrayValue"] as BooleanArray)
    assertArrayEquals(byteArrayOf(42.toByte()), annotation.values["byteArrayValue"] as ByteArray)
    assertArrayEquals(charArrayOf('x'), annotation.values["charArrayValue"] as CharArray)
    assertArrayEquals(floatArrayOf(Math.E.toFloat()), annotation.values["floatArrayValue"] as FloatArray, 0f)
    assertArrayEquals(doubleArrayOf(Math.PI), annotation.values["doubleArrayValue"] as DoubleArray, 0.0)
    assertArrayEquals(intArrayOf(42), annotation.values["intArrayValue"] as IntArray)
    assertArrayEquals(longArrayOf(42L), annotation.values["longArrayValue"] as LongArray)
    assertArrayEquals(shortArrayOf(42.toShort()), annotation.values["shortArrayValue"] as ShortArray)
    @Suppress("UNCHECKED_CAST")
    assertArrayEquals(arrayOf("x"), annotation.values["stringArrayValue"] as Array<String>)
    @Suppress("UNCHECKED_CAST")
    assertArrayEquals(arrayOf(innerAnnotation), annotation.values["annotationArrayValue"] as Array<Any>)
  }

  @Test
  fun testResolvedAnnotation() {
    val annotation = newAnnotationBuilder("ResolvedAnnotation").build()
    assertEquals("ResolvedAnnotation", annotation.type.internalName)
    assertTrue(annotation.values.isEmpty())
  }

  private fun newAnnotationBuilder(annotationName: String): AnnotationMirror.Builder {
    val type = getObjectTypeByInternalName(annotationName)
    return AnnotationMirror.Builder().type(type)
  }
}
