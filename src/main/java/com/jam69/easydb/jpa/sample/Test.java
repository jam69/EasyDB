package com.jam69.easydb.jpa.sample;

import com.jam69.easydb.jpa.EntityManager;
import com.jam69.easydb.jpa.EntityManagerFactory;
import com.jam69.easydb.jpa.Persistence;

public class Test {
	
	
	Test(){
		
		EntityManagerFactory emf=Persistence.createEntityManagerFactory("TestTable");
		EntityManager em=emf.createEntityManager();
		
		Persona data=new Persona(1,"Pedro",25);
		
		try {
			em.getTransaction().begin();
			em.persist(data);
			em.getTransaction().commit();
		} catch (Exception e) {

			e.printStackTrace();
		}finally {
			em.close();
		}
	}

}
