/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2024 OBEO. All rights reserved.
 *
 * Contributors:
 * Obeo - initial API and implementation
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.obeonetwork.mbse.capella.vpx.design;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

/**
 * Services for Eclipse services.
 *
 * @author nperansin
 */
public class UiAccesses {

    /**
     * Returns the current shell.
     *
     * @return shell
     */
    static Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    /**
     * Shows and provided a console based on it name.
     * <p>
     * If no console exists, a message console is created.
     * </p>
     *
     * @param name
     *     of console
     * @param icon
     *     of console (may be null)
     * @return existing or created console
     */
    static IOConsole getShownConsole(String name, String icon) {
        IConsoleManager cmanager = ConsolePlugin.getDefault().getConsoleManager();
        for (IConsole console : cmanager.getConsoles()) {
            if (console instanceof IOConsole &&
                name.equals(console.getName())) {
                cmanager.showConsoleView(console);
                return (IOConsole) console;
            }
        }

        return createMessageConsole(cmanager, name, icon);
    }

    private static IOConsole createMessageConsole(IConsoleManager mgr, String name, String icon) {
        ImageDescriptor image = null;
        try {
            if (icon != null && !icon.isBlank()) {
                image = ImageDescriptor.createFromURL(new URL(icon));
            }
        } catch (MalformedURLException e) {
            // ignored no image
        }

        MessageConsole result = new MessageConsole(name, image);
        mgr.addConsoles(new MessageConsole[] {
            result
        });
        mgr.showConsoleView(result);
        return result;
    }

    /**
     * Displays a message in a IO consoles.
     *
     * @param console
     *     to display on
     * @param message
     *     to display
     */
    static void logConsole(IOConsole console, String message) {
        if (console == null) {
            System.out.println(message);
        } else {
            try (IOConsoleOutputStream out = console.newOutputStream()) {
                out.write(message + "\n");
                out.flush();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Schedules a job.
     *
     * @param jobName
     *     of operation
     * @param operation
     *     to perform
     */
    static void scheduleInUI(String jobName, IOConsole trace, IRunnableWithProgress operation) {
        scheduleInUI(jobName, trace, 0L, operation);
    }

    /**
     * Schedules a job.
     *
     * @param jobName
     *     of operation
     * @param operation
     *     to perform
     */
    static void scheduleInUI(
            String jobName, IOConsole trace, long delay, IRunnableWithProgress operation) {
        WorkspaceJob job = new WorkspaceJob(jobName) {

            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    operation.run(monitor);
                } catch (InvocationTargetException e) {
                    logConsole(trace, "'" + jobName + "' failed : " + e.getMessage() + "\n");
                    return Status.error(getName(), e);
                } catch (InterruptedException e) {
                    logConsole(trace, "'" + jobName + "' canceled.\n");
                    return Status.CANCEL_STATUS;
                }
                logConsole(trace, "'" + jobName + "' done.\n");

                return Status.OK_STATUS;
            }

        };
        job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        job.setUser(true);
        job.setPriority(Job.BUILD);
        job.schedule(delay);
    }

    /**
     * Finds the LaunchConfiguration with provided name.
     *
     * @param cfgName
     *     name of configuration
     * @return configuration found
     * @throws CoreException
     *     if configurations cannot be accessed.
     */
    private static ILaunchConfiguration findLaunchConfiguration(String cfgName)
            throws CoreException {
        ILaunchManager runManager = DebugPlugin.getDefault().getLaunchManager();
        for (ILaunchConfiguration cfg : runManager.getLaunchConfigurations()) {
            if (cfgName.equals(cfg.getName())) {
                return cfg;
            }
        }
        return null;
    }

    private static class LaunchTracer implements ILaunchesListener2 {
        // ILaunchListener and ILaunchesListener does not receive Terminate events.

        private final IOConsole output;
        private final ILaunch target;
        private final ILaunchManager runManager = DebugPlugin.getDefault().getLaunchManager();

        public LaunchTracer(IOConsole output, ILaunch target) {
            this.output = output;
            this.target = target;
        }

        void activate() {
            runManager.addLaunchListener(this);
            if (target.isTerminated()) { // Event is already past.
                launchesTerminated(new ILaunch[] {
                    target
                });
            }
        }

        private boolean isApplicable(ILaunch[] launches) {
            return Arrays.asList(launches).contains(target);
        }

        @Override
        public void launchesAdded(ILaunch[] launches) {
        }

        @Override
        public void launchesChanged(ILaunch[] launches) {
        }

        @Override
        public void launchesTerminated(ILaunch[] launches) {
            if (isApplicable(launches)) {
                logConsole(output, "'" + target.getLaunchConfiguration().getName() + "' finished.");
                runManager.removeLaunchListener(this);
            }
        }

        @Override
        public void launchesRemoved(ILaunch[] launches) {
            if (isApplicable(launches)) {
                runManager.removeLaunchListener(this);
            }
        }

    }

    /**
     * Starts a launch configuration.
     *
     * @param cfg
     *     configuration
     * @param trace
     *     console
     * @param monitor
     *     of execution
     * @throws CoreException
     *     if cannot start
     */
    static void startLaunchConfiguration(
            ILaunchConfiguration cfg, IOConsole trace, IProgressMonitor monitor)
            throws CoreException {
        logConsole(trace, "Launching '" + cfg.getName() + "' ...");
        ILaunch exe = cfg.launch(ILaunchManager.RUN_MODE, monitor, true);
        logConsole(trace, "'" + cfg.getName() + "' started.");
        new LaunchTracer(trace, exe).activate();
    }

    /**
     * Starts a launch configuration.
     *
     * @param cfg
     *     configuration name
     * @param trace
     *     console
     * @param monitor
     *     of execution
     * @throws CoreException
     *     if cannot start
     */
    static void startLaunchConfiguration(
            String cfgName, IOConsole trace, IProgressMonitor monitor) {
        try {
            ILaunchConfiguration found = UiAccesses.findLaunchConfiguration(cfgName);
            if (found != null) {
                startLaunchConfiguration(found, trace, monitor);
            } else {
                logConsole(trace, "No Launch Configuration: '" + cfgName + "'.");
            }
        } catch (CoreException ce) {
            logConsole(trace, "Error when launching '" + cfgName + "'.\n"
                + ce.getLocalizedMessage());
        }
    }

}
