package com.bolsadeideas.springboot.app.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LocaleController {
	
	@GetMapping("/locale")
	public String locale(HttpServletRequest request) {
		String ultimaUrl = request.getHeader("referer");	//Nos entrega el link de nuestra ultima pagina, con referer
		
		return "redirect:".concat(ultimaUrl);
	}
	
}
