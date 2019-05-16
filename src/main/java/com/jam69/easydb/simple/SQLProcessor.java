package com.jam69.easydb.simple;

import java.sql.ResultSetMetaData;

public interface SQLProcessor{
	public void begin(ResultSetMetaData resultSetMetaData);
    public void process(Object[] rs);
    public void end();
}