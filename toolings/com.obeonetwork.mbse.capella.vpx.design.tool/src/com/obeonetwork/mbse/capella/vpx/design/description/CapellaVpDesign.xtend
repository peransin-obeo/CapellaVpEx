/*******************************************************************************
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package com.obeonetwork.mbse.capella.vpx.design.description

import com.obeonetwork.mbse.capella.vpx.design.Activator
import com.obeonetwork.mbse.capella.vpx.design.VpGenerationServices
import com.obeonetwork.mbse.capella.vpx.design.VpToolsServices
import com.obeonetwork.mbse.capella.vpx.design.tool.DesignToolBase
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.sirius.diagram.DiagramPackage
import org.eclipse.sirius.viewpoint.description.Group
import org.mypsycho.modit.emf.sirius.api.SiriusVpGroup

/**
 * Design of EcoreTools extension for Capella Viewpoint.
 */
class CapellaVpDesign extends SiriusVpGroup {
	
	public static val ICONS = '''/«Activator.PLUGIN_ID»/icons/'''
	public static val COMMON_ID = "Common"
	
	public static val EDITED_PKGS = #[
		DiagramPackage.eINSTANCE,
		EcorePackage.eINSTANCE,
		GenModelPackage.eINSTANCE
	]
	
	new () { super(EDITED_PKGS) }

	override initContent(Group it) {
		name = "capellavp"
		version = "12.0.0.2017041100"
		viewpoint("CapellaVp") [
			modelFileExtension = "ecore xcore ecorebin"
			owned(EntitiesDiagramExtension)
			use(VpToolsServices)
			use(VpGenerationServices)
		]
		properties(CapellaVpProperties)
		colorsPalette("CapellaVpColors",
			CapellaVpDesign.COMMON_ID.moduleColorId.color( 169, 204, 227), // Navy
			"OperationalAnalysis".moduleColorId.color(255, 223, 239), // pink
			"ContextArchitecture".moduleColorId.color(217, 196, 215), // purple
			"LogicalArchitecture".moduleColorId.color(204, 242, 166), // Vert
			"PhysicalArchitecture".moduleColorId.color(194, 239, 255), // light blue
			"EPBSArchitecture".moduleColorId.color(253, 206, 137) // orange
		)
	}
	
	static def String moduleColorId(String module)
		'''CAPVP_«module»_Class'''

	override initExtras() {
		super.initExtras
		
		extras += DesignToolBase.getEcoreToolsExtras(resource.resourceSet)
			.entrySet
			.toMap([ value ])[ key ]
	}

}
