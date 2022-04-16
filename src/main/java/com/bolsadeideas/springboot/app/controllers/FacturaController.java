package com.bolsadeideas.springboot.app.controllers;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.entity.Factura;
import com.bolsadeideas.springboot.app.models.entity.ItemFactura;
import com.bolsadeideas.springboot.app.models.entity.Producto;
import com.bolsadeideas.springboot.app.sercice.IClienteService;

@Controller
@Secured("ROLE_ADMIN")
@RequestMapping("/factura")
@SessionAttributes("factura")	//Mantenemos el objeto factura hasta que se guarde en el form
public class FacturaController {
	
	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private MessageSource messageSource;

	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash, Locale locale) {

		Factura factura = clienteService.fetchByIdWithClienteWithItemFacturaWithProducto(id); // clienteService.findFacturaById(id);

		if (factura == null) {
			flash.addFlashAttribute("error", messageSource.getMessage("text.factura.flash.db.error", null, locale));
			return "redirect:/listar";
		}

		model.addAttribute("factura", factura);
		model.addAttribute("titulo", String.format(messageSource.getMessage("text.factura.ver.titulo", null, locale), factura.getDescripcion()));
		return "factura/ver";
	}
	
	@GetMapping("/form/{clienteId}")		//IMPORTANTE AQUI VEMOS EJEMPLO CON EL GETMAPPING TB {id}
	public String crear(@PathVariable(value="clienteId") Long clienteId, Model model, RedirectAttributes flash) {
		
		Cliente cliente = clienteService.findOne(clienteId);	//Necesitamos el cliente para dar de alta la factura
		if(cliente == null) {
			flash.addAttribute("error", "El cliente no existe en la bse de datos");
			return "redirect:/listar";
		}
		
		Factura factura = new Factura();
		factura.setCliente(cliente);
		
		model.addAttribute("factura", factura);
		model.addAttribute("titulo", "Crear Factura");
		return "factura/form";
	}
	
	@GetMapping(value="/cargar-productos/{term}", produces={"application/json"})	//Debemos poner la url que pusimos en el js del form del autocompletado, con produces genera la salida Json y en la url le pasamos el term tb
	public @ResponseBody List<Producto> cargarProductos(@PathVariable String term){	//@ResponseBody suprime el cargar una vista de thymeleaf y en vez de eso toma el resultado retornado convertido a json y lo puebla dentro  del body en la respuesta
		return clienteService.findByNombre(term);
	}
	
	@PostMapping("/form")
	public String guardar(Factura factura, 			//Pillamos por parametro request 2 arrays uno de items y otro de cantidad
			@RequestParam(name = "item_id[]", required=false) Long[] itemId, 
			@RequestParam(name = "cantidad[]", required=false) Integer[] cantidad,
			RedirectAttributes flash,
			SessionStatus status) {	//Pasamos el valor tal cual como lo tenemos en plantilla-items.html, en los name del input id y cantidad
		
		for(int i = 0; i < itemId.length; i++) {
			Producto producto = clienteService.findProductoById(itemId[i]);
			
			ItemFactura linea = new ItemFactura();
			linea.setCantidad(cantidad[i]);
			linea.setProducto(producto)	;
			factura.addItemFactura(linea);
		}
		clienteService.saveFactura(factura);
		
		status.setComplete();
		
		flash.addFlashAttribute("sucess", "Factura creada con exito");
		return "redirect:/ver/" + factura.getCliente().getId();
	}
	
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value="id") Long id) {
		
		Factura factura = clienteService.findFacturaById(id);
		
		if(factura!=null) {
			clienteService.deleteFactura(id);
			return "redirect:/ver/"+factura.getCliente().getId();
		}
		return "redirect:/listar";
	}
}
