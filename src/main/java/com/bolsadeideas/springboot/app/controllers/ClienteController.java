package com.bolsadeideas.springboot.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jni.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.app.models.dao.IClienteDao;
import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.paginator.PageRender;
import com.bolsadeideas.springboot.app.sercice.IClienteService;
import com.bolsadeideas.springboot.app.view.xml.ClienteList;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	@Autowired // Con esta anotación va a buscar un componente registrado en el contenedor que
				// implemente la interfaz cliente dao, busca un BEANS.
	private IClienteService clienteService; // Siempre ha de ser el tipo más generico, en este caso la interfaz

	@Secured("ROLE_USER")
	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		//Cliente cliente = clienteService.findOne(id); // Obtenemos el cliente
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", "El cliente no existe en la BBDD");
			return "redirect:/listar";
		}

		model.addAttribute("cliente", cliente);
		model.addAttribute("titulo", "Detalle cliente: " + cliente.getNombre());

		return "ver";
	}

	@GetMapping("/listar-rest")
	public @ResponseBody ClienteList listarRest() {		//Va a responder en formato Rest	
		return new ClienteList(clienteService.findAll());
	}
	
	
	@GetMapping({"/listar","/"}) // Pasamos el listado de clientes a la vista
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model, HttpServletRequest request) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if(auth != null) {
			logger.info("Utilizando forma estática SecurityContextHolder.getContext().getAuthentication(): Usuario autenticado: ".concat(auth.getName()));
		}	
		
		
		if(request.isUserInRole("ROLE_ADMIN")) {
			logger.info("Forma usando HttpServletRequest: Hola ".concat(auth.getName()).concat(" tienes acceso!"));
		} else {
			logger.info("Forma usando HttpServletRequest: Hola ".concat(auth.getName()).concat(" NO tienes acceso!"));
		}
		
		
		Pageable pageRequest = PageRequest.of(page, 6); // Mostramos 4 registros por pagina, se pueden colocar los que
														// quieras

		Page<Cliente> clientesPaginacion = clienteService.findAll(pageRequest); // Invocamos el listar todo, pero
																				// paginable del service

		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientesPaginacion);
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientesPaginacion); // Pasamos el cliente paginado
		model.addAttribute("page", pageRender);
		return "listar";
	}

	// Crear
	@Secured("ROLE_ADMIN")
	@GetMapping("/form")
	public String crear(Model model) {
		Cliente cliente = new Cliente();
		model.addAttribute("cliente", cliente);
		model.addAttribute("titulo", "Formulario de cliente");
		return "form";
	}
	
	@Secured("ROLE_ADMIN")
	@PostMapping("/form") // Guardamos en la bd
	public String guardar(Cliente cliente, SessionStatus status, RedirectAttributes flash,
			@RequestParam("file") MultipartFile foto) { // Recibe el objeto cliente que viene con los datos poblados y
														// lo guarda
		if (!foto.isEmpty()) {
			
			Path rootPath =Paths.get("uploads").resolve(foto.getOriginalFilename()); //Ruta absoluta
			
			Path rootAbsolutPath = rootPath.toAbsolutePath();
			
			try {
				Files.copy(foto.getInputStream(), rootAbsolutPath);
				cliente.setFoto(foto.getOriginalFilename()); // Finalmente pasamos la foto al cliente,el cual lo guarda
																// despues en la bd en este mismo metodo
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		clienteService.save(cliente);
		status.setComplete(); // Eliminamos el objeto guardado en la sesion al editar o crear
		flash.addAttribute("success", "Cliente Creado con exito!");
		return ("redirect:listar");
	}
	// Fin Crear

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/form/{id}") // Editar, @pathvariable es para pasarle el id mediante la url
	public String editar(@PathVariable(value = "id") Long id, Model model) {
		Cliente cliente = null;

		if (id > 0) {
			cliente = clienteService.findOne(id);
		} else {
			return "redirect://listar"; // Redirijimos por seguridad si es igual a 0
		}

		model.addAttribute("cliente", cliente);
		model.addAttribute("titulo", "Editar Cliente");
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/eliminar/{id}") // Eliminar
	public String eliminar(@PathVariable(value = "id") Long id) {

		if (id > 0) {
			clienteService.delete(id);			
		}

		return "redirect:/listar"; // Lo elimina y retorna a la vista
	}
	
	private boolean hasRole(String role) {	//RETORNA TRUE SI EL ROL QUE PONEMOS POR STRING ES IGUAL AL QUE TIENE
		SecurityContext context = SecurityContextHolder.getContext();
		
		if(context == null) {
			return false;
		}
		
		Authentication auth = context.getAuthentication();
		
		if(auth == null) {
			return false;
		}
		
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		
		for(GrantedAuthority authority: authorities) {
			if(role.equals(authority.getAuthority())) {
				return true;
			}
		}
		
		return false;
	}
}
