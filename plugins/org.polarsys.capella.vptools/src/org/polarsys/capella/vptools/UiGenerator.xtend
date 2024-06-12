/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.lang.reflect.Modifier
import org.eclipse.emf.ecore.EClass

/**
 * Generation of tab sections and menu contribution in UI.
 * <p>
 * Generation happens in fragment to avoid plugin.xml merging.
 * </p>
 * 
 * @author nperansin
 */
class UiGenerator extends AbstractGenerator {
	
	static val UI_TABS = #[ 
		"org.polarsys.capella.core.data.capellamodeller.properties" -> "BaseCapella", 
		"org.eclipse.sirius.diagram.ui" -> "BaseSiriusDiagram", 
		"org.eclipse.sirius.table.ui.EditorID" -> "BaseSiriusTable"
	]

	new(GenerationContext ctxt) {
		super(ctxt, "ui.gen") // Previously in model.edit
	}
	
	override exec() {
		val allTabbedSections = UI_TABS.map[ key.getTabbedSections(value) ]
		
		val extensions = allTabbedSections + #[ menuContribs ]
		
		extensions.lines.writePluginXml
	}
	
	def getMenuContribs() {
		"org.polarsys.capella.common.menu.dynamic.MDEMenuItemContribution".extensionPoint(
			context.pkg.EClassifiers
				.filter(EClass)
				.map[ menuContrib ]
				.lines
		)
	}
	
	def String getMenuContrib(EClass it) {
		val contrib = RegisterHelperPattern.getInheritedMenuContributor(it)
		if (contrib === null || !Modifier.isAbstract(contrib.modifiers)) {
			return ""
		}

'''
<MDEMenuItemContribution
    id="«classPrefix».menu.contributions.information.«name»" 
    class="«createMenuContribClass(contrib)»" />
''' }
	
	def createMenuContribClass(EClass it, Class<?> contrib) {
		val eName = name
		
		"menu".toClassName(getClassName("ItemContribution")).writeJavaSource['''
/**
 * Menu Contribution for «eName».
 * 
 * @generated
 */
public class «target» extends «formatClass(contrib)» {

    @Override
    public «formatClass(EClass)» getMetaclass() {
        return «formatClass(context.pkgClass)».eINSTANCE.get«eName»();
    }
}
''']}
	
	def String getTabbedSections(String section, String tab) {
		"org.eclipse.ui.views.properties.tabbed.propertySections".extensionPoint('''
<propertySections contributorId="«section»">
	«context.pkg.EClassifiers.filter(EClass)
		.map[ getTabSection(tab) ]
		.lines»
</propertySections>
''')}
	
	def String getTabSection(EClass it, String tab) {
		val sectionClass = RegisterHelperPattern.getInheritedSection(it)
		if (sectionClass === null) {
			return ""
		}
		val customFilter = '''«pluginName».tabsection.«name»Filter'''
		val filterClass = customFilter.classExists 
			? customFilter
			: '''«pluginName».EClassTabSectionFilter:«name»'''
		
		// id must be unique in the extension
'''
<propertySection tab="«tab»"
    id="«classPrefix».provider.sections.«name».section"
    filter="«filterClass»"
    class="«sectionClass.name»" />
''' }
	
	
}
