/*******************************************************************************
 * Copyright (c) 2024 Obeo. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package com.obeonetwork.mbse.capella.vpx.design.tool

import com.obeonetwork.mbse.capella.vpx.design.Activator
import java.nio.file.Path
import java.nio.file.Paths
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.mypsycho.modit.emf.sirius.api.SiriusDependencies

/**
 * Common elements for tool of dynamic Design model of '*.sirius' plugin.
 * 
 * @author Obeo
 */
class DesignToolBase {

	public static val ECORETOOLS_DESIGN = "org.eclipse.emf.ecoretools.design/description/ecore.odesign"

	// Specific
	protected static val PLUGINS_PATH = "../../plugins"
		
	protected static val REVERSE_PATH = Path.of("target/rvs")
	
	protected static val DESIGN_PATH = Activator.DESIGN_PATH
	
	// Derived
	protected static val ODESIGN_FILE = Paths.get(PLUGINS_PATH)
		// /!\ File path matches java bundle path.
		.resolve(DESIGN_PATH)
		.toAbsolutePath
		
	static def isAliasIgnored(EObject it) {
		val uri = EcoreUtil.getURI(it).toString
		// Remove duplicated tools in EcoreToolsReverse alias
		uri.contains("@additionalLayers[name='Constraint']") 
			&& uri.endsWith("@ownedTools[name='Edit%20Detail']")
	}
	
	static def getEcoreToolsExtras(ResourceSet rs) {
		SiriusDependencies
			.getDependencyExtras("etools", rs, ECORETOOLS_DESIGN)
			// Remove duplication in ODesign				
			.filter[ key, value| !key.aliasIgnored ]
		
	}
}
