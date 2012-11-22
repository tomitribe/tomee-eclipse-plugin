/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package com.tomitribe.eclipse.tomee.server.internal.command;

import com.tomitribe.eclipse.tomee.server.internal.ITomcatConfigurationWorkingCopy;
import com.tomitribe.eclipse.tomee.server.internal.Messages;
import com.tomitribe.eclipse.tomee.server.internal.WebModule;
/**
 * Command to change a web module.
 */
public class ModifyWebModuleCommand extends ConfigurationCommand {
	protected int index;
	protected WebModule oldModule;
	protected WebModule newModule;

	public ModifyWebModuleCommand(ITomcatConfigurationWorkingCopy configuration, int index, WebModule module) {
		super(configuration, Messages.configurationEditorActionModifyWebModule);
		this.index = index;
		newModule = module;
	}

	/**
	 * Execute the command.
	 */
	public void execute() {
		oldModule = (WebModule) configuration.getWebModules().get(index);
		configuration.modifyWebModule(index, newModule.getDocumentBase(), newModule.getPath(), newModule.isReloadable());
	}

	/**
	 * Undo the command.
	 */
	public void undo() {
		configuration.modifyWebModule(index, oldModule.getDocumentBase(), oldModule.getPath(), oldModule.isReloadable());
	}
}