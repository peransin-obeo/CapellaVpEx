/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools;

import java.util.List
import org.eclipse.emf.ecore.EPackage
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.Suite
import org.junit.runners.model.InitializationError

/**
 * Constants for generation.
 * 
 * @author nperansin
 */
class VpGenerators extends Suite {

	/** All default Generators. */
    public static val ALL_GENERATORS = #[
		BrowserQueryGenerator,
		UiGenerator,
		ModelHelpersGenerator,
		ModelCustomGenerator,
		ModelEditCustomGenerator,
		ModelCdoGenerator
	]


	static class Context extends GenerationContext {
		protected val generators = ALL_GENERATORS
	
		new(Class<? extends EPackage> pkgClass, String qname, String fileHeader) {
			super(pkgClass, qname, fileHeader)
		}
	}
	
	static class Generation extends Runner {

		var GenerationContext context
		var Class<? extends AbstractGenerator> generation
		
		new(GenerationContext ctxt, Class<? extends AbstractGenerator> gen) {
			context = ctxt
			generation = gen
		}

		override getDescription() {
			Description.createSuiteDescription(generation) => [
				addChild(Description.createTestDescription(generation, "exec"))
			]
		}
		
		override run(RunNotifier it) {
			val descr = description
			fireTestSuiteStarted(descr)
			val exec = descr.children.head
			fireTestStarted(exec)
	        try {
	            AbstractGenerator.exec(generation, context)
	        } catch (Throwable e) {
	        	fireTestFailure(new Failure(exec, e))
	        } finally {
	        	fireTestFinished(exec)
	        	fireTestSuiteFinished(descr)
	        }
		}

	}

	new(Class<?> contextType) throws InitializationError {
		super(contextType, createRunners(contextType))
	}
	
	static def List<Runner> createRunners(Class<?> contextType) {
		if (!Context.isAssignableFrom(contextType)) {
			throw new InitializationError("Class must extend " + contextType.name)
		}
		val context = (contextType as Class<? extends Context>)
			.declaredConstructor
			.newInstance
		context.generators.map[ new Generation(context, it) ]
	}

}
