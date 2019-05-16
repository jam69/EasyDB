package com.jam69.easydb.jam.impl;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldDescriptor {
	
	private static final Logger log=LoggerFactory.getLogger(FieldDescriptor.class);

	
	private final String name;
	private final Class type;
//	private final int pos;
	
	private transient Method getter;
	private transient Method setter;
	
	public FieldDescriptor(String name,Class clazz) {
		this.name=name;
		this.type=clazz;
//		this.pos=pos;
	}
	
	public String getName() {
		return name;
	}
	
	public Class getType() {
		return type;
	}
	
//	public int getPos() {
//		return pos;
//	}

	public void setGetterMethod(Method readMethod) {
		this.getter=readMethod;
	}
	
	public void setSetterMethod(Method writeMethod) {
		this.setter=writeMethod;
	}

	public Method getGetterMethod() {
		return getter;
	}

	public Method getSetterMethod() {
		return setter;
	}
}