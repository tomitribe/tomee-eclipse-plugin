/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package com.tomitribe.eclipse.tomee.server.internal.command;

import com.tomitribe.eclipse.tomee.server.internal.ITomcatConfigurationWorkingCopy;
import com.tomitribe.eclipse.tomee.server.internal.Messages;
import com.tomitribe.eclipse.tomee.server.internal.MimeMapping;
/**
 * Command to add a mime mapping.
 */
public class AddMimeMappingCommand extends ConfigurationCommand {
	protected MimeMapping map;

	/**
	 * AddMimeMappingCommand constructor.
	 * 
	 * @param configuration a tomcat configuration
	 * @param map a mime mapping
	 */
	public AddMimeMappingCommand(ITomcatConfigurationWorkingCopy configuration, MimeMapping map) {
		super(configuration, Messages.configurationEditorActionAddMimeMapping);
		this.map = map;
	}

	/**
	 * Execute the command.
	 */
	public void execute() {
		configuration.addMimeMapping(0, map);
	}

	/**
	 * Undo the command.
	 */
	public void undo() {
		configuration.removeMimeMapping(0);
	}
}
