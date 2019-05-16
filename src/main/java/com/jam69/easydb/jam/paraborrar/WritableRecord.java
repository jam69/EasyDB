package com.jam69.easydb.jam.paraborrar;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jam69.easydb.jam.impl.FieldDescriptor;
import com.jam69.easydb.jam.impl.TableDescriptor;


public class WritableRecord {
	
	private static final Logger log=LoggerFactory.getLogger(TableDescriptor.class);

	
	private final PreparedStatement stmt;
	private Map<String,FieldDescriptor> dc;
	
	public WritableRecord(PreparedStatement stmt) {
		this.stmt=stmt;
		
		try {
			dc=new HashMap<>();
			 ResultSetMetaData meta = stmt.getMetaData();
			 for(int i=0;i<meta.getColumnCount();i++) {
				 Class fClass=SQLTypeToClass(meta.getColumnType(i),meta.getPrecision(i));
				 FieldDescriptor fd=new FieldDescriptor(meta.getColumnName(i),fClass);
				 dc.put(fd.getName(),fd);
			 }
		} catch (SQLException e) {
			log.error("getting query metadata", e);
		}
	}
	
	private Class SQLTypeToClass(int columnType,int decimals) {
		switch(columnType) {
		case Types.INTEGER: return Integer.class;
		case Types.CHAR:return String.class;
		case Types.DOUBLE: return Double.class;
		case Types.DECIMAL: 
			if(decimals==0) {
				return Integer.class;
			}else {
				return Double.class;
			}
		default: 
			log.error("Tipo desconocido"+columnType);
			return String.class;
		}
		
	}

	public WritableRecord set(int ndx, int value) {
		try {
			stmt.setInt(ndx, value);
		} catch (SQLException e) {
			log.error("setting int value", e);
		}
		return this;
	}
	
	public WritableRecord set(int ndx, String value) {
		try {
			stmt.setString(ndx, value);
		} catch (SQLException e) {
			log.error("setting String value", e);
		}
		return this;
	}


	
	public WritableRecord insert(Object[] data) {
		try {
			for(int i=0;i<data.length;i++) {
				stmt.setObject(i+1, data[i]);
			}
			save();
		} catch (SQLException e) {
			log.error("setting record values", e);
		}
		return this;
	}
	
	public WritableRecord save() {
		try {
//			if(batchMode){
//                if(batchCount>=batchMax){
//                    int[]res=batchStatement.executeBatch();
//                    checkBatchResult(res);
//                    batchCount=0;
//                }
//                stmt.addBatch();
//                batchCount++;
//            }else{
            	stmt.executeUpdate();
//            }
		} catch (SQLException e) {
			log.error("setting int value", e);
		}
		return this;
	}
	
}