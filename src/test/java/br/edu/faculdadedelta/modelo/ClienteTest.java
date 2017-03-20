package br.edu.faculdadedelta.modelo;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.hibernate.LazyInitializationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import br.edu.faculdadedelta.util.JPAUtil;

public class ClienteTest {
	
	private static final String CPF_PADRAO = "000.001.002-00";
	private EntityManager em;
	
	@Test(expected = LazyInitializationException.class)
	public void naoDeveAcessarAtributoLazyForaEscopoEntityManager(){
		deveSalvarCliente();
		
		Cliente cliente = em.find(Cliente.class, 1L);
		
		assertNotNull("Verifica se encontrou um registro",cliente);
		
		em.detach(cliente);
		// estou tirando o cliente do escopo do EntityManager pode ser o .clear() para limpar tudo
		// os atributos mapeados como EAGER e atributos normais continuam no objeto. Só os lazy somem
		
		cliente.getCompras().size();
		
		fail("deve desparar LazyInitializationException ao Acessar Atributo Lazy Fora do Escopo EntityManager");
	}
	
	@Test
	public void deveAcessarAtributoLazy(){
		deveSalvarCliente();
		
		Cliente cliente = em.find(Cliente.class, 1L);
		
		assertNotNull("Verifica se encontrou um registro",cliente);
		assertNotNull("Lista lazy não deve ser nula",cliente.getCompras());
	}
	
	@Test(expected = NoResultException.class)
	public void naoDeveFuncionarSingleResultComNenhumRegistro(){
		deveSalvarCliente();
		deveSalvarCliente();
		
		Query query = em.createQuery("SELECT c.id FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", "000.000.000-00");
		
		query.getSingleResult();
		
		fail("metodo getSingleResult deve desparar exception NoResultException");
	}
	
	@Test(expected = NonUniqueResultException.class)
	public void naoDeveFuncionarSingleResultComMuitosRegistros(){
		deveSalvarCliente();
		deveSalvarCliente();
		
		Query query = em.createQuery("SELECT c.id FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", CPF_PADRAO);
		
		query.getSingleResult();
		
		fail("metodo getSingleResult deve desparar exception NonUniqueResultException");
	}
	
	@Test
	public void deveVerificarExistenciaCliente(){
		deveSalvarCliente();
		
		Query query = em.createQuery("SELECT COUNT(c.id) FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", CPF_PADRAO);
		
		Long qtdResultado = (Long) query.getSingleResult();
			
		assertTrue("Verifica se há registros na lista",qtdResultado > 0L);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void deveConsultarIdNomeForeach(){
		deveSalvarCliente();
		
		//varios atributos usando foreach no lugar do for
		Query query = em.createQuery("SELECT c.id, c.nome FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", CPF_PADRAO);
		
		List<Object[]> resultado = query.getResultList();
			
		assertFalse("Verifica se há registros na lista",resultado.isEmpty());
		
		resultado.forEach(linha -> {
			assertTrue("Verifica que o cpf deve estar nulo",linha[0] instanceof Long);
			assertTrue("Verifica que o cpf deve estar nulo",linha[1] instanceof String);
			
			Cliente cliente = new Cliente((Long)linha[0],(String)linha[1]);
			
			assertNotNull("Verifica que o cliente não deve estar nulo",cliente);
		});

	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void deveConsultarIdNome(){
		deveSalvarCliente();
		
		//varios atributos
		Query query = em.createQuery("SELECT c.id, c.nome FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", CPF_PADRAO);
		
		List<Object[]> resultado = query.getResultList();
			
		assertFalse("Verifica se há registros na lista",resultado.isEmpty());
		
		for(Object[] linha : resultado){
			assertTrue("Verifica que o cpf deve estar nulo",linha[0] instanceof Long);
			assertTrue("Verifica que o cpf deve estar nulo",linha[1] instanceof String);
			
			Cliente cliente = new Cliente((Long)linha[0],(String)linha[1]);
			
			assertNotNull("Verifica que o cliente não deve estar nulo",cliente);
		}
	}
	
	@Test
	public void deveConsultarApenasIdNome(){
		//outra forma de fazer o for
		deveSalvarCliente();
		
		//Constructor Express
		Query query = em.createQuery("SELECT new Cliente(c.id, c.nome) FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", CPF_PADRAO);
		
		
		@SuppressWarnings("unchecked")
		List<Cliente> clientes = query.getResultList();
			
		assertFalse("Verifica se há registros na lista",clientes.isEmpty());
		
		clientes.forEach(cliente -> {
			assertNull("Verifica que o cpf deve estar nulo",cliente.getCpf());
		});
	}
	@Test
	@SuppressWarnings("unchecked")
	public void deveConsultarClienteComIdNome(){
		deveSalvarCliente();
		
		//Constructor Express
		Query query = em.createQuery("SELECT new Cliente(c.id, c.nome) FROM Cliente c WHERE c.cpf = :cpf");
		query.setParameter("cpf", CPF_PADRAO);
		
		
		List<Cliente> clientes = query.getResultList();
			
		assertFalse("Verifica se há registros na lista",clientes.isEmpty());
		
		for(Cliente cliente : clientes){
			assertNull("Verifica que o cpf deve estar nulo",cliente.getCpf());
			
			cliente.setCpf(CPF_PADRAO);
		}
		
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void deveConsultarCpf(){
		deveSalvarCliente();
		
		String filtro = "Guilherme";
		
		Query query = em.createQuery("SELECT c.cpf FROM Cliente c WHERE c.nome LIKE :nome");
		query.setParameter("nome", "%"+filtro+"%");
		
		List<String> listaCpf = query.getResultList();
		
		assertFalse("Deve possuir itens",listaCpf.isEmpty());
	}
	
	@Test
	public void deveSalvarCliente(){
		Cliente cliente = new Cliente();
		cliente.setNome("Werlon Guilherme");
		cliente.setCpf(CPF_PADRAO);
		
		// no trabalho naõ precisa do begin nem do commit (porque usaremos hibernate)
		
		assertTrue("Não deve ter id definido",cliente.isTransient());
		
		em.getTransaction().begin();
		
		em.persist(cliente);
		
		em.getTransaction().commit();
		
		assertFalse("Deve ter definido",cliente.isTransient());
		assertNotNull("Deve ter id definido",cliente.getId());
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
		
		Query query = entityManager.createQuery("DELETE FROM Cliente c");
		
		int registrosExcluidos = query.executeUpdate();
		
		entityManager.getTransaction().commit();
		
		assertTrue("Deve ter excluido registros", registrosExcluidos > 0);
		
	}
}
