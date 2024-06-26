/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 * Obeo - initial API and implementation
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.obeonetwork.mbse.capella.vpx.design;

import static com.obeonetwork.mbse.capella.vpx.design.EmfLoopholeGenerations.FULL_GENERATION;
import static com.obeonetwork.mbse.capella.vpx.design.EmfLoopholeGenerations.createOperation;
import static com.obeonetwork.mbse.capella.vpx.design.UiAccesses.getShell;
import static com.obeonetwork.mbse.capella.vpx.design.UiAccesses.getShownConsole;
import static com.obeonetwork.mbse.capella.vpx.design.UiAccesses.logConsole;
import static com.obeonetwork.mbse.capella.vpx.design.UiAccesses.scheduleInUI;
import static com.obeonetwork.mbse.capella.vpx.design.UiAccesses.startLaunchConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.ui.console.IOConsole;
import org.eclipselabs.emf.loophole.internal.model.GenGapModel;

/**
 * Services for Viewpoint extension in EcoreTools.
 *
 * @author nperansin
 */
@SuppressWarnings("restriction") // No GenGapModel API
public class VpGenerationServices {

    private static final String CONSOLE_NAME = "Capella Viewpoint Generation";
    private static final String CONSOLE_ICON =
        "platform:/plugin/org.polarsys.kitalpha.ad.viewpoint.coredomain.model.edit/icons/full/obj16/Viewpoint.gif";

    private static String getCustomLaunchName(EObject anchor) {
        EPackage pkg = VpToolsServices.eAncestor(anchor, EPackage.class);
        return pkg.getName() + " Generation";
    }

    private static String getProjectSegment(EObject anchor) {
        URI modelPath = anchor.eResource().getURI();

        return modelPath.isPlatform()
            ? modelPath.segment(1)
            : modelPath.toString();
    }

    private static String[][] appendGenTypes(
            StringBuffer message, GenGapModel cfg, String[][] types) {
        message.append(MessageFormat.format(" - {0} : ", getProjectSegment(cfg)));
        for (String[] type : types) {
            message.append(MessageFormat.format("[{0}] ", type[1]));
        }
        message.append('\n');
        return types;
    }

    /**
     * Generates Viewpoint for Capella model extension.
     *
     * @param anchor
     * @param genModels
     * @return
     */
    public static EObject generateViewpoint(EObject anchor, Set<GenGapModel> genModels) {
        IOConsole fb = getShownConsole(CONSOLE_NAME, CONSOLE_ICON); // Feedback

        if (genModels.isEmpty()) {
            logConsole(fb, "No GenGapModel found in session.");
            return anchor;
        }

        StringBuffer message = new StringBuffer("Schedule Viewpoint generation for:\n");
        LoopholeGeneratorOperation operation = createOperation(
            getShell(),
            genModels,
            model -> appendGenTypes(message, model, FULL_GENERATION));

        logConsole(fb, message.toString());

        scheduleInUI("Viewpoint Model Generation", fb, monitor -> {
            try {
                operation.execute(monitor);
            } catch (CoreException e) {
                logConsole(fb,
                    MessageFormat.format("Generation failed {0}\n", e.getLocalizedMessage()));
                throw new InvocationTargetException(e);
            }
            scheduleInUI("Viewpoint Custom Generation", fb, 2_000,
                subProgress -> startLaunchConfiguration(getCustomLaunchName(anchor),
                    fb, subProgress));
        });

        return anchor;
    }

}
