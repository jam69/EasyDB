package com.jam69.easydb.jam.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction {
	
	private static final Logger log=LoggerFactory.getLogger(Transaction.class);
	
	private final Connection con;
	
	public Transaction(Connection con) {
		this.con=con;
	}
	
	public void commit() {
		try {
			con.commit();
		} catch (SQLException e) {
			log.error("Doing commit",e);
		}
	}
	
	public void rollback() {
		try {
			con.rollback();
		} catch (SQLException e) {
			log.error("Doing rollback",e);
		}
	}

}
