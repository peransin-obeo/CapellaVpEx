/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 * Obeo - initial API and implementation
 * ***************************************************************************** */
package com.obeonetwork.mbse.capella.vpx.design;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.codegen.ecore.CodeGenEcorePlugin;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.emf.loophole.internal.generator.GenGapModelUtil;
import org.eclipselabs.emf.loophole.internal.model.GenGapModel;

/**
 * Services for Viewpoint extension in EcoreTools.
 *
 * @author nperansin
 */
@SuppressWarnings("restriction") // No GenGapModel API
public class EmfLoopholeGenerations {

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

    /**
     * Creates a Loophole Gen operation.
     *
     * @param shell
     *     to popup error (may be null, no message)
     * @param genModels
     * @param generationProvider
     *     provide
     * @return
     */
    static LoopholeGeneratorOperation createOperation(
            Shell shell,
            Collection<? extends GenGapModel> genModels,
            Function<GenGapModel, String[][]> generationProvider) {
        LoopholeGeneratorOperation result = new LoopholeGeneratorOperation(shell);

        for (GenGapModel model : genModels) {
            addGenerations(result, model, generationProvider.apply(model));
        }
        return result;
    }

    private static void addGenerations(
            LoopholeGeneratorOperation operation,
            GenGapModel model,
            String[][] generations) {
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
