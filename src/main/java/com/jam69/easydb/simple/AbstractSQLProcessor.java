package com.jam69.easydb.simple;

import java.sql.ResultSetMetaData;

public abstract class AbstractSQLProcessor implements SQLProcessor {
	
	@Override
	public void begin(ResultSetMetaData meta) {
	}
	
    @Override
	abstract public void process(Object[] rs);
    
    @Override
	public void end() {
    }
    
}
