package com.tomitribe.eclipse.tomee.server.internal;

import org.eclipse.debug.core.ILaunch;

public class ServerDeployer {
	
	private final ILaunch launch;

	public ServerDeployer(ILaunch launch) {
		super();
		this.launch = launch;
	}

	public String deploy(String filename) {
		throw new UnsupportedOperationException();
	}
	

	public boolean undeploy(String filename) {
		throw new UnsupportedOperationException();
	}
}