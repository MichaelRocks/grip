package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectType
import java.io.File
import kotlin.reflect.KClass

class TestFileRegistry(vararg classes: KClass<*>) : FileRegistry {
  private val classesByType = classes.associateBy { getObjectType(it) }

  override fun contains(file: File): Boolean = true
  override fun contains(type: Type.Object): Boolean = type in classesByType

  override fun classpath(): Collection<File> = listOf(DEFAULT_FILE)

  override fun readClass(type: Type.Object): ByteArray {
    val classLoader = classesByType[type]!!.java.classLoader
    return classLoader.getResourceAsStream(type.internalName + ".class").readBytes()
  }

  override fun findTypesForFile(file: File): Collection<Type.Object> = classesByType.keys
  override fun findFileForType(type: Type.Object) = if (contains(type)) DEFAULT_FILE else null

  companion object {
    private val DEFAULT_FILE = File("/")
  }
}
