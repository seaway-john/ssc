package com.seaway.game.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.http.MediaType.*;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.seaway.game.web.controller",
		"com.seaway.game.web.config.security" })
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	private final static String WEBAPP_PREFIX = "classpath:/webapp/";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/*.html").addResourceLocations(
				WEBAPP_PREFIX);
		registry.addResourceHandler("/css/**").addResourceLocations(
				WEBAPP_PREFIX + "css/");
		registry.addResourceHandler("/fonts/**").addResourceLocations(
				WEBAPP_PREFIX + "fonts/");
		registry.addResourceHandler("/images/**").addResourceLocations(
				WEBAPP_PREFIX + "images/");
		registry.addResourceHandler("/js/**").addResourceLocations(
				WEBAPP_PREFIX + "js/");
		registry.addResourceHandler("/libs/**").addResourceLocations(
				WEBAPP_PREFIX + "libs/");
		registry.addResourceHandler("/templates/**").addResourceLocations(
				WEBAPP_PREFIX + "templates/");
	}

	@Override
	public void configureContentNegotiation(
			ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
		configurer.mediaType("json", APPLICATION_JSON_UTF8);
		configurer.defaultContentType(APPLICATION_JSON_UTF8);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("index");
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		InternalResourceViewResolver viewResolve = new InternalResourceViewResolver();
		viewResolve.setPrefix("/");
		viewResolve.setSuffix(".html");

		registry.viewResolver(viewResolve);
	}

}
