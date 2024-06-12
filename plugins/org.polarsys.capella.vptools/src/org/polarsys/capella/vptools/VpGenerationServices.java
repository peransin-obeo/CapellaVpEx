/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools;

import static org.polarsys.capella.vptools.UiAccesses.getShell;
import static org.polarsys.capella.vptools.UiAccesses.getShownConsole;
import static org.polarsys.capella.vptools.UiAccesses.logConsole;
import static org.polarsys.capella.vptools.UiAccesses.scheduleInUI;
import static org.polarsys.capella.vptools.UiAccesses.startLaunchConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.codegen.ecore.CodeGenEcorePlugin;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.IOConsole;
import org.eclipselabs.emf.loophole.internal.generator.GenGapModelUtil;
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

    static final String[][] MODEL_GENERATION = {
        {
            GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE,
            CodeGenEcorePlugin.INSTANCE.getString("_UI_ModelProject_name") //$NON-NLS-1$
        }
    };
    static final String[][] FULL_GENERATION = {
        MODEL_GENERATION[0],
        {
            GenBaseGeneratorAdapter.EDIT_PROJECT_TYPE,
            CodeGenEcorePlugin.INSTANCE.getString("_UI_EditProject_name") //$NON-NLS-1$
        }
    };

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

    /**
     * Creates a Loophole Gen operation.
     * 
     * @param shell to popup error (may be null, no message)
     * @param genModels 
     * @param generationProvider provide
     * @return
     */
    static LoopholeGeneratorOperation createOperation(
            Shell shell, 
            Collection<? extends GenGapModel> genModels, 
            Function<GenGapModel, String[][]> generationProvider
            ) {
        LoopholeGeneratorOperation result = new LoopholeGeneratorOperation(shell);

        for (GenGapModel model : genModels) {
            addGenerations(result, model, generationProvider.apply(model));
        }
        return result;
    }
    
    private static String[][] appendGenTypes(StringBuffer message, GenGapModel cfg, String[][] types) {
        message.append(MessageFormat.format(" - {0} : ", getProjectSegment(cfg)));
        for(String[] type : types) {
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
                logConsole(fb, MessageFormat.format("Generation failed {0}\n", e.getLocalizedMessage()));
                throw new InvocationTargetException(e);
            }
            scheduleInUI("Viewpoint Custom Generation", fb,
                subProgress -> startLaunchConfiguration(getCustomLaunchName(anchor), 
                    fb, subProgress));
        });

        return anchor;
    }

    private static void addGenerations(
            LoopholeGeneratorOperation operation, 
            GenGapModel model, 
            String[][] generations
            ) {
        model.reconcile();
        model.getGenModel().setCanGenerate(true);
        Generator generator = GenGapModelUtil.createGenerator(model);
        generator.requestInitialize();
        for (String[] genType : generations) {
            operation.addGeneratorAndArguments(generator, model.getGenModel(),
                genType[0], genType[1], model);
        }
    }

    static List<? extends GenGapModel> filterGenGapModel(Collection<?> values) {
        return values.stream()
                .filter(GenGapModel.class::isInstance)
                .map(GenGapModel.class::cast)
                .collect(Collectors.toList());
    }
}
