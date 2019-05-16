package com.jam69.easydb.jdo.sample;

import com.jam69.easydb.jdo.annotations.PersistenceCapable;
import com.jam69.easydb.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Persona {
	
	public Persona(int idNum,String nombre, int edad) {
		this.idNum=idNum;
		this.nombre=nombre;
		this.edad=edad;
	}

	@PrimaryKey
	int idNum;
	
	String nombre;
	
	int edad;
	
	

}
