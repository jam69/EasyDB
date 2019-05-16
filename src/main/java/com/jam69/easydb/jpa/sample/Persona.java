package com.jam69.easydb.jpa.sample;

import com.jam69.easydb.jpa.annotations.Column;
import com.jam69.easydb.jpa.annotations.Entity;
import com.jam69.easydb.jpa.annotations.Id;
import com.jam69.easydb.jpa.annotations.Table;

@Entity
@Table("Persona")
public class Persona {
	
	public Persona(int idNum,String nombre, int edad) {
		this.idNum=idNum;
		this.nombre=nombre;
		this.edad=edad;
	}

	@Id
	int idNum;
	
	@Column
	String nombre;
	
	@Column
	int edad;
	
	

}
