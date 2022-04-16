package com.bolsadeideas.springboot.app.auth.filter;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bolsadeideas.springboot.app.auth.service.JWTService;
import com.bolsadeideas.springboot.app.auth.service.JWTServiceImpl;
import com.bolsadeideas.springboot.app.models.entity.Usuario;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

	private AuthenticationManager authenticationManager;
	private JWTService jwtService;
	
	
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTService jwtService) {
		this.authenticationManager = authenticationManager;	
		this.jwtService = jwtService;
		//setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));	//Por si queremos personalizar rutas del login en vez de /login
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		
		String username = obtainUsername(request);	//Nombre que tengamos en el entity Usuario
		String password = obtainPassword(request);	// "" ""	
		
		//Debug
		if (username != null && password != null) {
			logger.info("Username desde request parameter (form-data):" + username);
			logger.info("Password desde request parameter (form-data):" + password);
		}else {		//Si enviamos en raw (JSon)
			//Convertimos los datos que estamos recibiendo en el JSON raw a Objeto
			Usuario user = null;
			try {
				user = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);
				
				username = user.getUsername();
				password = user.getPassword();
				
				logger.info("Username desde request InputStream (raw):" + username);
				logger.info("Password desde request InputStream (raw):" + password);
			} catch (StreamReadException e) {
				e.printStackTrace();
			} catch (DatabindException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Creamos el usernamePasswordAuthenticationTOken:
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);			
				
		return authenticationManager.authenticate(authToken);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		String token = jwtService.create(authResult);
		
		//Guardamos el token en la cabecera
		response.addHeader(JWTServiceImpl.HEADER_STRING, JWTServiceImpl.TOKEN_PREFIX.concat(token));
		
		//Pasar el token a una structura JSON tb
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("token", token);	//Pasamos el token
		body.put("user", (User)authResult.getPrincipal());	//Pasamos el objeto user entero
		body.put("mensaje", String.format("Hola %s, has iniciado sesion con exito", authResult.getName()));	//Pasamos un mensaje de exito
		
		//Guardamos el body en formato JSON en la respuesta  
		response.getWriter().write(new ObjectMapper().writeValueAsString(body));//Convertimos el objeto de java en un JSON
		response.setStatus(200); //Todo esta OK de estado
		response.setContentType("application/json");		
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("mensaje", "Error de autenticacion: username o password incorrecto!");
		body.put("error", failed.getMessage());
		
		response.getWriter().write(new ObjectMapper().writeValueAsString(body));
		response.setStatus(401); 
		response.setContentType("application/json");	
	}
		
}
