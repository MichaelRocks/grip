/*
 * Copyright 2016 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.grip.annotations

import io.michaelrocks.grip.mirrors.AbstractAnnotationMirror
import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.buildAnnotation
import org.objectweb.asm.Type
import java.util.*

fun createAnnotationMirror(annotationName: String): AnnotationMirror =
    createAnnotationMirror(annotationName, emptyMap<String, Any>())

fun createAnnotationMirror(annotationName: String, defaultValue: Any): AnnotationMirror =
    createAnnotationMirror(annotationName, "value", defaultValue)

fun createAnnotationMirror(annotationName: String, methodName: String, defaultValue: Any): AnnotationMirror =
    createAnnotationMirror(annotationName, Collections.singletonMap(methodName, defaultValue))

fun createAnnotationMirror(annotationName: String, vararg values: Pair<String, Any>): AnnotationMirror =
    createAnnotationMirror(annotationName, hashMapOf(*values))

fun createAnnotationMirror(annotationName: String, values: Map<String, Any>): AnnotationMirror =
    buildAnnotation(getAnnotationType(annotationName)) {
      addValues(SimpleAnnotationMirror(getAnnotationType(annotationName), values))
    }

fun getAnnotationType(annotationName: String): Type =
    Type.getObjectType(annotationName)

private class SimpleAnnotationMirror(
    override val type: Type,
    override val values: Map<String, Any>
) : AbstractAnnotationMirror()
