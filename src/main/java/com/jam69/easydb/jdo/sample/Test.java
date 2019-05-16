package com.jam69.easydb.jdo.sample;

import com.jam69.easydb.jdo.PersistenceManager;
import com.jam69.easydb.jdo.PersistenceManagerFactory;

public class Test {
	
	
	Test(){
		
		PersistenceManagerFactory emf=new PersistenceManagerFactory();
		PersistenceManager em=emf.getPersistenceManager();
		
		Persona data=new Persona(1,"Pedro",25);
		
		try {
			em.currentTransaction().begin();
			em.makePersistent(data);
			em.currentTransaction().commit();
			em.flush();
		} catch (Exception e) {

			e.printStackTrace();
		}finally {
			
		}
	}

}
