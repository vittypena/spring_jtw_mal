package com.bolsadeideas.springboot.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.bolsadeideas.springboot.app.models.entity.Producto;

public interface IProductoDao extends CrudRepository<Producto, Long>{
	//Implementamos el metodo personalizado para poder buscar por una query con like% e ir autocompletando con ese like%
	
	//En la query con ?1 hacemos referencia al parametro 1 que hemos metido, es decir, %?1% es = %parametro%
	
	@Query("select p from Producto p where lower(p.nombre) like %?1%")
	public List<Producto> findByNombre(String term); //Recibira el termino que vamos metiendo e implementamos la consulta con like con la anotacion @Query
	
}
