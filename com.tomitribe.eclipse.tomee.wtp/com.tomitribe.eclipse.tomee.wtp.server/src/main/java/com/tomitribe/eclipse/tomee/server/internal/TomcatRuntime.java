/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package com.tomitribe.eclipse.tomee.server.internal;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.launching.*;

import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.model.RuntimeDelegate;
/**
 * 
 */
public class TomcatRuntime extends RuntimeDelegate implements ITomcatRuntime, ITomcatRuntimeWorkingCopy {
	protected static final String PROP_VM_INSTALL_TYPE_ID = "vm-install-type-id";
	protected static final String PROP_VM_INSTALL_ID = "vm-install-id";
	
	protected static Map<File, Boolean> sdkMap = new HashMap<File, Boolean>(2);

	public TomcatRuntime() {
		// do nothing
	}

	public ITomcatVersionHandler getVersionHandler() {
		IRuntimeType type = getRuntime().getRuntimeType();
		return TomcatPlugin.getTomcatVersionHandler(type.getId());
	}

	protected String getVMInstallTypeId() {
		return getAttribute(PROP_VM_INSTALL_TYPE_ID, (String)null);
	}

	protected String getVMInstallId() {
		return getAttribute(PROP_VM_INSTALL_ID, (String)null);
	}
	
	public boolean isUsingDefaultJRE() {
		return getVMInstallTypeId() == null;
	}

	public IVMInstall getVMInstall() {
		if (getVMInstallTypeId() == null)
			return JavaRuntime.getDefaultVMInstall();
		try {
			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(getVMInstallTypeId());
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			int size = vmInstalls.length;
			String id = getVMInstallId();
			for (int i = 0; i < size; i++) {
				if (id.equals(vmInstalls[i].getId()))
					return vmInstalls[i];
			}
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	public List getRuntimeClasspath(IPath configPath) {
		IPath installPath = getRuntime().getLocation();
		// If installPath is relative, convert to canonical path and hope for the best
		if (!installPath.isAbsolute()) {
			try {
				String installLoc = (new File(installPath.toOSString())).getCanonicalPath();
				installPath = new Path(installLoc);
			} catch (IOException e) {
				// Ignore if there is a problem
			}
		}
		return getVersionHandler().getRuntimeClasspath(installPath, configPath);
	}

	/**
	 * Verifies the Tomcat installation directory. If it is
	 * correct, true is returned. Otherwise, the user is notified
	 * and false is returned.
	 * @return boolean
	 */
	public IStatus verifyLocation() {
		return getVersionHandler().verifyInstallPath(getRuntime().getLocation());
	}
	
	/*
	 * Validate the runtime
	 */
	public IStatus validate() {
		IStatus status = super.validate();
		if (!status.isOK())
			return status;
	
		status = verifyLocation();
		if (!status.isOK())
			return status;
//			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorInstallDir, null);
		// don't accept trailing space since that can cause startup problems
		if (getRuntime().getLocation().hasTrailingSeparator())
			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorInstallDirTrailingSlash, null);
		if (getVMInstall() == null)
			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorJRE, null);

		// JRG 04/11/2012 -- removed code to look for tools.jar - TC7 provides Eclipse JDT to compile JSPs,
		// so this code is redundant
		String id = getRuntime().getRuntimeType().getId();
		
		File f = getRuntime().getLocation().append("conf").toFile();
		File[] conf = f.listFiles();
		if (conf != null) {
			int size = conf.length;
			for (int i = 0; i < size; i++) {
				if (!f.canRead())
					return new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, Messages.warningCantReadConfig, null);
			}
		}
		
		// JRG 04/11/2012 -- removing code to check different JRE versions for different Tomcat versions
		// we *only* use Tomcat 7.0 so this check is much simpler
		
		// Else for TomEE 1.5, ensure we have J2SE 6.0
		if (id != null && id.indexOf("tomee.runtime.15") > 0) {
			IVMInstall vmInstall = getVMInstall();
			if (vmInstall instanceof IVMInstall2) {
				String javaVersion = ((IVMInstall2)vmInstall).getJavaVersion();
				if (javaVersion != null && !isVMMinimumVersion(javaVersion, 106)) {
					return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorJRETomcat70, null);
				}
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * @see RuntimeDelegate#setDefaults(IProgressMonitor)
	 */
	public void setDefaults(IProgressMonitor monitor) {
		IRuntimeType type = getRuntimeWorkingCopy().getRuntimeType();
		getRuntimeWorkingCopy().setLocation(new Path(TomcatPlugin.getPreference("location" + type.getId())));
	}

	public void setVMInstall(IVMInstall vmInstall) {
		if (vmInstall == null) {
			setVMInstall(null, null);
		} else
			setVMInstall(vmInstall.getVMInstallType().getId(), vmInstall.getId());
	}
	
	protected void setVMInstall(String typeId, String id) {
		if (typeId == null)
			setAttribute(PROP_VM_INSTALL_TYPE_ID, (String)null);
		else
			setAttribute(PROP_VM_INSTALL_TYPE_ID, typeId);
		
		if (id == null)
			setAttribute(PROP_VM_INSTALL_ID, (String)null);
		else
			setAttribute(PROP_VM_INSTALL_ID, id);
	}
	
	/**
	 * Checks for the existence of the Java compiler in the given java
	 * executable. A main program is run (<code>org.eclipse.jst.tomcat.core.
	 * internal.ClassDetector</code>), that dumps a true or false value
	 * depending on whether the compiler is found. This output is then
	 * parsed and cached for future reference.
	 * 
	 * @return true if the compiler was found
	 */	
	protected boolean checkForCompiler() {
		// first try the cache
		File javaHome = getVMInstall().getInstallLocation();
		try {
			Boolean b = sdkMap.get(javaHome);
			return b.booleanValue();
		} catch (Exception e) {
			// ignore
		}

		// locate tomcatcore.jar - it contains the class detector main program
		File file = TomcatPlugin.getPlugin();
		if (file != null && file.exists()) {
			IVMRunner vmRunner = getVMInstall().getVMRunner(ILaunchManager.RUN_MODE);
			VMRunnerConfiguration config = new VMRunnerConfiguration("com.tomitribe.eclipse.tomee.server.internal.ClassDetector", new String[] { file.getAbsolutePath() });
			config.setProgramArguments(new String[] { "com.sun.tools.javac.Main" });
			ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
			try {
				vmRunner.run(config, launch, null);
				for (int i = 0; i < 600; i++) {
					// wait no more than 30 seconds (600 * 50 mils)
					if (launch.isTerminated()) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// ignore
					}
				}
				IStreamsProxy streamsProxy = launch.getProcesses()[0].getStreamsProxy();
				String text = null;
				if (streamsProxy != null) {
					text = streamsProxy.getOutputStreamMonitor().getContents();
				
					if (text != null && text.length() > 0) {
						boolean found = false;
						if ("true".equals(text))
							found = true;
						
						sdkMap.put(javaHome, new Boolean(found));
						return found;
					}
				}
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error checking for JDK", e);
			} finally {
				if (!launch.isTerminated()) {
					try {
						launch.terminate();
					} catch (Exception ex) {
						// ignore
					}
				}
			}
		}
		
		// log error that we were unable to check for the compiler
		TomcatPlugin.log(MessageFormat.format("Failed compiler check for {0}", (Object []) new String[] { javaHome.getAbsolutePath() }));
		return false;
	}

	private static Map<String, Integer> javaVersionMap = new ConcurrentHashMap<String, Integer>();

	private boolean isVMMinimumVersion(String javaVersion, int minimumVersion) {
		Integer version = javaVersionMap.get(javaVersion);
		if (version == null) {
			int index = javaVersion.indexOf('.');
			if (index > 0) {
				try {
					int major = Integer.parseInt(javaVersion.substring(0, index)) * 100;
					index++;
					int index2 = javaVersion.indexOf('.', index);
					if (index2 > 0) {
						int minor = Integer.parseInt(javaVersion.substring(index, index2));
						version = new Integer(major + minor);
						javaVersionMap.put(javaVersion, version);
					}
				}
				catch (NumberFormatException e) {
					// Ignore
				}
			}
		}
		// If we have a version, and it's less than the minimum, fail the check
		if (version != null && version.intValue() < minimumVersion) {
			return false;
		}
		return true;
	}
}
