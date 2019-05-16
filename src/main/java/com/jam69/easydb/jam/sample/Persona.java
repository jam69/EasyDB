package com.jam69.easydb.jam.sample;

import com.jam69.easydb.jam.annotations.Persistent;
import com.jam69.easydb.jam.annotations.PrimaryKey;

/*
 * 
 * Para crear la tabla de pruebas
 * drop table persona;
 * 
 * create table PERSONA ( idNum int primary key , nombre char(30), edad integer);
 * 
 * 
 */



@Persistent(tableName="hola")
public class Persona {
	
	public Persona(int idNum,String nombre, int edad) {
		this.idNum=idNum;
		this.nombre=nombre;
		this.edad=edad;
	}

	@PrimaryKey
	private int idNum;
	
	private String nombre;
	
	private int edad;
	
	public Persona() {
		
	}

	public int getIdNum() {
		return idNum;
	}

	public void setIdNum(int idNum) {
		this.idNum = idNum;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}

	@Override
	public String toString() {
		return "Persona [idNum=" + idNum + ", nombre=" + nombre + ", edad=" + edad + "]";
	}
	
	
}
