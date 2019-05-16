package com.jam69.easydb.jam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jam69.easydb.jam.impl.Condition;
import com.jam69.easydb.jam.impl.ConnectionManager;
import com.jam69.easydb.jam.impl.FieldDescriptor;
import com.jam69.easydb.jam.impl.Intro;
import com.jam69.easydb.jam.impl.QueryProcessor;
import com.jam69.easydb.jam.impl.TableDescriptor;
import com.jam69.easydb.jam.impl.Transaction;
import com.jam69.easydb.jam.paraborrar.WritableRecord;

public class EntityManager {
	
	private static final Logger log=LoggerFactory.getLogger(EntityManager.class);

	
	
	private Map<String,TableDescriptor> tabsByClassName=new HashMap<>(); 
	
	private ConnectionManager cManager=new ConnectionManager();  // @Inject
	

	public EntityManager(String string) {
		// read properties file
		
	}

	public void persist(Object data) {
		
		Class clazz= data.getClass();
		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
		if(td==null) {
			td=Intro.getTableDescription(clazz);
			tabsByClassName.put(clazz.getSimpleName(), td);
		}
		
		Connection con=cManager.getConnection();  // don't close connection
		try {
				
			
			PreparedStatement st=con.prepareStatement(td.getInsertSQL());
			insertObject(td,data,st);
			boolean v=st.execute();
			st.close();
			con.commit();
			
			log.info("Insert: "+data+" ret="+v);
		} catch (SQLException e) {
			if(con!=null) {
				try {
					con.rollback();
				} catch (SQLException e1) {
					log.error("clossing",e);
				}
			}
			
			log.error("clossing",e);
		}
		
	}
	
	
//	public void persist(List<Object> data) {
//		
//		Class clazz= data.getClass();
//		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
//		if(td==null) {
//			td=Intro.getTableDescription(clazz);
//			tabsByClassName.put(clazz.getSimpleName(), td);
//		}
//		
//		Connection con=cManager.getConnection();  // don't close connection
//		try (
//			Statement st=con.createStatement();
//			){
//			for(Object obj:data) {
//				insertObject(td,obj);
//			}
//			
//		} catch (SQLException e) {
//			log.error("clossing",e);
//		}
//		
//	}
	
	public <T> T queryById(Class<T> clazz,Object key) {
		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
		if(td==null) {
			td=Intro.getTableDescription(clazz);
			tabsByClassName.put(clazz.getSimpleName(), td);
		}
		Connection con=cManager.getConnection();
		PreparedStatement st=null;
		ResultSet rs=null;
		try {
			st = con.prepareStatement(td.getQueryByIdSQL());
			rs =st.executeQuery();
			while(rs.next()) {
				Object obj=getObject(td,rs);
				return (T) obj;
			}
			
		} catch (SQLException e) {
			log.error("processing Query",e);
		}finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
			if(st!=null) {
				try {
					st.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
		}
        return null;
	}



	public <T> List<T> queryList(Class<T> clazz, Condition condition) {
		return queryList(clazz,condition.toSQL());
	}
	
	public <T> List<T> queryList(Class<T> clazz, String whereString) {
		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
		if(td==null) {
			td=Intro.getTableDescription(clazz);
			tabsByClassName.put(clazz.getSimpleName(), td);
		}
		List<T> ret=new ArrayList<>();
		Connection con=cManager.getConnection();
		Statement st=null;
		ResultSet rs=null;
		try {
			st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			String sql=td.getQuerySQL(whereString);
			log.info("SQL(for list):"+sql);
			rs =st.executeQuery(sql);
			while(rs.next()) {
//			Record record=new Record(rs);
				Object obj=getObject(td,rs);
				ret.add((T) obj);
			}
			return ret;
			
		} catch (SQLException e) {
			log.error("processing Query",e);
		}finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
			if(st!=null) {
				try {
					st.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
		}
        return null;
		
	}

	public <T> void queryProcess(Class<T> clazz, Condition condition, QueryProcessor<T> queryProcessor) {
		queryProcess(clazz,condition.toSQL(),queryProcessor);
	}
	
	public <T> void queryProcess(Class<T> clazz, String whereString, QueryProcessor<T> queryProcessor) {
		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
		if(td==null) {
			td=Intro.getTableDescription(clazz);
			tabsByClassName.put(clazz.getSimpleName(), td);
		}
		Connection con=cManager.getConnection();
		queryProcessor.begin();
		Statement st=null;
		ResultSet rs=null;
		try {
			st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			String sql=td.getQuerySQL(whereString);
			log.info("SQL(for Update):"+sql);
			rs =st.executeQuery(sql);
			while(rs.next()) {
				Object obj=getObject(td,rs);
				queryProcessor.process( (T) obj);
			}
			queryProcessor.end();
			
		} catch (SQLException e) {
			log.error("processing Query",e);
		}finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
			if(st!=null) {
				try {
					st.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
		}
  	}
	
	public <T> void updaterProcess(Class<T> clazz, Condition condition, QueryProcessor<T> queryProcessor) {
		updaterProcess(clazz,condition.toSQL(),queryProcessor);
	}
	
	public <T> void updaterProcess(Class<T> clazz, String whereString, QueryProcessor<T> queryProcessor) {
		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
		if(td==null) {
			td=Intro.getTableDescription(clazz);
			tabsByClassName.put(clazz.getSimpleName(), td);
		}
		Connection con=cManager.getConnection();
		queryProcessor.begin();
		Statement st=null;
		ResultSet rs=null;
		try {
			st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
			String sql=td.getQuerySQL(whereString);
			log.info("SQL(for Update):"+sql);
			rs =st.executeQuery(sql);
			while(rs.next()) {
				Object obj=getObject(td,rs);
				queryProcessor.process( (T) obj);				
				updateObject(td,obj,rs);
				rs.updateRow();
				log.info("Updated entity:"+obj);
			}
			queryProcessor.end();
			
			con.commit();
			
		} catch (SQLException e) {
			log.error("processing Query",e);
		}finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
			if(st!=null) {
				try {
					st.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
		}
	}

	public <T> void delete(Class<T> clazz, Condition condition) {
		delete(clazz, condition.toSQL());
	}
	
	public <T> void delete(Class<T> clazz, String whereString) {
		TableDescriptor td=tabsByClassName.get(clazz.getSimpleName());
		if(td==null) {
			td=Intro.getTableDescription(clazz);
			tabsByClassName.put(clazz.getSimpleName(), td);
		}
		Connection con=cManager.getConnection();
		Statement st=null;
	
		try {
			st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
			String sql=td.getDeleteSQL(whereString);
			log.info("SQL(delete):"+sql);
			int n=st.executeUpdate(sql);
			log.info("Deleted "+n+" registros");
			con.commit();
			
		} catch (SQLException e) {
			log.error("processing Query",e);
		}finally {

			if(st!=null) {
				try {
					st.close();
				} catch (SQLException e) {
					log.error("clossing",e);
				}
			}
		}
		
		
	}



	private Object getObject(TableDescriptor td, ResultSet rs) {
		Object ret=Intro.createInstance(td);
		Object value;
		try {
			int i=1;
			for(FieldDescriptor fd:td.getFields()) {
				value = rs.getObject(i++);
				Intro.setValue(fd,ret,value );
			}
		} catch (SQLException e) {
			log.error("gettingObject",e);
		}
		return ret;
	}
	
	private void updateObject(TableDescriptor td,Object data, ResultSet rs) {
		try {
			int i=1;
			for(FieldDescriptor fd:td.getFields()) {
				Object value=Intro.getValue(fd, data);
				rs.updateObject(i++,value);
			}
		} catch (SQLException e) {
			log.error("settingObject",e);
		}
	}
	
//	private void insertObject(TableDescriptor td,Object data, ResultSet rs) {
//		try {
//			rs.moveToInsertRow();
//			for(FieldDescriptor fd:td.getFields()) {
//				Object value=Intro.getValue(fd, data);
//				rs.updateObject(fd.getPos(),value);
//			}
//			rs.insertRow();
//		} catch (SQLException e) {
//			log.error("settingObject",e);
//		}
//	}
	
//	private void insertObject(TableDescriptor td, Object data) {
//		
//		try {
//			Connection con=cManager.getConnection();
//			PreparedStatement st = con.prepareStatement(td.getInsertSQL());
//			WritableRecord wr=new WritableRecord(st);
//			Object[] values=Intro.getValues(td,data);
//			wr.insert(values);
//		} catch (SQLException e) {
//			log.error("inserting values",e);
//		}   
//	}
	
	private void insertObject(TableDescriptor td,Object data, PreparedStatement st) {
	try {
		int i=1;
		for(FieldDescriptor fd:td.getFields()) {
			Object value=Intro.getValue(fd, data);
			st.setObject(i++,value);
		}
	} catch (SQLException e) {
		log.error("settingObject",e);
	}
}

//	public Transaction begin() {
//		con.
//		return new Transaction(con);
//	}
//	

}
