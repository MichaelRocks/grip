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
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.DEFAULT_CONSTRUCTOR_TYPE
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.STATIC_INITIALIZER_NAME
import io.michaelrocks.grip.mirrors.STATIC_INITIALIZER_TYPE
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectType
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test

class ClassMatchersTest {
  private val classMirror = mock<ClassMirror>().apply {
    given(version).thenReturn(51)
    given(superType).thenReturn(getObjectType("LSuper;"))
    given(interfaces).thenReturn(listOf(getObjectType("Lio/michaelrocks/Interface;")))
    given(fields).thenReturn(listOf(FieldMirror.Builder().name("field").type(Type.Primitive.Int).build()))
    given(constructors).thenReturn(
        listOf(MethodMirror.Builder().name(CONSTRUCTOR_NAME).type(DEFAULT_CONSTRUCTOR_TYPE).build())
    )
    given(methods).thenReturn(
        listOf(MethodMirror.Builder().name(STATIC_INITIALIZER_NAME).type(STATIC_INITIALIZER_TYPE).build())
    )
  }

  private val interfaceMirror = mock<ClassMirror>().apply {
    given(version).thenReturn(51)
    given(superType).thenReturn(null)
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

  @Test fun testSuperNameTrue() = classMirror.testSuperName(true) { superType { _, _ -> true } }
  @Test fun testSuperNameFalse() = classMirror.testSuperName(false) { superType { _, _ -> false } }
  @Test fun testHasSuperNameTrue() = classMirror.testSuperName(true) { hasSuperType() }
  @Test fun testHasSuperNameFalse() = interfaceMirror.testSuperName(false) { hasSuperType() }

  @Test fun testInterfacesContainTrue() = classMirror.testInterfaces(true) {
    interfacesContain(getObjectTypeByInternalName("io/michaelrocks/Interface"))
  }
  @Test fun testInterfacesContainFalse() = classMirror.testInterfaces(false) {
    interfacesContain(getObjectTypeByInternalName("io/michaelrocks/AnotherInterface"))
  }
  @Test fun testInterfacesAreEmptyTrue() = interfaceMirror.testInterfaces(true) { interfacesAreEmpty() }
  @Test fun testInterfacesAreEmptyFalse() = classMirror.testInterfaces(false) { interfacesAreEmpty() }

  @Test fun testWithFieldTrue() = classMirror.testFields(true) { withField { _, _ -> true } }
  @Test fun testWithFieldFalse() = classMirror.testFields(false) { withField { _, _ -> false } }
  @Test fun testWithFieldEmpty() = interfaceMirror.testFields(false) { withField { _, _ -> true } }

  @Test fun testWithConstructorTrue() = classMirror.testConstructors(true) {
    withConstructor { _, _ -> true }
  }
  @Test fun testWithConstructorFalse() = classMirror.testConstructors(false) {
    withConstructor { _, _ -> false }
  }
  @Test fun testWithConstructorEmpty() = interfaceMirror.testConstructors(false) {
    withConstructor { _, _ -> true }
  }

  @Test fun testWithMethodTrue() = classMirror.testMethods(true) { withMethod { _, _ -> true } }
  @Test fun testWithMethodFalse() = classMirror.testMethods(false) { withMethod { _, _ -> false } }
  @Test fun testWithMethodEmpty() = interfaceMirror.testMethods(false) { withMethod { _, _ -> true } }

  private inline fun ClassMirror.testVersion(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { version }
  private inline fun ClassMirror.testSuperName(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { superType }
  private inline fun ClassMirror.testInterfaces(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { interfaces }
  private inline fun ClassMirror.testFields(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { fields }
  private inline fun ClassMirror.testConstructors(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { constructors }
  private inline fun ClassMirror.testMethods(condition: Boolean, body: () -> ((Grip, ClassMirror) -> Boolean)) =
      assertAndVerify(condition, body) { methods }
}
