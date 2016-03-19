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

import io.michaelrocks.grip.classes.*
import io.michaelrocks.grip.commons.getType
import io.michaelrocks.grip.mirrors.ReflectorImpl
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.Closeable
import java.io.File

class GripTest {
  private lateinit var grip: Grip

  @Before
  fun createGrip() {
    val fileRegistry = TestFileRegistry(
        Class1::class,
        Class2::class,
        Annotation1::class,
        Annotation2::class,
        Enum1::class
    )
    val reflector = ReflectorImpl()
    val classRegistry = ClassRegistryImpl(fileRegistry, reflector)
    grip = GripImpl(fileRegistry, classRegistry, Closeable {})
  }

  @Test
  fun testClasses() {
    val file = File("")
    assertClassesResultContains<Class1>(
        grip select classes from file where (name(contains("Class1")) and isPublic())
    )
    assertClassesResultContains<Class1>(
        grip select classes from file where (name(endsWith("Class1")) and isPublic())
    )
    assertClassesResultContains<Class2>(
        grip select classes from file where (not(name(endsWith("Class1") or startsWith("Class1"))) and not(isPackagePrivate()))
    )
    assertClassesResultContains<Class1>(
        grip select classes from file where (annotatedWith(getType<Annotation1>()))
    )
    assertClassesResultNotContains<Class1>(
        grip select classes from file where (annotatedWith(getType<Retention>()))
    )
  }

  @Test
  fun testMethods() {
    val file = File("")
    val classes = grip select classes from file where name(contains("Class"))
    val methods = grip select methods from classes where (not(isStatic()) and not(isConstructor()))
    assertEquals(1, methods.execute()[getType<Class1>()]!!.size)
    assertEquals(1, methods.execute()[getType<Class2>()]!!.size)
  }

  private inline fun <reified T : Any> assertClassesResultContains(query: Query<ClassesResult>) {
    val result = query.execute()
    val type = getType<T>()
    assert(result.classes.any { it.type == type })
  }

  private inline fun <reified T : Any> assertClassesResultNotContains(query: Query<ClassesResult>) {
    val result = query.execute()
    val type = getType<T>()
    assert(!result.classes.any { it.type == type })
  }

  private fun assertClassesResultIsEmpty(query: Query<ClassesResult>) {
    val result = query.execute()
    assertTrue(result.classes.isEmpty())
  }

  private fun assertClassesResultIsNotEmpty(query: Query<ClassesResult>) {
    val result = query.execute()
    assertFalse(result.classes.isEmpty())
  }
}
