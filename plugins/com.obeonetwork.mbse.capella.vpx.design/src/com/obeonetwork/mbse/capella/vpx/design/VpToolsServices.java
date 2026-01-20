/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 * Obeo - initial API and implementation
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.obeonetwork.mbse.capella.vpx.design;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Services for capellavp.odesign.
 *
 * @author nperansin
 */
public class VpToolsServices {

    private static final String CP_MODEL_PLUGIN = "org.polarsys.capella.core.data.gen";

    private static final String URI_BASE = "platform:/plugin/" + CP_MODEL_PLUGIN + "/model/";

    // Ecore
    public static final String ECORE_ANNOTATION =
        "http://www.polarsys.org/kitalpha/emde/1.0.0/constraintMapping";

    public static final String ECORE_CLASS_KEY = "Mapping";

    // Qualified
    public static final String QN_ANNOTATION =
        "http://www.polarsys.org/kitalpha/emde/1.0.0/constraint";

    public static final String QN_CLASS_KEY = "ExtendedElement";

    public static final String EMDE_ECORE = "org.polarsys.kitalpha.emde/model/eMDE.ecore";

    public static final URI EMDE_EXTENSION_URI = URI // appendFragment avoids '#' encoding
        .createPlatformPluginURI(EMDE_ECORE, false)
        .appendFragment("//ElementExtension");

    private static final EcoreFactory EFCT = EcoreFactory.eINSTANCE;

    /** Module names for custom color. */
    public static final List<String> MODULE_NAMES = Arrays.asList(
        "OperationalAnalysis",
        "ContextArchitecture",
        "LogicalArchitecture",
        "PhysicalArchitecture",
        "EPBSArchitecture");

    /** Module ecore filename. */
    public static final List<String> MODULE_ECORES = MODULE_NAMES
        .stream()
        .map(it -> it + ".ecore")
        .collect(Collectors.toList());

    /**
     * Logs an AQL call in IO/Standard stream.
     * <p>
     * Should only be used during development time.
     * </p>
     *
     * @param it
     *     debugged element
     * @param message
     *     to print
     * @return provided element
     */
    @Deprecated
    public static final EObject toVsmDebug(EObject it, String message) {
        System.out.println("VSM-Call " + message + ": " + it); //$NON-NLS-1$ //$NON-NLS-2$
        return it;
    }

    /**
     * Evaluates if an EClass belongs to a specified Capella module.
     * <p>
     * When no Capella module is provided, we consider it as common parts.
     * </p>
     *
     * @param it
     *     class
     * @param module
     *     name of Capella
     * @return true if belongs
     */
    public static boolean isCapellaEcore(EClass it, String module) {
        Resource rs = it.eResource();
        if (rs == null) {
            return false;
        }
        URI resourceUri = it.eResource().getURI();
        if (!resourceUri.toString().startsWith(URI_BASE)) {
            return false;
        }

        String filename = resourceUri.lastSegment();
        // Questionable:
        // Should we list explicitly all known Capella modules
        return module == null
            ? !MODULE_ECORES.contains(filename)
            : filename.startsWith(module);
    }

    /**
     * Gets the extended EClass.
     *
     * @param source
     *     class
     * @return associated EClass
     */
    public static List<EClass> getEmdeExtensions(EClass source) {
        return findEAnnotation(source, ECORE_ANNOTATION)
            .map(anno -> splitToList(anno.getDetails().get(ECORE_CLASS_KEY)))
            .stream().flatMap(List::stream)
            .map(uri -> getEClass(source, uri))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Evaluates if an element is in read-only resources (plugin).
     *
     * @param it
     *     model
     * @return true in read-only resource
     */
    public static boolean isInLibrary(EModelElement it) {
        Resource res = it.eResource();
        return res != null && res.getURI().isPlatformPlugin();
    }

    private static Optional<EAnnotation> findEAnnotation(EClass container, String source) {
        return container.getEAnnotations()
            .stream()
            .filter(ann -> Objects.equals(source, ann.getSource()))
            .findFirst();
    }

    private static EAnnotation getEAnnotation(EClass container, String source, boolean required) {
        return findEAnnotation(container, source)
            .orElseGet(() -> required
                ? createEAnnotation(container, source)
                : null);
    }

    private static List<String> splitToList(String values) {
        return values != null && !values.isBlank()
            ? Arrays.asList(values.trim().split("\\s+"))
            : Collections.emptyList();
    }

    /**
     * Adds EMDE extensions targeting provided EClass.
     *
     * @param extension
     *     to annotate
     * @param extended
     *     targeted EClass
     * @return extension
     */
    public static EClass addEmdeExtension(EClass extension, EClass extended) {
        addToDetails(getEAnnotation(extension, QN_ANNOTATION, true),
            QN_CLASS_KEY, getQualifiedName(extended));
        addToDetails(getEAnnotation(extension, ECORE_ANNOTATION, true),
            ECORE_CLASS_KEY, EcoreUtil.getURI(extended).toString());

        // Ensure the class inherits from ElementExtension.
        EClass extensionClass = getEClass(extension, EMDE_EXTENSION_URI);
        if (!extension.getEAllSuperTypes().contains(extensionClass)) {
            extension.getESuperTypes().add(extensionClass);
        }

        return extension;
    }

    private static void addToDetails(EAnnotation container, String key, String reference) {
        EMap<String, String> details = container.getDetails();
        String values = details.get(key);
        List<String> references = splitToList(values);
        if (!references.contains(reference)) {
            String value = references.isEmpty()
                ? reference
                : values + ' ' + reference;
            details.put(key, value);
        }
    }

    /**
     * Removes EMDE extensions from provided EClass.
     *
     * @param extension
     *     to annotate
     * @param extended
     *     targeted EClass
     * @return extension
     */
    public static EClass removeEmdeExtension(EClass extension, EClass extended) {
        removeToDetails(getEAnnotation(extension, QN_ANNOTATION, false),
            QN_CLASS_KEY, getQualifiedName(extended));
        removeToDetails(getEAnnotation(extension, ECORE_ANNOTATION, false),
            ECORE_CLASS_KEY, EcoreUtil.getURI(extended).toString());

        return extension;
    }

    private static void removeToDetails(EAnnotation container, String key, String reference) {
        if (container == null) {
            return;
        }
        EMap<String, String> details = container.getDetails();
        String values = details.get(key);
        List<String> references = splitToList(values);
        if (references.contains(reference)) {
            if (references.size() > 1) {
                List<String> newReferences = new ArrayList<>(references);
                newReferences.remove(reference);
                String newValue = newReferences.stream().collect(Collectors.joining(" "));
                container.getDetails().put(key, newValue);
            } else if (details.size() > 1) {
                details.removeKey(key);
            } else {
                ((EClass) container.eContainer()).getEAnnotations().remove(container);
            }
        }
    }

    private static String getQualifiedName(EClass it) {
        return it.getEPackage().getNsURI() + '#' + it.eResource().getURIFragment(it);
    }

    private static EAnnotation createEAnnotation(EClass target, String source) {
        EAnnotation result = EFCT.createEAnnotation();
        result.setSource(source);
        target.getEAnnotations().add(result);
        return result;
    }

    /**
     * Evaluates if annotation is EMDE Constraint targeting the provided class.
     *
     * @param it
     *     annotation
     * @param target
     *     EClass of detail
     * @return true if is expected annotation
     */
    public static boolean isEmdeConstraintExtensionOf(EAnnotation it, EClass target) {
        return isEmdeExtensionOf(it, QN_ANNOTATION, QN_CLASS_KEY,
            getQualifiedName(target));
    }

    /**
     * Evaluates if annotation is EMDE Mapping targeting the provided class.
     *
     * @param it
     *     annotation
     * @param target
     *     EClass of detail
     * @return true if is expected annotation
     */
    public static boolean isEmdeMappingExtensionOf(EAnnotation it, EClass target) {
        return isEmdeExtensionOf(it, ECORE_ANNOTATION, ECORE_CLASS_KEY,
            EcoreUtil.getURI(target).toString());
    }

    private static boolean isEmdeExtensionOf(
            EAnnotation it, String source, String key, String value) {
        String details = it.getDetails().get(key);
        List<String> values = splitToList(details);

        return Objects.equals(it.getSource(), source)
            && values.contains(value);
    }

    /**
     * Evaluates if a class is a sub-class of EMDE Extension.
     *
     * @param it
     *     class
     * @return true if extension
     */
    public static boolean isEmdeExtensionClass(EClass it) {
        return it.getESuperTypes().contains(getEClass(it, EMDE_EXTENSION_URI));
    }

    private static EClass getEClass(EObject context, String uri) {
        try {
            return getEClass(context, URI.createURI(uri));
        } catch (Exception e) {
            return null;
        }
    }

    private static EClass getEClass(EObject context, URI uri) {
        ResourceSet rs = context.eResource().getResourceSet();

        EObject result = rs.getEObject(uri, false);
        return result instanceof EClass
            ? (EClass) result
            : null;
    }

    @SuppressWarnings("unchecked")
    static <T extends EObject> T eAncestor(EObject it, Class<T> type) {
        return it == null || type.isInstance(it)
            ? (T) it
            : eAncestor(it.eContainer(), type);
    }

    public static boolean containHiddenExtensions(EClass it) {
        if (it.getESuperTypes().size() < 2
            || containExtension(it, false)) { // Nothing hidden if explicit
            return false;
        }
        return it.getESuperTypes()
            .stream()
            .skip(1)
            .anyMatch(type -> containExtension(type, true));
    }

    static boolean containExtension(EClass it, boolean withParent) {
        if (it.getEAnnotations()
            .stream()
            .anyMatch(ann -> Objects.equals(ann.getSource(), QN_ANNOTATION))) {
            return true;
        }
        // Only first inheritance:
        // https://github.com/eclipse/kitalpha/
        // blob/v6.2.0/
        // emde/plugins/org.polarsys.kitalpha.emde.model.edit/
        // src/org/polarsys/kitalpha/emde/model/edit/provider/helpers/EMDEHelper.java#L187

        // If a supertype is wrong, it will be displayed.
        return withParent
            && !it.getESuperTypes().isEmpty()
            && containExtension(it.getESuperTypes().get(0), true);
    }

}
