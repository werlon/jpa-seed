package br.edu.faculdadedelta.util;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JPAUtilTest {

	private EntityManager em;
	
	@Test
	public void deveTerInstanciaDoEntityManager(){
		assertNotNull("Deve ter instanciado o entity manager",em);
	}
	
	@Test
	public void deveFecharEntityManager(){
		em.close();
		
		assertFalse("Deve ter instanciado o entity manager",em.isOpen());
	}
	
	@Test
	public void deveAbrirUmaTransacao(){
		assertFalse("Transacao deve estar fechada",em.getTransaction().isActive());	
		em.getTransaction().begin();
		assertTrue("Transacao deve estar aberta",em.getTransaction().isActive());
	}
	
	@Before
	public void instanciarEntityManager(){
		em = JPAUtil.INSTANCE.getEntityManager();
	}
		
	@After
	public void fecharEntityManager(){
		if(em.isOpen()){
			em.close();
		}
	}
}
