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
package com.tomitribe.eclipse.tomee.server.internal.xml.server32;

import com.tomitribe.eclipse.tomee.server.internal.xml.*;
/**
 * 
 */
public class Parameter extends XMLElement {
	public Parameter() {
		// do nothing
	}
	
	public String getName() {
		return getAttributeValue("name");
	}
	
	public String getValue() {
		return getAttributeValue("value");
	}
	
	public void setName(String name) {
		setAttributeValue("name", name);
	}
	
	public void setValue(String value) {
		setAttributeValue("value", value);
	}
}