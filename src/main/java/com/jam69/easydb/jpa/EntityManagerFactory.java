package com.jam69.easydb.jpa;

public class EntityManagerFactory {

	public EntityManagerFactory(String persistenceUnitName) {
	
	}

	public EntityManager createEntityManager() {
		return new EntityManager();
	}



}
