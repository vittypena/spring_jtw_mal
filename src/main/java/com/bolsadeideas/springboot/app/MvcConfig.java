package com.bolsadeideas.springboot.app;

import java.nio.file.Paths;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class MvcConfig implements WebMvcConfigurer{

	@Bean		//Esto es para encryptar la password
	public static BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(new Class[] {com.bolsadeideas.springboot.app.view.xml.ClienteList.class});	//En el segundo parametro ponemos la clase wrapper
		return marshaller;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {	//Metodo para agregar directorios-recursos externos a nuestro proyecto
		WebMvcConfigurer.super.addResourceHandlers(registry);
		
		String resourcePath = Paths.get("uploads").toAbsolutePath().toUri().toString();
		
		registry.addResourceHandler("/uploads/**").addResourceLocations(resourcePath);
				
	}
	
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/error_403").setViewName("error_403");
	}
	
	@Bean
	public LocaleResolver localeResolver(){//Importamos LocaleResolver de spring.webserlet
			SessionLocaleResolver localeResolver = new SessionLocaleResolver();		//Se guarda en la session http
			//Establecemos el lenguaje por defecto
			localeResolver.setDefaultLocale(new Locale("es", "ES"));	//Primero el codigo del lenguaje y segundo el codigo del pais
			return localeResolver;
	}
	
	//Creamos el interceptor para cambiar el locale, cada vez que pasemos el parametro lang por URL
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
		localeInterceptor.setParamName("lang");	//Cada vez que se pase este parametro por URL se va a ejecutar este interceptor
		return localeInterceptor;
	}

	//Tenemos que agregar el interceptor, registrarlo vaya
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
	
	
}
