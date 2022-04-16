package com.bolsadeideas.springboot.app.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.bolsadeideas.springboot.app.models.entity.Factura;

public interface IFacturaDao extends CrudRepository<Factura, Long>{

	//Factura se une a cliente y a items directamente y luego a producto desde items factura, l es alias del itemfactura por lo tanto lo obtenemos desde ahi
	//cliente y producto son atributos de factura por eso se obtienen desde f.
	@Query("select f from Factura f join fetch f.cliente c join fetch f.items l join fetch l.producto where f.id =?1")
	public Factura fetchByIdWithClienteWithItemFacturaWithProducto(Long id); 
}
