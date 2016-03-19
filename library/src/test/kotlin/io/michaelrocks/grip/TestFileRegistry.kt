package io.michaelrocks.grip

import org.objectweb.asm.Type
import java.io.File
import kotlin.reflect.KClass

class TestFileRegistry(vararg classes: KClass<*>) : FileRegistry {
  private val classesByType = classes.associateBy { Type.getType(it.java) }

  override fun contains(file: File): Boolean = true
  override fun classpath(): Collection<File> = listOf(File("/"))

  override fun readClass(type: Type): ByteArray {
    val classLoader = classesByType[type]!!.java.classLoader
    return classLoader.getResourceAsStream(type.internalName + ".class").readBytes()
  }

  override fun findTypesForFile(file: File): Collection<Type> = classesByType.keys
}
