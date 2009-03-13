/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.xtext.generator;

public class BindKey {
	private String type;
	private boolean isSingleton = false;
	private boolean isEagerSingleton = false;
	
	
	public String getType() {
		return type;
	}


	public boolean isSingleton() {
		return isSingleton;
	}


	public boolean isEagerSingleton() {
		return isEagerSingleton;
	}


	private BindKey(String type, boolean isSingleton, boolean isEagerSingleton) {
		super();
		this.type = type;
		this.isSingleton = isSingleton;
		this.isEagerSingleton = isEagerSingleton;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BindKey other = (BindKey) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}


	public static BindKey type(String name) {
		return new BindKey(name, false, false);
	}
	public static BindKey eagerSingleton(String name) {
		return new BindKey(name, true, true);
	}
	public static BindKey singleton(String name) {
		return new BindKey(name, true, false);
	}
}