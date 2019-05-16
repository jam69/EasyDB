package com.jam69.easydb.jam.paraborrar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Record {
	
	private static final Logger log=LoggerFactory.getLogger(Record.class);

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
