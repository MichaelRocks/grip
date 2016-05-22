package io.michaelrocks.grip.commons

internal inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
  return Array(size, { index -> transform(this[index]) })
}

internal inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
  return Array(size, { index -> transform(this[index]) })
}
