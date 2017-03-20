package br.edu.faculdadedelta.modelo;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.edu.faculdadedelta.util.JPAUtil;

public class RelatorioTest {
	private static final String CPF_PADRAO = "000.001.002-00";
	private EntityManager em;
	
	
	private Session getSession(){
		return (Session) em.getDelegate();
	}
	
	private Criteria createCriteria(Class<?> clazz){
		return getSession().createCriteria(clazz);
	}
	
	private Criteria createCriteria(Class<?> clazz, String alias){
		return getSession().createCriteria(clazz, alias);
	}
	
	@Test
	public void deveConsultarVendaPorNomeClienteUsandoSubquery(){
		criarVendas(1);
		
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Cliente.class,"c")
				.add(Restrictions.in("c.id", 1L,2L,3L,4L,5L,6L,7L,8L,9L,10L))
				.setProjection(Projections.property("c.nome"));
		
		Criteria criteria = createCriteria(Venda.class,"v")
				.createAlias("v.cliente", "cli")
				.add(Subqueries.propertyIn("cli.nome", detachedCriteria));
		
		@SuppressWarnings("unchecked")
		List<Venda> vendas = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		assertTrue("Verifica se teve pelomenos 1 vendas", vendas.size() >= 1);
		
		vendas.forEach(venda -> assertFalse(venda.getCliente().isTransient()));
		
	}
	
	@Test
	public void deveConsultarIdENomeConverterCliente(){
		criarClientes(3);
		
		ProjectionList projectionList = Projections.projectionList()
				.add(Projections.property("c.id").as("id"))
				.add(Projections.property("c.nome").as("nome"));

		Criteria criteria = createCriteria(Cliente.class, "c");
		criteria.setProjection(projectionList);
		
		@SuppressWarnings("unchecked")
		List<Cliente> clientes = criteria
				.setResultTransformer(Transformers.aliasToBean(Cliente.class)) //evida dados repetidos
				.list();
		
		assertTrue("Deve ter de 3 a mais clientes ",clientes.size() >= 3);
		
		clientes.forEach(cliente -> {
			assertTrue(cliente.getId() != null);
			assertTrue(cliente.getNome() != null);
			assertTrue(cliente.getCpf() == null);
		});
		
	}
	
	@Test
	public void deveConsultarClientesChaveValor(){
		criarClientes(5);
		
		ProjectionList projectionList = Projections.projectionList()
				.add(Projections.property("c.id").as("id"))
				.add(Projections.property("c.nome").as("nome"));

		Criteria criteria = createCriteria(Cliente.class, "c");
		criteria.setProjection(projectionList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> clientes = criteria
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 produtos", clientes.size() >= 5);
		
		clientes.forEach(clienteMap -> {
			clienteMap.forEach((chave, valor) ->{
				assertTrue("primeiro deve ser string",chave instanceof String);
				assertTrue("segundo pode ser string ou long",valor instanceof String || valor instanceof Long );
			});
			
		});
		
	}
	
	@Test
	public void deveConsultarIdENomeProduto(){
		criarProdutos(1);
		
		ProjectionList projectionList = Projections.projectionList()
				.add(Projections.property("p.id").as("id"))
				.add(Projections.property("p.nome").as("nome"));
		
		Criteria criteria = createCriteria(Produto.class, "p");
		criteria.setProjection(projectionList);
		
		@SuppressWarnings("unchecked")
		List<Object[]> produtos = criteria
				.setResultTransformer(Criteria.PROJECTION)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 produtos", produtos.size() >= 1);
		
		produtos.forEach(produto -> {
			assertTrue("primeiro deve ser id",produto[0] instanceof Long);
			assertTrue("segundo deve ser id",produto[1] instanceof String);
		});
		
	}
	
	@Test
	public void deveConsultarVendaENomeClienteCasoExista(){
		criarVendas(1);
		
		Criteria criteria = createCriteria(Venda.class,"v")
				.createAlias("v.cliente", "c", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.ilike("c.nome","Werlon", MatchMode.START));
		
		@SuppressWarnings("unchecked")
		List<Venda> vendas = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", vendas.size() >= 1);
		
		vendas.forEach(venda -> assertFalse(venda.isTransient()));
	}
	
	@Test
	public void deveConsultarNotebooksSamsungOuDell(){
		criarProdutos(3);
		
		Criteria criteria = createCriteria(Produto.class, "p");
		criteria.add(Restrictions.or(
				Restrictions.eq("p.fabricante", "Dell"),
				Restrictions.eq("p.fabricante", "Samsung")
				));
		
		@SuppressWarnings("unchecked")
		List<Produto> notebooks = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", notebooks.size() >= 3);
		
		notebooks.forEach(notebook -> assertFalse(notebook.isTransient()));
	}
	
	@Test
	public void deveConsultarProdutosContendoParteDoNome(){
		criarProdutos(3);
		
		Criteria criteria = createCriteria(Produto.class, "p")
				.add(Restrictions.ilike("p.nome", "book",MatchMode.ANYWHERE));
		
		@SuppressWarnings("unchecked")
		List<Produto> notebooks = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", notebooks.size() >= 3);
		
		notebooks.forEach(notebook -> assertFalse(notebook.isTransient()));
	}
	
	@Test
	public void deveConsultarQuantidadeVendasPorCliente(){
		criarVendas(3);
		
		Criteria criteria = createCriteria(Venda.class, "v")
				.createAlias("v.cliente", "c")
				.add(Restrictions.eq("c.cpf", CPF_PADRAO))
				.setProjection(Projections.rowCount());
		
		Long qtdRegistros = (Long) criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.uniqueResult();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", qtdRegistros >= 3);
		
	}
	
	@Test
	public void deveConsultarDezPrimeirosProdutos(){
		criarProdutos(20);
		
		Criteria criteria = createCriteria(Produto.class, "p")
				.setFirstResult(1)
				.setMaxResults(10);
		
		@SuppressWarnings("unchecked")
		List<Produto> notebooks = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", notebooks.size() == 10);
		
		notebooks.forEach(notebook -> assertFalse(notebook.isTransient()));
	}
	
	@Test
	public void deveConsultarNotebooks(){
		criarProdutos(3);
		
		Criteria criteria = createCriteria(Produto.class, "p")
				.add(Restrictions.in("p.nome", "Notebook", "Netbook", "Macbook"))
				.addOrder(Order.desc("p.fabricante"));
		
		@SuppressWarnings("unchecked")
		List<Produto> notebooks = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", notebooks.size() >= 3);
		
		notebooks.forEach(notebook -> assertFalse(notebook.isTransient()));
	}
	
	@Test
	public void deveConsultarVendaDaUltimaSemana(){
		criarVendas(3);
		Calendar ultimaSemana = Calendar.getInstance();
		ultimaSemana.add(Calendar.WEEK_OF_YEAR, -1);
		
		Criteria criteria = createCriteria(Venda.class, "v")
				.add(Restrictions.between("v.dataHora", ultimaSemana.getTime(), new Date()))
				.setProjection(Projections.rowCount());
		
		Long qtdRegistros = (Long) criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.uniqueResult();
		
		assertTrue("Verifica se teve pelomenos 3 vendas", qtdRegistros >= 3);
		
	}
	
	@Test
	public void deveConsultarMaiorIdCliente(){
		criarClientes(3);
		
		Criteria criteria = createCriteria(Cliente.class, "c")
				.setProjection(Projections.max("c.id"));
		
		Long maiorId = (Long) criteria
				.setResultTransformer(Criteria.PROJECTION)
				.uniqueResult();
		
		assertTrue("Verifica se o maximo registro ficou maior ou igual a 3",maiorId >= 3L);
	}

	@Test
	public void deveConsultarTodosclientes(){
		criarClientes(3);
		Criteria criteria = createCriteria(Cliente.class, "c");
		
		@SuppressWarnings("unchecked")
		List<Cliente> clientes = criteria
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY) //evida dados repetidos
				.list();
		
		assertTrue("Deve ter de 3 a mais clientes ",clientes.size() >= 3);
		
		clientes.forEach(cliente -> assertFalse(cliente.isTransient()));
	}
	

	private void criarVendas(int quantidade){
		em.getTransaction().begin();
		
		for(int i = 0 ; i < quantidade ; i++){
			Venda venda = criarVenda();
			venda.getProdutos().add(criarProduto("Notebook", "Sony"));
			venda.getProdutos().add(criarProduto("Mouse", "Razer"));
			
			em.persist(venda);
		}
		
		em.getTransaction().commit();
	}
	
	private void criarProdutos(int quantidade){
		em.getTransaction().begin();
		
		for(int i = 0 ; i < quantidade ; i++){
			Produto produto = criarProduto("Notebook", "Dell");
			
			em.persist(produto);
		}
		
		em.getTransaction().commit();
	}
	
	private void criarClientes(int quantidade){
		em.getTransaction().begin();
		
		for(int i = 0 ; i < quantidade ; i++){
			Cliente cliente = new Cliente();
			cliente.setNome("Werlon Guilherme");
			cliente.setCpf(CPF_PADRAO);
			
			em.persist(cliente);
		}
		
		em.getTransaction().commit();
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

}
