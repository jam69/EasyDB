package com.jam69.easydb.jam.impl;

public class Condition {

	public String toSQL() {
		return toString();
	}
	
	/**
	 * Convierte un valor en string para pasar a SQL
	 * Si es nulo pone NULL
	 * Si es String lo pone entre comillas
	 * Si es fecha ... ? // TODO
	 * Si es boolean ... ? // TODO
	 * Si es double .. ? // TODO
	 * Si es blob... ? // TODO
	 * 
	 * @param obj
	 * @return
	 */
	private static String toSql(Object obj) {
		if(obj==null) {
			return "NULL";
		}
		if(obj instanceof String) {
			return "'"+obj+"'";
		}
		return obj.toString();
	}
	
	public static Condition fieldValue(String fieldName, String operator,Object value) {
		return new FieldValue(fieldName,operator,value);
	}
	
	private static class FieldValue extends Condition {
		
		private final String fieldName;
		private final String operator;
		private final Object value;
		
		public FieldValue(String fieldName, String operator,Object value){
			this.fieldName=fieldName;
			this.operator=operator;
			this.value=value;
		}
		
		@Override
		public String toSQL() {
			return fieldName +" "+ operator+" "+ toSql(value)  ;
		}
		
	}
	
	public static Condition and(Condition condLeft,Condition condRight){
		return new And(condLeft,condRight);
	}
	
	private static class And extends Condition {
		
		private final Condition condLeft;
		private final Condition condRight;
		
		public And(Condition condLeft,Condition condRight){
			this.condLeft=condLeft;
			this.condRight=condRight;
		}
		
		@Override
		public String toSQL() {
			return condLeft.toSQL() + " AND "+ condRight.toSQL();
		}
	
	}
	
	public static Condition or(Condition condLeft,Condition condRight){
		return new Or(condLeft,condRight);
	}
	private static class Or extends Condition {
		
		private final Condition condLeft;
		private final Condition condRight;
		
		public Or(Condition condLeft,Condition condRight){
			this.condLeft=condLeft;
			this.condRight=condRight;
		}
		
		@Override
		public String toSQL() {
			return condLeft.toSQL() + " OR "+ condRight.toSQL();
		}
	
	}
	
	public static Condition not(Condition condition){
		return new Not(condition);
	}
	
	private static class Not extends Condition {
		
		private final Condition condition;
	
		public Not(Condition condition){
			this.condition=condition;
		}
		
		@Override
		public String toSQL() {
			return " NOT ("+ condition.toSQL()+ ")" ;
		}
	
	}
	
	public static Condition in(String fieldName,Object[] values){
		return new In(fieldName,values);
	}
	
	private static class In extends Condition {
		
		private final String fieldName;
		private final Object[] values;
	
		public In(String fieldName,Object[] values){
			this.fieldName=fieldName;
			this.values=values;
		}
		
		@Override
		public String toSQL() {
//			return fieldName +" IN ("+ values+ ")" 
			StringBuilder sb=new StringBuilder();
			sb.append(fieldName).append(" IN (");
			boolean first=true;
			for(Object o :values) {
				if(first) {
					first=false;
				}else {
					sb.append(",");
				}
				sb.append(toSql(o));
			}
			sb.append(")");
			return sb.toString() ;
		}
	
	}
	

}
