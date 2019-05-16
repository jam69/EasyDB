/*
 *  Copyright &copy; Indra 2016
 */
package com.jam69.easydb.intro;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jam69.easydb.jam.annotations.PrimaryKey;
import com.jam69.easydb.simple.AbstractSQLProcessor;
import com.jam69.easydb.simple.SQLProcessor;
import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JdbcRowSetImpl;

/**
 *
 * @author jamartinm
 */
public class PersistenceBase
{

    private static final Logger log=LoggerFactory.getLogger(PersistenceBase.class);

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ssa");

    private Connection con;

    private PrintWriter outputWriter=null;
    private FileReader dbOptionsFile=null;
    private String dataSourceName=null;
    private String user;
    private String password;
    private String url;
    private String driverName;

    private boolean batchMode=false;
    private int batchCount=0;
    private Statement batchStatement;
    private int batchMax=50;
    
   
    private Map<String,Savepoint> savepoints=new HashMap<>();


    public void setDBOptionsFile(FileReader reader){
        dbOptionsFile=reader;
    }

    public void setOutputWriter(PrintWriter pw)
    {
        outputWriter=pw;
    }
    
    public void setDataSourceName(String name){
        dataSourceName=name;
    }

    public void setMaxBatch(int max){
        batchMax=max;
    }

    public void setAutoCommit(boolean auto){
        if(checkCon()){
            try
            {
                con.setAutoCommit(auto);
            } catch (SQLException ex)
            {
                log.error("Al cambiar AutoComit", ex);
            }
        }
    }
    
    private Connection getConnection(){
        if(con!=null){
            return con;
        }
        if(outputWriter!=null){
            return null;
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
        if(outputWriter!=null){
            return false;
        }
        con=getConnection();
        
        return con!=null;
    }


    public void callProcedure(String name, Object... args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" call ").append(name).append("(");
        for (Object s : args)
        {
            sb.append(" '").append(s.toString()).append("',");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");

        if (outputWriter != null)
        {
            outputWriter.append(sb.toString() + ";");
            return;
        }

        con = getConnection();

        try
        {
            log.trace("Call procedure:"+sb);
            CallableStatement stmt = con.prepareCall(sb.toString());
            boolean hasResult = stmt.execute();
            if (hasResult)
            {
                ResultSet rs = stmt.getResultSet();
                log.trace("ResultSet=" + rs);
            } else
            {
                int res = stmt.getUpdateCount();
                log.trace("CountResult=" + res);
            }

            stmt.close();
            con.commit();

        } catch (SQLException ex)
        {
            log.error("Calling procedure", ex);
        }

    }

     public Object callFunction(String name, Object... args)
    {
    	StringBuilder sb = new StringBuilder();
        sb.append(" call ").append(name).append("(");
        for (Object s : args)
        {
            sb.append(" '").append(s.toString()).append("',");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");

        if (outputWriter != null)
        {
            outputWriter.append("-- "+sb.toString() + ";");
            return null;
        }

        con = getConnection();

        try
        {
            log.trace("Call procedure:"+sb);
            CallableStatement stmt = con.prepareCall(sb.toString());
            boolean hasResult = stmt.execute();
            if (hasResult)
            {
                ResultSet rs = stmt.getResultSet();
                log.trace("ResultSet=" + rs);
            } else
            {
                int res = stmt.getUpdateCount();
                log.trace("CountResult=" + res);
            }

            stmt.close();
            con.commit();

            return null;

        } catch (SQLException ex)
        {
            log.error("Calling procedure", ex);
            return null;
        }

    }



    public void commit(){
        if(checkCon()){
            try
            {
                con.commit();
            } catch (SQLException ex)
            {
               log.error("Haciendo Commit", ex);
            }
        }
    }

     public void rollback(){
        if(checkCon()){
            try
            {
                con.rollback();
            } catch (SQLException ex)
            {
               log.error("Haciendo Commit", ex);
            }
        }
    }


     public void setSavePoint(String name){
        try
        {
            Savepoint s= con.setSavepoint(name);
            savepoints.put(name, s);
        } catch (SQLException ex)
        {
            log.error("Guardando savepoint", ex);
        }
     }
     public void rollback(String name){
        try
        {
            Savepoint s=savepoints.get(name);
            if(s==null){
                log.error("No encuentro el savepoint '"+name+"'");
                return;
            }
            con.rollback(s);
        } catch (SQLException ex)
        {
            log.error("Volviendo a savepoint "+name, ex);
        }
     }
     public void releaseSavepoint(String name){
        try
        {
            Savepoint s=savepoints.get(name);
            if(s==null){
                log.error("No encuentro el savepoint '"+name+"'");
                return;
            }
            con.releaseSavepoint(s);
        } catch (SQLException ex)
        {
            log.error("Olvidando savepoint "+name, ex);
        }
     }


    public  void exit()
    {
        try
        {
            if (con != null)
            {
                con.close();
                log.info("Closed DB");
            }
            if(outputWriter!=null){
                outputWriter.close();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void startBatch(){
        batchMode=true;
        if(batchCount>0){
            try {
                log.error("Quedan comandos en el batch y no puedo empezar otro");
                batchStatement.clearBatch();
            } catch (SQLException ex) {
                log.error("Limpiando el batch", ex);
            }
        }
    }
    public void endBatch(){
        batchMode=false;
        if(batchCount>0){
            try
            {
                checkBatchResult(batchStatement.executeBatch());
                batchCount=0;
            } catch (SQLException ex)
            {
                log.info("Finalizando el batch", ex);
            }
        }
    }

    public void doIt(String command){

        if(outputWriter!=null){
            outputWriter.println(command+";");
            return;
        }

        System.out.println(command+";");
        try
        {
            con =getConnection();
            
            if(batchMode){
                if(batchCount>=batchMax){
                    int[]res=batchStatement.executeBatch();
                    checkBatchResult(res);
                    batchCount=0;
                }
                if(batchCount==0){
                    if(batchStatement!=null){
                         batchStatement.clearBatch();
                    }else{
                        batchStatement = con.createStatement();
                    }
                }
                batchStatement.addBatch(command);
                batchCount++;
            }else{
                Statement st = con.createStatement();
                int r = st.executeUpdate(command);
                System.out.println("Insertados " + r + " registros");
                st.close();
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }
        
    }

    private void checkBatchResult(int[] res){
        int contOK=0;
        int contKO=0;
        for(int r:res){
            if(r<0){
                contKO++;
            }else{
                contOK++;
            }
        }
        log.info("Batch: ok:"+contOK+" errores:"+contKO);
    }

    public static String NULL()
    {
        return "NULL";
    }

    public static String str(String s)
    {
        if(s==null) return "NULL";
        return "'" + s + "'";
    }

    public static String date(Date s)
    {
        if(s==null) return "NULL";
        String dateStr = df.format(s);
        return "TO_DATE('" + dateStr + "','yyyy/mm/dd hh:mi:ssam')";
    }
     public static String blob(String s)
    {
        if(s==null) return "NULL";
        return "RAWTOHEX('" + s + "')";
    }
    

     private WritableRecord compile(String insertStr) {
    	 System.out.println("compile:["+insertStr+"]");
         try
         {
             con = getConnection();
            
             // TODO   añadir values ? a los nombres de campo
             PreparedStatement st = con.prepareStatement(insertStr);
            
             return new WritableRecord(st);
             
         } catch (SQLException ex)
         {
             log.error("compile(prepare) insert", ex);
             return null;
         }
     }
     
     private Object doRetrieveBykey(TableDescriptor td,Object key)
     {
         System.out.println("retrieveByKey:["+key+"]");
         try
         {
             con = getConnection();
             
             Object ret=null;
             
             
             String keyName=td.getKeyName();
             Class itemClass=td.getClassInstance();
           
             Statement st = con.createStatement();
             String sql="select * from "+td.getName()+" where "+keyName+"="+key;
             ResultSet rs =st.executeQuery(sql);
             int nFields=rs.getMetaData().getColumnCount();
             while(rs.next() ){
                  ret=itemClass.newInstance();
                  for(int i=0;i<nFields;i++){
                	  Object v=rs.getObject(i);
                	  String pName=rs.getMetaData().getColumnName(i);
                      set(ret,pName,v);
                  }
             }
             rs.close();
             st.close();
             return ret;
         } catch (SQLException ex)
         {
             log.error("doQueryProcess", ex);
         } catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         return null;
     } 
     
     
     private String introGetKeyName(Class clazz) {
    	 String pkName=null;
			
    	 for( Field f:clazz.getDeclaredFields()) {
    		 Key a = f.getAnnotation(Key.class);
    		 if(a!=null) {
    			 //	System.out.println("Field "+f + " es PrimaryKey");
    			 pkName=f.getName();
    		 }
    	 }
		return pkName;
     }
     
     private void set(Object obj,String pName,Object value) {
    	 Class[] paramClases=new Class[1];
    	 Object[] paramValues=new Object[1];
    	 paramClases[0]=value.getClass();
    	 paramValues[0]=value;
    	 
    	 Class c=obj.getClass();
    	 Method m;
		try {
			m = c.getMethod("set"+pName,paramClases);
			m.invoke(obj, paramValues);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	 
     }

    private void doQueryProcess(String queryStr,SQLProcessor p)
    {
        System.out.println("query:["+queryStr+"]");
        try
        {
            con = getConnection();
            

            Statement st = con.createStatement();
          
            ResultSet rs =st.executeQuery(queryStr);
            p.begin(rs.getMetaData());
             int nFields=rs.getMetaData().getColumnCount();
            while(rs.next() ){
               Object[] rec=new Object[nFields];
                for(int i=0;i<nFields;i++){
                    rec[i]=rs.getObject(i+1);
                 }
                p.process(rec);
            }
            p.end();
            rs.close();
            st.close();
        } catch (SQLException ex)
        {
            log.error("doQueryProcess", ex);
        }
    }
    
     private void doParallelProcess(String queryStr,int n,SQLProcessor p)
    {
        try
        {
            con =getConnection();
            

            ExecutorService service=Executors.newFixedThreadPool(n);

            Statement st = con.createStatement();
            ResultSet rs =st.executeQuery(queryStr);
            int nFields=rs.getMetaData().getColumnCount();
            p.begin(rs.getMetaData());
            while(rs.next() ){
                Object[] rec=new Object[nFields];
                for(int i=0;i<nFields;i++){
                    rec[i]=rs.getObject(i+1);
                 }
               service.execute(() ->
                {
                    p.process(rec);
                });
            }
            rs.close();
            st.close();
            p.end();

        } catch (SQLException ex)
        {
            log.error("doQueryProcess", ex);
        }

    }

    private CachedRowSet doCachedRowSet(String queryStr)
    {
        try
        {
           con =getConnection();
                       
            Statement st = con.createStatement();
            ResultSet rs=st.getResultSet();
            CachedRowSetImpl crs=new CachedRowSetImpl();
            crs.populate(rs);
            rs.close();
            st.close();
            return crs;
        } catch (SQLException ex)
        {
            log.error("doCachedRowSet", ex);
        }

        return null;
    }
    private CachedRowSet doLargeCachedRowSet(String queryStr,int pageSize)
    {
        try
        {
            con =getConnection();
            

            CachedRowSetImpl crs=new CachedRowSetImpl();
            crs.setUrl(queryStr);
            crs.setDataSourceName(dataSourceName);
            crs.setUsername(queryStr);
            crs.setPassword(queryStr);
            crs.setCommand(queryStr);
            crs.setPageSize(pageSize);
          //  crs.execute(con);
            crs.execute();
            return crs;
        } catch (SQLException ex)
        {
            log.error("doCachedRowSet", ex);
        }

        return null;
    }

     private RowSet doRowSet(String queryStr)
    {
        try
        {
            con = getConnection();

            Statement st = con.createStatement();
            ResultSet rs=st.getResultSet();
            RowSet crs=new JdbcRowSetImpl(rs);
            rs.close();
            st.close();
            return crs;
        } catch (SQLException ex)
        {
            log.error("doRowSet", ex);
        }

        return null;
    }

    private static class DumpProcessor extends AbstractSQLProcessor{

        private final PrintWriter out;
        
        public DumpProcessor(){
            this.out=new PrintWriter(System.out);
        }

        public DumpProcessor(PrintWriter out){
            this.out=out;
        }

        @Override
        public void process(Object[] rs)
        {
            StringBuilder sb=new StringBuilder("\t");
            for (Object x: rs){
                sb.append(x).append("\t");
            }
            out.println(sb);
            out.flush();
        }

    }

    public Insert newInsert(String table){
        Insert q =new Insert();
        return q.insert(table);
    }

    public class Insert {
        private final StringBuilder sb=new StringBuilder();

        public Insert insert(String table){
            sb.append("INSERT INTO ").append(table).append(" (");
            return this;
        }
        public Insert fields(String f){
            sb.append(f).append(") VALUES (");
            return this;
        }
        public Insert add(String f){
            sb.append(str(f)).append(",");
            return this;
        }
        public Insert add(Integer f){
            sb.append(f).append(",");
            return this;
        }
        public Insert add(Long f){
            sb.append(f).append(",");
            return this;
        }
        public Insert add(Date f){
            sb.append(date(f)).append(",");
            return this;
        }
        public Insert addBlob(String s){
            sb.append(blob(s)).append(",");
            return this;
        }
        public void save(){
            sb.deleteCharAt(sb.length()-1);
            sb.append(")");
            doIt(sb.toString());
        }
        public WritableRecord compile(){
            sb.deleteCharAt(sb.length()-1);
            sb.append(")");
            return PersistenceBase.this.compile(sb.toString());
        }
        
    }
    
    public class FieldDescriptor {
    	private final String name;
    	private final int type;
    	private final int pos;
    	
    	public FieldDescriptor(String name,int type,int pos) {
    		this.name=name;
    		this.type=type;
    		this.pos=pos;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public int getType() {
    		return type;
    	}
    	
    	public int getPos() {
    		return pos;
    	}
    }
    
    public class WritableRecord {
    	
    	private final PreparedStatement stmt;
    	private Map<String,FieldDescriptor> dc;
    	
    	public WritableRecord(PreparedStatement stmt) {
    		this.stmt=stmt;
    		
    		try {
				dc=new HashMap<>();
				 ResultSetMetaData meta = stmt.getMetaData();
				 for(int i=0;i<meta.getColumnCount();i++) {
					 FieldDescriptor fd=new FieldDescriptor(meta.getColumnName(i),meta.getColumnType(i),i+1);
					 dc.put(fd.getName(),fd);
				 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	public WritableRecord set(int ndx, int value) {
    		try {
				stmt.setInt(ndx, value);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return this;
    	}
    	
    	public WritableRecord set(int ndx, String value) {
    		try {
				stmt.setString(ndx, value);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return this;
    	}
    	public WritableRecord set(String fieldName, int value) {
    		try {
    			int ndx=dc.get(fieldName).getPos();
				stmt.setInt(ndx, value);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return this;
    	}
    	
    	public WritableRecord set(String fieldName, String value) {
    		try {
    			int ndx=dc.get(fieldName).getPos();
				stmt.setString(ndx, value);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return this;
    	}
    	
    	public WritableRecord save() {
    		try {
    			if(batchMode){
                    if(batchCount>=batchMax){
                        int[]res=batchStatement.executeBatch();
                        checkBatchResult(res);
                        batchCount=0;
                    }
                    stmt.addBatch();
                    batchCount++;
                }else{
                	stmt.executeUpdate();
                }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return this;
    	}
    	
    }
    
    public Delete newDelete(String table){
        Delete q =new Delete();
        return q.delete(table);
    }

    public class Delete {
        private final StringBuilder sb=new StringBuilder();

        public Delete delete(String table){
            sb.append("DELETE ").append(table).append(" FROM ");
            return this;
        }
        public void where(String condition){
            sb.deleteCharAt(sb.length()-1);
            sb.append(" WHERE ").append(condition);
            doIt(sb.toString());
        }
        public void where(String fmt,Object[] args){
            sb.deleteCharAt(sb.length()-1);
            sb.append(" WHERE ").append(String.format(fmt, args));
            doIt(sb.toString());
        }
    }

    public Update newUpdate(String table){
        Update q =new Update();
        return q.update(table);
    }

    public class Update {
        private final StringBuilder sb=new StringBuilder();

        public Update update(String table){
            sb.append("UPDATE ").append(table).append(" SET ");
            return this;
        }
        public Update set(String field,String f){
            sb.append(field).append("=").append(str(f)).append(",");
            return this;
        }
        public Update set(String field,Integer f){
            sb.append(field).append("=").append(f).append(",");
            return this;
        }
        public Update set(String field,Long f){
            sb.append(field).append("=").append(f).append(",");
            return this;
        }
        public Update set(String field,Date f){
            sb.append(field).append("=").append(date(f)).append(",");
            return this;
        }
        public void where(String condition){
            sb.deleteCharAt(sb.length()-1);
            sb.append(" WHERE ").append(condition);
            doIt(sb.toString());
        }
        public void where(String fmt,Object[] args){
            sb.deleteCharAt(sb.length()-1);
            sb.append(" WHERE ").append(String.format(fmt, args));
            doIt(sb.toString());
        }
    }

    public Select newSelect(){
        Select q =new Select();
        return q.select();
    }
    
    public Select newSelect(String fields){
        Select q =new Select();
        return q.select(fields);
    }
    

    
    public class TableDescriptor {
    	private String name;
    	private String keyName;
    	private Class clazz;
    	
    	// meta
    	public TableDescriptor(String name) {
    		this.name=name;
    	}
    	
    	public TableDescriptor(Class clazz) {
    		this.clazz=clazz;
    		this.name=clazz.getSimpleName();
    		this.keyName=introGetKeyName(clazz);
    	}

		public Class getClassInstance() {
			return clazz;
		}

		public String getKeyName() {
			return keyName;
		}

		public String getName() {
			return name;
		}
    }
    
    public class Select {
    	
    	private TableDescriptor table =null; 
    	
        private final StringBuilder sb=new StringBuilder();

        public Select select(String fields){
            sb.append("SELECT ").append(fields);
            return this;
        }
        public Select select(){
            return this;
        }
        public Select from(String tableName){
        	if(sb.length()==0) {
        		table =new TableDescriptor(tableName);
        	}else {
        		sb.append(" FROM ").append(tableName);
        	}
            return this;
        }
        public Select from(Class clazz){
        	if(sb.length()==0) {
        		table =new TableDescriptor(clazz);
        	}else {
        		// No debería pasar por aqui
        	}
            return this;
        }
        public Select where(String condition){
        	if(table!=null) {
        		// todo
        	}else {
        		sb.append(" WHERE ").append(condition);
        	}
            return this;
        }
        public void dump(){
            doQueryProcess(sb.toString(),new DumpProcessor());
        }
        public void dump(PrintWriter out){
            doQueryProcess(sb.toString(),new DumpProcessor(out));
        }
        public void process(SQLProcessor processor){
            doQueryProcess(sb.toString(),processor);
        }
        public void parallel(int numThreads,SQLProcessor processor){
            doParallelProcess(sb.toString(),numThreads,processor);
        }
        public RowSet rowSet(){
            return doRowSet(sb.toString());
        }
        public CachedRowSet cachedRowSet(){
            return doCachedRowSet(sb.toString());
        }
        public Object byKey(Object key){
            return doRetrieveBykey(table,key);
        }
    }

    public class Result implements Iterator<Record> {

        private final ResultSet rs;

        public Result(ResultSet rs){
            this.rs=rs;
        }

        @Override
        public boolean hasNext()
        {
            try
            {
                return rs.next();
            } catch (SQLException ex)
            {
                log.error("gettingNext", ex);
                return false;
            }
        }

        @Override
        public Record next()
        {
            return new Record(rs);
        }


    }

    public class Record {

        private final ResultSet rs;

        public Record(ResultSet rs){
            this.rs=rs;
        }

        public Object getObject(String field){
            try{
                return rs.getObject(field);
            } catch (SQLException ex){
               log.error("gettingObject", ex);
               return null;
            }
        }
        public int getInt(String field){
             try{
                return rs.getInt(field);
            } catch (SQLException ex){
               log.error("gettingInt", ex);
               return 0;
            }
        }
        public long getLong(String field){
            try{
                return rs.getLong(field);
            } catch (SQLException ex){
               log.error("gettingLong", ex);
               return 0L;
            }
        }
        public Date getDate(String field){
             try{
                return rs.getDate(field);
            } catch (SQLException ex){
               log.error("gettingDate", ex);
               return null;
            }
        }
    }

}
