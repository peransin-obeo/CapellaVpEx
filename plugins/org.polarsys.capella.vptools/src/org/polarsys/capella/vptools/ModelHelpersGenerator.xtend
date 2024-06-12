/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.ecore.EAnnotation
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature
import org.polarsys.capella.common.mdsofa.common.helper.ExtensionPointHelper
import org.polarsys.capella.common.model.helpers.IHelper
import org.polarsys.capella.core.business.queries.IBusinessQuery

/**
 * Generation of Helpers for model.
 * <p>
 * Generation happens in fragment to avoid plugin.xml merging.
 * </p>
 * 
 * @author nperansin
 */
class ModelHelpersGenerator extends AbstractGenerator {

	static val CANDIDATES_QUERY_EXT = "org.polarsys.capella.core.data.business.queries.MDEBusinessQueries"

	new(GenerationContext ctxt) {
		super(ctxt, "model.helpers") // Previously in model
	}
	
	override exec() {
		// TODO fragment of model
		// // TODO also implements derived feature
		//   needed when Directly defined or when superclass does not extend EClass interface
		
		val queries = createQueriesGroups
		
		val extensions = #[ 
			CANDIDATES_QUERY_EXT.extensionPoint(
				context.pkg.EClassifiers // keep model order
					.filter(EClass)
					.map[ queries.findFirst[ g | g.target == it ] ]
					.filterNull
					.flatMap[ 
						createBusinessQueries
						queryReferences.map[ q | getActualQuery(q) ]
					]
					.map[ '''<MDEBusinessQueries class="«it»" />'''  ]
					.lines('\n')
			),
			
			"org.polarsys.capella.common.model.helpers.helper".extensionPoint('''
				<helperImpl
				    class="«helperPackageName.toClassName»"
				    nsURI="«context.pkg.nsURI»" />
				''')
		]
		
		extensions.lines.writePluginXml

		writePackageHelper.forEach[ writeEClassHelper ]
	}
	
	def createBusinessQueries(QueriesGroup it) {
		if (!queryReferences.exists[q | !isCustomQuery(q) ]) {
			return null
		}
		
		queriesClass.writeJavaSource [ f | '''
/**
 * Queries for «target.name» class.
 * <p>
 * Reminder: Capella does not provide inheritance in it default queries.
 * @see org.polarsys.capella.core.business.queries.capellacore.BusinessQueriesProvider#getContribution(EClass, EStructuralFeature)
 * </p>
 *
 * @generated
 */
public class «f.target» {

«
FOR ref : queryReferences SEPARATOR "\n" »«
	IF isCustomQuery(ref) || isAmbiguous(ref)
»   // Implementation in «getCustomQuery(ref).parseClassname.value».
«		IF !isCustomQuery(ref)
»   /* 
     TODO: Merge 
      «queries.get(ref).map[ "- " + class.name ].join(",\n") »
     */
«		ENDIF »«
	ELSE 
»    /** Query inherited from parent EClass. */
    public class «getDefaultQueryName(ref)» extends «f.formatClass(queries.get(ref).head.class)» {

        @Override
        public «f.formatClass(EClass)» getEClass() {
            return «target.toJavaId(f)»;
        }

    }
«	ENDIF»
«
ENDFOR
»

}
''']}
	
	
	def String getHelperPackageName() 
		'''«context.pkg.name»PackageHelper'''
	
	def String getHelperClassName(EClass it) 
		'''«instanceClass.simpleName»Helper'''
	
	
	def findExistingCandidatesQueries() {
		Platform
			.extensionRegistry
			.getConfigurationElementsFor(CANDIDATES_QUERY_EXT)
			.filter[ contributor.name != pluginName ] // no self reference
			.map[ createExecutableExtension(ExtensionPointHelper.ATT_CLASS) as IBusinessQuery ]
			.toList
	}
	
	def createQueriesGroups() {
		val existings = findExistingCandidatesQueries
		
		context.pkg.EClassifiers
			.filter(EClass)
			.map[ 
				new QueriesGroup(this, it) => [
					init(existings)
				]
			].toList
	}
	
	static class QueriesGroup {
		val EClass target
		val Map<EReference, List<IBusinessQuery>> queries = new HashMap
		// Act as an internal class
		val extension ModelHelpersGenerator gen
		
		new(ModelHelpersGenerator context, EClass it) {
			target = it
			gen = context
		}
		
		def isApplicable(IBusinessQuery it) {
			EClass !== null // happens for deprecated stuff
				&& !(EStructuralFeatures ?: List.of).empty 
				&& EClass.isSuperTypeOf(target)
		}
		
		def init(List<IBusinessQuery> allExistings) {
			val existings = allExistings
				.filter[ applicable ]
				.toList
			existings.forEach[
				EStructuralFeatures.forEach[ f |
					queries.computeIfAbsent(f) [ new ArrayList<IBusinessQuery> ]
						.add(it)
				]
			]
			queries
				.values
				.forEach[ trimOverrides ]
		}
		
		def getQueryReferences() {
			queries.keySet
				.sortBy[ name ]
				.toList
		}
		
		def getActualQuery(EReference it) {
			isCustomQuery
				? getCustomQuery
				: getDefaultQuery
		}

		def String getCustomQuery(EReference it) 
			'''«classPrefix».business.queries.«target.getClassName("")»_«getDefaultQueryName»'''

		def String getQueriesClass() 
			'''«classPrefix».business.queries.«target.getClassName("Queries")»'''
	
		def String getDefaultQuery(EReference it)
			'''«getQueriesClass»$«defaultQueryName»'''
		
		def getDefaultQueryName(EReference it) {
			name.toFirstUpper
		}
		
		def isCustomQuery(EReference it) {
			getCustomQuery.isClassExists
		}
		
		def isAmbiguous(EReference it) {
			queries.getOrDefault(it,  List.of).size > 1
		}
		
		def trimOverrides(List<IBusinessQuery> values) {
			if (values.size == 1) {
				return // done
			}
			
			for (var i = 0; i < values.size - 1; i++) {
				val current = values.get(i)
				for (var j = i + 1; j < values.size; j++) {
					val follow = values.get(j)
					if (current.isSuperTypeOf(follow)) {
						// current is overridden
						values.remove(i)
						i--
						j = values.size // End of sub-run (<=> break)
					} else if (follow.isSuperTypeOf(current)) {
						values.remove(j)
						j--
					} // else conflict unless trimmed by better
				}
			}
			
		}
		
		def isSuperTypeOf(IBusinessQuery parent, IBusinessQuery child) {
			parent.EClass.isSuperTypeOf(child.EClass)
		}
	}
	
	def writePackageHelper() {
		val eClasses = context.pkg.EClassifiers
			.filter(EClass)
			// Derived class 
			.toList
			
		helperPackageName.toClassName.writeJavaSource [ '''
/**
 * Switch of helper for «context.pkg.name» Package.
 * 
 * @generated
 */
public class «target» implements «formatClass(IHelper)» {

    @Override
    public Object getValue(«formatClass(EObject)» object, «
    		formatClass(EStructuralFeature)» feature, «
    		formatClass(EAnnotation)» annotation) {
        Object result = null;
        // order class inheritance
«
FOR eClass : eClasses
»        if (result == null && object instanceof «formatClass(eClass.instanceClass)») {
            result = «eClass.helperClassName».getInstance()
                .doSwitch((«formatClass(eClass.instanceClass)») object, feature);
        }
«
ENDFOR
»
        return result;
    }

}
''']
		eClasses
	}
	
	def findInheritedHelper(EClass eClass) {
		val Class<?> inheritedHelper = RegisterHelperPattern.getInheritedHelper(eClass)
		if (inheritedHelper === null) {
			return null
		}
		val localInheritance = eClass.ESuperTypes
			.findFirst[ eClass.EPackage.EClassifiers.contains(it) ]

		localInheritance
			?.helperClassName
			?: inheritedHelper
	}
	
	
	
	def writeEClassHelper(EClass eClass) {
		val classSimplename = eClass.helperClassName
		val classname = classSimplename.toClassName
		if (classname.classExists) {
			// return // no override // log ?
		}
		val inheritedHelper = eClass.findInheritedHelper
		val inheritedHelperClass =
			inheritedHelper instanceof Class
				? inheritedHelper.name
				: inheritedHelper instanceof String
				? inheritedHelper.toClassName
				: null
		
		// TODO get from RegisterHelper with 
		val derivedFeatures = eClass.EStructuralFeatures.filter[ derived ].toList
		
		classname.writeJavaSource ['''
/**
 * Switch of helper for derived feature of «eClass.name».
 * 
 * @generated
 */
public class «classSimplename» {

    private static final «classSimplename» instance = new «classSimplename»();

    /**
     * Returns single instance.
     *
     * @return the instance
     */
    public static «classSimplename» getInstance() {
        return instance;
    }

    /**
     * Gets the value for specified feature of given object.
     *
     * @param object The object that the feature value is requested.
     * @param feature The feature that the value is requested.
     * @return <code>null</code> if no value is returned.
     */
    public Object doSwitch(«formatClass(eClass.instanceClass)» object, «formatClass(EStructuralFeature)» feature) {
«
IF !derivedFeatures.empty
»		// handle derivated features
«	FOR feature : derivedFeatures
»        if (feature == «formatClass(context.pkgClass)».eINSTANCE.get«eClass.name»_«feature.name.toFirstUpper»()) {
             return get«feature.name.toFirstUpper»(object);
         }
«	ENDFOR »«
ENDIF // !derivedFeatures.empty
»		// delegate to parent helper
        return «
IF inheritedHelper === null
			»null«
ELSE		»«formatClass(inheritedHelperClass)».getInstance().doSwitch(object, feature)«
ENDIF			»;
    }

«
FOR feature : derivedFeatures
»    private «formatFeatureType(feature)
			» get«feature.name.toFirstUpper»(«formatClass(eClass.instanceClass)» object) {
        // TODO implements
        return null;
    }

«
ENDFOR // derivedFeatures
»}
''']}

}
