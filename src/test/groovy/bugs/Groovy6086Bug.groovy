/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.bugs

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class Groovy6086Bug extends GroovyTestCase {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    // Note that this unit test reproduces the code that we can
    // see on the Grails build. However, it never managed to reproduce
    // the issue so the latter has been fixed independently.
    void testGroovy6086() {

        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = testFolder.newFolder()
            jointCompilationOptions = [stubDir: testFolder.newFolder()]
        }

        def unit = new JavaAwareCompilationUnit(config, null, new GroovyClassLoader(getClass().classLoader))

        unit.addSource('Boo.java', 'interface Boo {}')
        unit.addSource('Wrapper.groovy', '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Wrapper {
                private Map cache
                Boo[] boos() {
                    Boo[] locations = (Boo[]) cache.a
                    if (locations == null) {
                        if (true) {
                            locations = [].collect { File it -> it }
                        }
                        else {
                            locations = [] as Boo[]
                        }
                    }
                    return locations
                }
            }
        ''')
        unit.compile(CompilePhase.INSTRUCTION_SELECTION.phaseNumber)
    }
}
