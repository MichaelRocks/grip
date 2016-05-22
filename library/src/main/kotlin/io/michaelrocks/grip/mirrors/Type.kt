package io.michaelrocks.grip.mirrors

import io.michaelrocks.grip.commons.mapToArray
import kotlin.reflect.KClass
import org.objectweb.asm.Type as AsmType

// TODO: Consider caching AsmType to Type conversion in properties
sealed class Type private constructor(internal val type: AsmType) {
  val descriptor: String
    get() = type.descriptor
  open val internalName: String
    get() = type.internalName
  open val className: String
    get() = type.className

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    val that = other as Type? ?: return false
    return type == that.type
  }

  override fun hashCode(): Int =
      type.hashCode()

  override fun toString(): String =
      type.toString()

  sealed class Primitive(type: AsmType) : Type(type) {
    override val internalName: String
      get() = throw UnsupportedOperationException("Cannot get internalName for a primitive type")

    object Void : Primitive(AsmType.VOID_TYPE)
    object Boolean : Primitive(AsmType.BOOLEAN_TYPE)
    object Char : Primitive(AsmType.CHAR_TYPE)
    object Byte : Primitive(AsmType.BYTE_TYPE)
    object Short : Primitive(AsmType.SHORT_TYPE)
    object Int : Primitive(AsmType.INT_TYPE)
    object Float : Primitive(AsmType.FLOAT_TYPE)
    object Long : Primitive(AsmType.LONG_TYPE)
    object Double : Primitive(AsmType.DOUBLE_TYPE)
  }

  class Array(type: AsmType) : Type(type) {
    val elementType: Type
      get() = type.elementType.toType()
    val dimensions: Int
      get() = type.dimensions
  }

  class Object(type: AsmType) : Type(type)

  class Method(type: AsmType) : Type(type) {
    override val internalName: String
      get() = throw UnsupportedOperationException("Cannot get internalName for a method type")
    override val className: String
      get() = throw UnsupportedOperationException("Cannot get javaName for a method type")

    val returnType: Type
      get() = type.returnType.toType()
    val argumentTypes: List<Type>
      get() = type.argumentTypes.map { it.toType() }
  }
}

val Type.isPrimitive: Boolean
  get() = this is Type.Primitive
val Type.isArray: Boolean
  get() = this is Type.Array
val Type.isObject: Boolean
  get() = this is Type.Object
val Type.isMethod: Boolean
  get() = this is Type.Method

val Type.Object.packageName: String
  get() = internalName.substringBeforeLast('/', "")

val Class<*>.internalName: String
  get() = AsmType.getInternalName(this)
val Class<*>.descriptor: String
  get() = AsmType.getDescriptor(this)

val KClass<*>.internalName: String
  get() = java.internalName
val KClass<*>.descriptor: String
  get() = java.descriptor

fun Type.toAsmType(): AsmType = type

fun AsmType.toType(): Type =
    when (sort) {
      AsmType.VOID -> Type.Primitive.Void
      AsmType.BOOLEAN -> Type.Primitive.Boolean
      AsmType.CHAR -> Type.Primitive.Char
      AsmType.BYTE -> Type.Primitive.Byte
      AsmType.SHORT -> Type.Primitive.Short
      AsmType.INT -> Type.Primitive.Int
      AsmType.FLOAT -> Type.Primitive.Float
      AsmType.LONG -> Type.Primitive.Long
      AsmType.DOUBLE -> Type.Primitive.Double
      AsmType.ARRAY -> Type.Array(this)
      AsmType.OBJECT -> Type.Object(this)
      AsmType.METHOD -> Type.Method(this)
      else -> throw IllegalArgumentException("Unsupported ASM type $this")
    }

fun Class<*>.toType(): Type = getType(this)
fun KClass<*>.toType(): Type = getType(this)

inline fun <reified C : Any, reified T : Type> getTypeAs(): T =
    getTypeAs(C::class.java)
inline fun <reified T : Type> getTypeAs(type: Class<*>): T =
    AsmType.getType(type).toType() as T
inline fun <reified T : Type> getTypeAs(type: KClass<*>): T =
    AsmType.getType(type.java).toType() as T
inline fun <reified T : Type> getTypeAs(descriptor: String): T =
    AsmType.getType(descriptor).toType() as T
inline fun <reified T : Type> getTypeFromInternalNameAs(internalName: String): T =
    AsmType.getObjectType(internalName).toType() as T

inline fun <reified C : Any> getType(): Type =
    getTypeAs<C, Type>()
fun getType(type: Class<*>): Type =
    getTypeAs(type)
fun getType(type: KClass<*>): Type =
    getTypeAs(type.java)
fun getType(descriptor: String): Type =
    getTypeAs(descriptor)
fun getTypeFromInternalName(internalName: String): Type =
    getTypeFromInternalNameAs(internalName)

inline fun <reified T : Any> getArrayType(): Type.Array =
    getTypeAs<T, Type.Array>()
fun getArrayType(type: Class<*>): Type.Array =
    getTypeAs(type)
fun getArrayType(type: KClass<*>): Type.Array =
    getTypeAs(type.java)
fun getArrayType(descriptor: String): Type.Array =
    getTypeAs(descriptor)
fun getArrayTypeFromInternalName(internalName: String): Type.Array =
    getTypeFromInternalNameAs(internalName)

inline fun <reified T : Any> getObjectType(): Type.Object =
    getTypeAs<T, Type.Object>()
fun getObjectType(type: Class<*>): Type.Object =
    getTypeAs(type)
fun getObjectType(type: KClass<*>): Type.Object =
    getTypeAs(type.java)
fun getObjectType(descriptor: String): Type.Object =
    getTypeAs(descriptor)
fun getObjectTypeByInternalName(internalName: String): Type.Object =
    getTypeFromInternalNameAs(internalName)

fun getMethodType(descriptor: String): Type.Method =
    getTypeAs(descriptor)
fun getMethodType(returnType: Type, vararg argumentTypes: Type): Type.Method =
    getTypeAs(AsmType.getMethodDescriptor(returnType.toAsmType(), *argumentTypes.mapToArray { it.toAsmType() }))

fun Type.toArrayType(dimensions: Int = 1): Type.Array {
  check(dimensions > 0) { "The number of array dimensions must be positive, but $dimensions is given" }
  val arrayDescriptor = buildString {
    for (index in 1..dimensions) {
      append('[')
      append(descriptor)
    }
  }
  return getArrayType(arrayDescriptor)
}
