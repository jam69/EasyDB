package com.jam69.easydb.jam.sample;

import java.util.List;

import com.jam69.easydb.jam.EntityManager;
import com.jam69.easydb.jam.impl.Condition;
import com.jam69.easydb.jam.impl.QueryProcessor;

public class Test {
	
	public static void main(String[] args) {
		Test t=new Test();
		
	}
	
	
	Test(){
		
		EntityManager em=new EntityManager("TestTable");
		
		
		// Insercion
		em.persist(new Persona(1,"Pedro",25));	
		em.persist(new Persona(4,"Pedro",25));	
		em.persist(new Persona(5,"Pedro",25));	
		em.persist(new Persona(6,"Pedro",25));	
		em.persist(new Persona(3,"Pedro",27));
		
		
//		List<Persona> lista=new ArrayList<>();
//		lista.add(new Persona(2,"Juan",35));
//		lista.add(new Persona(3,"Lucas",45));
//		em.persist(lista);
				

		// Get By Id
//		Persona p=em.queryById(Persona.class,1);
//		System.out.println("Result:"+p);
		
		// Query
		List<Persona> list=em.queryList(Persona.class, "nombre = 'Pedro' ");
		System.out.println("List:"+list);
		
		Condition condition=Condition.fieldValue("nombre","=","Pedro");
		Condition c=Condition.and(condition, Condition.not(Condition.or(condition,condition)));
		List<Persona> list2=em.queryList(Persona.class,condition);
		System.out.println("List2:"+list2);
		
		em.queryProcess(Persona.class,Condition.not(Condition.not(condition)),new QueryProcessor<Persona>() {
			
			int cont;
			
			@Override
			public void begin() {
				cont=0;
				System.out.println("--Begin--");
			}

			@Override
			public boolean process(Persona entity) {
				System.out.println( cont +":"+entity);
				cont++;
				return true;
			}
			
			@Override
			public void end() {
				System.out.println(cont +" personas encontradas");
				System.out.println("--End--");
			}
		});
		
		// Update
		em.updaterProcess(Persona.class,Condition.fieldValue("nombre","like","Pedro%"),new QueryProcessor<Persona>() {
			
			@Override
			public boolean process(Persona entity) {
				if(entity.getEdad()> 25 ) {
					entity.setNombre(entity.getNombre()+" senior");
				}
				return true;
			}
			
		});
		
		
		// Delete
		em.delete(Persona.class,Condition.fieldValue("nombre", "=", "Pedro"));
		
		
		
	}

}
