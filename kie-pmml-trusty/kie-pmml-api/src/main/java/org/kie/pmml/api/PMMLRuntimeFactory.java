/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.pmml.api;

import java.io.File;

import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.runtime.PMMLRuntime;

public interface PMMLRuntimeFactory {


    /**
     * Retrieve a <code>PMMLRuntime</code> built around the given <code><KieMemoryCompiler.MemoryCompilerClassLoader</code>.
     * To be used for already-compiled resources provided by context (e.g. provided by <b>Maven</b> dependency)
     * 
     * @param memoryCompilerClassLoader
     * @return
     */
    PMMLRuntime getPMMLRuntimeFromClassloader(KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader);

    /**
     * Retrieve a <code>PMMLRuntime</code> bound to the given <code><File</code>
     * with an <i>on-the-fly</i> compilation.
     * @param pmmlFile
     * @return
     */
    PMMLRuntime getPMMLRuntimeFromFile(File pmmlFile);

    /**
     * Retrieve a <code>PMMLRuntime</code> bound to the given <b>pmmlFileName</b>
     * with an <i>on-the-fly</i> compilation.
     * Such file will be looked for in the classpath
     * (e.g. provided by <b>Maven</b> dependency)
     *
     * @param pmmlFileName
     * @return
     */
    PMMLRuntime getPMMLRuntimeFromClasspath(String pmmlFileName);
}
