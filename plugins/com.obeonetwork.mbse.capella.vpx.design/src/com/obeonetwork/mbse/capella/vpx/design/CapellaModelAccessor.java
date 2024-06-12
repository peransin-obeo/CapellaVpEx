/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 * Obeo - initial API and implementation
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.obeonetwork.mbse.capella.vpx.design;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManagerListener;

/**
 * @author nperansin
 *
 */
public class CapellaModelAccessor extends SessionManagerListener.Stub {

    private static final String CFG_PATH = "/.settings/ecoretools.pde.plugins";
    private static final Predicate<String> NOT_BLANK = it -> !it.isBlank();

    private static final ILog LOGGER = Platform.getLog(CapellaModelAccessor.class);

// private static List<String> DD = Arrays.asList(
// "org.polarsys.kitalpha.ad.metadata", // "org.polarsys.kitalpha.ad.metadata.model"
// "org.polarsys.kitalpha.emde", // "org.polarsys.kitalpha.emde.model"

    // resource/org.polarsys.capella.vptools/description/capellavp.odesign
    // plugin/com.obeonetwork.mbse.capella.vpx.design/description/CapellaVpEx.odesign

    @Override
    public void notifyAddSession(Session newSession) {

        URI sessionUri = newSession.getSessionResource().getURI();
        if (!sessionUri.isPlatformResource()) {
            return; // no project to search in.
        }
        ResourceSet set = newSession.getTransactionalEditingDomain().getResourceSet();

        String cfgPath = "/" + sessionUri.segment(1) + CFG_PATH;
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        IResource iRes = wsRoot.findMember(cfgPath);
        if (!(iRes != null && iRes.exists() && iRes.getType() == IResource.FILE)) {
            return;
        }
        List<String> pluginNames;
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(((IFile) iRes).getContents(true)))) {
            pluginNames = reader.lines()
                .filter(NOT_BLANK)
                .map(String::trim)
                .collect(Collectors.toList());

        } catch (IOException | CoreException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Error while reading ecoretools target plugins from : " + cfgPath);
            return;
        }
        if (pluginNames.isEmpty()) {
            return;
        }

        Map<String, URI> bundleUris = CommonPlugin.getTargetPlatformBundleMappings();
        for (String pluginName : pluginNames) {
            URI targetLocation = bundleUris.get(pluginName);
            if (targetLocation != null) {
                set.getURIConverter().getURIMap().put(
                    URI.createPlatformPluginURI(pluginName + "/", true),
                    targetLocation);
            }
        }
        // set.getURIConverter().getURIMap().putAll(result);
    }

}
