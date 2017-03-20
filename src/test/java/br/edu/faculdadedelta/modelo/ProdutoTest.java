package br.edu.faculdadedelta.modelo;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import br.edu.faculdadedelta.util.JPAUtil;

public class ProdutoTest {

	private EntityManager em;
	
	@Test
	public void deveExcluirProduto(){
		deveSalvarProduto();
		
		TypedQuery<Long> query = em.createQuery(" SELECT MAX(p.id) FROM Produto p",Long.class);
		Long id = query.getSingleResult();
		
		em.getTransaction().begin();
		
		Produto produto = em.find(Produto.class, id);
		//Produto produto = new Produto(); //forçar um erro
		//produto.setId(12345678L);
		em.remove(produto);
		
		em.getTransaction().commit();
		
		Produto produtoExcluido = em.find(Produto.class, id);
		
		assertNull("Não deve achar o produto", produtoExcluido);
	}
	
	@Test
	public void deveAlterarProduto(){
		deveSalvarProduto();
		
		/*TypedQuery<Produto> query = em.createNamedQuery("SELECT p FROM Produto p ",
				Produto.class
				).setMaxResults(1);*/
		
		//TypedQuery<Produto> query = em.createQuery("QueryProduto", Produto.class).setMaxResults(1);
		TypedQuery<Produto> query = em.createQuery(" SELECT p FROM Produto p", Produto.class).setMaxResults(1);
		
		Produto produto = query.getSingleResult();
		
		assertNotNull("Dever ter encontrado produto",produto);
		
		Integer versao = produto.getVersion();
		
		em.getTransaction().begin();
		
		produto.setFabricante("HP");
		produto = em.merge(produto);
		em.getTransaction().commit();
		
		assertNotEquals("Versão deve ser diferente",versao, produto.getVersion());
	}

	@Test	
	public void devePesquisarProdutos(){
		for(int i = 0; i<10; i++){
			deveSalvarProduto();
		}
		
		TypedQuery<Produto> query = em.createQuery(" SELECT p FROM Produto p", Produto.class);
		List<Produto> produtos = query.getResultList();
		
		assertFalse("Deve ter produtos na lista",produtos.isEmpty());
		assertTrue("Deve ter produtos na lista",produtos.size() >= 10);
	}
	
	@Test
	public void deveSalvarProduto(){
		Produto produto = new Produto();
		produto.setNome("Notebook");
		produto.setFabricante("Dell");
		
		// no trabalho naõ precisa do begin nem do commit
		
		assertTrue("Não deve ter id definido",produto.isTransient());
		
		em.getTransaction().begin();
		
		em.persist(produto);
		
		em.getTransaction().commit();
		
		assertFalse("Deve ter definido",produto.isTransient());
		assertNotNull("Deve ter id definido",produto.getId());
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
	
	@AfterClass
	public static void deveLimparBase(){
		EntityManager entityManager = JPAUtil.INSTANCE.getEntityManager();
	
		entityManager.getTransaction().begin();
		
		Query query = entityManager.createQuery("DELETE FROM Produto p");
		
		int registrosExcluidos = query.executeUpdate();
		
		entityManager.getTransaction().commit();
		
		assertTrue("Deve ter excluido registros", registrosExcluidos > 0);
		
	}
	
}
