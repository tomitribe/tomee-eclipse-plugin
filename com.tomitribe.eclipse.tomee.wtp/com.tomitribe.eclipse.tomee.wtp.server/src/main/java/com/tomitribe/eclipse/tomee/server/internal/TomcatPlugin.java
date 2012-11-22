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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
/**
 * The Tomcat plugin.
 */
public class TomcatPlugin extends Plugin {
	protected static TomcatPlugin singleton;

	public static final String PLUGIN_ID = "com.tomitribe.eclipse.tomee.wtp.server";
	public static final String TOMEE_15 = "com.tomitribe.eclipse.tomee.15";

	protected static final String VERIFY_INSTALL_FILE = "verifyInstall.properties";
	protected static VerifyResourceSpec[] verify15;
	
	protected static final IStatus emptyInstallDirStatus = new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorInstallDirEmpty, null);
	protected static final IStatus wrongDirVersionStatus = new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorInstallDirWrongVersion, null);
	protected static final IStatus installDirDoesNotExist = new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorInstallDirDoesNotExist, null);

	private static ConfigurationResourceListener configurationListener;
	/**
	 * TomcatPlugin constructor comment.
	 */
	public TomcatPlugin() {
		super();
		singleton = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		configurationListener = new ConfigurationResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(configurationListener, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(configurationListener);
		super.stop(context);
	}

	/**
	 * Returns the singleton instance of this plugin.
	 * @return org.eclipse.jst.server.tomcat.internal.TomcatPlugin
	 */
	public static TomcatPlugin getInstance() {
		return singleton;
	}

	/**
	 * Return the install location preference.
	 * 
	 * @param id a runtime type id
	 * @return the install location
	 */
	public static String getPreference(String id) {
		return getInstance().getPluginPreferences().getString(id);
	}
	
	/**
	 * Set the install location preference.
	 * 
	 * @param id the runtimt type id
	 * @param value the location
	 */
	public static void setPreference(String id, String value) {
		getInstance().getPluginPreferences().setValue(id, value);
		getInstance().savePluginPreferences();
	}

	/**
	 * Convenience method for logging.
	 *
	 * @param status a status object
	 */
	public static void log(IStatus status) {
		getInstance().getLog().log(status);
	}

	/**
	 * Returns the Tomcat home directory.
	 * @return java.lang.String
	 */
	protected static String getTomcatStateLocation() {
		try {
			return getInstance().getStateLocation().toOSString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the Tomcat version handler.
	 * 
	 * @param id
	 * @return a version handler
	 */
	public static ITomcatVersionHandler getTomcatVersionHandler(String id) {
		int runtimeIndex = id.indexOf("runtime");
		if (runtimeIndex > 0)
			id = "com.tomitribe.eclipse.tomee." + id.substring(runtimeIndex + 8);
		if (TOMEE_15.equals(id))
			return new TomEE15Handler();
		else
			return null;
	}

	/**
	 * Loads the files to verify the Tomcat installation.
	 */
	public static void loadVerifyFiles() {
		if (verify15 != null)
			return;
	
		// backup (empty) values
		verify15 = new VerifyResourceSpec[0];
		
		try {
			URL url = getInstance().getBundle().getEntry(VERIFY_INSTALL_FILE);
			url = FileLocator.resolve(url);
			Properties p = new Properties();
			p.load(url.openStream());

			// v1.5
			// Check backdoor system property, use internal spec if not found
			String verify = System.getProperty(PLUGIN_ID + ".verify15install");
			if (verify == null) {
				verify = p.getProperty("verify15install");
			}
			verify.replace('/', File.separatorChar);

			StringTokenizer st = new StringTokenizer(verify, ",");
			List<VerifyResourceSpec> list = new ArrayList<VerifyResourceSpec>();
			while (st.hasMoreTokens())
				list.add(new VerifyResourceSpec(st.nextToken()));
			Trace.trace(Trace.FINEST, "Verify15: " + list.toString());
			verify15 = new VerifyResourceSpec[list.size()];
			list.toArray(verify15);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not load installation verification properties", e);
		}
	}

	/**
	 * Utility method to verify an installation directory according to the
	 * specified server ID.  The verification includes checking the installation
	 * directory name to see if it indicates a different version of Tomcat.
	 * 
	 * @param installPath Path to verify
	 * @param id Type ID of the server
	 * @return Status of the verification.  Will be Status.OK_STATUS, if verification
	 *    was successful, or error status if not.
	 */
	public static IStatus verifyInstallPathWithFolderCheck(IPath installPath, String id) {
		IStatus status = verifyTomcatVersionFromPath(installPath, id);
		if (status.isOK()) {
			status = verifyInstallPath(installPath, id);
		}
		return status;
	}

	/**
	 * Verify the Tomcat installation directory.
	 * 
	 * @param installPath Path to verify
	 * @param id Type ID of the server
	 * @return Status of the verification.  Will be Status.OK_STATUS, if verification
	 *    was successful, or error status if not.
	 */
	public static IStatus verifyInstallPath(IPath installPath, String id) {
		if (installPath == null)
			return emptyInstallDirStatus;
		
		String dir = installPath.toOSString();
		if (dir.trim().length() == 0)
			return emptyInstallDirStatus;

		File file = new File(dir);
		if (!file.exists())
			return installDirDoesNotExist;

		if (!dir.endsWith(File.separator))
			dir += File.separator;
		
		// look for the following files and directories
		TomcatPlugin.loadVerifyFiles();
		
		VerifyResourceSpec[] specs = null;
		if (TOMEE_15.equals(id)) {
			specs = verify15;
		} else {
			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorUnknownVersion, null);
		}
		
		for (int i = 0; i < specs.length; i++) {
			VerifyResourceSpec fs = specs[i];
			IStatus status = fs.checkResource(dir);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	public static IStatus verifyTomcatVersionFromPath(IPath installPath, String version) {
		if (version == null) 
			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorVersionEmpty, null);
		if (installPath == null)
			return emptyInstallDirStatus;

		String s = installPath.lastSegment();
		if (s == null)
			return Status.OK_STATUS;
		if (s.indexOf("-1.5") > 0 || s.indexOf(" 1.5") > 0)
			return TOMEE_15.equals(version) ? Status.OK_STATUS : wrongDirVersionStatus;

		return Status.OK_STATUS;
	}

	/**
	 * Return a <code>java.io.File</code> object that corresponds to the specified
	 * <code>IPath</code> in the plugin directory.
	 * 
	 * @return a file
	 */
	protected static File getPlugin() {
		try {
			URL installURL = getInstance().getBundle().getEntry("/");
			URL localURL = FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException ioe) {
			return null;
		}
	}

	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
	}
}
