/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.util.Map
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EStructuralFeature
import org.polarsys.capella.common.data.modellingcore.ModelElement
import org.polarsys.capella.common.lib.IdGenerator

/**
 * Generation of Helpers for model.
 * <p>
 * Generation happens in fragment to avoid plugin.xml merging.
 * </p>
 * 
 * @author nperansin
 */
class ModelCustomGenerator extends AbstractGenerator {

	val Map<EClass, GenClass> genPool = newHashMap

	new(GenerationContext ctxt) { this(ctxt, "model") }
	
	// For CDO generation
	protected new(GenerationContext ctxt, String pluginType) {
		super(ctxt, pluginType)
	}
	
	override exec() {
		genPool += context.pkg.EClassifiers
			// Sub-packages are not supported by Kit-alpha gen
			.filter(EClass)
			.toInvertedMap[
				new GenClass(it)
			]
		
		genPool.values.forEach[ init(this) ]
		genPool.values.forEach[ createCustomImpl ]

		genPool.clear
	}
	
	def getModelElementBasedClasses() {
		context.pkg
			.EClassifiers
			.filter(EClass)
			.filter[ !abstract && ModelElement.isAssignableFrom(instanceClass) ]
			.toList
	}
	
	def String getCustomClass(EClass it)
		'''«classPrefix».impl.«getClassName("CustomImpl")»'''
	
	def createCustomImpl(GenClass gen) {
		val it = gen.source
		// Fix not implemented feature 
		val derivedFeatures = gen.generatedDerivedFeatures

		println(
			'''Generated «name» EClass custom impl for : «
				derivedFeatures.join(',')[ name ]
			»''')

		val derivedHelpersClass = '''«classPrefix».util.DerivedHelpers'''
		
		customClass.writeJavaSource(pluginSrcMan) [ f | '''
/**
 * Custom implementation of «name.toFirstUpper».
 *
 * @generated
 */
public « // Custom class and default
IF abstract »abstract «
ENDIF	    »class «f.target» extends «getClassName("Impl")» {

« // Kit-alpha generate this code in the factory.
IF !abstract && ModelElement.isAssignableFrom(instanceClass)
»	/**
     * Default constructor.
     */
    public «f.target»() {
        setId(«f.formatClass(IdGenerator)».createId());
    }

«ENDIF»
«
FOR feature : derivedFeatures
»    @Override
    public «f.formatFeatureType(feature)
		» «feature.derivedMethodName»() {
        return «f.formatClass(derivedHelpersClass)»«
	IF feature.many ».getDerivedValues«
	ELSE            ».getDerivedValue«
	ENDIF                            »(this, 
	        «feature.toJavaId(f)»);
    }

«ENDFOR»
}'''] }
	
	static def getDefaultImpl(EClass eClass) {
		val it = eClass.instanceClass
		classLoader.loadClass('''«packageName».impl.«simpleName»Impl''')
	}

	static def getDerivedMethodName(EStructuralFeature it) {
		val prefix = !many
				&& (EType.instanceClass == boolean || EType.instanceClass == Boolean)
			? "is"
			: "get"
		prefix + name.toFirstUpper
	}
	
	static def isHelperDerivedFeature(EStructuralFeature it) {
		derived && !changeable /* no setter */ && volatile /* no field */
	}
	
	static class GenClass {
		
		val EClass source
		val Map<EStructuralFeature, Object> implementations = newHashMap

		new(EClass src) { source = src }
		
		def getAllDerivedFeatures() {
			source.EAllStructuralFeatures
				.filter[ isHelperDerivedFeature
					// && !isLocalSuperClass
					// TODO issue with derived inherited locally
				].toList
		}
			
		def getGeneratedDerivedFeatures() {
			allDerivedFeatures.filter[ implementations.get(it) == this ]
		}
		
		def init(ModelCustomGenerator gen) {
			val deriveds = allDerivedFeatures
			
			// Default implementation only contains 
			val nativeImpl = source.defaultImpl
				
			implementations += deriveds.toInvertedMap[
				gen.getMethodImplementation(it, this, nativeImpl)
			]
		}
	}
	
	def getMethodImplementation(EStructuralFeature it, GenClass genClass, Class<?> nativeImpl) {
		if (EContainingClass.EPackage == genClass.source.EPackage) {
			// In this pool of generation
			// Wrong ! 
			// We should search in parent in implementation to be sure.
			val declaring = genPool.get(EContainingClass)
			declaring.source.defaultImpl.isAssignableFrom(nativeImpl)
				? declaring
				: genClass
		} else {
			try {
				// Find in all inherited class
				nativeImpl.superclass.getMethod(derivedMethodName)
			} catch (NoSuchMethodException e) {
				// No inherited implementation: This class should 
				genClass // : generate in this class
			}
		}
	}
}
