package com.jam69.easydb.jam.impl;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {
	
	private static final Logger log=LoggerFactory.getLogger(ConnectionManager.class);

	 
	private String dataSourceName;
	private Connection con;
	private FileReader dbOptionsFile;
	private String driverName;
	private String user;
	private String password;


	private String url;
	
	public ConnectionManager() {
		readOptionsFile();
	}

	public void setDataSourceName(String name){
	        dataSourceName=name;
	    }
	
	  public Connection getConnection(){
	        if(con!=null){
	            return con;
	        }

	        if(dbOptionsFile!=null){
	            readOptionsFile();
	        }
	        if(dataSourceName!=null){
	            createConnectionFromDatasource(dataSourceName);
	        }else{
	            createConnectionFromDriver(driverName);
	        }
	        return con;
	    }
	    
	    private void createConnectionFromDatasource(String name){
	        try
	        {
	            Context ctx = new InitialContext();
	            DataSource ds = (DataSource)ctx.lookup(name);
	            con=ds.getConnection(user, password);
	        } catch (NamingException|SQLException ex)
	        {
	            log.error("obteniendo conexion from datasource '"+name+"'", ex);
	        }
	    }
	    
	    public void setUserPassword(String user,String password){
	        this.user=user;
	        this.password=password;
	    }

	    private void createConnectionFromDriver(String driver){

	        try{
	        //OracleConnectionPoolDataSource ds=new OracleConnectionPoolDataSource();
	        //ds.setURL(url);
	        //ds.setUser(user);
	        //ds.setPassword(password);
	        //con=ds.getConnection( );
	        Class.forName(driver);
	        con = DriverManager.getConnection(url, user, password);
	        con.setAutoCommit(false);
	         } catch (ClassNotFoundException | SQLException ex)
	        {
	            log.error("obteniendo conexion from driver '"+driver+"'", ex);
	        }

	    }

	    public void readOptionsFile()
	    {
	        try
	        {
	            Properties p = new Properties();
	            if (dbOptionsFile != null)
	            {

	                p.load(dbOptionsFile);

	            } else
	            {
	                p.load(new FileReader("DBase.properties"));
	            }

	            url = p.getProperty("dburl");
	            user = p.getProperty("user");
	            password = p.getProperty("password");
	            driverName = p.getProperty("driver");
	            dataSourceName = p.getProperty("datasource");
	        } catch (IOException ex)
	        {
	            log.info("No encuentro el fichero de opciones", ex);
	        }
	    }

	    private boolean checkCon(){

	        con=getConnection();
	        
	        return con!=null;
	    }

}
