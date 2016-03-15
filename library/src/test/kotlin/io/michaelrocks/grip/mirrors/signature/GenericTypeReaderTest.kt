package io.michaelrocks.grip.mirrors.signature

import io.michaelrocks.grip.commons.getType
import io.michaelrocks.grip.mirrors.signature.GenericType.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.objectweb.asm.Type

class GenericTypeReaderTest {
  @Test
  fun testRawType() {
    assertParsedSignatureEquals(
        "Ljava/lang/Boolean;",
        RawType(getType<Boolean>())
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
        GenericArrayType(TypeVariable("T"))
    )
  }

  @Test
  fun testParameterizedType() {
    assertParsedSignatureEquals(
        "Ljava/util/Map<TK;TV;>;",
        ParameterizedType(getType<Map<*, *>>(), TypeVariable("K"), TypeVariable("V"))
    )
  }

  @Test
  fun testInnerType() {
    assertParsedSignatureEquals(
        "Ljava/util/Map<TK;TV;>.Entry<TK;TV;>;",
        InnerType(
            ParameterizedType(Type.getObjectType("Entry"), TypeVariable("K"), TypeVariable("V")),
            ParameterizedType(getType<Map<*, *>>(), TypeVariable("K"), TypeVariable("V"))
        )
    )
  }

  @Test
  fun testUpperBoundedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<+TT;>;",
        ParameterizedType(
            getType<List<*>>(),
            UpperBoundedType(TypeVariable("T"))
        )
    )
  }

  @Test
  fun testLowerBoundedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<-TT;>;",
        ParameterizedType(
            getType<List<*>>(),
            LowerBoundedType(TypeVariable("T"))
        )
    )
  }

  @Test
  fun testMultiDimensionalArray() {
    assertParsedSignatureEquals(
        "[[[Ljava/util/List<TT;>;",
        GenericArrayType(
            GenericArrayType(
                GenericArrayType(
                    ParameterizedType(getType<List<*>>(), TypeVariable("T"))
                )
            )
        )
    )
  }

  @Test
  fun testParameterizedTypeWithArray() {
    assertParsedSignatureEquals(
        "Ljava/util/List<[TT;>;",
        ParameterizedType(getType<List<*>>(), GenericArrayType(TypeVariable("T")))
    )
  }

  @Test
  fun testNestedParameterizedType() {
    assertParsedSignatureEquals(
        "Ljava/util/List<Ljava/util/List<Ljava/lang/Boolean;>;>;",
        ParameterizedType(getType<List<*>>(),
            ParameterizedType(getType<List<*>>(), RawType(getType<Boolean>()))
        )
    )
  }
}

private fun assertParsedSignatureEquals(signature: String, expected: GenericType) {
  val actual = readGenericType(signature)
  assertEquals(expected, actual)
}
