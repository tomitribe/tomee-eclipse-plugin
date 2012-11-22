/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package com.tomitribe.eclipse.tomee.server.internal.command;

import com.tomitribe.eclipse.tomee.server.internal.ITomcatServerWorkingCopy;
import com.tomitribe.eclipse.tomee.server.internal.Messages;
/**
 * Command to enable or disable serving modules without publishing
 */
public class SetSaveSeparateContextFilesCommand extends ServerCommand {
	protected boolean sscf;
	protected boolean oldSscf;

	/**
	 * SetSeparateContextFilesCommand constructor comment.
	 * 
	 * @param server a Tomcat server
	 * @param sscf <code>true</code> to enable saving separate context XML
	 * files. Otherwise contexts are kept in server.xml when published.
	 */
	public SetSaveSeparateContextFilesCommand(ITomcatServerWorkingCopy server, boolean sscf) {
		super(server, Messages.serverEidtorActionSetSeparateContextFiles);
		this.sscf = sscf;
	}

	/**
	 * Execute the command.
	 */
	public void execute() {
		oldSscf = server.isSaveSeparateContextFiles();
		server.setSaveSeparateContextFiles(sscf);
	}

	/**
	 * Undo the command.
	 */
	public void undo() {
		server.setSaveSeparateContextFiles(oldSscf);
	}
}