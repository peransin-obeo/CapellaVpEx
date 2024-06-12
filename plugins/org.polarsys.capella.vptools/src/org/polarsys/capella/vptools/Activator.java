/* ***************************************************************************** *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 *    Obeo - initial API and implementation
 * ***************************************************************************** */
package org.polarsys.capella.vptools;

import java.util.Set;

import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author nperansin
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID. */
    public static final String PLUGIN_ID = "org.polarsys.capella.vptools";

    /** The Odesign path. */
    public static final String DESIGN_PATH = PLUGIN_ID + "/description/capellavp.odesign";

    // The shared instance
    private static Activator instance;

    private Set<Viewpoint> viewpoints = null;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        instance = this;
        viewpoints = ViewpointRegistry.getInstance().registerFromPlugin(DESIGN_PATH);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (viewpoints != null) {
            for (Viewpoint viewpoint : viewpoints) {
                ViewpointRegistry.getInstance().disposeFromPlugin(viewpoint);
            }
            viewpoints = null;
        }
        instance = null;

        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return instance;
    }

}
