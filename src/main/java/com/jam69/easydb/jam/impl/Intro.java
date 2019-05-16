package com.jam69.easydb.jam.impl;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jam69.easydb.jam.annotations.Persistent;
import com.jam69.easydb.jam.annotations.PrimaryKey;

public class Intro {
	
	private static final Logger log=LoggerFactory.getLogger(Intro.class);


	public static TableDescriptor getTableDescription(Class clazz) {
		
		try {
			
			for(Annotation v:clazz.getAnnotations()) {
				System.out.println("A: "+v+ " Type:"+v.annotationType()+" ");		
			}
			
			Persistent p = (Persistent)clazz.getAnnotation(Persistent.class);
			System.out.println("persistent.name="+p.tableName());
			
			
			
			BeanInfo info=Introspector.getBeanInfo(clazz);
			
			BeanDescriptor bd=info.getBeanDescriptor();
			
			TableDescriptor td=new TableDescriptor(clazz);
			
			
			String pkName=null;
			
			for( Field f:clazz.getDeclaredFields()) {
				PrimaryKey a = f.getAnnotation(PrimaryKey.class);
				if(a!=null) {
					System.out.println("Field "+f + " es PrimaryKey");
					pkName=f.getName();
				}
			}
		
			int i=0;
			for(PropertyDescriptor pd:info.getPropertyDescriptors()) {
				if("class".equals(pd.getName())) {
					continue;
				}
				String name=pd.getName();
				
				FieldDescriptor fd=new FieldDescriptor(pd.getName(), pd.getPropertyType());
				fd.setGetterMethod(pd.getReadMethod());
				fd.setSetterMethod(pd.getWriteMethod());
				
				Method getter=pd.getReadMethod();
				for(Annotation v:getter.getAnnotations()) {
					System.out.println("A: "+v+ " Type:"+v.annotationType()+" ");		
				}
				
				td.addField(fd);
				if(pkName!=null && pkName.equals(name)) {
					td.setPrimaryKey(fd);
				}
			}
			
			return td;
			
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static Object[] getValues(TableDescriptor td, Object data) {
		int numCampos=td.getFieldsCount();
		Object[] ret=new Object[numCampos];
		for(int i=0;i<numCampos;i++) {
			ret[0]=getValue(td.getField(i),data);
		}
		return ret;
	}
	
	public static Object getValue(FieldDescriptor fd,Object  data) {
		Method getter=fd.getGetterMethod();
		
		 Object[] paramValues=new Object[0];
		 try {
			return getter.invoke(data, paramValues);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error("getting value",e);
		}
		 return null;
		
	}
	
//	public static void setValues() {
//	       int nFields=rs.getMetaData().getColumnCount();
//	        while(rs.next() ){
//	             ret=itemClass.newInstance();
//	             for(int i=0;i<nFields;i++){
//	           	  Object v=rs.getObject(i);
//	           	  String pName=rs.getMetaData().getColumnName(i);
//	                 set(ret,pName,v);
//	             }
//	        }
//	}

	public static Object createInstance(TableDescriptor td) {
		Object obj;
		try {
			obj = td.getClassInstance().newInstance();
			return obj;
		} catch (InstantiationException e) {
			log.error("creating instance of "+td.getClassInstance() ,e);
		} catch (IllegalAccessException e) {
			log.error("creating instance of "+td.getClassInstance() ,e);
		}
		return null;
	}

	public static void setValue(FieldDescriptor fd,Object data, Object value) {
		Method setter=fd.getSetterMethod();
		 Object[] paramValues=new Object[1];
		 paramValues[0]=value;
		 try {
			setter.invoke(data, paramValues);
		} catch (IllegalAccessException e) {
			log.error("setting value '"+value+"' in field "+fd.getName()+" for "+data ,e);
		} catch (IllegalArgumentException e) {
			log.error("setting value '"+value+"' in field "+fd.getName()+" for "+data ,e);
		} catch (InvocationTargetException e) {
			log.error("setting value '"+value+"' in field "+fd.getName()+" for "+data ,e);
		}
	}



}
