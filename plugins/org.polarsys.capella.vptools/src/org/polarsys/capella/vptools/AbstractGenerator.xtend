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
import java.util.jar.Manifest
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EFactory
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.pde.core.plugin.IPluginElement
import org.eclipse.pde.core.plugin.IPluginModel
import org.eclipse.pde.core.plugin.IPluginModelBase
import org.eclipse.pde.core.plugin.PluginRegistry

/**
 * Common class for Generation.
 * 
 * @author nperansin
 */
abstract class AbstractGenerator {
	
	protected static val PLUGIN_XML = "plugin.xml"
	
	protected static val MANIFEST = "META-INF/MANIFEST.MF"

	protected val GenerationContext context
	protected val String pluginName
	protected val Path pluginPath
	protected val Path pluginSrc
	protected val Path pluginSrcMan
	
	protected val Path pluginXml
	
	new(GenerationContext ctxt, String pluginType) {
		context = ctxt
		pluginName = ctxt.getDefaultPluginName(pluginType)
		pluginPath = ctxt.getDefaultPluginPath(pluginType)
		pluginSrc = pluginPath.resolve("src")
		pluginSrcMan = pluginPath.resolve("src-man")
		pluginXml = pluginPath.resolve(PLUGIN_XML)
	}
	
	abstract def void exec()
	
	/** Class based Factory. */
	static def exec(Class<? extends AbstractGenerator> gen, GenerationContext ctxt) {
		println("[ " + gen.simpleName + " ]")
		gen.getConstructor(GenerationContext)
			.newInstance(ctxt)
    		.exec
		println
	}

	def getPluginModel() { PluginRegistry.findModel(pluginName) as IPluginModel }

	def getManifestFile() { pluginPath.resolve(MANIFEST) }
	
	def getManifestModel() {
		try (val is = Files.newInputStream(pluginPath.resolve(MANIFEST))) {
			new Manifest(is)
		}
	}

	def save(Manifest it) {
		try (val os = Files.newOutputStream(pluginPath.resolve(MANIFEST))) {
			write(os)
		}
	}

	
	//
	// Plugin and Classes properties.
	// 
	
	def getClassPrefix() { context.pkgClass.packageName }
	
	def getPkgClassName() { context.pkgClass.simpleName }

	def getPkgClassName(String ext) { pkgClassName.replace("Package", ext) }
	
	def getClassName(EClass it, String ext) { instanceClass.simpleName + ext }
	
	def toPackageClass(EClassifier it) { EPackage.class.interfaces.head }
	
	def toPackageClass(EStructuralFeature it) { EContainingClass.toPackageClass }
	
	def toJavaId(EStructuralFeature it, JavaClassFormatter jcf) {
		val cls = EContainingClass.toJavaId(jcf)
		'''«cls.substring(0, cls.length - 2)»_«name.toFirstUpper»()'''
	}
	
	def String toJavaId(EClass it, JavaClassFormatter jcf) {
		val pkg = jcf.formatClass(toPackageClass)
		'''«pkg».eINSTANCE.get«name.toFirstUpper»()'''
	}
	
	
	def getDefaultFactory() {
		val fClass = '''«classPrefix».impl.«getPkgClassName("FactoryImpl")»'''
		context.pkgClass
			.classLoader
			.loadClass(fClass)
			.getDeclaredConstructor()
			.newInstance() as EFactory
	}
	
	def getBundleClass(String classname) {
		Platform.getBundle(pluginName).loadClass(classname)
	}
	
	def isClassExists(String classname) { Files.exists(classname.javaSource) }
	
	def getJavaSource(String classname, Path src) {
		if (classname.contains("$")) {
			throw new IllegalArgumentException("Inner class has no file: " + classname);
		}
		src.resolve(classname.replace('.', '/') + ".java")
	}
	
	def getJavaSource(String classname) { classname.getJavaSource(pluginSrc) }
	
	def String toPackageName(String packagePrefix) {
		val qualif = packagePrefix !== null && !packagePrefix.blank
			? "." + packagePrefix
			: ""
		
		'''«pluginName»«qualif»'''
	}

	static def parseClassname(String it) {
		val index = lastIndexOf('.')
		substring(0, index) -> substring(index + 1)
	}
	
	static def String fullClassname(String packageName, String simpleName)
		'''«packageName».«simpleName»'''

	def String toClassName(String packagePrefix, String simpleName) {
		fullClassname(toPackageName(packagePrefix), simpleName)
	}
	
	def String toClassName(String simpleName) {
		toClassName(null, simpleName)
	}
	
	/** Writes a UTF-8 file in the target plugin. */
	def writeFile(String filename, CharSequence content) {
		pluginPath.resolve(filename).writeFile(content)
	}
	
	/** Writes a UTF-8 file. */
	def void writeFile(Path file, CharSequence content) {
		Files.createDirectories(file.parent)
		
		try (val out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			// UTF-8 by default for all source
			out.append(content)
		}
		println(" - " + pluginPath.relativize(file))
	}

	static def lines(Iterable<String> values) {
		values.lines("")
	}
	
	static def lines(Iterable<String> values, String sep) {
		values.filter[ !empty ].join(sep)
	}
	
	//
	// Writing XML 
	// 
	
	/** Writes the plugin XML file. */
	def writePluginXml(CharSequence content) {
		pluginXml.writeFile('''
<?xml version="1.0" encoding="UTF-8"?>
<?eclipse version="3.4"?>
<plugin>
  
  «content»
</plugin>
''')}
	
		
	static def String extensionPoint(String id, String content) {
		(content ?: "").trim.empty ? "" :
'''
<extension point="«id»">
  «content»
</extension>

''' }
	
	def getPluginXmlModel() { PluginModelBases.getPluginXmlModel(pluginXml) }
	
	def save(IPluginModelBase it) { PluginModelBases.save(it, pluginXml) }
	
	
	def findExtensionChildren(IPluginModel model, String extName) {
		model.plugin.extensions
			.findFirst[ point == extName ]
			.children
			.filter(IPluginElement)
	}
	
	//
	// Writing Java
	// 
	
	/** Writes a java file in default source path. */
	def writeJavaSource(CharSequence classname, (JavaClassFormatter)=>String content) {
		classname.writeJavaSource(pluginSrc, content)
	}
	
	/** Writes a java file in provided source path. */
	def writeJavaSource(CharSequence classname, Path src, (JavaClassFormatter)=>String content) {
		val classname_ = classname.toString
		val importer = new Importer(classname_)
		content.apply(importer)
		
		classname_
			.getJavaSource(src)
			.writeFile(
'''
«context.fileHeader»
package «importer.currentPackage»;

«
FOR importing : importer.listImports
»import «importing»;
«ENDFOR»

«content.apply(new RealFormatter(importer))»''')
		
		// TODO invoke PDE formatter or save actions ...
		
		classname_
	}
	
	/**
	 * Inteface used in Java Template to deals with imports.
	 * <p>
	 * For clean generation, it is easier/safer to perform 2 runs.<br/>
	 * Run 1 detects used classes.<br/>
	 * Run 2 is the actual result with imports.
	 * </p>
	 */
	static interface JavaClassFormatter {
		
		def String getTarget()
		
		def String formatClass(String packageName, String simpleName)
		
		def formatClass(Class<?> it) {
			formatClass(packageName, simpleName)
		}
		
		def formatClass(String it) {
			val id = parseClassname
			formatClass(id.key, id.value)
		}
		
		def formatFeatureType(EStructuralFeature it) {
			val type = formatClass(EType.instanceClass)
			many
				? '''«formatClass(EList)»<«type»>'''
				: type
		}
	
	}
	
	/** Recorder of used classes. */
	static class Importer implements JavaClassFormatter {
		
		val String target
		val String currentPackage
		val importings = <String, String>newHashMap
		
		new(String classname) {
			val id = parseClassname(classname)
			target = id.value
			currentPackage = id.key
			importings.put(target, currentPackage)
		}
		
		override formatClass(String packageName, String simpleName) {
			if (packageName == currentPackage) {
				importings.put(simpleName, currentPackage)
			} else {
				importings.putIfAbsent(simpleName, fullClassname(packageName, simpleName))
			}
			""
		}
		
		override getTarget() { target }
		
		def listImports() {
			importings
				.values
				.filter[ it !== currentPackage ]
				.sort
		}

	}
	
	/** Provider of class name according a set of imports. */
	static class RealFormatter implements JavaClassFormatter {
		
		val Importer importer
		
		new (Importer classReg) { importer = classReg }
		
		override getTarget() { importer.target }
		
		override formatClass(String packageName, String simpleName) {
			val name = fullClassname(packageName, simpleName)
			// Same Package
			packageName == importer.currentPackage
				? simpleName
				// In imports list
				: importer.importings.get(simpleName) == name
				? simpleName
				// Default
				: name
		}
		
	}
	
}
