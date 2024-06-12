/*******************************************************************************
 * Copyright (c) 2024 Obeo. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package com.obeonetwork.mbse.capella.vpx.design.tool

import com.obeonetwork.mbse.capella.vpx.design.description.CapellaVpDesign
import java.nio.file.Path
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.sirius.viewpoint.description.Group
import org.junit.Test
import org.mypsycho.modit.emf.sirius.tool.SiriusReverseIt

/**
 * Tool to reverse Sirius design model from '*.design' plugin.
 * <p>
 * Reverse is required when model is modified directly to compare difference with
 * generated model.
 * </p>
 *
 * @author nperansin
 *
 */
class DesignReverse extends DesignToolBase {

	@Test
	def void reverseModel() {
		SiriusReverseIt.loadSiriusGroup(DESIGN_PATH)
			.createSiriusReverseIt(
				REVERSE_PATH, 
				"com.obeonetwork.mbse.capella.vpx.design.description.CapellaVpDesign"
			).perform
	}
	

	static def createSiriusReverseIt(Group content, Path dir, String classname) {
		new SiriusReverseIt(content, dir, classname, CapellaVpDesign.EDITED_PKGS) {
			
			override addExplicitExtras(ResourceSet rs, Map<EObject, String> extras) {
				extras += rs.getEcoreToolsExtras
			}
			
		}
	}

}
