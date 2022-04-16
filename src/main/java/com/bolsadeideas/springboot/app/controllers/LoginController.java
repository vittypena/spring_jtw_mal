package com.bolsadeideas.springboot.app.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
	
	@GetMapping("/login")
	public String login(@RequestParam(value="error", required=false) String error, Model model, Principal principal, RedirectAttributes flash) { 		//Principal es de java Security, si principal es distinto de null ya habia iniciado sesion, entonces redirigimos.
		
		if(principal != null) {//Para evitar doble login
			flash.addFlashAttribute("info", "Ya ha iniciado sesion");
			return "redirect:/";
		}		
		
		if(error != null) {
			model.addAttribute("error", "Error en el login: Nombre de usuario o contraseña incorrecta, por favor vuelva a intentarlo");
		}
		return "login";
	}
	
}
