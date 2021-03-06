/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package com.tomitribe.eclipse.tomee.server.internal;
/**
 * 
 */
public interface IMimeMapping {
	/**
	 * Returns the extension.
	 * 
	 * @return the extension
	 */
	public String getExtension();

	/**
	 * Returns the mime type.
	 * 
	 * @return the mime type
	 */
	public String getMimeType();
}