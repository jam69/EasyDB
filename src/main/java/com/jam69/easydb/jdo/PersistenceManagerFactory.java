package com.jam69.easydb.jdo;

import java.net.URL;

import com.jam69.easydb.jpa.EntityManager;

public class PersistenceManagerFactory {
	
	private URL urlConnection;
	
	void setConnectionUrl(URL url){
		this.urlConnection=url;
	}
	
	PersistenceManager getConnectionFactory() {
		return new PersistenceManager(urlConnection);
		
	}

	public PersistenceManager getPersistenceManager() {
		return new PersistenceManager(urlConnection);
	}

}
