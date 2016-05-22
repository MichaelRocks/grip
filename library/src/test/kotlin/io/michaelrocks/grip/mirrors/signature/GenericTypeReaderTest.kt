package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.mirrors.getObjectType
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.mirrors.signature.GenericType.Array
import io.michaelrocks.grip.mirrors.signature.GenericType.Inner
import io.michaelrocks.grip.mirrors.signature.GenericType.LowerBounded
import io.michaelrocks.grip.mirrors.signature.GenericType.Parameterized
import io.michaelrocks.grip.mirrors.signature.GenericType.Raw
import io.michaelrocks.grip.mirrors.signature.GenericType.TypeVariable
import io.michaelrocks.grip.mirrors.signature.GenericType.UpperBounded
import org.junit.Assert.assertEquals
import org.junit.Test

class GenericTypeReaderTest {
  @Test
  fun testRawType() {
    assertParsedSignatureEquals(
        "Ljava/lang/Boolean;",
        Raw(getObjectType<Boolean>())
    )
  }

  @Test
  fun testTypeVariable() {
    assertParsedSignatureEquals(
        "TT;",
        TypeVariable("T")
    )
  }

  @Test
  fun testGenericArray() {
    assertParsedSignatureEquals(
        "[TT;",
        Array(TypeVariable("T"))
    )
  }

  @Test
  fun testParameterizedType() {
    assertParsedSignatureEquals(
        "Ljava/util/Map<TK;TV;>;",
        Parameterized(getObjectType<Map<*, *>>(), TypeVariable("K"), TypeVariable("V"))
    )
  }

  @Test
  fun testInnerType() {
    assertParsedSignatureEquals(
        "Ljava/util/Map<TK;TV;>.Entry<TK;TV;>;",
        Inner(
            Parameterized(getObjectTypeByInternalName("Entry"), TypeVariable("K"), TypeVariable("V")),
            Parameterized(getObjectType<Map<*, *>>(), TypeVariable("K"), TypeVariable("V"))
        )
    )
  }

  @Test
  fun testUpperBoundedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<+TT;>;",
        Parameterized(
            getObjectType<List<*>>(),
            UpperBounded(TypeVariable("T"))
        )
    )
  }

  @Test
  fun testLowerBoundedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<-TT;>;",
        Parameterized(
            getObjectType<List<*>>(),
            LowerBounded(TypeVariable("T"))
        )
    )
  }

  @Test
  fun testMultiDimensionalArray() {
    assertParsedSignatureEquals(
        "[[[Ljava/util/List<TT;>;",
        Array(
            Array(
                Array(
                    Parameterized(getObjectType<List<*>>(), TypeVariable("T"))
                )
            )
        )
    )
  }

  @Test
  fun testParameterizedTypeWithArray() {
    assertParsedSignatureEquals(
        "Ljava/util/List<[TT;>;",
        Parameterized(getObjectType<List<*>>(), Array(TypeVariable("T")))
    )
  }

  @Test
  fun testNestedParameterizedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<Ljava/util/List<Ljava/lang/Boolean;>;>;",
        Parameterized(getObjectType<List<*>>(),
            Parameterized(getObjectType<List<*>>(), Raw(getObjectType<Boolean>()))
        )
    )
  }
}

private fun assertParsedSignatureEquals(signature: String, expected: GenericType) {
  val actual = readGenericType(signature)
  assertEquals(expected, actual)
}
