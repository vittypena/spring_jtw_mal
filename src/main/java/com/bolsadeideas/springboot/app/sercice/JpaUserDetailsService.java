package com.bolsadeideas.springboot.app.sercice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bolsadeideas.springboot.app.models.dao.IUsuarioDao;
import com.bolsadeideas.springboot.app.models.entity.Rol;
import com.bolsadeideas.springboot.app.models.entity.Usuario;


@Service("jpaUserDetailService")
public class JpaUserDetailsService implements UserDetailsService{

	//Inyectamos la clase dao
	@Autowired
	private IUsuarioDao usuarioDao;	
	
	private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);
	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {	//Este metodo carga el usuario a traves del username
		//Obtenemos el usuario
		Usuario usuario = usuarioDao.findByUsername(username);				
		
		if(usuario == null) {
			logger.error("Error login: no existe el usuario: " + username);
			throw new UsernameNotFoundException("Username " + username + " no existe en el sistema!");
		}
		
		//Obtenemos sus roles
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();		//GrantedAuthority es del tipo abstracto dnd estan los roles?¿
		
		for(Rol role: usuario.getRoles()) {
			logger.info("Role: ".concat(role.getAuthority()));
			authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
		}
		
		if(authorities.isEmpty()) {
			logger.error("Error login: " +  username +  " no tiene roles asignados!");
			throw new UsernameNotFoundException("Error login: " +  username +  " no tiene roles asignados!");
		}
		
		return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, true, true, authorities);
	}
	
}
