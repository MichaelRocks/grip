package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Reflector
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.UnresolvedAnnotationMirror
import io.michaelrocks.grip.mirrors.buildAnnotation
import org.objectweb.asm.Opcodes
import java.util.HashMap

interface ClassRegistry {
  fun getClassMirror(type: Type.Object): ClassMirror
  fun getAnnotationMirror(type: Type.Object): AnnotationMirror
}

internal class ClassRegistryImpl(
    private val fileRegistry: FileRegistry,
    private val reflector: Reflector
) : ClassRegistry {
  private val classesByType = HashMap<Type, ClassMirror>()
  private val annotationsByType = HashMap<Type, AnnotationMirror>()

  override fun getClassMirror(type: Type.Object): ClassMirror =
      classesByType.getOrPut(type) { readClassMirror(type, false) }

  override fun getAnnotationMirror(type: Type.Object): AnnotationMirror =
      annotationsByType.getOrPut(type) {
        if (type !in fileRegistry) {
          return UnresolvedAnnotationMirror(type)
        } else {
          val classMirror = readClassMirror(type, true)
          buildAnnotation(type) {
            check(classMirror.access or Opcodes.ACC_ANNOTATION != 0)
            for (method in classMirror.methods) {
              method.defaultValue?.let { addValue(method.name, it) }
            }
          }
        }
      }

  private fun readClassMirror(type: Type.Object, forAnnotation: Boolean): ClassMirror {
    return try {
      reflector.reflect(fileRegistry.readClass(type), this, forAnnotation)
    } catch (exception: Exception) {
      throw IllegalArgumentException("Unable to read a ClassMirror for ${type.internalName}", exception)
    }
  }
}
