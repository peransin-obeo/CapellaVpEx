/*******************************************************************************
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package com.obeonetwork.mbse.capella.vpx.design.tool

import java.nio.file.Files
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.junit.Test
import org.mypsycho.modit.emf.sirius.tool.SiriusReverseIt

/**
 * Tool to reverse Sirius design model from '*.design' plugin.
 * <p>
 * Reverse is required when model is modified directly to compare difference with
 * generated model.
 * </p>
 *
 * @author Obeo
 *
 */
class EcoreToolsReverse extends DesignToolBase {
	
	@Test
	def void reverseModel() {
		new SiriusReverseIt(
			DesignToolBase.ECORETOOLS_DESIGN, 
			REVERSE_PATH, 
			'''org.eclipse.emf.ecoretools.design.EcoreToolsDesign'''
		).perform
		
		var uri = URI.createPlatformPluginURI(DesignToolBase.ECORETOOLS_DESIGN, true)
		try(val out = Files.newOutputStream(REVERSE_PATH.resolve("ecoretools.odesign"))) {
			new ResourceSetImpl().getResource(uri, true).save(out, #{})
		}
	}
	
}
