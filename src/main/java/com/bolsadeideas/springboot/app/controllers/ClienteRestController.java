package com.bolsadeideas.springboot.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bolsadeideas.springboot.app.sercice.IClienteService;
import com.bolsadeideas.springboot.app.view.xml.ClienteList;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {
	
	@Autowired
	private IClienteService clienteService;
	
	@GetMapping("/listar")
	@Secured("ROLE_ADMIN")
	public ClienteList listarRestControl() {		//Va a responder en formato Rest	
		return new ClienteList(clienteService.findAll());
	}
	
}
