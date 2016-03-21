package io.michaelrocks.grip

import io.michaelrocks.grip.io.FileSource
import io.michaelrocks.mockito.RETURNS_DEEP_STUBS
import io.michaelrocks.mockito.any
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Assert.*
import org.junit.Test
import org.objectweb.asm.Type
import java.io.File

class FileRegistryImplTest {
  @Test
  fun containsFile() {
    val source = mock<FileSource>()
    val file = File("source1")
    val factory = mock<FileSource.Factory>()
    given(factory.createFileSource(file.canonicalFile)).thenReturn(source)
    val registry = FileRegistryImpl(listOf(file), factory)
    assertTrue(registry.contains(file))
    assertFalse(registry.contains(File("source2")))
  }

  @Test
  fun containsType() {
    val source = mock<FileSource>()
    given(source.listFiles(any())).thenAnswer {
      @Suppress("UNCHECKED_CAST")
      val callback = it.arguments[0] as (name: String, type: FileSource.EntryType) -> Unit
      callback("Type1.class", FileSource.EntryType.CLASS)
    }
    val file = File("source")
    val factory = mock<FileSource.Factory>()
    given(factory.createFileSource(file.canonicalFile)).thenReturn(source)
    val registry = FileRegistryImpl(listOf(file), factory)
    assertTrue(registry.contains(Type.getObjectType("Type1")))
    assertFalse(registry.contains(Type.getObjectType("Type2")))
  }

  @Test
  fun classpath() {
    val classpath = (1..1000).map { File("source$it") }
    val factory = mock<FileSource.Factory>(RETURNS_DEEP_STUBS)
    val registry = FileRegistryImpl(classpath, factory)
    assertEquals(classpath.map { it.canonicalFile }, registry.classpath().toList())
  }

  @Test
  fun readClass() {
    val data = ByteArray(0)
    val source = mock<FileSource>()
    given(source.listFiles(any())).thenAnswer {
      @Suppress("UNCHECKED_CAST")
      val callback = it.arguments[0] as (name: String, type: FileSource.EntryType) -> Unit
      callback("Type1.class", FileSource.EntryType.CLASS)
    }
    given(source.readFile("Type1.class")).thenReturn(data)
    val file = File("source")
    val factory = mock<FileSource.Factory>()
    given(factory.createFileSource(file.canonicalFile)).thenReturn(source)
    val registry = FileRegistryImpl(listOf(file), factory)
    assertSame(data, registry.readClass(Type.getObjectType("Type1")))
    assertThrows<IllegalArgumentException> { registry.readClass(Type.getObjectType("Type2")) }
  }

  @Test
  fun findTypesForFile() {
    val source1 = mock<FileSource>()
    given(source1.listFiles(any())).thenAnswer {
      @Suppress("UNCHECKED_CAST")
      val callback = it.arguments[0] as (name: String, type: FileSource.EntryType) -> Unit
      callback("Type1.class", FileSource.EntryType.CLASS)
    }
    val source2 = mock<FileSource>()
    val file1 = File("file1")
    val file2 = File("file2")
    val factory = mock<FileSource.Factory>()
    given(factory.createFileSource(file1.canonicalFile)).thenReturn(source1)
    given(factory.createFileSource(file2.canonicalFile)).thenReturn(source2)
    val registry = FileRegistryImpl(listOf(file1, file2), factory)
    assertEquals(listOf(Type.getObjectType("Type1")), registry.findTypesForFile(file1).toList())
    assertEquals(listOf<Type>(), registry.findTypesForFile(file2).toList())
    assertThrows<IllegalArgumentException> { registry.findTypesForFile(File("file3")) }
  }

  @Test
  fun close() {
    val factory = mock<FileSource.Factory>(RETURNS_DEEP_STUBS)
    val registry = FileRegistryImpl(listOf(File("source")), factory)
    registry.close()

    assertThrows<IllegalStateException> { registry.contains(File("source")) }
    assertThrows<IllegalStateException> { registry.contains(Type.BOOLEAN_TYPE) }
    assertThrows<IllegalStateException> { registry.classpath() }
    assertThrows<IllegalStateException> { registry.readClass(Type.BOOLEAN_TYPE) }
    assertThrows<IllegalStateException> { registry.findTypesForFile(File("source")) }
    registry.close()
  }

  private inline fun <reified T : Throwable> assertThrows(noinline body: () -> Any) {
    assertThrows(T::class.java, body)
  }

  private fun <T : Throwable> assertThrows(exceptionClass: Class<T>, body: () -> Any) {
    try {
      body()
      throw AssertionError("$exceptionClass expected but no exception was thrown")
    } catch (exception: Throwable) {
      if (exception.javaClass != exceptionClass) {
        throw AssertionError("$exceptionClass expected but ${exception.javaClass} was thrown")
      }
    }
  }
}
