package br.edu.faculdadedelta.modelo;

import static org.junit.Assert.*;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import br.edu.faculdadedelta.util.JPAUtil;

public class VendaTest {
	private static final String CPF_PADRAO = "000.001.002-00";
	private EntityManager em;
	
	@Test
	public void deveConsultarQuantidadeDeProdutosVendidos(){
		Venda venda = criarVenda("010.020.030-04");	
		
		for (int i = 0; i < 10; i++) {
			venda.getProdutos().add(criarProduto("Produto"+i, "Marca"+i));
		}
		
		em.getTransaction().begin();
		em.persist(venda);
		em.getTransaction().commit();
		
		assertFalse("Deve ter persistido a venda",venda.isTransient());
		
		int qtdProdutosAdicionados = venda.getProdutos().size();
		
		assertTrue("Lista de produtos deve ter itens", qtdProdutosAdicionados > 0);
		
		StringBuilder jpql = new StringBuilder();
		jpql.append(" SELECT COUNT(p.id) ");
		jpql.append(" FROM Venda v ");
		jpql.append(" INNER JOIN v.produtos p ");
		jpql.append(" INNER JOIN v.cliente c ");
		jpql.append(" WHERE c.cpf = :cpf ");
		
		Query query = em.createQuery(jpql.toString());
		query.setParameter("cpf", "010.020.030-04");
		
		Long qtdProdutosDaVenda = (Long) query.getSingleResult();
		
		assertEquals("quantidade de produtos deve ser igual a quantidade da lista", qtdProdutosDaVenda.intValue(), qtdProdutosAdicionados);
		
	}
	
	
	@Test(expected = IllegalStateException.class)
	public void naoDeveFazerMergeEmObjetosTransient(){
		Venda venda = criarVenda();	
		
		venda.getProdutos().add(criarProduto("Notebook", "Dell"));
		venda.getProdutos().add(criarProduto("Mouse", "Razer"));
		
		assertTrue("Não deve ter id definido",venda.isTransient());
		
		em.getTransaction().begin();
		venda = em.merge(venda);
		em.getTransaction().commit();
		
		fail("Não deveria ter salvo (merge) uma venda nova com relacionamentos transient");
	}
	
	@Test
	public void deveSalvarVendaComRelacionamentosEmCascataForeach(){
		Venda venda = criarVenda();	
		
		venda.getProdutos().add(criarProduto("Notebook", "Dell"));
		venda.getProdutos().add(criarProduto("Mouse", "Razer"));
		
		assertTrue("Não deve ter id definido",venda.isTransient());
		
		em.getTransaction().begin();
		em.persist(venda);
		em.getTransaction().commit();
		
		assertFalse("Deve ter id definido",venda.isTransient());
		assertFalse("Deve ter id definido",venda.getCliente().isTransient());
		
		venda.getProdutos().forEach(produto->{
			assertFalse("Deve ter id definido",produto.isTransient());
		});
	}
	
	@Test
	public void deveSalvarVendaComRelacionamentosEmCascata(){
		Venda venda = criarVenda();	
		
		venda.getProdutos().add(criarProduto("Notebook", "Dell"));
		venda.getProdutos().add(criarProduto("Mouse", "Razer"));
		
		assertTrue("Não deve ter id definido",venda.isTransient());
		
		em.getTransaction().begin();
		em.persist(venda);
		em.getTransaction().commit();
		
		assertFalse("Deve ter id definido",venda.isTransient());
		assertFalse("Deve ter id definido",venda.getCliente().isTransient());
		
		for(Produto produto : venda.getProdutos()){
			assertFalse("Deve ter id definido",produto.isTransient());
		}
	}
	
	private Produto criarProduto(String nome, String marca){
		Produto produto = new Produto();
		produto.setNome(nome);
		produto.setFabricante(marca);
		
		return produto;
	}
	
	private Venda criarVenda(){
		return criarVenda(null);
	}
	
	private Venda criarVenda(String cpf){
		Cliente cliente = new Cliente();
		cliente.setNome("Werlon Guilherme");
		cliente.setCpf(cpf == null ? CPF_PADRAO : cpf);
		
		assertTrue("Não deve ter id definido",cliente.isTransient());
		
		Venda venda = new Venda();
		venda.setDataHora(new Date());
		venda.setCliente(cliente);
		return venda;
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
		
		Query query = entityManager.createQuery("DELETE FROM Venda v");
		
		int registrosExcluidos = query.executeUpdate();
		
		entityManager.getTransaction().commit();
		
		assertTrue("Deve ter excluido registros", registrosExcluidos > 0);
		
	}
}
