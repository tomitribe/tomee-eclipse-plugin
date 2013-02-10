package com.tomitribe.eclipse.tomee.server.internal;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;

import com.tomitribe.eclipse.tomee.server.internal.xml.Factory;
import com.tomitribe.eclipse.tomee.server.internal.xml.XMLUtil;
import com.tomitribe.eclipse.tomee.server.internal.xml.server40.Context;
import com.tomitribe.eclipse.tomee.server.internal.xml.server40.Server;
import com.tomitribe.eclipse.tomee.server.internal.xml.server40.ServerInstance;

public class TomEEDeployerInstaller {

	public void installDeployer(IPath baseDir) {
		try {
			IPath confDir = baseDir.append("conf");
			IPath deployerDir = baseDir.append("wtpdeploy");
			deployerDir.toFile().mkdirs();
			
			URL installURL = TomcatPlugin.getInstance().getBundle().getEntry("wtp-mgmt.war");
			URL localURL = FileLocator.toFileURL(installURL);
			
			File destDir = deployerDir.append("wtp-mgmt").toFile();
			destDir.mkdirs();
			
			Unzip.unzip(localURL, destDir);
			
			File serverXmlFile = confDir.append("server.xml").toFile();
			
			Factory factory = new Factory();
			factory.setPackageName("com.tomitribe.eclipse.tomee.server.internal.xml.server40");
			
			Server publishedServer = (Server) factory.loadDocument(new FileInputStream(serverXmlFile));
			ServerInstance publishedInstance = new ServerInstance(publishedServer, null, null);
			
			if (! hasMgmtContext(publishedInstance)) {
				Context context = publishedInstance.createContext(0);
				context.setDocBase(destDir.getAbsolutePath());
				context.setPath("wtp-mgmt");

				DocumentBuilder builder = XMLUtil.getDocumentBuilder();
				Document contextDoc = builder.newDocument();
				contextDoc.appendChild(contextDoc.importNode(publishedServer.getElementNode(), true));
				XMLUtil.save(serverXmlFile.getAbsolutePath(), contextDoc);			
			}
		} catch (Exception e) {
		}
	}
	
	private boolean hasMgmtContext(ServerInstance serverInstance) {
		Context[] contexts = serverInstance.getContexts();
		for (Context context : contexts) {
			if ("wtp-mgmt".equals(context.getPath())) {
				return true;
			}
		}
		
		return false;
	}

	public void removeDeployer(IPath baseDir) {
		
	}
	
}
