/*******************************************************************************
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 *******************************************************************************/
package com.obeonetwork.mbse.capella.vpx.design.description

import com.obeonetwork.mbse.capella.vpx.design.VpToolsServices
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference
import org.eclipse.sirius.diagram.DNodeList
import org.eclipse.sirius.diagram.DNodeListElement
import org.eclipse.sirius.diagram.DiagramPackage
import org.eclipse.sirius.diagram.EdgeArrows
import org.eclipse.sirius.diagram.HideFilter
import org.eclipse.sirius.diagram.description.AdditionalLayer
import org.eclipse.sirius.diagram.description.CenteringStyle
import org.eclipse.sirius.diagram.description.ContainerMapping
import org.eclipse.sirius.diagram.description.DiagramDescription
import org.eclipse.sirius.diagram.description.DiagramExtensionDescription
import org.eclipse.sirius.diagram.description.EdgeMapping
import org.eclipse.sirius.diagram.description.MappingBasedDecoration
import org.eclipse.sirius.diagram.description.style.FlatContainerStyleDescription
import org.eclipse.sirius.diagram.description.style.StylePackage
import org.eclipse.sirius.diagram.description.tool.DeleteElementDescription
import org.eclipse.sirius.diagram.description.tool.EdgeCreationDescription
import org.eclipse.sirius.diagram.description.tool.ToolSection
import org.eclipse.sirius.viewpoint.description.DecorationDistributionDirection
import org.eclipse.sirius.viewpoint.description.Position
import org.eclipse.sirius.viewpoint.description.UserFixedColor
import org.eclipse.sirius.viewpoint.description.tool.ContainerViewVariable
import org.eclipse.sirius.viewpoint.description.tool.ElementVariable
import org.eclipse.sirius.viewpoint.description.tool.ElementViewVariable
import org.eclipse.sirius.viewpoint.description.tool.OperationAction
import org.eclipse.sirius.viewpoint.description.tool.RemoveElement
import org.eclipse.sirius.viewpoint.description.tool.ToolDescription
import org.mypsycho.modit.emf.sirius.api.SiriusDiagramExtension

import static extension org.mypsycho.modit.emf.sirius.api.SiriusDesigns.*

/**
 * Extension of EntitiesDiagram diagram from EcoreTools.
 */
class EntitiesDiagramExtension extends SiriusDiagramExtension {
	
	static val DSTYLE = StylePackage.eINSTANCE
	static val DPKG = DiagramPackage.eINSTANCE
	
	static val TO_EMDE_CLASSES = '''
		.eAnnotations
		  ->select(it | it.source = '«VpToolsServices.ECORE_ANNOTATION»')
		  .getEmdeAnnotationElement()
		'''
	
	val ContainerMapping eClassMapping
	
	new(extension CapellaVpDesign parent) {
		super(parent, DiagramDescription.extraRef("etools§EntitiesDiagram"))
		eClassMapping = ContainerMapping.extraRef("node:etools§EntitiesDiagram#EC EClass")
	}

	override initContent(DiagramExtensionDescription it) {

// Validation is not live unlike decorators ...
//   (kind of useless)
					
//		ownedValidations += ViewValidationRule.create("Lost Extensions") [
//			level = ERROR_LEVEL.ERROR_LITERAL
//			// https://github.com/eclipse/kitalpha/
//			// blob/v6.2.0/
//			// emde/plugins/org.polarsys.kitalpha.emde.model.edit/
//			// src/org/polarsys/kitalpha/emde/model/edit/provider/helpers/EMDEHelper.java#L187
//			message = ''' 'EMDE only consider extension of first superclass' '''.trimAql
//			targets += eClassMapping
//			validFor('''not self.target.containHiddenExtensions()'''.trimAql)
//		]
		
		
		layers += AdditionalLayer.create("Capella Extension") [
			label = "%capellavp.entities.layer"
			activeByDefault = true
			icon = CapellaVpDesign.ICONS + "Viewpoint.gif"
			
			val eClassNode = eClassMapping

			val colorCustoms = #[ "null" -> CapellaVpDesign.COMMON_ID ]
				+ VpToolsServices.MODULE_NAMES
					.map[ "'" + it + "'" -> it ];
					
			colorCustoms.forEach[ colorCustom |
				styleCustomisations += '''self.isCapellaEcore(«colorCustom.key»)'''.trimAql.thenStyle(
					DSTYLE.flatContainerStyleDescription_ForegroundColor.customization(
						UserFixedColor.ref("color:" + CapellaVpDesign.moduleColorId(colorCustom.value)),
						eClassNode.allStyles(FlatContainerStyleDescription)
					)
				)
			]
			
			decorations += MappingBasedDecoration.create [
				name = "ExtensionHint"
				position = Position.NORTH_EAST_LITERAL
				distributionDirection = DecorationDistributionDirection.HORIZONTAL
				preconditionExpression = '''self.isEmdeExtensionClass()'''.trimAql
				imageExpression = CapellaVpDesign.ICONS + "extension.png"
				mappings += eClassNode
			]
			decorations += MappingBasedDecoration.create [
				name = "HiddenExtensions"
				position = Position.NORTH_EAST_LITERAL
				distributionDirection = DecorationDistributionDirection.HORIZONTAL
				preconditionExpression = '''self.containHiddenExtensions()'''.trimAql
				imageExpression = "/org.eclipse.jface/org/eclipse/jface/dialogs/images/message_error.png"
				tooltipExpression = '''
					'EMDE only consider extension of first superclass.\n
					Change supertypes order or add explicit extension.' '''.trimAql
				mappings += eClassNode
			]
			

			edgeMappings += EdgeMapping.createAs(Ns.edge, "emdeExtension") [
				// domainClass = EAnnotation
				// useDomainElement = true
				
				deletionDescription = DeleteElementDescription.localRef(Ns.del, "emdeExtensionDel")
				
				sourceMapping += eClassNode
				targetMapping += eClassNode

				targetFinderExpression = '''self«TO_EMDE_CLASSES»'''.trimAql

				style = [
					endsCentering = CenteringStyle.NONE
					targetArrow = EdgeArrows.OUTPUT_FILL_CLOSED_ARROW_LITERAL
				]
			]
			
			toolSections += createExistingElementsTools
			toolSections += createRelationTools
		]
	}


	def createExistingElementsTools() {
		ToolSection.create("Existing Elements") [
			// ownedTools += TODO hide Content		
			ownedTools += ToolDescription.createAs(Ns.operation, "HideContent") [
				label = "%capellavp.entities.hideContent"
				label = "Hide content"
				forceRefresh = true
				iconPath = "/org.eclipse.emf.ecoretools.design/icons/full/etools16/search.gif"
				precondition = '''
					element«isInstanceAql(EClass)»
					  and elementView«isInstanceAql(DNodeList)»
					'''.trimAql
				element = ElementVariable.create("element")
				elementView = ElementViewVariable.create("elementView")
				operation = "var:elementView".toContext('''
						self.ownedElements
						  ->filter(«DNodeListElement.asAql»)
						  ->select(it | not it.graphicalFilters
						    ->exists(f | f«isInstanceAql(HideFilter)»))
					'''.trimAql.forDo(
						DPKG.DDiagramElement_GraphicalFilters.creator(HideFilter)
					),
					'''
						self.outgoingEdges
						  ->select(it | it.target«isInstanceAql(EReference)»)
					'''.trimAql.forDo(
						DPKG.DDiagramElement_GraphicalFilters.creator(HideFilter)
					)
				)
			]
			
			ownedTools += ToolDescription.createAs(Ns.operation, "ShowContent") [
				initVariables
				label = "%capellavp.entities.hideContent"
				label = "Show content"
				forceRefresh = true
				iconPath = "/org.eclipse.emf.ecoretools.design/icons/full/etools16/search.gif"
				precondition = '''
					element«isInstanceAql(EClass)»
					  and elementView«isInstanceAql(DNodeList)»
					'''.trimAql
				operation = "var:elementView".toContext('''
						self.ownedElements
						  ->filter(«DNodeListElement.asAql»)
						  .graphicalFilters
						  ->filter(«HideFilter.asAql»)
					'''.trimAql.forDo(
						RemoveElement.create
					),
					'''
						self.outgoingEdges
						  ->select(it | it.target«isInstanceAql(EReference)»)
						  .graphicalFilters
						  ->filter(«HideFilter.asAql»)
					'''.trimAql.forDo(
						RemoveElement.create
					)
				)
			]
			
			ownedTools += OperationAction.createAs(Ns.operation, "Show Extended Classes") [
				forceRefresh = true
				icon = "/org.eclipse.emf.ecoretools.design/icons/full/etools16/search.gif"
				view = ContainerViewVariable.create("views")
				operation = 
					'''
					diagram.getDisplayedEClassifiers()->filter(ecore::EClass)
					'''.trimAql.letDo("classes", 
						'''classes«TO_EMDE_CLASSES» - classes'''.trimAql.forDo(
							"service:markForAutosize".toContext(
								eClassMapping
									.viewDo("diagram")
							)
						)
					)
			]
		]
	}
	
	def createRelationTools() {
		ToolSection.create("Relation") [
			// ownedTools += TODO create Extension
			//   Add 2 Eannotation
			//   Add superclass extension (if needed)
			ownedTools += EdgeCreationDescription.createAs(Ns.connect, "emdeExtensionCreate") [
				iconPath = CapellaVpDesign.ICONS + "extension.png"
				label = "Extension"
				edgeMappings += EdgeMapping.localRef(Ns.edge, "emdeExtension")
				initVariables
				connectionStartPrecondition = '''not preSource.isInLibrary()'''.trimAql
				precondition = '''
					preTarget.eAllSuperTypes->exists(it | it.name = 'ExtensibleElement')
					  and not preSource«TO_EMDE_CLASSES»
					    ->includes(preTarget)
				'''.trimAql
			
				operation = "source.addEmdeExtensions(target)".trimAql.toOperation()
			]
			ownedTools += DeleteElementDescription.createAs(Ns.del, "emdeExtensionDel")[
				initVariables
				operation = '''
					let target = elementView.targetNode.target in
					elementView.sourceNode.target.eAnnotations
					  ->select(it | it.isEmdeConstraintExtensionOf(target)
					    or it.isEmdeMappingExtensionOf(target))
					'''.trimAql.forDo(RemoveElement.create)
			]
		]
	}


}
