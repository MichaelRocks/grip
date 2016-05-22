package io.michaelrocks.grip

import io.michaelrocks.grip.classes.Annotation1
import io.michaelrocks.grip.classes.Annotation2
import io.michaelrocks.grip.mirrors.EnumMirror
import io.michaelrocks.grip.mirrors.ReflectorImpl
import io.michaelrocks.grip.mirrors.getObjectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClassRegistryImplTest {
  private lateinit var classRegistry: ClassRegistry

  @Before
  fun createClassRegistry() {
    val fileRegistry = TestFileRegistry(
        Annotation1::class,
        Annotation2::class
    )
    val reflector = ReflectorImpl()
    classRegistry = ClassRegistryImpl(fileRegistry, reflector)
  }

  @Test
  fun testIntAnnotation() {
    val mirror = classRegistry.getClassMirror(getObjectType<Annotation1>())
    assertEquals(1, mirror.annotations.size)
    assertTrue(getObjectType<Annotation1>() in mirror.annotations)
    val annotation = mirror.annotations[getObjectType<Annotation1>()]!!
    assertEquals(1, annotation.values.size)
    assertEquals(42, annotation.values["value"])
  }

  @Test
  fun testEnumAnnotation() {
    val mirror = classRegistry.getClassMirror(getObjectType<Annotation2>())
    assertEquals(1, mirror.annotations.size)
    assertTrue(getObjectType<Annotation2>() in mirror.annotations)
    val annotation = mirror.annotations[getObjectType<Annotation2>()]!!
    assertEquals(1, annotation.values.size)
    assertEquals(
        EnumMirror(getObjectType<AnnotationRetention>(), AnnotationRetention.RUNTIME.toString()),
        annotation.values["value"]
    )
  }
}
