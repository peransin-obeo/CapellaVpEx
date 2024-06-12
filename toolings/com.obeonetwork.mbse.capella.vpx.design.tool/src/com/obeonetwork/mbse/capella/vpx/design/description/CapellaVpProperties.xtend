/*******************************************************************************
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
 package com.obeonetwork.mbse.capella.vpx.design.description

import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage
import org.eclipse.emf.ecore.EPackage
import org.eclipse.sirius.properties.Category
import org.eclipse.sirius.properties.PageDescription
import org.eclipse.sirius.properties.PageOverrideDescription
import org.mypsycho.modit.emf.sirius.api.AbstractPropertySet

import static extension org.mypsycho.modit.emf.sirius.api.SiriusDesigns.*

/**
 * Property editor 'CapellaVpProperties'.
 * 
 * @generated
 */
class CapellaVpProperties extends AbstractPropertySet {

	new(CapellaVpDesign parent) {
		super(parent, "CapellaVpProperties")
	}

	override initDefaultCategory(Category it) {
		overrides += PageOverrideDescription.create("vp_generation") [
			overrides = PageDescription.extraRef("page:etools§#generation_page")
			action("Generate Viewpoint Models", CapellaVpDesign.ICONS + "Viewpoint.gif",
				// no GenModel or GenGapModel in this plugin 
				'''
				self.eContainerOrSelf(«EPackage.asAql»).generateViewpoint(
				  self.eInverse()
				    ->select(it | it.eClass().ePackage.nsURI = '«GenModelPackage.eNS_URI»')
				    .eContainerOrSelf(genmodel::GenModel)->asSet()
				    .eInverse(loophole::GenGapModel)->asSet()
				)'''.trimAql.toOperation
			)
		]
	}
}
