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

import io.michaelrocks.grip.mirrors.*
import io.michaelrocks.mockito.RETURNS_SMART_NULLS
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test
import org.objectweb.asm.Type

class ClassMatchersTest {
  private val classMirror = mock<ClassMirror>(RETURNS_SMART_NULLS).apply {
    given(version).thenReturn(51)
    given(superName).thenReturn("Super")
    given(interfaces).thenReturn(listOf(Type.getType("Lio/michaelrocks/Interface;")))
    given(fields).thenReturn(listOf(FieldMirror.Builder().name("field").type(Type.INT_TYPE).build()))
    given(constructors).thenReturn(
        listOf(MethodMirror.Builder().name(CONSTRUCTOR_NAME).type(DEFAULT_CONSTRUCTOR_TYPE).build())
    )
    given(methods).thenReturn(
        listOf(MethodMirror.Builder().name(STATIC_INITIALIZER_NAME).type(STATIC_INITIALIZER_TYPE).build())
    )
  }

  private val interfaceMirror = mock<ClassMirror>(RETURNS_SMART_NULLS).apply {
    given(version).thenReturn(51)
    given(superName).thenReturn(null)
  }

  @Test fun testVersionTrue() = classMirror.testVersion(true) { version(51) }
  @Test fun testVersionFalse() = classMirror.testVersion(false) { version(52) }
  @Test fun testVersionIsGreaterTrue() = classMirror.testVersion(true) { versionIsGreater(50) }
  @Test fun testVersionIsGreaterFalse() = classMirror.testVersion(false) { versionIsGreater(52) }
  @Test fun testVersionIsGreaterOrEqualTrue() = classMirror.testVersion(true) { versionIsGreaterOrEqual(51) }
  @Test fun testVersionIsGreaterOrEqualFalse() = classMirror.testVersion(false) { versionIsGreaterOrEqual(52) }
  @Test fun testVersionIsLowerTrue() = classMirror.testVersion(true) { versionIsLower(52) }
  @Test fun testVersionIsLowerFalse() = classMirror.testVersion(false) { versionIsLower(50) }
  @Test fun testVersionIsLowerOrEqualTrue() = classMirror.testVersion(true) { versionIsLowerOrEqual(51) }
  @Test fun testVersionIsLowerOrEqualFalse() = classMirror.testVersion(false) { versionIsLowerOrEqual(50) }

  @Test fun testSuperNameTrue() = classMirror.testSuperName(true) { superName { grip, name -> true } }
  @Test fun testSuperNameFalse() = classMirror.testSuperName(false) { superName { grip, name -> false } }
  @Test fun testHasSuperNameTrue() = classMirror.testSuperName(true) { hasSuperName() }
  @Test fun testHasSuperNameFalse() = interfaceMirror.testSuperName(false) { hasSuperName() }

  @Test fun testInterfacesContainTrue() = classMirror.testInterfaces(true) {
    interfacesContain(Type.getObjectType("io/michaelrocks/Interface"))
  }
  @Test fun testInterfacesContainFalse() = classMirror.testInterfaces(false) {
    interfacesContain(Type.getObjectType("io/michaelrocks/AnotherInterface"))
  }
  @Test fun testInterfacesAreEmptyTrue() = interfaceMirror.testInterfaces(true) { interfacesAreEmpty() }
  @Test fun testInterfacesAreEmptyFalse() = classMirror.testInterfaces(false) { interfacesAreEmpty() }

  @Test fun testWithFieldTrue() = classMirror.testFields(true) { withField { grip, field -> true } }
  @Test fun testWithFieldFalse() = classMirror.testFields(false) { withField { grip, field -> false } }
  @Test fun testWithFieldEmpty() = interfaceMirror.testFields(false) { withField { grip, field -> true } }

  @Test fun testWithConstructorTrue() = classMirror.testConstructors(true) {
    withConstructor { grip, method -> true }
  }
  @Test fun testWithConstructorFalse() = classMirror.testConstructors(false) {
    withConstructor { grip, method -> false }
  }
  @Test fun testWithConstructorEmpty() = interfaceMirror.testConstructors(false) {
    withConstructor { grip, method -> true }
  }

  @Test fun testWithMethodTrue() = classMirror.testMethods(true) { withMethod { grip, method -> true } }
  @Test fun testWithMethodFalse() = classMirror.testMethods(false) { withMethod { grip, method -> false } }
  @Test fun testWithMethodEmpty() = interfaceMirror.testMethods(false) { withMethod { grip, method -> true } }

  private inline fun ClassMirror.testVersion(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { version }
  private inline fun ClassMirror.testSuperName(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { superName }
  private inline fun ClassMirror.testInterfaces(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { interfaces }
  private inline fun ClassMirror.testFields(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { fields }
  private inline fun ClassMirror.testConstructors(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { constructors }
  private inline fun ClassMirror.testMethods(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { methods }
}
