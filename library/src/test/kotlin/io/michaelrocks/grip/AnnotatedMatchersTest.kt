/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.grip

import io.michaelrocks.grip.mirrors.Annotated
import io.michaelrocks.grip.mirrors.ImmutableAnnotationCollection
import io.michaelrocks.grip.mirrors.buildAnnotation
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.mockito.given
import io.michaelrocks.mockito.mock
import org.junit.Test

class AnnotatedMatchersTest {
  val annotated = mock<Annotated>().apply {
    given(annotations).thenReturn(
        ImmutableAnnotationCollection(
            buildAnnotation(getObjectTypeByInternalName("io/michaelrocks/mocks/Annotation"), visible = true)
        )
    )
  }

  @Test fun testAnnotatedWithByTypeTrue() = annotated.testAnnotations(true) {
    annotatedWith(getObjectTypeByInternalName("io/michaelrocks/mocks/Annotation"))
  }
  @Test fun testAnnotatedWithByTypeFalse() = annotated.testAnnotations(false) {
    annotatedWith(getObjectTypeByInternalName("io/michaelrocks/mocks/AnotherAnnotation"))
  }
  @Test fun testAnnotatedWithByPredicateTrue() = annotated.testAnnotations(true) {
    annotatedWith { _, annotation -> annotation.values.isEmpty() }
  }
  @Test fun testAnnotatedWithByPredicateFalse() = annotated.testAnnotations(false) {
    annotatedWith { _, annotation -> annotation.values.isNotEmpty() }
  }

  @Test fun testAnnotatedWithByTypeAndPredicateTrue() = annotated.testAnnotations(true) {
    annotatedWith(getObjectTypeByInternalName("io/michaelrocks/mocks/Annotation")) { _, annotation ->
      annotation.values.isEmpty()
    }
  }
  @Test fun testAnnotatedWithByTypeAndPredicateFalse() = annotated.testAnnotations(false) {
    annotatedWith(getObjectTypeByInternalName("io/michaelrocks/mocks/AnotherAnnotation")) { _, annotation ->
      annotation.values.isNotEmpty()
    }
  }

  private inline fun Annotated.testAnnotations(condition: Boolean, body: () -> ((Grip, Annotated) -> Boolean)) =
      assertAndVerify(condition, body) { annotations }
}
