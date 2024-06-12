/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.plugin.AbstractPluginModelBase;
import org.eclipse.pde.internal.core.plugin.ExternalPluginModel;

/**
 * Services for Viewpoint extension in EcoreTools.
 *
 * @author nperansin
 */
@SuppressWarnings("restriction")
public class PluginModelBases {

    /**
     * Returns a plugin model from provided file.
     * 
     * @param xmlFile to load
     * @return plugin model
     * @throws CoreException if loading fails.
     */
    public static IPluginModel getPluginXmlModel(Path xmlFile) throws CoreException {
        ExternalPluginModel result = new ExternalPluginModel() {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isEditable() { 
                return true;
            }
        };
        return loadFile(result, xmlFile);
    }
    
    private static <M extends AbstractPluginModelBase> M loadFile(M it, Path xmlFile) throws CoreException {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            it.load(is, true); // loading is at model level
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return it;
    }
    
    /**
     * Saves the provided model into a file.
     * 
     * @param model to save
     * @param xmlFile to save in
     */
    public static void save(IPluginModelBase model, Path xmlFile) {
        try (PrintWriter writer = new PrintWriter(xmlFile.toFile(), StandardCharsets.UTF_8)) {
            IPluginBase base = null;
            if (model instanceof IPluginModel) {
                base = ((IPluginModel) model).getPlugin();
            } else if (model instanceof IFragmentModel) {
                base = ((IFragmentModel) model).getFragment();
            } else {
                throw new UnsupportedOperationException("Unexpected model type : " + model);
            }
            base.write("  ", writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
