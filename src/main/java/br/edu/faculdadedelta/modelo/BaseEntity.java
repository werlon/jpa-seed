package br.edu.faculdadedelta.modelo;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class BaseEntity<T> implements Serializable {

	/**
	 * Default serialId criado pelo eclipse
	 */
	private static final long serialVersionUID = 1L;
	
	
	public abstract T getId();//especifico que o que for passado no T deve ser retornado
	
	@Version
	private Integer version;

	/**
	 * Obtém a versão da entidade
	 * 
	 * @return
	 */
	public Integer getVersion() {
		return version;
	}
	
	/**
	 * Metodo acessório para vericar se já tem identificador na entidade
	 * @return
	 */
	public boolean isTransient(){
		return getId() == null;
	}
	
	//mando o eclipse gerar o equals e hashcode do version, depois substituo
	//tudo que for version por getId() dentro do que foi gerado

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseEntity other = (BaseEntity) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	
}
