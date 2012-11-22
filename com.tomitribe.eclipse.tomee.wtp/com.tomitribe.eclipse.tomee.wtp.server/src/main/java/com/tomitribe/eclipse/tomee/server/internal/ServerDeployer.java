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
	private String tomeeDir;
	private final int httpPort;
	
	public ServerDeployer(String openejbDir, int httpPort) {
		super();
		this.tomeeDir = openejbDir;
		this.httpPort = httpPort;
	}

	public String deploy(String filename) {
		Object obj = callDeployerBusinessRemote("deploy", filename);
		if (obj == null) {
			return null;
		}
		
		try {
			return (String) obj.getClass().getDeclaredField("jarPath").get(obj);
		} catch (Exception e) {
			return null;
		}
	}

	private Object callDeployerBusinessRemote(String methodName, String filename) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Properties properties =	 new Properties();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
			properties.put(Context.PROVIDER_URL, "http://localhost:" + httpPort + "/tomee");
			properties.put(Context.SECURITY_PRINCIPAL, "tomee");
			properties.put(Context.SECURITY_CREDENTIALS, "tomee");
			
			URL[] urls = getUrls(tomeeDir);
			URLClassLoader cl = new URLClassLoader(urls, classLoader);
			Thread.currentThread().setContextClassLoader(cl);
			
			InitialContext context = new InitialContext(properties);
			Object ref = context.lookup("java:openejb/DeployerBusinessRemote");
			
			Method method = ref.getClass().getMethod(methodName, String.class);
			return method.invoke(ref, filename);
		} catch (Exception e) {
			return null;
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

	public boolean undeploy(String filename) {
		return callDeployerBusinessRemote("undeploy", filename) != null;
	}

	private URL[] getUrls(String directory) {
		List<URL> urlList = new ArrayList<URL>();
		File openEjbDir = new File(directory + File.separator + "lib");
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