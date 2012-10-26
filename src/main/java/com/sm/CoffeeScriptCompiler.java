package com.sm;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Copyright 2012 Valeriy Filatov.
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
 * 
 */
public class CoffeeScriptCompiler {

	private final Scriptable globalScope;

	public CoffeeScriptCompiler() throws Exception {
		InputStream inputStream = CoffeeScriptCompiler.class
				.getResourceAsStream("/coffee-script-1.4.0/coffee-script.js");

		try {
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			try {
				Context context = Context.enter();
				context.setOptimizationLevel(-1); // Without this, Rhino
													// hits a 64K bytecode
													// limit and fails
				try {
					globalScope = context.initStandardObjects();
					context.evaluateReader(globalScope, reader,
							"coffee-script.js", 0, null);
				} finally {
					Context.exit();
				}
			} finally {
				reader.close();
			}
		} finally {
			inputStream.close();
		}
	}

	public String compile(String coffeeScriptSource) throws Exception {
		Context context = Context.enter();
		try {
			Scriptable compileScope = context.newObject(globalScope);
			compileScope.setParentScope(globalScope);
			compileScope.put("coffeeScriptSource", compileScope,
					coffeeScriptSource);
			return (String) context.evaluateString(compileScope, String.format(
					"CoffeeScript.compile(coffeeScriptSource, %s);",
					"{bare: true}"), "JCoffeeScriptCompiler", 0, null);
		} finally {
			Context.exit();
		}
	}

}
