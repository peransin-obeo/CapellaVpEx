/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools

import java.util.Collection
import java.util.List
import org.eclipse.emf.common.notify.AdapterFactory
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor
import org.polarsys.kitalpha.emde.^extension.edit.ChildCreationExtenderManager

/**
 * Generation of Helpers for model.
 * <p>
 * Generation happens in fragment to avoid plugin.xml merging.
 * </p>
 * 
 * @author nperansin
 */
class ModelEditCustomGenerator extends AbstractGenerator {

	new(GenerationContext ctxt) { super(ctxt, "model.edit") }
	
	override exec() {
		val extendersClasses = updatePluginXml
		
		createCustomIpaFactory(extendersClasses)
		
		context.pkg.EClassifiers
			.filter(EClass)
			.forEach[ createCustomProvider ]
	}
	
	def String getIpPrefix() 
		'''«classPrefix».provider.'''

	def String getCustomIpaFactory()
		'''«ipPrefix»«getPkgClassName("ItemProviderAdapterFactoryCustomImpl")»'''
		
	def String getEditPluginClass()
		'''«ipPrefix»«getPkgClassName("EditPlugin")»'''

	def String getVpFilterClass()
		'''«ipPrefix»util.VpChildCreationExtender'''

	def updatePluginXml() {
		val model = pluginXmlModel

		// Update ItemProviderAdapterFactories
		model.findExtensionChildren("org.eclipse.emf.edit.itemProviderAdapterFactories")
			.head => [
				val classnameAtt = getAttribute("class")
				classnameAtt.value = customIpaFactory
			]
		
		// Update ChildCreationExtenders
		val extendersClasses = model
			.findExtensionChildren("org.eclipse.emf.edit.childCreationExtenders")
			.map[				
				val classnameAtt = getAttribute("class")
				val classname = classnameAtt.value
				val simplename = classname.substring(classname.lastIndexOf("$") + 1)
				// Update plugin.xml
				classnameAtt.value = '''«customIpaFactory»$«simplename»'''
				
				simplename
			].toList
		
		model.save
		
		extendersClasses
	}
	
		
	def createCustomIpaFactory(List<String> extendersClasses) {
 		// same package as target
		val ipaFactory = getPkgClassName("ItemProviderAdapterFactory")
		
		customIpaFactory.writeJavaSource(pluginSrcMan) ['''
/**
 * Custom Item Provider factory of «context.pkg.name».
 * <p>
 * It also contains Child Creation Extenders that filtering extensions using 
 * Viewpoint activation.
 * </p>
 * 
 * @generated
 */
public class «target»
        extends «ipaFactory» {

    /**
     * Default constructor.
     */
    public «target»() {
    	// Use EMDE ChildCreationExtenderManager. (Also fetch extender through inheritance)
    	childCreationExtenderManager =
    	    new «formatClass(ChildCreationExtenderManager)
    			»(«formatClass(editPluginClass)».INSTANCE, «
    			formatClass(context.pkgClass)».eNS_URI);
    }

«FOR extenderClass : extendersClasses
»    /** Filtering of «extenderClass» with Viewpoint manager. */
    public static class «extenderClass» extends «formatClass(vpFilterClass)» {

        /** Default Constructor. */
        public «extenderClass»() {
            super(new «ipaFactory».«extenderClass»());
        }

    }
    
«ENDFOR
»
}'''] }


	def String getDefaultIpClass(EClass it)
		'''«ipPrefix»«getClassName("ItemProvider")»''' 
	
	def String getCustomClass(EClass it)
		'''«ipPrefix»«getClassName("ItemProviderCustomImpl")»'''
	
	def String toPropertyMethod(EReference it)
		'''add«name.toFirstUpper»PropertyDescriptor'''
	
	def String toPropertyField(EReference it)
		'''«name»PropDesc'''
	
	
	def createCustomProvider(EClass it) {
		val editLabelsClass = '''«ipPrefix»util.«getPkgClassName("EditLabels")»'''
		
		// Search feature with no many
		val defaultIp = getBundleClass(defaultIpClass)
		
		val implMethods = defaultIp.declaredMethods
			.map[ name ]
			.toList
		
		val checkedProperties = EAllReferences.filter[
			// Only applicable for single reference:
// https://github.com/eclipse/kitalpha/
//   blob/bc3ca5cc0acecfbbe266ef03388f8dcd88bdba3b/
//   emde/plugins/org.polarsys.kitalpha.emde.egf/
//   templates/pattern._Oa03kGSsEd-v4L49liVchg/method._TbPA-2StEd-v4L49liVchg.pt#L4
			!many && implMethods.contains(toPropertyMethod)
		]
		
		// checkChildCreationExtender
		val checkCceOverride = try {
				// Search in inherited method (TODO check)
				defaultIp.getMethod("checkChildCreationExtender", Object) !== null
			} catch (Exception e) {
				false
			}
		
		customClass.writeJavaSource(pluginSrcMan) [ f | '''
/**
 * Custom Item Provider of «name.toFirstUpper».
 *
 * @generated
 */
public class «f.target» extends «getClassName("ItemProvider")» {

«FOR prop : checkedProperties
»    protected «f.formatClass(IItemPropertyDescriptor)» «prop.toPropertyField»;
«ENDFOR
»« !checkedProperties.empty ? "\n" : "" // force newline
»    /**
     * Default constructor.
     */
    public «f.target»(«f.formatClass(AdapterFactory)» adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {
        return «f.formatClass(editLabelsClass)».getItemLabel((«f.formatClass(EObject)») object);
    }
    
    @Override
    protected void collectNewChildDescriptors(«
    		f.formatClass(Collection)»<Object> newChildDescriptors, Object object) {
        super.collectNewChildDescriptors(newChildDescriptors, object);
        «f.formatClass(vpFilterClass)».filterInvalidExtensions(object, newChildDescriptors);
    }

«IF !checkedProperties.empty
»    @Override
    public «f.formatClass(List)»<«
    		f.formatClass(IItemPropertyDescriptor)
    		»> getPropertyDescriptors(Object object) {
        super.getPropertyDescriptors(object);
        checkChildCreationExtender(object);
        return itemPropertyDescriptors;
    }

«	IF checkCceOverride
»	@Override
    public void checkChildCreationExtender(Object object) {
        super.checkChildCreationExtender(object);
«	ELSE
»	public void checkChildCreationExtender(Object object) {
«	ENDIF
»«	FOR prop : checkedProperties
»        «f.formatClass(vpFilterClass)».checkPropertyByValue(object, «prop.toPropertyField
				», itemPropertyDescriptors);
«	ENDFOR
»    }

«	FOR prop : checkedProperties
»    @Override
     protected void «prop.toPropertyMethod»(Object object) {
        super.«prop.toPropertyMethod»(object);
        «prop.toPropertyField» = itemPropertyDescriptors.get(itemPropertyDescriptors.size() - 1);
    }

«	ENDFOR
»
«ENDIF
»
}'''] }

}
