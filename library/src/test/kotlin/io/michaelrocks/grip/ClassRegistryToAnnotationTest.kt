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

import io.michaelrocks.grip.annotations.AnnotationGenerator
import io.michaelrocks.grip.annotations.createAnnotationMirror
import io.michaelrocks.grip.annotations.getAnnotationType
import io.michaelrocks.grip.commons.getType
import io.michaelrocks.grip.mirrors.EnumMirror
import io.michaelrocks.grip.mirrors.ReflectorImpl
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import io.michaelrocks.mockito.notNull
import org.junit.Assert.*
import org.junit.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type

class ClassRegistryToAnnotationTest {
  @Test
  fun testEmptyAnnotation() {
    val annotationType = Type.getObjectType("EmptyAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType)
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertTrue(actualAnnotation.values.isEmpty())
  }

  @Test
  fun testExplicitValueAnnotation() {
    val annotationType = Type.getObjectType("ExplicitValueAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("explicitValue", getType<String>())
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertTrue(actualAnnotation.values.isEmpty())
  }

  @Test
  fun testImplicitValueAnnotation() {
    val annotationType = Type.getObjectType("ImplicitValueAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("implicitValue", getType<String>(), "defaultImplicitValue")
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals("defaultImplicitValue", actualAnnotation.values["implicitValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testExplicitAndImplicitValuesAnnotation() {
    val annotationType = Type.getObjectType("ExplicitAndImplicitValuesAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("explicitValue", getType<String>())
          addMethod("implicitValue", getType<String>(), "defaultImplicitValue")
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals("defaultImplicitValue", actualAnnotation.values["implicitValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testSimpleValuesAnnotation() {
    val annotationType = Type.getObjectType("PrimitiveValuesAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("booleanValue", Type.BOOLEAN_TYPE, true)
          addMethod("byteValue", Type.BYTE_TYPE, 42.toByte())
          addMethod("charValue", Type.CHAR_TYPE, 'x')
          addMethod("floatValue", Type.FLOAT_TYPE, Math.E.toFloat())
          addMethod("doubleValue", Type.DOUBLE_TYPE, Math.PI)
          addMethod("intValue", Type.INT_TYPE, 42)
          addMethod("longValue", Type.LONG_TYPE, 42L)
          addMethod("shortValue", Type.SHORT_TYPE, 42.toShort())
          addMethod("stringValue", getType<String>(), "x")
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(true, actualAnnotation.values["booleanValue"])
    assertEquals(42.toByte(), actualAnnotation.values["byteValue"])
    assertEquals('x', actualAnnotation.values["charValue"])
    assertEquals(Math.E.toFloat(), actualAnnotation.values["floatValue"] as Float, 0f)
    assertEquals(Math.PI, actualAnnotation.values["doubleValue"] as Double, 0.0)
    assertEquals(42, actualAnnotation.values["intValue"])
    assertEquals(42L, actualAnnotation.values["longValue"])
    assertEquals(42.toShort(), actualAnnotation.values["shortValue"])
    assertEquals("x", actualAnnotation.values["stringValue"])
    assertEquals(9, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testArrayValuesAnnotation() {
    val annotationType = Type.getObjectType("ArrayValuesAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("booleanArrayValue", getType<BooleanArray>(), booleanArrayOf(true, false, true))
          addMethod("byteArrayValue", getType<ByteArray>(), byteArrayOf(42, 43, 44))
          addMethod("charArrayValue", getType<CharArray>(), charArrayOf('x', 'y', 'z'))
          addMethod("floatArrayValue", getType<FloatArray>(), floatArrayOf(42f, 43f, 44f))
          addMethod("doubleArrayValue", getType<DoubleArray>(), doubleArrayOf(42.0, 43.0, 44.0))
          addMethod("intArrayValue", getType<IntArray>(), intArrayOf(42, 43, 44))
          addMethod("longArrayValue", getType<LongArray>(), longArrayOf(42, 43, 44))
          addMethod("shortArrayValue", getType<ShortArray>(), shortArrayOf(42, 43, 44))
          addMethod("stringArrayValue", getType<Array<String>>(), arrayOf("x", "y", "z"))
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertArrayEquals(booleanArrayOf(true, false, true), actualAnnotation.values["booleanArrayValue"] as BooleanArray)
    assertArrayEquals(byteArrayOf(42, 43, 44), actualAnnotation.values["byteArrayValue"] as ByteArray)
    assertArrayEquals(charArrayOf('x', 'y', 'z'), actualAnnotation.values["charArrayValue"] as CharArray)
    assertArrayEquals(floatArrayOf(42f, 43f, 44f), actualAnnotation.values["floatArrayValue"] as FloatArray, 0f)
    assertArrayEquals(doubleArrayOf(42.0, 43.0, 44.0), actualAnnotation.values["doubleArrayValue"] as DoubleArray, 0.0)
    assertArrayEquals(intArrayOf(42, 43, 44), actualAnnotation.values["intArrayValue"] as IntArray)
    assertArrayEquals(longArrayOf(42, 43, 44), actualAnnotation.values["longArrayValue"] as LongArray)
    assertArrayEquals(shortArrayOf(42, 43, 44), actualAnnotation.values["shortArrayValue"] as ShortArray)
    @Suppress("UNCHECKED_CAST")
    assertEquals(listOf("x", "y", "z"), actualAnnotation.values["stringArrayValue"])
    assertEquals(9, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testEnumAnnotation() {
    val enumType = Type.getObjectType("TestEnum")
    val enumValue = EnumMirror(enumType, "TEST")
    val annotationType = Type.getObjectType("EnumAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("enumValue", enumType, enumValue)
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(enumValue, actualAnnotation.values["enumValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testEnumArrayAnnotation() {
    val enumType = Type.getObjectType("TestEnum")
    val enumArrayType = Type.getType("[${enumType.descriptor}")
    val enumValues = arrayOf(
        EnumMirror(enumType, "TEST1"),
        EnumMirror(enumType, "TEST2"),
        EnumMirror(enumType, "TEST3")
    )
    val annotationType = Type.getObjectType("testEnumArrayAnnotation")
    val classRegistry = createClassRegistry(
        annotationType to generateAnnotation(annotationType) {
          addMethod("enumArrayValue", enumArrayType, enumValues)
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(listOf(*enumValues), actualAnnotation.values["enumArrayValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testNestedAnnotationAnnotation() {
    val nestedAnnotationType = getAnnotationType("NestedAnnotation")
    val nestedAnnotation = createAnnotationMirror("NestedAnnotation", "Nested")
    val annotationType = Type.getObjectType("NestedAnnotationAnnotation")
    val classRegistry = createClassRegistry(
        nestedAnnotationType to generateAnnotation(nestedAnnotationType),
        annotationType to generateAnnotation(annotationType) {
          addMethod("annotationValue", nestedAnnotation.type, nestedAnnotation)
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(nestedAnnotation, actualAnnotation.values["annotationValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testNestedAnnotationArrayAnnotation() {
    val nestedAnnotationType = getAnnotationType("NestedAnnotation")
    val nestedAnnotations = arrayOf(
        createAnnotationMirror("NestedAnnotation", "Nested1"),
        createAnnotationMirror("NestedAnnotation", "Nested2"),
        createAnnotationMirror("NestedAnnotation", "Nested3")
    )
    val annotationArrayType = Type.getType("[${nestedAnnotations[0].type.descriptor}")
    val annotationType = Type.getObjectType("NestedAnnotationArrayAnnotation")
    val classRegistry = createClassRegistry(
        nestedAnnotationType to generateAnnotation(nestedAnnotationType),
        annotationType to generateAnnotation(annotationType) {
          addMethod("annotationArrayValue", annotationArrayType, nestedAnnotations)
        }
    )
    val actualAnnotation = classRegistry.getAnnotationMirror(annotationType)
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(listOf(*nestedAnnotations), actualAnnotation.values["annotationArrayValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  private fun generateAnnotation(type: Type): ByteArray =
      generateAnnotation(type) {}

  private inline fun generateAnnotation(type: Type, builder: AnnotationGenerator.() -> Unit): ByteArray =
      ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).let { writer ->
        AnnotationGenerator
            .create(writer, type)
            .apply { builder() }
            .generate()
        writer.toByteArray()
      }

  private fun createClassRegistry(vararg entries: Pair<Type, ByteArray>): ClassRegistry =
      ClassRegistryImpl(createFileRegistry(*entries), ReflectorImpl())

  private fun createFileRegistry(vararg entries: Pair<Type, ByteArray>): FileRegistry =
      mock<FileRegistry>().apply {
        given(findTypesForFile(notNull())).thenReturn(entries.map { it.first })
        for ((type, data) in entries) {
          given(readClass(type)).thenReturn(data)
        }
      }
}
