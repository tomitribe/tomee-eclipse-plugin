package com.tomitribe.eclipse.tomee.server.internal;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;


public class ServerDeployer {
	
	private final File baseDir;
	private final int httpPort;
	
	public ServerDeployer(File baseDir, int httpPort) {
		super();
		this.baseDir = baseDir;
		this.httpPort = httpPort;
	}

	public String deploy(String filename) {
		ClassLoader oldCl = setupClassLoader();
		Object obj = callDeployerBusinessRemote("deploy", filename);
		resetClassLoader(oldCl);
		
		try {
			return (String) obj.getClass().getDeclaredField("jarPath").get(obj);
		} catch (Exception e) {
			return null;
		}
	}

	private void resetClassLoader(ClassLoader oldClassLoader) {
		Thread.currentThread().setContextClassLoader(oldClassLoader);
	}

	private ClassLoader setupClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		URL[] urls = getUrls(baseDir);
		URLClassLoader cl = new URLClassLoader(urls, classLoader);
		Thread.currentThread().setContextClassLoader(cl);
		
		return classLoader;
	}

	public boolean undeploy(String filename) {
		ClassLoader oldCl = setupClassLoader();
		Object object = callDeployerBusinessRemote("undeploy", filename);
		resetClassLoader(oldCl);
		
		return object != null;
	}
	
	private Object callDeployerBusinessRemote(String methodName, String filename) {
		try {
			Properties properties =	 new Properties();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.RemoteInitialContextFactory");
			properties.put(Context.PROVIDER_URL, "http://localhost:" + httpPort + "/wtp-mgmt/ejb/");
			
			InitialContext context = new InitialContext(properties);
			Object ref = context.lookup("openejb/DeployerBusinessRemote");
			
			Method method = ref.getClass().getMethod(methodName, String.class);
			return method.invoke(ref, filename);
		} catch (Exception e) {
			return null;
		}
	}
	
	private URL[] getUrls(File baseDir) {
		List<URL> urlList = new ArrayList<URL>();
		File openEjbDir = new File(baseDir, "lib");
		File[] files = openEjbDir.listFiles();
		
		for (File file : files) {
			if (file.getName().endsWith(".jar")) {
				try {
					urlList.add(file.getAbsoluteFile().toURL());
				} catch (MalformedURLException e) {
				}
			}
		}

		return urlList.toArray(new URL[urlList.size()]);
	}
}
