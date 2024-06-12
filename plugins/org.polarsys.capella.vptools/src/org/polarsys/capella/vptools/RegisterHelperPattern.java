/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.polarsys.capella.common.data.activity.ActivityPackage;
import org.polarsys.capella.common.data.helpers.activity.delegates.AbstractActivityHelper;
import org.polarsys.capella.common.data.helpers.activity.delegates.ActivityEdgeHelper;
import org.polarsys.capella.common.data.helpers.activity.delegates.ActivityExchangeHelper;
import org.polarsys.capella.common.data.helpers.activity.delegates.ActivityNodeHelper;
import org.polarsys.capella.common.data.helpers.activity.delegates.ActivityPartitionHelper;
import org.polarsys.capella.common.data.helpers.modellingcore.delegates.AbstractTypeHelper;
import org.polarsys.capella.common.data.helpers.modellingcore.delegates.InformationsExchangerHelper;
import org.polarsys.capella.common.data.helpers.modellingcore.delegates.ModelElementHelper;
import org.polarsys.capella.common.data.helpers.modellingcore.delegates.TraceableElementHelper;
import org.polarsys.capella.common.data.modellingcore.ModellingcorePackage;
import org.polarsys.capella.core.data.capellacommon.CapellacommonPackage;
import org.polarsys.capella.core.data.capellacore.CapellacorePackage;
import org.polarsys.capella.core.data.capellamodeller.CapellamodellerPackage;
import org.polarsys.capella.core.data.common.properties.sections.AbstractStateRealizationSection;
import org.polarsys.capella.core.data.common.properties.sections.AbstractStateSection;
import org.polarsys.capella.core.data.common.properties.sections.ChoicePseudoStateSection;
import org.polarsys.capella.core.data.common.properties.sections.FinalStateSection;
import org.polarsys.capella.core.data.common.properties.sections.ForkPseudoStateSection;
import org.polarsys.capella.core.data.common.properties.sections.GenericTraceSection;
import org.polarsys.capella.core.data.common.properties.sections.InitialPseudoStateSection;
import org.polarsys.capella.core.data.common.properties.sections.JoinPseudoStateSection;
import org.polarsys.capella.core.data.common.properties.sections.ModeSection;
import org.polarsys.capella.core.data.common.properties.sections.StateMachineSection;
import org.polarsys.capella.core.data.common.properties.sections.StateSection;
import org.polarsys.capella.core.data.common.properties.sections.TerminatePseudoStateSection;
import org.polarsys.capella.core.data.core.properties.sections.AllocationSection;
import org.polarsys.capella.core.data.core.properties.sections.BooleanPropertyValueSection;
import org.polarsys.capella.core.data.core.properties.sections.ConstraintSection;
import org.polarsys.capella.core.data.core.properties.sections.DefaultNamedElementSection;
import org.polarsys.capella.core.data.core.properties.sections.EnumerationPropertyValueSection;
import org.polarsys.capella.core.data.core.properties.sections.FloatPropertyValueSection;
import org.polarsys.capella.core.data.core.properties.sections.GeneralizableElementSection;
import org.polarsys.capella.core.data.core.properties.sections.IntegerPropertyValueSection;
import org.polarsys.capella.core.data.core.properties.sections.ModelElementSection;
import org.polarsys.capella.core.data.core.properties.sections.NamingRuleSection;
import org.polarsys.capella.core.data.core.properties.sections.PropertyValueGroupSection;
import org.polarsys.capella.core.data.core.properties.sections.StringPropertyValueSection;
import org.polarsys.capella.core.data.core.properties.sections.StructureSection;
import org.polarsys.capella.core.data.core.properties.sections.TypedElementSection;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.cs.properties.sections.ComponentSection;
import org.polarsys.capella.core.data.cs.properties.sections.ExchangeItemAllocationSection;
import org.polarsys.capella.core.data.cs.properties.sections.InterfaceImplementationSection;
import org.polarsys.capella.core.data.cs.properties.sections.InterfacePkgSection;
import org.polarsys.capella.core.data.cs.properties.sections.InterfaceSection;
import org.polarsys.capella.core.data.cs.properties.sections.InterfaceUseSection;
import org.polarsys.capella.core.data.cs.properties.sections.PartSection;
import org.polarsys.capella.core.data.cs.properties.sections.PhysicalLinkEndSection;
import org.polarsys.capella.core.data.cs.properties.sections.PhysicalLinkSection;
import org.polarsys.capella.core.data.cs.properties.sections.PhysicalPathInvolvementSection;
import org.polarsys.capella.core.data.cs.properties.sections.PhysicalPathSection;
import org.polarsys.capella.core.data.cs.properties.sections.PhysicalPortSection;
import org.polarsys.capella.core.data.ctx.CtxPackage;
import org.polarsys.capella.core.data.ctx.properties.sections.CapabilitySection;
import org.polarsys.capella.core.data.ctx.properties.sections.MissionSection;
import org.polarsys.capella.core.data.ctx.properties.sections.OperationalAnalysisRealizationSection;
import org.polarsys.capella.core.data.ctx.properties.sections.SystemFunctionPkgSection;
import org.polarsys.capella.core.data.ctx.properties.sections.SystemFunctionSection;
import org.polarsys.capella.core.data.epbs.EpbsPackage;
import org.polarsys.capella.core.data.epbs.properties.sections.ConfigurationItemSection;
import org.polarsys.capella.core.data.epbs.properties.sections.EPBSArchitecturePkgSection;
import org.polarsys.capella.core.data.epbs.properties.sections.EPBSArchitectureSection;
import org.polarsys.capella.core.data.epbs.properties.sections.PhysicalArchitectureRealizationSection;
import org.polarsys.capella.core.data.epbs.properties.sections.PhysicalArtifactRealizationSection;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.properties.sections.AbstractFunctionSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentExchangeAllocationSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentExchangeAllocatorSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentExchangeEndSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentExchangeFunctionalExchangeAllocationSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentExchangeRealizationSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentExchangeSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentFunctionalAllocationSection;
import org.polarsys.capella.core.data.fa.properties.sections.ComponentPortSection;
import org.polarsys.capella.core.data.fa.properties.sections.ExchangeLinkSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionInputPortSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionOutputPortSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionRealizationSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionSpecificationSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionalChainInvolvementSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionalChainSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionalExchangeRealizationSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionalExchangeSection;
import org.polarsys.capella.core.data.fa.properties.sections.FunctionalExchangeSpecificationSection;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.AbstractStateHelper;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.AbstractStateRealizationHelper;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.CapabilityRealizationInvolvedElementHelper;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.CapabilityRealizationInvolvementHelper;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.GenericTraceHelper;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.StateHelper;
import org.polarsys.capella.core.data.helpers.capellacommon.delegates.StateTransitionRealizationHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.AbstractPropertyValueHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.AllocationHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.CapellaElementHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.FeatureHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.GeneralizableElementHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.InvolvedElementHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.InvolvementHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.InvolverElementHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.NamedElementHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.NamedRelationshipHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.PropertyValueGroupHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.RelationshipHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.StructureHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.TraceHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.TypeHelper;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.TypedElementHelper;
import org.polarsys.capella.core.data.helpers.capellamodeller.delegates.SystemEngineeringHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.AbstractPhysicalLinkEndHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.ArchitectureAllocationHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.BlockArchitectureHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.ComponentHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.DeployableElementHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.ExchangeItemAllocationHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.InterfaceAllocationHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.InterfaceAllocatorHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.InterfaceHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.InterfaceImplementationHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.InterfaceUseHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.PartHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.PhysicalLinkEndHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.PhysicalLinkHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.PhysicalPathHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.PhysicalPathInvolvementHelper;
import org.polarsys.capella.core.data.helpers.cs.delegates.PhysicalPortHelper;
import org.polarsys.capella.core.data.helpers.ctx.delegates.CapabilityHelper;
import org.polarsys.capella.core.data.helpers.ctx.delegates.MissionHelper;
import org.polarsys.capella.core.data.helpers.ctx.delegates.OperationalAnalysisRealizationHelper;
import org.polarsys.capella.core.data.helpers.ctx.delegates.SystemAnalysisHelper;
import org.polarsys.capella.core.data.helpers.ctx.delegates.SystemFunctionHelper;
import org.polarsys.capella.core.data.helpers.epbs.delegates.ConfigurationItemHelper;
import org.polarsys.capella.core.data.helpers.epbs.delegates.EPBSArchitectureHelper;
import org.polarsys.capella.core.data.helpers.epbs.delegates.PhysicalArchitectureRealizationHelper;
import org.polarsys.capella.core.data.helpers.epbs.delegates.PhysicalArtifactRealizationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.AbstractFunctionAllocationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.AbstractFunctionHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.AbstractFunctionalBlockHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentExchangeAllocationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentExchangeAllocatorHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentExchangeEndHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentExchangeFunctionalExchangeAllocationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentExchangeHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentExchangeRealizationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentFunctionalAllocationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ComponentPortHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ExchangeLinkHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ExchangeSpecificationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.ExchangeSpecificationRealizationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionInputPortHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionOutputPortHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionPortHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionRealizationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionSpecificationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionalChainHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionalChainInvolvementHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionalExchangeHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionalExchangeRealizationHelper;
import org.polarsys.capella.core.data.helpers.fa.delegates.FunctionalExchangeSpecificationHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.BinaryExpressionHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.ClassHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.CollectionHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.CommunicationItemHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.CommunicationLinkExchangerHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.CommunicationLinkHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.DataTypeHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.DataValueHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.ExchangeItemElementHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.ExchangeItemHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.ExchangeItemRealizationHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.OperationAllocationHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.OperationHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.PortAllocationHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.PortHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.PortRealizationHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.PropertyHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.ServiceHelper;
import org.polarsys.capella.core.data.helpers.information.delegates.UnaryExpressionHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.AbstractCapabilityHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.AbstractCapabilityRealizationHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.AbstractEndHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.AbstractFunctionAbstractCapabilityInvolvementHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.CombinedFragmentHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.EventHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.ExecutionEndHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.ExecutionHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.FragmentEndHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.FunctionalChainAbstractCapabilityInvolvementHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.InstanceRoleHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.InteractionStateHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.InteractionUseHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.MessageEndHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.ScenarioRealizationHelper;
import org.polarsys.capella.core.data.helpers.interaction.delegates.SequenceMessageHelper;
import org.polarsys.capella.core.data.helpers.la.delegates.CapabilityRealizationHelper;
import org.polarsys.capella.core.data.helpers.la.delegates.ContextInterfaceRealizationHelper;
import org.polarsys.capella.core.data.helpers.la.delegates.LogicalArchitectureHelper;
import org.polarsys.capella.core.data.helpers.la.delegates.LogicalComponentHelper;
import org.polarsys.capella.core.data.helpers.la.delegates.LogicalFunctionHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.ActivityAllocationHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.EntityHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.EntityOperationalCapabilityInvolvementHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.OperationalActivityHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.OperationalAnalysisHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.OperationalCapabilityHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.OperationalProcessHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.RoleAllocationHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.RoleHelper;
import org.polarsys.capella.core.data.helpers.oa.delegates.SwimlaneHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.ComponentInstanceHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.LogicalArchitectureRealizationHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.LogicalInterfaceRealizationHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.PhysicalArchitectureHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.PhysicalComponentHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.PhysicalFunctionHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.PhysicalNodeHelper;
import org.polarsys.capella.core.data.helpers.pa.delegates.PortInstanceHelper;
import org.polarsys.capella.core.data.information.InformationPackage;
import org.polarsys.capella.core.data.information.communication.CommunicationPackage;
import org.polarsys.capella.core.data.information.communication.properties.sections.CommunicationItemSection;
import org.polarsys.capella.core.data.information.communication.properties.sections.CommunicationLinkSection;
import org.polarsys.capella.core.data.information.communication.properties.sections.SignalInstanceSection;
import org.polarsys.capella.core.data.information.communication.properties.sections.SignalSection;
import org.polarsys.capella.core.data.information.datatype.DatatypePackage;
import org.polarsys.capella.core.data.information.datatype.properties.sections.BooleanTypeSection;
import org.polarsys.capella.core.data.information.datatype.properties.sections.DataTypeSection;
import org.polarsys.capella.core.data.information.datatype.properties.sections.EnumerationSection;
import org.polarsys.capella.core.data.information.datatype.properties.sections.NumericTypeSection;
import org.polarsys.capella.core.data.information.datatype.properties.sections.PhysicalQuantitySection;
import org.polarsys.capella.core.data.information.datavalue.DatavaluePackage;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.BinaryExpressionSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.BooleanReferenceSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.CollectionValueReferenceSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.CollectionValueSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.ComplexValueReferenceSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.ComplexValueSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.DataValueSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.EnumerationLiteralSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.EnumerationReferenceSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.LiteralBooleanValueSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.LiteralNumericValueSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.LiteralStringValueSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.NumericReferenceSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.StringReferenceSection;
import org.polarsys.capella.core.data.information.datavalue.properties.sections.UnaryExpressionSection;
import org.polarsys.capella.core.data.information.properties.sections.ClassSection;
import org.polarsys.capella.core.data.information.properties.sections.CollectionSection;
import org.polarsys.capella.core.data.information.properties.sections.DomainElementSection;
import org.polarsys.capella.core.data.information.properties.sections.ExchangeItemElementSection;
import org.polarsys.capella.core.data.information.properties.sections.ExchangeItemInstanceSection;
import org.polarsys.capella.core.data.information.properties.sections.ExchangeItemRealizationSection;
import org.polarsys.capella.core.data.information.properties.sections.ExchangeItemSection;
import org.polarsys.capella.core.data.information.properties.sections.OperationAllocationSection;
import org.polarsys.capella.core.data.information.properties.sections.ParameterSection;
import org.polarsys.capella.core.data.information.properties.sections.PortAllocationSection;
import org.polarsys.capella.core.data.information.properties.sections.PortRealizationSection;
import org.polarsys.capella.core.data.information.properties.sections.PortSection;
import org.polarsys.capella.core.data.information.properties.sections.PropertySection;
import org.polarsys.capella.core.data.information.properties.sections.ServiceSection;
import org.polarsys.capella.core.data.information.properties.sections.UnionPropertySection;
import org.polarsys.capella.core.data.information.properties.sections.UnionSection;
import org.polarsys.capella.core.data.interaction.InteractionPackage;
import org.polarsys.capella.core.data.interaction.properties.sections.CombinedFragmentSection;
import org.polarsys.capella.core.data.interaction.properties.sections.ConstraintDurationSection;
import org.polarsys.capella.core.data.interaction.properties.sections.ExecutionEndSection;
import org.polarsys.capella.core.data.interaction.properties.sections.ExecutionSection;
import org.polarsys.capella.core.data.interaction.properties.sections.FragmentEndSection;
import org.polarsys.capella.core.data.interaction.properties.sections.FunctionalChainAbstractCapabilityInvolvementSection;
import org.polarsys.capella.core.data.interaction.properties.sections.InstanceRoleSection;
import org.polarsys.capella.core.data.interaction.properties.sections.InteractionStateSection;
import org.polarsys.capella.core.data.interaction.properties.sections.InteractionUseSection;
import org.polarsys.capella.core.data.interaction.properties.sections.MessageEndSection;
import org.polarsys.capella.core.data.interaction.properties.sections.ScenarioRealizationSection;
import org.polarsys.capella.core.data.interaction.properties.sections.ScenarioSection;
import org.polarsys.capella.core.data.interaction.properties.sections.SequenceMessageSection;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.la.properties.sections.CapabilityRealizationPkgSection;
import org.polarsys.capella.core.data.la.properties.sections.CapabilityRealizationSection;
import org.polarsys.capella.core.data.la.properties.sections.ContextInterfaceRealizationSection;
import org.polarsys.capella.core.data.la.properties.sections.LogicalArchitecturePkgSection;
import org.polarsys.capella.core.data.la.properties.sections.LogicalArchitectureSection;
import org.polarsys.capella.core.data.la.properties.sections.LogicalComponentSection;
import org.polarsys.capella.core.data.la.properties.sections.LogicalFunctionSection;
import org.polarsys.capella.core.data.la.properties.sections.SystemAnalysisRealizationSection;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.AbstractCapabilityPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.ChoicePseudoStateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.FinalStateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.ForkPseudoStateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.InitialPseudoStateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.JoinPseudoStateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.ModeItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.StateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.StateMachineItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacommon.TerminatePseudoStateItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.AbstractPropertyValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.BooleanPropertyValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.ConstraintItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.EnumerationPropertyValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.FloatPropertyValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.IntegerPropertyValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.NamingRuleItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.PropertyValueGroupItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellacore.StringPropertyValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.capellamodeller.SystemEngineeringPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.cs.InterfaceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.cs.InterfacePkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.cs.PartItemContribution;
import org.polarsys.capella.core.data.menu.contributions.cs.PhysicalLinkEndItemContribution;
import org.polarsys.capella.core.data.menu.contributions.cs.PhysicalLinkItemContribution;
import org.polarsys.capella.core.data.menu.contributions.cs.PhysicalPortItemContribution;
import org.polarsys.capella.core.data.menu.contributions.ctx.CapabilityItemContribution;
import org.polarsys.capella.core.data.menu.contributions.ctx.CapabilityPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.ctx.MissionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.ctx.SystemAnalysisItemContribution;
import org.polarsys.capella.core.data.menu.contributions.ctx.SystemFunctionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.ctx.SystemFunctionPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.epbs.ConfigurationItemItemContribution;
import org.polarsys.capella.core.data.menu.contributions.epbs.EPBSArchitectureItemContribution;
import org.polarsys.capella.core.data.menu.contributions.epbs.EPBSArchitecturePkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.AbstractFunctionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.ComponentExchangeItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.ComponentPortItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionInputPortItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionOutputPortItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionSpecificationItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionalChainInvolvementItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionalChainItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionalExchangeItemContribution;
import org.polarsys.capella.core.data.menu.contributions.fa.FunctionalExchangeSpecificationItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.AbstractInstanceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.BinaryExpressionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.BooleanReferenceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.BooleanTypeItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ClassItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.CollectionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.CollectionValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.CollectionValueReferenceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ComplexValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ComplexValueReferenceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.DomainElementItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.EnumerationItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.EnumerationLiteralItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.EnumerationReferenceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ExchangeItemElementItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ExchangeItemInstanceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ExchangeItemItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.LiteralBooleanValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.LiteralNumericValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.LiteralStringValueItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.NumericReferenceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.NumericTypeItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ParameterItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.PhysicalQuantityItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.PropertyItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.ServiceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.SignalInstanceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.SignalItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.StringReferenceItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.UnaryExpressionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.UnionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.information.UnionPropertyItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.ConstraintDurationItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.EventItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.InstanceRoleItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.InteractionFragmentItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.ScenarioItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.SequenceMessageItemContribution;
import org.polarsys.capella.core.data.menu.contributions.interaction.TimeLapseItemContribution;
import org.polarsys.capella.core.data.menu.contributions.la.CapabilityRealizationPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.la.LogicalArchitectureItemContribution;
import org.polarsys.capella.core.data.menu.contributions.la.LogicalArchitecturePkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.la.LogicalComponentItemContribution;
import org.polarsys.capella.core.data.menu.contributions.la.LogicalFunctionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.la.LogicalFunctionPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.CapabilityConfigurationItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.CommunicationMeanItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.ConceptItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.ConceptPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.EntityItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.ItemInConceptItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.LocationItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.OperationalActivityItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.OperationalActivityPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.OperationalAnalysisItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.OperationalCapabilityPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.OperationalProcessItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.RoleAssemblyUsageItemContribution;
import org.polarsys.capella.core.data.menu.contributions.oa.RoleItemContribution;
import org.polarsys.capella.core.data.menu.contributions.pa.PhysicalArchitectureItemContribution;
import org.polarsys.capella.core.data.menu.contributions.pa.PhysicalArchitecturePkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.pa.PhysicalComponentItemContribution;
import org.polarsys.capella.core.data.menu.contributions.pa.PhysicalFunctionItemContribution;
import org.polarsys.capella.core.data.menu.contributions.pa.PhysicalFunctionPkgItemContribution;
import org.polarsys.capella.core.data.menu.contributions.pa.PhysicalNodeItemContribution;
import org.polarsys.capella.core.data.modeller.properties.sections.SystemEngineeringPkgSection;
import org.polarsys.capella.core.data.modeller.properties.sections.SystemEngineeringSection;
import org.polarsys.capella.core.data.oa.OaPackage;
import org.polarsys.capella.core.data.oa.properties.sections.ActivityAllocationSection;
import org.polarsys.capella.core.data.oa.properties.sections.CapabilityConfigurationSection;
import org.polarsys.capella.core.data.oa.properties.sections.CommunicationMeanSection;
import org.polarsys.capella.core.data.oa.properties.sections.ConceptPkgSection;
import org.polarsys.capella.core.data.oa.properties.sections.ConceptSection;
import org.polarsys.capella.core.data.oa.properties.sections.EntityOperationalCapabilityInvolvementSection;
import org.polarsys.capella.core.data.oa.properties.sections.EntitySection;
import org.polarsys.capella.core.data.oa.properties.sections.ItemInConceptSection;
import org.polarsys.capella.core.data.oa.properties.sections.LocationSection;
import org.polarsys.capella.core.data.oa.properties.sections.OperationalActivityPkgSection;
import org.polarsys.capella.core.data.oa.properties.sections.OperationalActivitySection;
import org.polarsys.capella.core.data.oa.properties.sections.OperationalAnalysisSection;
import org.polarsys.capella.core.data.oa.properties.sections.OperationalCapabilitySection;
import org.polarsys.capella.core.data.oa.properties.sections.OperationalProcessSection;
import org.polarsys.capella.core.data.oa.properties.sections.RoleAllocationSection;
import org.polarsys.capella.core.data.oa.properties.sections.RoleAssemblyUsageSection;
import org.polarsys.capella.core.data.oa.properties.sections.RoleSection;
import org.polarsys.capella.core.data.oa.properties.sections.SwimlaneSection;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.core.data.pa.deployment.DeploymentPackage;
import org.polarsys.capella.core.data.pa.deployment.properties.sections.ComponentInstanceSection;
import org.polarsys.capella.core.data.pa.deployment.properties.sections.PortInstanceSection;
import org.polarsys.capella.core.data.pa.properties.sections.LogicalArchitectureRealizationSection;
import org.polarsys.capella.core.data.pa.properties.sections.LogicalInterfaceRealizationSection;
import org.polarsys.capella.core.data.pa.properties.sections.PhysicalArchitecturePkgSection;
import org.polarsys.capella.core.data.pa.properties.sections.PhysicalArchitectureSection;
import org.polarsys.capella.core.data.pa.properties.sections.PhysicalComponentSection;
import org.polarsys.capella.core.data.pa.properties.sections.PhysicalFunctionPkgSection;
import org.polarsys.capella.core.data.pa.properties.sections.PhysicalFunctionSection;
import org.polarsys.capella.core.data.pa.properties.sections.PhysicalNodeSection;

/**
 * Register of helpers.
 *
 * @author nperansin
 */
public class RegisterHelperPattern {

    // We now care about order, Info objects are used to build a kind of emf
    // switch
    private static final Map<EClass, HelperInfo> INFOS = new LinkedHashMap<>();

    // @See https://github.com/eclipse/capella-studio/blob/master/
    //   extension/plugins/org.polarsys.capella.extension.tools/generated/
    //   org/polarsys/capella/extension/tools/patterns/model/generation/RegisterHelperPattern.java
    static {

        registerHelper(CsPackage.eINSTANCE.getArchitectureAllocation(),
            ArchitectureAllocationHelper.class);
        registerHelper(CsPackage.eINSTANCE.getBlockArchitecture(),
            BlockArchitectureHelper.class);

        registerHelperAndSection(CsPackage.eINSTANCE.getComponent(),
            ComponentHelper.class,
            ComponentSection.class);
        registerHelper(CsPackage.eINSTANCE.getDeployableElement(),
            DeployableElementHelper.class);
        registerHelperAndSection(
            CsPackage.eINSTANCE.getExchangeItemAllocation(),
            ExchangeItemAllocationHelper.class, ExchangeItemAllocationSection.class);
        registerHelper(CsPackage.eINSTANCE.getInterfaceAllocation(),
            InterfaceAllocationHelper.class);
        registerHelper(CsPackage.eINSTANCE.getInterfaceAllocator(),
            InterfaceAllocatorHelper.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getInterface(),
            InterfaceHelper.class,
            InterfaceItemContribution.class, InterfaceSection.class);
        registerHelperAndSection(
            CsPackage.eINSTANCE.getInterfaceImplementation(),
            InterfaceImplementationHelper.class, InterfaceImplementationSection.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getInterfaceUse(),
            InterfaceUseHelper.class,
            InterfaceUseSection.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getPart(),
            PartHelper.class,
            PartItemContribution.class, PartSection.class);

        registerHelperAndSection(CsPackage.eINSTANCE.getInterfacePkg(), null,
            InterfacePkgItemContribution.class, InterfacePkgSection.class);

        registerHelperAndSection(CtxPackage.eINSTANCE.getCapability(),
            CapabilityHelper.class,
            CapabilityItemContribution.class, CapabilitySection.class);
        registerHelperAndSection(CtxPackage.eINSTANCE.getMission(),
            MissionHelper.class,
            MissionItemContribution.class, MissionSection.class);

        registerHelperAndSection(
            CtxPackage.eINSTANCE.getOperationalAnalysisRealization(),
            OperationalAnalysisRealizationHelper.class,
            OperationalAnalysisRealizationSection.class);

        registerHelper(CtxPackage.eINSTANCE.getSystemAnalysis(),
            SystemAnalysisHelper.class,
            SystemAnalysisItemContribution.class);

        registerHelperAndSection(CtxPackage.eINSTANCE.getSystemFunction(),
            SystemFunctionHelper.class, SystemFunctionItemContribution.class,
            SystemFunctionSection.class);

        registerHelper(CtxPackage.eINSTANCE.getCapabilityPkg(), null,
            CapabilityPkgItemContribution.class);
        registerHelperAndSection(CtxPackage.eINSTANCE.getSystemFunctionPkg(),
            null,
            SystemFunctionPkgItemContribution.class, SystemFunctionPkgSection.class);
        registerHelperAndSection(EpbsPackage.eINSTANCE.getConfigurationItem(),
            ConfigurationItemHelper.class, ConfigurationItemItemContribution.class,
            ConfigurationItemSection.class);
        registerHelperAndSection(EpbsPackage.eINSTANCE.getEPBSArchitecture(),
            EPBSArchitectureHelper.class, EPBSArchitectureItemContribution.class,
            EPBSArchitectureSection.class);

        registerHelperAndSection(
            EpbsPackage.eINSTANCE.getPhysicalArchitectureRealization(),
            PhysicalArchitectureRealizationHelper.class,
            PhysicalArchitectureRealizationSection.class);
        registerHelperAndSection(
            EpbsPackage.eINSTANCE.getPhysicalArtifactRealization(),
            PhysicalArtifactRealizationHelper.class, PhysicalArtifactRealizationSection.class);
        registerHelperAndSection(
            EpbsPackage.eINSTANCE.getEPBSArchitecturePkg(), null,
            EPBSArchitecturePkgItemContribution.class, EPBSArchitecturePkgSection.class);
        registerHelper(FaPackage.eINSTANCE.getAbstractFunctionalBlock(),
            AbstractFunctionalBlockHelper.class);
        registerHelper(FaPackage.eINSTANCE.getAbstractFunctionAllocation(),
            AbstractFunctionAllocationHelper.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getAbstractFunction(),
            AbstractFunctionHelper.class, AbstractFunctionItemContribution.class,
            AbstractFunctionSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getComponentExchangeEnd(),
            ComponentExchangeEndHelper.class, ComponentExchangeItemContribution.class,
            ComponentExchangeEndSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getComponentExchangeFunctionalExchangeAllocation(),
            ComponentExchangeFunctionalExchangeAllocationHelper.class,
            ComponentExchangeFunctionalExchangeAllocationSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getComponentExchange(),
            ComponentExchangeHelper.class, ComponentExchangeSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getComponentExchangeRealization(),
            ComponentExchangeRealizationHelper.class, ComponentExchangeRealizationSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getComponentFunctionalAllocation(),
            ComponentFunctionalAllocationHelper.class, ComponentFunctionalAllocationSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getComponentPort(),
            ComponentPortHelper.class,
            ComponentPortItemContribution.class, ComponentPortSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getExchangeLink(),
            ExchangeLinkHelper.class,
            ExchangeLinkSection.class);
        registerHelper(FaPackage.eINSTANCE.getExchangeSpecification(),
            ExchangeSpecificationHelper.class);
        registerHelper(
            FaPackage.eINSTANCE.getExchangeSpecificationRealization(),
            ExchangeSpecificationRealizationHelper.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getFunctionalChain(),
            FunctionalChainHelper.class, FunctionalChainItemContribution.class,
            FunctionalChainSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getFunctionalChainInvolvement(),
            FunctionalChainInvolvementHelper.class,
            FunctionalChainInvolvementItemContribution.class,
            FunctionalChainInvolvementSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getFunctionalExchange(),
            FunctionalExchangeHelper.class, FunctionalExchangeItemContribution.class,
            FunctionalExchangeSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getFunctionalExchangeRealization(),
            FunctionalExchangeRealizationHelper.class, FunctionalExchangeRealizationSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getFunctionalExchangeSpecification(),
            FunctionalExchangeSpecificationHelper.class,
            FunctionalExchangeSpecificationItemContribution.class,
            FunctionalExchangeSpecificationSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getFunctionInputPort(),
            FunctionInputPortHelper.class, FunctionInputPortItemContribution.class,
            FunctionInputPortSection.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getFunctionOutputPort(),
            FunctionOutputPortHelper.class, FunctionOutputPortItemContribution.class,
            FunctionOutputPortSection.class);
        registerHelper(FaPackage.eINSTANCE.getFunctionPort(),
            FunctionPortHelper.class);
        registerHelperAndSection(FaPackage.eINSTANCE.getFunctionRealization(),
            FunctionRealizationHelper.class, FunctionRealizationSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getFunctionSpecification(),
            FunctionSpecificationHelper.class, FunctionSpecificationItemContribution.class,
            FunctionSpecificationSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getBinaryExpression(),
            BinaryExpressionHelper.class, BinaryExpressionItemContribution.class,
            BinaryExpressionSection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getClass_(),
            ClassHelper.class,
            ClassItemContribution.class, ClassSection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getCollection(),
            CollectionHelper.class, CollectionItemContribution.class, CollectionSection.class);
        registerHelperAndSection(
            CommunicationPackage.eINSTANCE.getCommunicationItem(),
            CommunicationItemHelper.class, CommunicationItemSection.class);
        registerHelper(
            CommunicationPackage.eINSTANCE.getCommunicationLinkExchanger(),
            CommunicationLinkExchangerHelper.class);
        registerHelperAndSection(
            CommunicationPackage.eINSTANCE.getCommunicationLink(),
            CommunicationLinkHelper.class, CommunicationLinkSection.class);
        registerHelperAndSection(DatatypePackage.eINSTANCE.getDataType(),
            DataTypeHelper.class,
            DataTypeSection.class);
        registerHelperAndSection(DatavaluePackage.eINSTANCE.getDataValue(),
            DataValueHelper.class,
            DataValueSection.class);
        registerHelper(InformationPackage.eINSTANCE.getExchangeItemElement(),
            ExchangeItemElementHelper.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getExchangeItem(),
            ExchangeItemHelper.class, ExchangeItemItemContribution.class,
            ExchangeItemSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getExchangeItemRealization(),
            ExchangeItemRealizationHelper.class, ExchangeItemRealizationSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getOperationAllocation(),
            OperationAllocationHelper.class, OperationAllocationSection.class);
        registerHelper(InformationPackage.eINSTANCE.getOperation(),
            OperationHelper.class);

        registerHelperAndSection(
            InformationPackage.eINSTANCE.getPortAllocation(),
            PortAllocationHelper.class, PortAllocationSection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getPort(),
            PortHelper.class,
            PortSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getPortRealization(),
            PortRealizationHelper.class, PortRealizationSection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getProperty(),
            PropertyHelper.class,
            PropertySection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getService(),
            ServiceHelper.class,
            ServiceSection.class);
        registerHelper(InformationPackage.eINSTANCE.getAbstractInstance(),
            null,
            AbstractInstanceItemContribution.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getBooleanReference(), null,
            BooleanReferenceItemContribution.class, BooleanReferenceSection.class);
        registerHelperAndSection(DatatypePackage.eINSTANCE.getBooleanType(),
            null,
            BooleanTypeItemContribution.class, BooleanTypeSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getCollectionValue(), null,
            CollectionValueItemContribution.class, CollectionValueSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getCollectionValueReference(), null,
            CollectionValueReferenceItemContribution.class, CollectionValueReferenceSection.class);
        registerHelperAndSection(DatavaluePackage.eINSTANCE.getComplexValue(),
            null,
            ComplexValueItemContribution.class, ComplexValueSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getComplexValueReference(), null,
            ComplexValueReferenceItemContribution.class, ComplexValueReferenceSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getDomainElement(), null,
            DomainElementItemContribution.class, DomainElementSection.class);
        registerHelperAndSection(DatatypePackage.eINSTANCE.getEnumeration(),
            null,
            EnumerationItemContribution.class, EnumerationSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getEnumerationLiteral(), null,
            EnumerationLiteralItemContribution.class, EnumerationLiteralSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getEnumerationReference(), null,
            EnumerationReferenceItemContribution.class, EnumerationReferenceSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getExchangeItemElement(), null,
            ExchangeItemElementItemContribution.class, ExchangeItemElementSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getExchangeItemInstance(), null,
            ExchangeItemInstanceItemContribution.class, ExchangeItemInstanceSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getLiteralBooleanValue(), null,
            LiteralBooleanValueItemContribution.class, LiteralBooleanValueSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getLiteralNumericValue(), null,
            LiteralNumericValueItemContribution.class, LiteralNumericValueSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getLiteralStringValue(), null,
            LiteralStringValueItemContribution.class, LiteralStringValueSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getNumericReference(), null,
            NumericReferenceItemContribution.class, NumericReferenceSection.class);
        registerHelperAndSection(DatatypePackage.eINSTANCE.getNumericType(),
            null,
            NumericTypeItemContribution.class, NumericTypeSection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getParameter(),
            null,
            ParameterItemContribution.class, ParameterSection.class);
        registerHelperAndSection(
            DatatypePackage.eINSTANCE.getPhysicalQuantity(), null,
            PhysicalQuantityItemContribution.class, PhysicalQuantitySection.class);
        registerHelper(InformationPackage.eINSTANCE.getProperty(), null,
            PropertyItemContribution.class);
        registerHelper(InformationPackage.eINSTANCE.getService(), null,
            ServiceItemContribution.class);
        registerHelperAndSection(
            CommunicationPackage.eINSTANCE.getSignalInstance(), null,
            SignalInstanceItemContribution.class, SignalInstanceSection.class);
        registerHelperAndSection(CommunicationPackage.eINSTANCE.getSignal(),
            null,
            SignalItemContribution.class, SignalSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getStringReference(), null,
            StringReferenceItemContribution.class, StringReferenceSection.class);
        registerHelperAndSection(
            DatavaluePackage.eINSTANCE.getUnaryExpression(), null,
            UnaryExpressionItemContribution.class, UnaryExpressionSection.class);
        registerHelperAndSection(InformationPackage.eINSTANCE.getUnion(), null,
            UnionItemContribution.class, UnionSection.class);
        registerHelperAndSection(
            InformationPackage.eINSTANCE.getUnionProperty(), null,
            UnionPropertyItemContribution.class, UnionPropertySection.class);
        registerHelper(DatavaluePackage.eINSTANCE.getUnaryExpression(),
            UnaryExpressionHelper.class);
        registerHelper(InteractionPackage.eINSTANCE.getAbstractCapability(),
            AbstractCapabilityHelper.class);
        registerHelper(
            InteractionPackage.eINSTANCE.getAbstractCapabilityRealization(),
            AbstractCapabilityRealizationHelper.class);
        registerHelper(InteractionPackage.eINSTANCE.getAbstractEnd(),
            AbstractEndHelper.class);
        registerHelper(
            InteractionPackage.eINSTANCE.getAbstractFunctionAbstractCapabilityInvolvement(),
            AbstractFunctionAbstractCapabilityInvolvementHelper.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getCombinedFragment(),
            CombinedFragmentHelper.class, CombinedFragmentSection.class);
        registerHelper(InteractionPackage.eINSTANCE.getEvent(),
            EventHelper.class,
            EventItemContribution.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getExecutionEnd(),
            ExecutionEndHelper.class, ExecutionEndSection.class);
        registerHelperAndSection(InteractionPackage.eINSTANCE.getExecution(),
            ExecutionHelper.class, ExecutionSection.class);
        registerHelperAndSection(InteractionPackage.eINSTANCE.getFragmentEnd(),
            FragmentEndHelper.class, FragmentEndSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getFunctionalChainAbstractCapabilityInvolvement(),
            FunctionalChainAbstractCapabilityInvolvementHelper.class,
            FunctionalChainAbstractCapabilityInvolvementSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getInstanceRole(),
            InstanceRoleHelper.class, InstanceRoleItemContribution.class,
            InstanceRoleSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getInteractionState(),
            InteractionStateHelper.class, InteractionStateSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getInteractionUse(),
            InteractionUseHelper.class, InteractionUseSection.class);
        registerHelperAndSection(InteractionPackage.eINSTANCE.getMessageEnd(),
            MessageEndHelper.class, MessageEndSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getScenarioRealization(),
            ScenarioRealizationHelper.class, ScenarioRealizationSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getSequenceMessage(),
            SequenceMessageHelper.class, SequenceMessageItemContribution.class,
            SequenceMessageSection.class);
        registerHelperAndSection(InteractionPackage.eINSTANCE.getScenario(),
            null,
            ScenarioItemContribution.class, ScenarioSection.class);
        registerHelperAndSection(
            InteractionPackage.eINSTANCE.getConstraintDuration(), null,
            ConstraintDurationItemContribution.class, ConstraintDurationSection.class);
        registerHelper(InteractionPackage.eINSTANCE.getInteractionFragment(),
            null,
            InteractionFragmentItemContribution.class);
        registerHelper(InteractionPackage.eINSTANCE.getTimeLapse(), null,
            TimeLapseItemContribution.class);
        registerHelperAndSection(
            LaPackage.eINSTANCE.getCapabilityRealizationPkg(), null,
            CapabilityRealizationPkgItemContribution.class, CapabilityRealizationPkgSection.class);
        registerHelperAndSection(
            LaPackage.eINSTANCE.getCapabilityRealization(),
            CapabilityRealizationHelper.class, CapabilityRealizationSection.class);
        registerHelperAndSection(
            LaPackage.eINSTANCE.getContextInterfaceRealization(),
            ContextInterfaceRealizationHelper.class, ContextInterfaceRealizationSection.class);
        registerHelperAndSection(LaPackage.eINSTANCE.getLogicalArchitecture(),
            LogicalArchitectureHelper.class, LogicalArchitectureItemContribution.class,
            LogicalArchitectureSection.class);

        registerHelperAndSection(LaPackage.eINSTANCE.getLogicalComponent(),
            LogicalComponentHelper.class, LogicalComponentItemContribution.class,
            LogicalComponentSection.class);

        registerHelperAndSection(LaPackage.eINSTANCE.getLogicalFunction(),
            LogicalFunctionHelper.class, LogicalFunctionItemContribution.class,
            LogicalFunctionSection.class);

        registerHelperAndSection(
            LaPackage.eINSTANCE.getLogicalArchitecturePkg(), null,
            LogicalArchitecturePkgItemContribution.class, LogicalArchitecturePkgSection.class);

        registerHelperAndSection(
            LaPackage.eINSTANCE.getSystemAnalysisRealization(),
            org.polarsys.capella.core.data.helpers.la.delegates.SystemAnalysisRealizationHelper.class,
            SystemAnalysisRealizationSection.class);

        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getAbstractState(),
            AbstractStateHelper.class, AbstractStateSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getAbstractStateRealization(),
            AbstractStateRealizationHelper.class, AbstractStateRealizationSection.class);
        registerHelper(
            CapellacommonPackage.eINSTANCE.getCapabilityRealizationInvolvedElement(),
            CapabilityRealizationInvolvedElementHelper.class);
        registerHelper(
            CapellacommonPackage.eINSTANCE.getCapabilityRealizationInvolvement(),
            CapabilityRealizationInvolvementHelper.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getGenericTrace(),
            GenericTraceHelper.class, GenericTraceSection.class);
        registerHelper(
            CapellacommonPackage.eINSTANCE.getStateTransitionRealization(),
            StateTransitionRealizationHelper.class);
        registerHelper(LaPackage.eINSTANCE.getLogicalFunctionPkg(), null,
            LogicalFunctionPkgItemContribution.class);
        registerHelper(
            CapellacommonPackage.eINSTANCE.getAbstractCapabilityPkg(), null,
            AbstractCapabilityPkgItemContribution.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getChoicePseudoState(), null,
            ChoicePseudoStateItemContribution.class, ChoicePseudoStateSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getFinalState(), null,
            FinalStateItemContribution.class, FinalStateSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getForkPseudoState(), null,
            ForkPseudoStateItemContribution.class, ForkPseudoStateSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getInitialPseudoState(), null,
            InitialPseudoStateItemContribution.class, InitialPseudoStateSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getJoinPseudoState(), null,
            JoinPseudoStateItemContribution.class, JoinPseudoStateSection.class);
        registerHelperAndSection(CapellacommonPackage.eINSTANCE.getMode(),
            null,
            ModeItemContribution.class, ModeSection.class);
        registerHelperAndSection(CapellacommonPackage.eINSTANCE.getState(),
            StateHelper.class,
            StateItemContribution.class, StateSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getStateMachine(), null,
            StateMachineItemContribution.class, StateMachineSection.class);
        registerHelperAndSection(
            CapellacommonPackage.eINSTANCE.getTerminatePseudoState(), null,
            TerminatePseudoStateItemContribution.class, TerminatePseudoStateSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getAbstractPropertyValue(),
            AbstractPropertyValueHelper.class, AbstractPropertyValueItemContribution.class);
        registerHelperAndSection(CapellacorePackage.eINSTANCE.getAllocation(),
            AllocationHelper.class, AllocationSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getFeature(),
            FeatureHelper.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getGeneralizableElement(),
            GeneralizableElementHelper.class, GeneralizableElementSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getInvolvedElement(),
            InvolvedElementHelper.class);
        registerHelper(CapellacorePackage.eINSTANCE.getInvolvement(),
            InvolvementHelper.class);
        registerHelper(CapellacorePackage.eINSTANCE.getInvolverElement(),
            InvolverElementHelper.class);
        registerHelper(CapellacorePackage.eINSTANCE.getCapellaElement(),
            CapellaElementHelper.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getNamedElement(),
            NamedElementHelper.class, null, DefaultNamedElementSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getNamedRelationship(),
            NamedRelationshipHelper.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getPropertyValueGroup(),
            PropertyValueGroupHelper.class, PropertyValueGroupItemContribution.class,
            PropertyValueGroupSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getRelationship(),
            RelationshipHelper.class);
        registerHelperAndSection(CapellacorePackage.eINSTANCE.getStructure(),
            StructureHelper.class, StructureSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getTrace(),
            TraceHelper.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getTypedElement(),
            TypedElementHelper.class, TypedElementSection.class);
        registerHelper(CapellacorePackage.eINSTANCE.getType(),
            TypeHelper.class);
        registerHelperAndSection(
            CapellamodellerPackage.eINSTANCE.getSystemEngineering(),
            SystemEngineeringHelper.class, SystemEngineeringSection.class);
        registerHelperAndSection(
            CapellamodellerPackage.eINSTANCE.getSystemEngineeringPkg(), null,
            SystemEngineeringPkgItemContribution.class, SystemEngineeringPkgSection.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getBooleanPropertyValue(), null,
            BooleanPropertyValueItemContribution.class, BooleanPropertyValueSection.class);
        registerHelperAndSection(CapellacorePackage.eINSTANCE.getConstraint(),
            null,
            ConstraintItemContribution.class, ConstraintSection.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getEnumerationPropertyValue(), null,
            EnumerationPropertyValueItemContribution.class, EnumerationPropertyValueSection.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getFloatPropertyValue(), null,
            FloatPropertyValueItemContribution.class, FloatPropertyValueSection.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getIntegerPropertyValue(), null,
            IntegerPropertyValueItemContribution.class, IntegerPropertyValueSection.class);
        registerHelperAndSection(CapellacorePackage.eINSTANCE.getNamingRule(),
            null,
            NamingRuleItemContribution.class, NamingRuleSection.class);
        registerHelperAndSection(
            CapellacorePackage.eINSTANCE.getStringPropertyValue(), null,
            StringPropertyValueItemContribution.class, StringPropertyValueSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getActivityAllocation(),
            ActivityAllocationHelper.class, ActivityAllocationSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getEntity(),
            EntityHelper.class,
            EntityItemContribution.class, EntitySection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getCommunicationMean(),
            null,
            CommunicationMeanItemContribution.class, CommunicationMeanSection.class);
        registerHelperAndSection(
            OaPackage.eINSTANCE.getEntityOperationalCapabilityInvolvement(),
            EntityOperationalCapabilityInvolvementHelper.class,
            EntityOperationalCapabilityInvolvementSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getOperationalAnalysis(),
            OperationalAnalysisHelper.class, OperationalAnalysisSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getOperationalActivity(),
            OperationalActivityHelper.class, OperationalActivityItemContribution.class,
            OperationalActivitySection.class);

        registerHelperAndSection(
            OaPackage.eINSTANCE.getOperationalCapability(),
            OperationalCapabilityHelper.class, OperationalCapabilitySection.class);

        registerHelperAndSection(OaPackage.eINSTANCE.getOperationalProcess(),
            OperationalProcessHelper.class, OperationalProcessItemContribution.class,
            OperationalProcessSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getRoleAllocation(),
            RoleAllocationHelper.class, RoleAllocationSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getRole(),
            RoleHelper.class,
            RoleItemContribution.class, RoleSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getSwimlane(),
            SwimlaneHelper.class,
            SwimlaneSection.class);
        registerHelperAndSection(
            OaPackage.eINSTANCE.getCapabilityConfiguration(), null,
            CapabilityConfigurationItemContribution.class, CapabilityConfigurationSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getConcept(), null,
            ConceptItemContribution.class, ConceptSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getConceptPkg(), null,
            ConceptPkgItemContribution.class, ConceptPkgSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getItemInConcept(), null,
            ItemInConceptItemContribution.class, ItemInConceptSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getLocation(), null,
            LocationItemContribution.class, LocationSection.class);
        registerHelperAndSection(
            OaPackage.eINSTANCE.getOperationalActivityPkg(), null,
            OperationalActivityPkgItemContribution.class, OperationalActivityPkgSection.class);
        registerHelper(OaPackage.eINSTANCE.getOperationalAnalysis(), null,
            OperationalAnalysisItemContribution.class);
        registerHelper(OaPackage.eINSTANCE.getOperationalCapabilityPkg(), null,
            OperationalCapabilityPkgItemContribution.class);

        registerHelper(CsPackage.eINSTANCE.getAbstractPhysicalLinkEnd(),
            AbstractPhysicalLinkEndHelper.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getComponentExchangeAllocation(),
            ComponentExchangeAllocationHelper.class, ComponentExchangeAllocationSection.class);
        registerHelperAndSection(
            FaPackage.eINSTANCE.getComponentExchangeAllocator(),
            ComponentExchangeAllocatorHelper.class, ComponentExchangeAllocatorSection.class);
        registerHelperAndSection(
            DeploymentPackage.eINSTANCE.getComponentInstance(),
            ComponentInstanceHelper.class, ComponentInstanceSection.class);

        registerHelperAndSection(
            PaPackage.eINSTANCE.getLogicalArchitectureRealization(),
            LogicalArchitectureRealizationHelper.class,
            LogicalArchitectureRealizationSection.class);

        registerHelperAndSection(
            PaPackage.eINSTANCE.getLogicalInterfaceRealization(),
            LogicalInterfaceRealizationHelper.class, LogicalInterfaceRealizationSection.class);
        registerHelper(PaPackage.eINSTANCE.getPhysicalArchitecture(),
            PhysicalArchitectureHelper.class);

        registerHelperAndSection(PaPackage.eINSTANCE.getPhysicalComponent(),
            PhysicalComponentHelper.class, PhysicalComponentItemContribution.class,
            PhysicalComponentSection.class);

        registerHelperAndSection(PaPackage.eINSTANCE.getPhysicalFunction(),
            PhysicalFunctionHelper.class, PhysicalFunctionItemContribution.class,
            PhysicalFunctionSection.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getPhysicalLinkEnd(),
            PhysicalLinkEndHelper.class, PhysicalLinkEndItemContribution.class,
            PhysicalLinkEndSection.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getPhysicalLink(),
            PhysicalLinkHelper.class,
            PhysicalLinkItemContribution.class, PhysicalLinkSection.class);
        registerHelperAndSection(PaPackage.eINSTANCE.getPhysicalNode(),
            PhysicalNodeHelper.class,
            PhysicalNodeItemContribution.class, PhysicalNodeSection.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getPhysicalPath(),
            PhysicalPathHelper.class,
            PhysicalPathSection.class);
        registerHelperAndSection(
            CsPackage.eINSTANCE.getPhysicalPathInvolvement(),
            PhysicalPathInvolvementHelper.class, PhysicalPathInvolvementSection.class);
        registerHelperAndSection(CsPackage.eINSTANCE.getPhysicalPort(),
            PhysicalPortHelper.class,
            PhysicalPortItemContribution.class, PhysicalPortSection.class);
        registerHelperAndSection(DeploymentPackage.eINSTANCE.getPortInstance(),
            PortInstanceHelper.class, PortInstanceSection.class);
        registerHelperAndSection(OaPackage.eINSTANCE.getRoleAssemblyUsage(),
            null,
            RoleAssemblyUsageItemContribution.class, RoleAssemblyUsageSection.class);

        registerHelperAndSection(PaPackage.eINSTANCE.getPhysicalArchitecture(),
            null,
            PhysicalArchitectureItemContribution.class, PhysicalArchitectureSection.class);
        registerHelperAndSection(
            PaPackage.eINSTANCE.getPhysicalArchitecturePkg(), null,
            PhysicalArchitecturePkgItemContribution.class, PhysicalArchitecturePkgSection.class);
        registerHelperAndSection(PaPackage.eINSTANCE.getPhysicalFunctionPkg(),
            null,
            PhysicalFunctionPkgItemContribution.class, PhysicalFunctionPkgSection.class);
        registerHelper(ActivityPackage.eINSTANCE.getAbstractActivity(),
            AbstractActivityHelper.class);
        registerHelper(ActivityPackage.eINSTANCE.getActivityEdge(),
            ActivityEdgeHelper.class);
        registerHelper(ActivityPackage.eINSTANCE.getActivityExchange(),
            ActivityExchangeHelper.class);
        registerHelper(ActivityPackage.eINSTANCE.getActivityNode(),
            ActivityNodeHelper.class);
        registerHelper(ActivityPackage.eINSTANCE.getActivityPartition(),
            ActivityPartitionHelper.class);
        registerHelper(ModellingcorePackage.eINSTANCE.getAbstractType(),
            AbstractTypeHelper.class);
        registerHelper(
            ModellingcorePackage.eINSTANCE.getInformationsExchanger(),
            InformationsExchangerHelper.class);
        registerHelperAndSection(
            ModellingcorePackage.eINSTANCE.getModelElement(),
            ModelElementHelper.class, ModelElementSection.class);
        registerHelper(ModellingcorePackage.eINSTANCE.getTraceableElement(),
            TraceableElementHelper.class);

    }

    public static void registerHelperAndSection(
            EClass eclass,
            Class<?> helperClass, Class<? extends ISection> sectionClass) {
        registerHelper(eclass, helperClass, null, sectionClass);
    }

    public static void registerHelperAndSection(
            EClass eclass,
            Class<?> helperClass, Class<?> menuContributorBaseClass,
            Class<? extends ISection> sectionClass) {
        registerHelper(eclass, helperClass, menuContributorBaseClass, sectionClass);
    }

    public static void registerHelper(EClass eclass, Class<?> helperClass) {
        registerHelper(eclass, helperClass, null, null);
    }

    public static void registerHelper(
            EClass eclass, Class<?> helperClass, Class<?> menuContributorBaseClass) {
        registerHelper(eclass, helperClass, menuContributorBaseClass, null);
    }

    static Class<? extends ISection> filterSectionClass(Class<? extends ISection> sectionClass) {
        return sectionClass != null && !Modifier.isAbstract(sectionClass.getModifiers())
                ? sectionClass
                : null;
    }
    
    private static void registerHelper(
            EClass eclass,
            Class<?> helperClass, Class<?> menuContributorBaseClass,
            Class<? extends ISection> sectionClass) {

        HelperInfo info = null;

        info = new HelperInfo(eclass, helperClass,
                menuContributorBaseClass, sectionClass);
        INFOS.put(eclass, info);
    }

    public static Class<?> getInheritedSection(EClass source) {
        return getHelperInfo(source, HelperInfo::getSectionClass);
    }

    public static Class<?> getInheritedMenuContributor(EClass source) {
        return getHelperInfo(source, HelperInfo::getMenuContributorBaseClass);
    }

    public static Class<?> getInheritedHelper(EClass source) {
        return getHelperInfo(source, HelperInfo::getHelperClass);
    }

    public static Class<?> getHelperInfo(EClass source, Function<HelperInfo, Class<?>> access) {
        return HelperInfo.getLinearSuperTypes(source) // Assume the order is applicable
            .stream()
            .map(it -> INFOS.get(it))
            .filter(it -> it != null && access.apply(it) != null)
            .findFirst()
            .map(access)
            .orElse(null);
    }


}