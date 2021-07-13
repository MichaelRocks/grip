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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassMirrorTest {
  @Test
  fun testTopLevelClass() {
    val type = getObjectTypeByInternalName("io/michaelrocks/test/Test")
    val mirror = ClassMirror.Builder().run {
      name(type.internalName)
      addInnerClass(getObjectTypeByInternalName("io/michaelrocks/test/Test\$Nested"), type, "Nested", 0)
      addInnerClass(getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local"), null, "Local", 0)
      addInnerClass(getObjectTypeByInternalName("io/michaelrocks/test/Test\$1"), null, null, 0)
      build()
    }

    assertEquals("io.michaelrocks.test.Test", mirror.name)
    assertEquals("Test", mirror.simpleName)
    assertEquals(3, mirror.types.size)
    assertTrue(mirror.types.contains(getObjectTypeByInternalName("io/michaelrocks/test/Test\$Nested")))
    assertTrue(mirror.types.contains(getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local")))
    assertTrue(mirror.types.contains(getObjectTypeByInternalName("io/michaelrocks/test/Test\$1")))
  }

  @Test
  fun testDeeplyNestedClass() {
    val outerType = getObjectTypeByInternalName("io/michaelrocks/test/Test")
    val nested1Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$Nested1")
    val nested2Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$Nested1\$Nested2")
    val nested3Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$Nested1\$Nested2\$Nested3")
    val mirror = ClassMirror.Builder().run {
      name(nested3Type.internalName)
      addInnerClass(nested3Type, nested2Type, "Nested3", 0)
      addInnerClass(nested2Type, nested1Type, "Nested2", 0)
      addInnerClass(nested1Type, outerType, "Nested1", 0)
      addInnerClass(getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local"), null, "Local", 0)
      build()
    }

    assertEquals("io.michaelrocks.test.Test.Nested1.Nested2.Nested3", mirror.name)
    assertEquals("Nested3", mirror.simpleName)
    assertEquals(0, mirror.types.size)
  }

  @Test
  fun testNestedInLocalClass() {
    val localType = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local")
    val nested1Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local\$Nested1")
    val nested2Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local\$Nested2")
    val mirror = ClassMirror.Builder().run {
      name(nested2Type.internalName)
      addInnerClass(nested2Type, nested1Type, "Nested2", 0)
      addInnerClass(nested1Type, localType, "Nested1", 0)
      addInnerClass(localType, null, "Local", 0)
      build()
    }

    assertEquals("io.michaelrocks.test.Test\$1Local.Nested1.Nested2", mirror.name)
    assertEquals("Nested2", mirror.simpleName)
    assertEquals(0, mirror.types.size)
  }

  @Test
  fun testLocalClass() {
    val outerType = getObjectTypeByInternalName("io/michaelrocks/test/Test")
    val localType = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local")
    val nested1Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1Local\$Nested1")
    val mirror = ClassMirror.Builder().run {
      name(localType.internalName)
      addInnerClass(nested1Type, localType, "Nested1", 0)
      addInnerClass(localType, null, "Local", 0)
      enclosure(Enclosure.Method.Named(outerType, "method", getMethodType(Type.Primitive.Void)))
      build()
    }

    assertEquals("io.michaelrocks.test.Test\$1Local", mirror.name)
    assertEquals("Local", mirror.simpleName)
    assertEquals(1, mirror.types.size)
    assertTrue(mirror.types.contains(nested1Type))
  }

  @Test
  fun testNestedInAnonymousClass() {
    val outerType = getObjectTypeByInternalName("io/michaelrocks/test/Test")
    val anonymousType = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1")
    val nested1Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1\$1Nested")
    val mirror = ClassMirror.Builder().run {
      name(nested1Type.internalName)
      addInnerClass(nested1Type, null, "Nested", 0)
      addInnerClass(anonymousType, null, null, 0)
      enclosure(Enclosure.Method.Named(outerType, "method", getMethodType(Type.Primitive.Void)))
      build()
    }

    assertEquals("io.michaelrocks.test.Test\$1\$1Nested", mirror.name)
    assertEquals("Nested", mirror.simpleName)
    assertEquals(0, mirror.types.size)
  }

  @Test
  fun testAnonymousClass() {
    val anonymousType = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1")
    val nested1Type = getObjectTypeByInternalName("io/michaelrocks/test/Test\$1\$1Nested1")
    val mirror = ClassMirror.Builder().run {
      name(anonymousType.internalName)
      addInnerClass(nested1Type, null, "Nested1", 0)
      addInnerClass(anonymousType, null, null, 0)
      enclosure(Enclosure.Method.Anonymous(anonymousType))
      build()
    }

    assertEquals("io.michaelrocks.test.Test\$1", mirror.name)
    assertEquals("", mirror.simpleName)
    assertEquals(1, mirror.types.size)
    assertTrue(mirror.types.contains(nested1Type))
  }

  private fun ClassMirror.Builder.addInnerClass(
      type: Type.Object,
      outerType: Type.Object?,
      innerName: String?,
      access: Int
  ) {
    addInnerClass(InnerClass(type, outerType, innerName, access))
  }
}
