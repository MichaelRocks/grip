package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Reflector
import io.michaelrocks.grip.mirrors.buildAnnotation
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.util.*

interface ClassRegistry {
  fun classpath(): Collection<File>
  fun findTypesForFile(file: File): Collection<Type>
  fun getClassMirror(type: Type): ClassMirror
  fun getAnnotationMirror(type: Type): AnnotationMirror
}

internal class ClassRegistryImpl(
    private val fileRegistry: FileRegistry,
    private val reflector: Reflector
) : ClassRegistry {
  private val classesByType = HashMap<Type, ClassMirror>()
  private val annotationsByType = HashMap<Type, AnnotationMirror>()

  override fun classpath(): Collection<File> =
      fileRegistry.classpath()

  override fun findTypesForFile(file: File): Collection<Type> =
      fileRegistry.findTypesForFile(file)

  override fun getClassMirror(type: Type): ClassMirror =
      classesByType.getOrPut(type) { readClassMirror(type, false) }

  override fun getAnnotationMirror(type: Type): AnnotationMirror =
      annotationsByType.getOrPut(type) {
        val classMirror = classesByType[type] ?: classesByType[type] ?: readClassMirror(type, true)
        buildAnnotation(type) {
          check(classMirror.access or Opcodes.ACC_ANNOTATION != 0)
          for (method in classMirror.methods) {
            method.defaultValue?.let { addValue(method.name, it) }
          }
        }
      }

  private fun readClassMirror(type: Type, forAnnotation: Boolean): ClassMirror {
    return try {
      reflector.reflect(fileRegistry.readClass(type), this, forAnnotation)
    } catch (exception: Exception) {
      throw IllegalArgumentException("Unable to read a ClassMirror for ${type.internalName}", exception)
    }
  }
}
