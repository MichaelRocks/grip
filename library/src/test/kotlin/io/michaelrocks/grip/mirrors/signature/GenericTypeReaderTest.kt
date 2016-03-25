package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.commons.getType
import io.michaelrocks.grip.mirrors.signature.GenericType.*
import io.michaelrocks.grip.mirrors.signature.GenericType.Array
import org.junit.Assert.assertEquals
import org.junit.Test
import org.objectweb.asm.Type

class GenericTypeReaderTest {
  @Test
  fun testRawType() {
    assertParsedSignatureEquals(
        "Ljava/lang/Boolean;",
        Raw(getType<Boolean>())
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
        Parameterized(getType<Map<*, *>>(), TypeVariable("K"), TypeVariable("V"))
    )
  }

  @Test
  fun testInnerType() {
    assertParsedSignatureEquals(
        "Ljava/util/Map<TK;TV;>.Entry<TK;TV;>;",
        Inner(
            Parameterized(Type.getObjectType("Entry"), TypeVariable("K"), TypeVariable("V")),
            Parameterized(getType<Map<*, *>>(), TypeVariable("K"), TypeVariable("V"))
        )
    )
  }

  @Test
  fun testUpperBoundedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<+TT;>;",
        Parameterized(
            getType<List<*>>(),
            UpperBounded(TypeVariable("T"))
        )
    )
  }

  @Test
  fun testLowerBoundedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<-TT;>;",
        Parameterized(
            getType<List<*>>(),
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
                    Parameterized(getType<List<*>>(), TypeVariable("T"))
                )
            )
        )
    )
  }

  @Test
  fun testParameterizedTypeWithArray() {
    assertParsedSignatureEquals(
        "Ljava/util/List<[TT;>;",
        Parameterized(getType<List<*>>(), Array(TypeVariable("T")))
    )
  }

  @Test
  fun testNestedParameterizedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<Ljava/util/List<Ljava/lang/Boolean;>;>;",
        Parameterized(getType<List<*>>(),
            Parameterized(getType<List<*>>(), Raw(getType<Boolean>()))
        )
    )
  }
}

private fun assertParsedSignatureEquals(signature: String, expected: GenericType) {
  val actual = readGenericType(signature)
  assertEquals(expected, actual)
}
