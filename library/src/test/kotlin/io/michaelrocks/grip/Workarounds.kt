package io.michaelrocks.grip

inline fun <reified T : Any> arrayOf(vararg elements: T) = listOf(*elements).toTypedArray()
