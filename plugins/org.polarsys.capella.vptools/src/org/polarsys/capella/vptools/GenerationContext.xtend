/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.nio.file.Path
import java.util.Objects
import org.eclipse.emf.ecore.EPackage
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * Common class for Generation.
 * 
 * @author nperansin
 */
 class GenerationContext {
	
	static val DEFAULT_ROOT_PATH = "../.."
	static val PLUGINS_PATH = "plugins"
	
	@Accessors
	val Class<? extends EPackage> pkgClass
	@Accessors
	val String qname
	@Accessors
	val EPackage pkg
	@Accessors
	val String eNsUri
	@Accessors
	val String fileHeader
	
	// Derived values
	@Accessors
	val rootPath = Path.of(DEFAULT_ROOT_PATH)
	@Accessors
	val pluginsPath = rootPath.resolve(PLUGINS_PATH)

	new(Class<? extends EPackage> pkgClass, String qname, String fileHeader) {
		this.pkgClass = Objects.requireNonNull(pkgClass)
		this.qname = Objects.requireNonNull(qname)
		this.fileHeader = fileHeader ?: ""
		pkg = pkgClass.getField("eINSTANCE").get(null) as EPackage
		eNsUri = pkgClass.getField("eNS_URI").get(null) as String
	}
	
	def String getDefaultPluginName(String type)
		'''«qname».«type»'''
	
	def getDefaultPluginPath(String type) {
		pluginsPath.resolve(type.defaultPluginName)
	}
	
	def execAll(Class<? extends AbstractGenerator>... gens) {
		gens.forEach[ AbstractGenerator.exec(it, this) ]
	}
}
