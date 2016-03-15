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

import io.michaelrocks.grip.mirrors.Element
import io.michaelrocks.mockito.RETURNS_MOCKS
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test
import org.objectweb.asm.Opcodes.*

class ElementMatchersTest {
  private val finalClass = mock<Element>(RETURNS_MOCKS).apply {
    given(access).thenReturn(ACC_PRIVATE or ACC_STATIC or ACC_FINAL or ACC_SUPER)
    given(name).thenReturn("FinalClass")
  }

  private val abstractClass = mock<Element>(RETURNS_MOCKS).apply {
    given(access).thenReturn(ACC_ABSTRACT or ACC_SUPER)
    given(name).thenReturn("AbstractClass")
  }

  private val enum = mock<Element>(RETURNS_MOCKS).apply {
    given(access).thenReturn(ACC_PROTECTED or ACC_FINAL or ACC_ENUM or ACC_SUPER)
    given(name).thenReturn("Enum")
  }

  private val annotation = mock<Element>(RETURNS_MOCKS).apply {
    given(access).thenReturn(ACC_PUBLIC or ACC_ANNOTATION or ACC_INTERFACE)
    given(name).thenReturn("Annotation")
  }

  @Test fun testAccessEqualsTrue() = abstractClass.testAccess(true) { access(ACC_ABSTRACT or ACC_SUPER) }
  @Test fun testAccessEqualsFalse() = finalClass.testAccess(false) { access(ACC_ABSTRACT or ACC_SUPER) }
  @Test fun testAccessHasAllOfTrue() = abstractClass.testAccess(true) { accessHasAllOf(ACC_ABSTRACT or ACC_SUPER) }
  @Test fun testAccessHasAllOfFalse() = abstractClass.testAccess(false) { accessHasAllOf(ACC_ABSTRACT or ACC_STATIC) }
  @Test fun testAccessHasAnyOfTrue() = abstractClass.testAccess(true) { accessHasAnyOf(ACC_ABSTRACT or ACC_STATIC) }
  @Test fun testAccessHasAnyOfFalse() = abstractClass.testAccess(false) { accessHasAnyOf(ACC_STATIC or ACC_FINAL) }
  @Test fun testAccessHasNoneOfTrue() = abstractClass.testAccess(true) { accessHasNoneOf(ACC_STATIC or ACC_FINAL) }
  @Test fun testAccessHasNoneOfFalse() = abstractClass.testAccess(false) { accessHasNoneOf(ACC_ABSTRACT or ACC_STATIC) }
  @Test fun testIsPublicTrue() = annotation.testAccess(true) { isPublic() }
  @Test fun testIsPublicFalse() = enum.testAccess(false) { isPublic() }
  @Test fun testIsProtectedTrue() = enum.testAccess(true) { isProtected() }
  @Test fun testIsProtectedFalse() = annotation.testAccess(false) { isProtected() }
  @Test fun testIsPrivateTrue() = finalClass.testAccess(true) { isPrivate() }
  @Test fun testIsPrivateFalse() = abstractClass.testAccess(false) { isPrivate() }
  @Test fun testIsPackagePrivateTrue() = abstractClass.testAccess(true) { isPackagePrivate() }
  @Test fun testIsPackagePrivateFalse() = finalClass.testAccess(false) { isPackagePrivate() }
  @Test fun testIsStaticTrue() = finalClass.testAccess(true) { isStatic() }
  @Test fun testIsStaticFalse() = abstractClass.testAccess(false) { isStatic() }
  @Test fun testIsFinalTrue() = finalClass.testAccess(true) { isFinal() }
  @Test fun testIsFinalFalse() = abstractClass.testAccess(false) { isFinal() }
  @Test fun testIsInterfaceTrue() = annotation.testAccess(true) { isInterface() }
  @Test fun testIsInterfaceFalse() = enum.testAccess(false) { isInterface() }
  @Test fun testIsAbstractTrue() = abstractClass.testAccess(true) { isAbstract() }
  @Test fun testIsAbstractFalse() = finalClass.testAccess(false) { isAbstract() }
  @Test fun testIsAnnotationTrue() = annotation.testAccess(true) { isAnnotation() }
  @Test fun testIsAnnotationFalse() = enum.testAccess(false) { isAnnotation() }
  @Test fun testIsEnumTrue() = enum.testAccess(true) { isEnum() }
  @Test fun testIsEnumFalse() = annotation.testAccess(false) { isEnum() }

  @Test fun testNameTrue() = finalClass.assertAndVerify(true, { name { true } }, { name })
  @Test fun testNameFalse() = finalClass.assertAndVerify(false, { name { false } }, { name })

  private inline fun Element.testAccess(condition: Boolean, body: () -> ((Element) -> Boolean)) =
      assertAndVerify(condition, body) { access }
}
