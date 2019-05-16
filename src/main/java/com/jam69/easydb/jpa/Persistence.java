package com.jam69.easydb.jpa;

public class Persistence {

	public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
		return new EntityManagerFactory(persistenceUnitName);
	}

}
