/*
 * Copyright 2020 the original author or authors.
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
package org.openrewrite.java.testing.junitassertj

import org.junit.jupiter.api.Test
import org.openrewrite.Parser
import org.openrewrite.RefactorVisitor
import org.openrewrite.RefactorVisitorTestForParser
import org.openrewrite.java.JavaParser
import org.openrewrite.java.tree.J

class AssertNullToAssertThatTest : RefactorVisitorTestForParser<J.CompilationUnit> {
    override val parser: Parser<J.CompilationUnit> = JavaParser.fromJavaVersion()
        .classpath("junit", "assertj-core", "apiguardian-api")
        .build()

    override val visitors: Iterable<RefactorVisitor<*>> = listOf(AssertNullToAssertThat())

    @Test
    fun singleStaticMethodNoMessage() = assertRefactored(
        before = """
                import org.junit.Test;

                import static org.junit.jupiter.api.Assertions.assertNull;

                public class A {
 
                    @Test
                    public void test() {
                        assertNull(notification());
                    }
                    private String notification() {
                        return null;
                    }
                }
            """,
        after = """
                import org.junit.Test;

                import static org.assertj.core.api.Assertions.assertThat;

                public class A {

                    @Test
                    public void test() {
                        assertThat(notification()).isNull();
                    }
                    private String notification() {
                        return null;
                    }
                }
            """
    )

    @Test
    fun singleStaticMethodWithMessageString() = assertRefactored(
        before = """
                import org.junit.Test;

                import static org.junit.jupiter.api.Assertions.assertNull;

                public class A {
 
                    @Test
                    public void test() {
                        assertNull(notification(), "Should be null");
                    }
                    private String notification() {
                        return null;
                    }
                }
            """,
        after = """
                import org.junit.Test;

                import static org.assertj.core.api.Assertions.assertThat;

                public class A {

                    @Test
                    public void test() {
                        assertThat(notification()).as("Should be null").isNull();
                    }
                    private String notification() {
                        return null;
                    }
                }
            """
    )

    @Test
    fun singleStaticMethodWithMessageSupplier() = assertRefactored(
        before = """
                import org.junit.Test;

                import static org.junit.jupiter.api.Assertions.assertNull;

                public class A {
 
                    @Test
                    public void test() {
                        assertNull(notification(), () -> "Should be null");
                    }
                    private String notification() {
                        return null;
                    }
                }
            """,
        after = """
                import org.junit.Test;

                import static org.assertj.core.api.Assertions.assertThat;

                public class A {

                    @Test
                    public void test() {
                        assertThat(notification()).withFailMessage(() -> "Should be null").isNull();
                    }
                    private String notification() {
                        return null;
                    }
                }
            """
    )

    @Test
    fun inlineReference() = assertRefactored(
        before = """
                import org.junit.Test;
 
                public class A {
                
                    @Test
                    public void test() {
                        org.junit.jupiter.api.Assertions.assertNull(notification());
                        org.junit.jupiter.api.Assertions.assertNull(notification(), "Should be null");
                        org.junit.jupiter.api.Assertions.assertNull(notification(), () -> "Should be null");
                    }
                    private String notification() {
                        return null;
                    }
                }
            """,
        after = """
                import org.junit.Test;
                
                import static org.assertj.core.api.Assertions.assertThat;
                
                public class A {
                
                    @Test
                    public void test() {
                        assertThat(notification()).isNull();
                        assertThat(notification()).as("Should be null").isNull();
                        assertThat(notification()).withFailMessage(() -> "Should be null").isNull();
                    }
                    private String notification() {
                        return null;
                    }
                }
            """
    )

    @Test
    fun mixedReferences() = assertRefactored(
        before = """
                import org.junit.Test;
                
                import static org.assertj.core.api.Assertions.*;
                import static org.junit.jupiter.api.Assertions.assertNull;
                
                public class A {
                
                    @Test
                    public void test() {
                        assertNull(notification());
                        org.junit.jupiter.api.Assertions.assertNull(notification(), "Should be null");
                        assertNull(notification(), () -> "Should be null");
                    }
                    private String notification() {
                        return null;
                    }
                }
            """,
        after = """
                import org.junit.Test;
                
                import static org.assertj.core.api.Assertions.*;
                
                public class A {
                
                    @Test
                    public void test() {
                        assertThat(notification()).isNull();
                        assertThat(notification()).as("Should be null").isNull();
                        assertThat(notification()).withFailMessage(() -> "Should be null").isNull();
                    }
                    private String notification() {
                        return null;
                    }
                }
            """
    )
}
