package com.jam69.easydb.jam.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableDescriptor {
	
	private static final Logger log=LoggerFactory.getLogger(TableDescriptor.class);

	
	private String name;
	private Class clazz;
	private List<FieldDescriptor> fields = new ArrayList<>();
	private FieldDescriptor primaryKey=null;

	private transient String sqlInsert;
	private transient String sqlQueryById;
	private transient String sqlQuery;

	// meta
//	public TableDescriptor(String name) {
//		this.name = name;
//	}

	public TableDescriptor(Class clazz) {
		this.clazz = clazz;
		this.name = clazz.getSimpleName();
	}

	public Class getClassInstance() {
		return clazz;
	}

	public String getKeyName() {
		if(primaryKey==null) {
			log.error("This entity don't have primaryKey field");
			return null;
		}
		return primaryKey.getName();
	}

	public String getName() {
		return name;
	}

	public void addField(FieldDescriptor fd) {
		fields.add(fd);
	}

	public int getFieldsCount() {
		return fields.size();
	}

	public FieldDescriptor getField(int i) {
		return fields.get(i);
	}

	public List<FieldDescriptor> getFields() {
		return fields;
	}
	
	public void setPrimaryKey(FieldDescriptor fd) {
		this.primaryKey=fd;
	}
	
	public FieldDescriptor getPrimaryKey() {
		return primaryKey;
	}

	public String getInsertSQL() {
		if (sqlInsert != null) {
			return sqlInsert;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(getName()).append("(");
		boolean first = true;
		for (FieldDescriptor fd : fields) {
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(fd.getName());
		}
		sb.append(") VALUES (");
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		sb.append(")");
		sqlInsert = sb.toString();
		return sqlInsert;
	}

	public String getQueryByIdSQL() {
		if (sqlQueryById != null) {
			return sqlQueryById;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");

		boolean first = true;
		for (FieldDescriptor fd : fields) {
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(fd.getName());
		}
		
		sb.append(" FROM ")
			.append(getName())
			.append(" WHERE ")
			.append(getKeyName())
			.append(" = ?");
		
		sqlQueryById = sb.toString();
		return sqlQueryById;
	}

	public String getQuerySQL(String whereString) {
		if (sqlQuery != null) {
			return sqlQuery + " WHERE "+whereString;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");

		boolean first = true;
		for (FieldDescriptor fd : fields) {
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(fd.getName());
		}
		
		sb.append(" FROM ")
			.append(getName());
		
		sqlQuery = sb.toString();
					
		return sqlQuery+ " WHERE "+whereString;
	}
	
	

	public String getDeleteSQL(String whereString) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ")
			.append(getName())
			.append(" WHERE ")
			.append(whereString);
		return sb.toString();
	}
}