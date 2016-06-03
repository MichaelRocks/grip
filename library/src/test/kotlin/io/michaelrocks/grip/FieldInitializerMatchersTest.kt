package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.mockito.RETURNS_SMART_NULLS
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test

class FieldInitializerMatchersTest {
  private val stringValueField = mock<FieldMirror>(RETURNS_SMART_NULLS).apply {
    given(value).thenReturn("String")
  }
  private val intValueField = mock<FieldMirror>(RETURNS_SMART_NULLS).apply {
    given(value).thenReturn(42)
  }
  private val nullValueField = mock<FieldMirror>()

  @Test fun testWithFieldInitializerTrue() = stringValueField.testFieldInitializer(true) {
    withFieldInitializer { grip, value -> true }
  }
  @Test fun testWithFieldInitializerFalse() = stringValueField.testFieldInitializer(false) {
    withFieldInitializer { grip, value -> false }
  }

  @Test fun testWithFieldInitializerStringTrue() = stringValueField.testFieldInitializer(true) {
    withFieldInitializer<String>()
  }
  @Test fun testWithFieldInitializerStringFalse() = nullValueField.testFieldInitializer(false) {
    withFieldInitializer<String>()
  }
  @Test fun testWithFieldInitializerIntTrue() = intValueField.testFieldInitializer(true) {
    withFieldInitializer<Int>()
  }
  @Test fun testWithFieldInitializerIntFalse() = stringValueField.testFieldInitializer(false) {
    withFieldInitializer<Int>()
  }

  @Test fun testHasFieldInitializerTrue() = stringValueField.testFieldInitializer(true) { hasFieldInitializer() }
  @Test fun testHasFieldInitializerFalse() = nullValueField.testFieldInitializer(false) { hasFieldInitializer() }

  private inline fun FieldMirror.testFieldInitializer(condition: Boolean, body: () -> ((Grip, FieldMirror) -> Boolean)) =
      assertAndVerify(condition, body) { value }
}