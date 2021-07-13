/*
 * Copyright 2021 Michael Rozumyanskiy
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

import io.michaelrocks.grip.commons.ResettableLazy
import io.michaelrocks.grip.mirrors.MethodMirror
import java.util.ArrayList

internal class MethodsQueryBuilder(
  grip: Grip
) : AbstractQueryBuilder<MethodMirror, MethodsResult>(grip) {

  override fun execute(source: ClassMirrorSource, matcher: (Grip, MethodMirror) -> Boolean): MethodsResult =
    buildMethodsResult {
      val methodMirrors = ResettableLazy { ArrayList<MethodMirror>() }
      for (classMirror in source.getClassMirrors()) {
        for (methodMirror in classMirror.constructors) {
          if (matcher(grip, methodMirror)) {
            methodMirrors.value.add(methodMirror)
          }
        }

        for (methodMirror in classMirror.methods) {
          if (matcher(grip, methodMirror)) {
            methodMirrors.value.add(methodMirror)
          }
        }

        if (methodMirrors.initialized) {
          addMethods(classMirror, methodMirrors.value)
        }

        methodMirrors.reset()
      }
    }
}
