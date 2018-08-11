[![Build Status](https://travis-ci.org/MichaelRocks/grip.svg?branch=master)](https://travis-ci.org/MichaelRocks/grip)

Grip
====

SQL-like queries on JVM classes metadata using Kotlin DSL.

Download
--------
Gradle:
```groovy
repositories {
  jcenter()
}

dependencies {
  compile 'io.michaelrocks:grip:0.6.3-beta'
}
```

Usage
-----
Before executing any queries you need to create an instance of `Grip` interface.
```kotlin
val grip = GripFactory.create(classpath)
```

Now you can query classes from a `.jar` file, or `classes` directory, or just a single `.class`
file that satisfy some condition. For example that's how you can find all `public` classes in a
`.jar` file.
```kotlin
val query = grip select classes from file where isPublic()
```
If you don't like the DSL syntax it's possible to use traditional method chaining.
```kotlin
val query = grip
    .select(classes)
    .from(file)
    .where(isPublic())
```
The query is a lazy object. It must be executed explicitly to return results. After the first
execution **results are cached**.
```kotlin
val classes = query.execute()
```
The `classes` variable contains a map from `Type` to `ClassMirror`. `ClassMirror` is an
object representation of class metadata. It provides functionality similar to `java.lang.Class`.
There're other *mirror* classes such as `MethodMirror`, `FieldMirror`, `AnnotationMirror` and more.

Query conditions can be much more complicated than just `isPublic()`. You can combine multiple
conditions with `and`, `or,` `xor` and `not`. That's how you can query all non-`final` classes
that implement `java.util.List`.
```kotlin
val query = grip select classes from file where
    (not(isFinal()) and interfacesContain(getType<List<*>>()))
```
Moreover, you can query methods or fields that satisfy some conditions. Here's how you can find
all deprecated methods from all classes.
```kotlin
val query = grip select methods from file where annotatedWith(getType<Deprecated>())
```
And finally, subqueries are supported. For example, you can queries `public` non-`final` fields
from `public` classes.
```kotlin
val subquery = grip select classes from file where isPublic()
val query = grip select fields from subquery where (isPublic() and not(isFinal()))
```

There're more conditions available, which you can find [here][1]. But if you need a condition
that's not present in the project you can add a new condition to your own project easily and use
it in queries.

License
=======
    Copyright 2016 Michael Rozumyanskiy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: https://github.com/MichaelRocks/grip/blob/master/library/src/main/kotlin/io/michaelrocks/grip/Matchers.kt
