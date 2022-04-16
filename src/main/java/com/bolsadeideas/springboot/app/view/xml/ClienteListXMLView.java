package com.bolsadeideas.springboot.app.view.xml;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.xml.MarshallingView;

import com.bolsadeideas.springboot.app.models.entity.Cliente;

@Component("listar.xml")
public class ClienteListXMLView  extends MarshallingView{

	
	@Autowired
	public ClienteListXMLView(Jaxb2Marshaller marshaller) {
		super(marshaller);
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		//Tenemos que eliminar todos los model que no sean la lista clientes y que hayamos pasado desde el controlador que esta gestionando esta ruta, listar en este caso
		model.remove("titulo");
		model.remove("page");
		
		
		//El cliente antes de borrarlo tenemos que obtener los clientes para hacer la conversion, en este caso era un Page de clientes
		Page<Cliente> clientes = (Page<Cliente>) model.get("clientes");
		model.remove("clientes");	//Una vez ya guardado podemos borrar el model
		
		model.put("clienteList", new ClienteList(clientes.getContent())); //Creamos la instancia con el Wrapper. El getContent es porque estaba paginado.

		super.renderMergedOutputModel(model, request, response);
	}
	
}
