package com.jam69.easydb.test;

import java.io.PrintWriter;

import javax.sql.rowset.CachedRowSet;

import com.jam69.easydb.intro.PersistenceBase;
import com.jam69.easydb.simple.AbstractSQLProcessor;

public class Prueba1 {

	public static void main(String[] args) {
		
		Prueba1 p=new Prueba1();
		p.doTest();

	}
	
	private void doTest() {
		
		PersistenceBase p=new PersistenceBase();
		p.readOptionsFile();
		
		
		p.setOutputWriter(new PrintWriter(System.out));  // No ejecuta los comandos, solo los pinta
		
		p.newSelect("codigo,situa")
			.from("GDOINTERR")
			.where("codigo=80413130")
			.dump();
		
		p.newSelect("codigo,situa")
		.from("GDOINTERR")
		.where("codigo=80413130")
		.process(new AbstractSQLProcessor() {
			
			@Override
			public void process(Object[] rs) {
				for(Object o:rs) {
					System.out.print(o+"/t");
				}
				System.out.println();
			}
		});
		
		p.newSelect("codigo,situa")
		.from("GDOINTERR")
		.where("codigo=80413130")
		.parallel(5,new AbstractSQLProcessor() {
			
			@Override
			public void process(Object[] rs) {
				for(Object o:rs) {
					System.out.print(o+"/t");
				}
				System.out.println();
			}
		});
		
		p.newSelect("codigo,situa")
		.from("GDOINTERR,GDOCT")
		.where("codigo=80413130 AND GDOINTERR.situa = GDOCT.codigo")
		.dump();
		
		Object inter=p.newSelect()
		.from(Interr.class)
		.byKey(80413130);
		
		
		// TODO
//		p.newSelect()
//		.from("GDOINTERR","GDOCT")
//		.select("codigo","situa")
//		.where("codigo=80413130 AND GDOINTERR.situa = GDOCT.codigo")
//		.dump();
		
		CachedRowSet rs = p.newSelect("codigo,situa")
		.from("GDOINTERR")
		.where("codigo=80413130")
		.cachedRowSet();
	
		
		p.newInsert("GDOINTERR")
			.fields("codigo,matricula")
			.add(666)
			.add("Matricula")
			.save();
		
		p.newInsert("GDOINTERR")
		.fields("codigo,matricula")
		.compile()
			.set(1,666)
			.set(2,"Matricula")
			.save()
			.set("codigo",777)
			.set("matricula","Otra matr")
			.save()
			.insert(new Object[] {888,"y otra matricula"});
		
		p.newUpdate("GDOINTERR")
			.set("codigo", "777")
			.set("matricula", "xxxx")
			.where("codigo=666");
		
		p.newDelete("GDOINTERR")
			.where("codigo = 666");
		
		Object res=p.callFunction("oneFunction", 1,2,3);
		
		p.callProcedure("oneMethod",1,2,3);
		
		p.setAutoCommit(false);
		p.commit();
		p.rollback();
		
		p.doIt("select codigo,situa from gdointerr where codigo=80413130");
		
		// Batch process
		p.setMaxBatch(500); // optional
		p.startBatch();
		// ops
		//...
		p.endBatch();
		
		p.setSavePoint("oneSavePoint");
		p.rollback("savePoint");
			
	}

}
