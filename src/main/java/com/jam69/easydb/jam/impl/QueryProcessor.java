package com.jam69.easydb.jam.impl;

public abstract class QueryProcessor<T> {
	
	public void begin() {
		
	}
	
	public void end() {
		
	}
	
	abstract public boolean process(T entity);
	
}
