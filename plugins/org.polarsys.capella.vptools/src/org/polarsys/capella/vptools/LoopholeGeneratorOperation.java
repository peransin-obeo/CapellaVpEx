/* *
 * Copyright (c) 2013 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Obeo - initial API and implementation
 */
package org.polarsys.capella.vptools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.codegen.ecore.genmodel.presentation.GeneratorUIUtil.GeneratorOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.emf.loophole.internal.model.GenGapModel;

/**
 * Copy from : mbarbero/emf-loophole
 * - bundles/org.eclipselabs.emf.loophole.ui/src/
 * org/eclipselabs/emf/loophole/ui/internal/model/editor/LoopholeGeneratorOperation
 * <p>
 * To solve compilation issue.
 * </p>
 * @author nperansin
 *
 */
@SuppressWarnings("restriction") 
public class LoopholeGeneratorOperation extends GeneratorOperation {

    // Index 
    private static final int PROJECT_TYPE_INDEX = 2;
    private static final int GENMODEL_INDEX = 4;

	public LoopholeGeneratorOperation(Shell arg0) {
		super(arg0);
	}
	
    public void addGeneratorAndArguments(Generator generator, Object object, Object projectType, String projectTypeName, GenGapModel genGapModel)
    {
      if (generatorAndArgumentsList == null)
      {
        generatorAndArgumentsList = new ArrayList<Object[]>();
      }
      Object[] info = {
          generator, object, 
          /*2*/projectType, projectTypeName, 
          /*4*/genGapModel
      };
      
      generatorAndArgumentsList.add(info);
    }
	
    private IFolder prepareDirectory(Object[] genArgs, IProgressMonitor monitor) throws CoreException {
        if (!(GENMODEL_INDEX < genArgs.length)) {
            return null;
        }
        GenGapModel genGapModel = (GenGapModel) genArgs[GENMODEL_INDEX];
        GenModel genModel = genGapModel.getGenModel();
        String directory = null;
        boolean clean = false;
        String projectType = genArgs[PROJECT_TYPE_INDEX] instanceof String 
                ? (String) genArgs[PROJECT_TYPE_INDEX]
                : "";
        
        switch(projectType) {
        case GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE:
            directory = genModel.getModelDirectory();
            clean = genGapModel.isCleanModelDirectory();
            break;
        case GenBaseGeneratorAdapter.EDIT_PROJECT_TYPE:
            directory = genModel.getModelDirectory();
            clean = genGapModel.isCleanEditDirectory();
            break;
        case GenBaseGeneratorAdapter.EDITOR_PROJECT_TYPE:
            directory = genModel.getEditorDirectory();
            break;
        case GenBaseGeneratorAdapter.TESTS_PROJECT_TYPE:
            directory = genModel.getTestsDirectory();
            break;
        }
        if (directory == null) {
            return null;
        }
        IFolder srcFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(directory));
        if (clean && srcFolder.exists()) {
            srcFolder.delete(true, monitor);
        }
        return srcFolder;
    }
    
	@Override
	protected void execute(IProgressMonitor progressMonitor) throws CoreException, InvocationTargetException, InterruptedException {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 80 + 15 * generatorAndArgumentsList.size());
        
		List<IProject> genProjets = new ArrayList<>();
		for (Object[] generatorAndArguments : generatorAndArgumentsList) {
		    IFolder srcFolder = prepareDirectory(generatorAndArguments, subMonitor.newChild(10));
		    if (srcFolder != null) {
		        genProjets.add(srcFolder.getProject());
		    }
        }
		super.execute(subMonitor.newChild(80));
		
		// Refresh containers.
		for (IProject target : genProjets) {
		    target.refreshLocal(IResource.DEPTH_INFINITE, subMonitor.newChild(5));
		}
	}
	
}
