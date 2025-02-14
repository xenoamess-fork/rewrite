/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.config.Environment;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ReplaceApacheCommonsLang3ValidateNotNullWithObjectsRequireNonNullTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("commons-lang3"))
          .recipe(Environment.builder()
            .scanRuntimeClasspath("org.openrewrite.java")
            .build()
            .activateRecipes("org.openrewrite.java.cleanup.ReplaceApacheCommonsLang3ValidateNotNullWithObjectsRequireNonNull"));
    }

    @Test
    void doNothingIfMethodNotFound() {
        rewriteRun(
          java(
            """
              import org.apache.commons.lang3.Validate;
              class Test {
                  void test(Object obj) {

                  }
              }
              """
          )
        );
    }

    @Test
    void replaceWithOneArgument() {
        rewriteRun(
          java(
            """
              import org.apache.commons.lang3.Validate;

              class Test {
                  void test(Object obj) {
                      Validate.notNull(obj);
                  }
              }
              """,
            """
              import java.util.Objects;

              class Test {
                  void test(Object obj) {
                      Objects.requireNonNull(obj);
                  }
              }
              """
          )
        );
    }

    @Test
    void replaceMethodsWithTwoArg() {
        rewriteRun(
          java(
            """
              import org.apache.commons.lang3.Validate;

              class Test {
                  void test(Object obj) {
                      Validate.notNull(obj,"Object should not be null");
                  }
              }
              """,
            """
              import java.util.Objects;

              class Test {
                  void test(Object obj) {
                      Objects.requireNonNull(obj, "Object should not be null");
                  }
              }
              """
          )
        );
    }

    @Test
    void replaceMethodsWithThreeArg() {
        rewriteRun(
          java(
            """
              import org.apache.commons.lang3.Validate;

              class Test {
                  void test(Object obj) {
                      Validate.notNull(obj, "Object in %s should not be null", "request xyz");
                  }
              }
              """,
            """
              import java.util.Objects;

              class Test {
                  void test(Object obj) {
                      Objects.requireNonNull(obj, () -> String.format("Object in %s should not be null", "request xyz"));
                  }
              }
              """
          )
        );
    }
}
