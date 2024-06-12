/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.util.List
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference

/**
 * Generation of Queries for browser.
 * 
 * @author nperansin
 */
class BrowserQueryGenerator extends AbstractGenerator {
	
	
	new(GenerationContext ctxt) { super(ctxt, "browser") }

	override exec() {
		context.pkg.browserQueryExtensions.writePluginXml
	}
	
	def List<Query> getQueries() {
		#[  
			new Query("Parent", "CurrentElementBrowser") [ true ], // element always has a container
			new Query("Owned", "CurrentElementBrowser") [ ownedApplicable ],
			new Query("Referencing", "ReferencingElementBrowser") [ referencingApplicable ],
			new Query("Referenced", "ReferencedElementBrowser") [ referencedApplicable ]
		]
	}
	
	def isHavingQueries(EClass it) { !^abstract }
	
	def getIdPart(EClass src) {
		var prefix = ""
		for (var pkg = src.EPackage; pkg.ESuperPackage !== null; pkg = pkg.ESuperPackage) {
			prefix = '''«pkg.name».«prefix»'''
		}
		'''«prefix»«src.name»'''
	}
	
	def isQueryApplicable(EClass it, Query query) {
		// TODO evaluates if explicit class exists
		query.applicable.apply(it)
	}
	
	def boolean isOwnedApplicable(EClass src) {
		src.isInScope(context.pkg)
			&& (src.EReferences
				.exists[ containment ]
				|| src.ESuperTypes.exists[ isOwnedApplicable ]
			)
	}
	
	def static boolean isInScope(EClass src, EPackage scope) {
		scope.EClassifiers.contains(src)
			|| scope.ESubpackages.exists[ src.isInScope(it) ]
	}
	
	def isReferencingApplicable(EClass src) {
		src.isReferencingApplicable(context.pkg)
	}
	
	def static boolean isReferencingApplicable(EClass src, EPackage scope) {
		// Exists reference to src or parent
		val directApplicable = scope.EClassifiers.filter(EClass)
			.flatMap[ EReferences ]
			// TODO Maps
			.filter[ navigableReference ]
			.exists[ (EType as EClass).isSuperTypeOf(src) ]
			
		directApplicable || scope.ESubpackages.exists[ src.isReferencingApplicable(it) ]
	}
	
	def boolean isReferencedApplicable(EClass src) {
		src.isInScope(context.pkg)
			&& (src.EReferences
				.exists[ navigableReference ]
				|| src.ESuperTypes.exists[ isReferencedApplicable ]
			)
	}

	def static boolean isNavigableReference(EReference it) {
		!containment && !derived && !transient && EType instanceof EClass
	}
	
	def getQueryClass(EClass src, String query) {
		// TODO evaluates if class exists
		'''«pluginName».BrowserQueries$«query»'''
	}

	def String getBrowserQueryExtensions(EPackage src) {
'''«
FOR eClass : src
	.EClassifiers
	.filter(EClass)
	.filter[ havingQueries ]»
  «eClass.browserQueryExtensions»
«ENDFOR /* end:eClass */»«
FOR sub : src.ESubpackages 
// Sub-packages are not supported by Viewpoint manager.
// It is ready for here.
»
«sub.browserQueryExtensions»
«
ENDFOR /* end:sub */»
''' }
	
	def getBrowserQueryExtensions(EClass it) {
		"org.polarsys.capella.common.ui.toolkit.browser.contentProviderCategory".extensionPoint(
'''
«
FOR query : queries.filter[ q | isQueryApplicable(q) ]
»
  «getBrowserQueryExtension(query.query, query.target)»
«
ENDFOR
»
''') }
	
	def getBrowserQueryExtension(EClass it, String query, String target) {
		// ID is not the same as Query class.
'''
<category id="«pluginName».queries.«idPart»#«query»" isTopLevel="true" name="%«query»Browser">
  <targetBrowserId id="«target»" />
  <availableForType class="«instanceClassName»" />
  <categoryQuery>
    <basicQuery class="«getQueryClass(query)»" />
  </categoryQuery>
</category>
''' }

	static class Query {
		
		val String query
		val String target
		val (EClass)=>boolean applicable
		
		new (String query, String target, (EClass)=>boolean applicable) {
			this.query = query
			this.target = target
			this.applicable = applicable
		}
		
	}
	
}
