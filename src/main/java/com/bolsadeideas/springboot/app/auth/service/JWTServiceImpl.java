package com.bolsadeideas.springboot.app.auth.service;

import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.bolsadeideas.springboot.app.auth.SimpleGrantedAuthoritiesMixin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTServiceImpl implements JWTService {

	public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	
	public static final long EXPIRATION_DATE = 14000000L;
	
	public static final String TOKEN_PREFIX = "Bearer ";
	
	public static final String HEADER_STRING = "Authorization";
	
	@Override
	public String create(Authentication auth) throws JsonProcessingException {
		//En este metodo la autenticacion ha salido bien por lo tanto creamos el token que vamos a retornar al usuario.
		Collection<? extends GrantedAuthority> roles = auth.getAuthorities(); //Obtenemos los roles		
		Claims claims = Jwts.claims();
		claims.put("authorities", new ObjectMapper().writeValueAsString(roles));//Guardamos los roles en el JWT	
					
        String token = Jwts.builder()
        				.setClaims(claims)	//Contiene los roles
                        .setSubject(auth.getName())
                        .signWith(SECRET_KEY)
                        .setIssuedAt(new Date())	//Fecha de creacion
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_DATE)) //Expira en 4 horas con + 14000000
                        .compact();
		return token;
	}

	@Override
	public boolean validate(String token) {
		//Validar el token
		Claims claims = null;
		try {
			getClaims(token);
		  	return true;
		}catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public Claims getClaims(String token) {//Obtenemos el token que hemos generado previamente
		Claims claims = Jwts		
			    .parserBuilder().setSigningKey(SECRET_KEY)
			    .build()
			    .parseClaimsJws(resolve(token)).getBody();
		return claims;
	}

	@Override
	public String getUsername(String token) {		
		return getClaims(token).getSubject();
	}

	@Override
	public Collection<? extends GrantedAuthority> getRoles(String token) throws StreamReadException, DatabindException, IOException {
		Object roles = getClaims(token).get("authorities");
		
		//Los roles hay que transformarlos con la clase SimpleGrantedAuthoritiesMixin.java que hemos creado para poder hacerlo.
		Collection<? extends GrantedAuthority> authorities = Arrays.asList(new ObjectMapper()
				.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthoritiesMixin.class)
				.readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));
		
		return authorities;
	}

	@Override
	public String resolve(String token) {
		if(token != null && token.startsWith(TOKEN_PREFIX)) {
		return token.replace(TOKEN_PREFIX, "");
		}
		return null;
	}

}
