package com.bolsadeideas.springboot.app.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.bolsadeideas.springboot.app.models.entity.Cliente;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long>{
	//Como primer parametro el tipo de dato de nuestra clase Entity y como segundo parametro el tipo de la key (id)
	
	@Query("select c from Cliente c left join fetch c.facturas f where c.id = ?1")
	public Cliente fetchByIdWithFacturas(Long id);
}
