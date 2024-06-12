/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.Objects
import java.util.jar.Manifest
import java.util.stream.Collectors
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.emf.codegen.ecore.genmodel.GenDelegationKind
import org.eclipse.emf.codegen.ecore.genmodel.GenModel
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.xmi.XMLResource

import static extension org.polarsys.capella.vptools.VpGenerationServices.*

/**
 * Generation of Helpers for model.
 * <p>
 * Generation happens in fragment to avoid plugin.xml merging.
 * </p>
 * 
 * @author nperansin
 */
class ModelCdoGenerator extends ModelCustomGenerator {

	static val EMF_OVERRIDE_EXTPOINT = 
		"org.eclipse.emf.ecore.factory_override"
		
	static val DEFAULT_CDO_PLUGIN_SUFFIX = ".cdo"

	// From 
	static val BUILTIN_MODEL_PLUGINS = "
			org.polarsys.kitalpha.ad.metadata.model
			org.polarsys.kitalpha.emde.model
			org.polarsys.capella.common.data.activity.gen
			org.polarsys.capella.common.data.behavior.gen
			org.polarsys.capella.common.data.core.gen
			org.polarsys.capella.common.libraries.gen
			org.polarsys.capella.common.re.gen
			org.polarsys.capella.core.data.gen
		"
		.split("\\s+")
		.filter[ !blank ]
		.toList


	new(GenerationContext ctxt) { super(ctxt, "model.cdo") }
	
	// We assume GenModels have the same path for regular and cdo generation.
	def createCdoPluginMapping() {
		BUILTIN_MODEL_PLUGINS.toInvertedMap[ it + DEFAULT_CDO_PLUGIN_SUFFIX ]
	}
	
	def toResource(Path it, ResourceSet rs, boolean load) {
		val uri = URI.createFileURI(toAbsolutePath.toString)
		rs.createResource(uri) => [
			load ? load(null)
		]
	}
	
	def getGenGapModels(String genModelPath, ResourceSet rs) {
		val path = genModelPath.replace(".genmodel", ".gengapmodel")
		pluginPath
			.resolve(path)
			.toResource(rs, true)
			.contents
			.filterGenGapModel
	}
	
	override exec() {
		val rs = new ResourceSetImpl
		val genModelPath = createGenModel(rs)
		
		ensureProject.refreshLocal(IResource.DEPTH_INFINITE, null)
		
		// Generate GenGapModel
		VpGenerationServices.createOperation(null,
			genModelPath.getGenGapModels(rs), 
			[ _ | VpGenerationServices.MODEL_GENERATION ]
		).execute(new NullProgressMonitor)
			
		// Generate PluginXml
		EMF_OVERRIDE_EXTPOINT.extensionPoint(
			'''
			<factory
			  uri="«context.pkg.nsURI»"
			  class="«classPrefix».cdo.«getPkgClassName("FactoryImpl")»"
			/>
			'''
		).writePluginXml
		
		// Remove API class
		deleteGeneratedApi

		// Generate Custom Classes
		super.exec

		// Rewrite manifest ?? 
		updateManifest
	}

	override getCustomClass(EClass it)
		'''«classPrefix».cdo.«getClassName("CustomImpl")»'''
	
	def updateValueList(Manifest it, String key, (List<String>)=>void update) {
		val input = mainAttributes.getValue(key) ?: ""
		val values = new ArrayList(input
			.split(",")
			.filter[ !blank ]
			.toList
		) // modifiable
		update.apply(values)
		mainAttributes.putValue(key, values.join(','))
	}
	
	def updateManifest() {
		val it = manifestModel
		// Provided by Model plugin
		val modelPackages = #[
			context.pkgClass.packageName,
			context.pkgClass.packageName + ".util"
		]
		
		// Remove to Import-Package
		updateValueList("Export-Package") [
			removeIf[ modelPackages.contains(it) ]
		]

		
		// Add to Import-Package
		updateValueList("Import-Package") [ pkgs |
			pkgs += modelPackages
				.filter[ !pkgs.contains(it) ]
		]
		
		save
	}
	
	
	def deleteGeneratedApi() {
		val path = context.pkgClass.name.getJavaSource.parent
		val utilPath = path.resolve("util")
		#[ path, utilPath ]
			.flatMap[ Files.list(it).collect(Collectors.toList) ]
			.filter[ Files.isRegularFile(it) ]
			.forEach[ Files.delete(it) ]
		Files.delete(utilPath)
	}
	
	def ensureProject() {
		val ws = ResourcesPlugin.workspace
		val expectedFile = pluginPath.toFile.canonicalFile
		
		ws.root.getProject(pluginName) => [ // only handle
			if (exists) {
				open(null) // no effect if already open 
				// rawLocation works only if open
				val oldFile = rawLocation.toFile.canonicalFile
				if (oldFile.equals(expectedFile)) {
					return
				}
			}
			delete(false, true, null)
			val descr = ws.newProjectDescription(pluginName) => [
				location = new org.eclipse.core.runtime.Path(expectedFile.toString)
			]
			create(descr, null)
			open(null)
		]
	}
	
	
	def createGenModel(ResourceSet rs) {
		val modelPlugin = context.getDefaultPluginPath("model")
		val modelXml = PluginModelBases.getPluginXmlModel(modelPlugin.resolve(PLUGIN_XML))
		val genModelPath = modelXml
			.findExtensionChildren("org.eclipse.emf.ecore.generated_package")
			.findFirst[ getAttribute("uri").value == context.pkg.nsURI ]
			.getAttribute("genModel").value
		
		val genModelRef = modelPlugin
			.resolve(genModelPath)
			.toResource(rs, true)
			.contents
			.filter(GenModel)
			.head
		
		// Create GenModel
		val cdoGenModel = EcoreUtil
			.copy(genModelRef)
			.refactorGenModel
		pluginPath
			.resolve(genModelPath)
			.toResource(rs, false) => [
				contents += cdoGenModel
				save(#{ 
					XMLResource.OPTION_ENCODING -> StandardCharsets.UTF_8.name()
				})
			]
		genModelPath
	}
	
	def refactorGenModel(GenModel it) {
		val cdoMapping = createCdoPluginMapping
		
		// https://wiki.eclipse.org/CDO/Preparing_EMF_Models
		modelPluginID = '''«context.qname».model.cdo'''
		modelDirectory = '''/«modelPluginID»/src'''
		// featureDelegation : 
		// Doc say 'Reflective' but 'kit-alpha' says 'Dynamic'
		featureDelegation = GenDelegationKind.DYNAMIC_LITERAL
		rootExtendsInterface="org.eclipse.emf.cdo.CDOObject"
		rootExtendsClass="org.eclipse.emf.internal.cdo.CDOObjectImpl"

		val cdoPackages = new ArrayList(usedGenPackages)
			.map[ toCdoGenPackage(cdoMapping) ]
			.toList
		usedGenPackages.clear
		usedGenPackages += cdoPackages
		
		modelPluginVariables += "CDO=org.eclipse.emf.cdo"

		genPackages.forEach[ classPackageSuffix = "cdo" ]
		it
	}
	
	def toCdoGenPackage(GenPackage it, Map<String, String> cdoMapping) {
		val resUri = eResource.URI
		if (!resUri.isPlatform) {
			throw new UnsupportedOperationException("Only platform resource is supported " + resUri)
		}
		
		val cdoUri = URI.createPlatformPluginURI(
			'''«resUri.segment(1).mapCdoProject(cdoMapping)»/«
				resUri.segmentsList
					.subList(2, resUri.segmentCount)
					.join('/')»''', true)
		val cdoRs = eResource
			.resourceSet
			.createResource(cdoUri)
		cdoRs.load(null) // required ??
		cdoRs.getEObject(eResource.getURIFragment(it)) as GenPackage
	}
	
	def mapCdoProject(String it, Map<String, String> cdoMapping) {
		Objects.requireNonNull(cdoMapping.get(it), '''No CDO project found for "«it»"''')
	}
	
}
