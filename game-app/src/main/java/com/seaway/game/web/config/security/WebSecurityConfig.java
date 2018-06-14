package com.seaway.game.web.config.security;

import com.seaway.game.common.utils.Constants;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private final DataSource dataSource;

	private static final String SELECT_USERS = "select username, password, enabled from users where username=? and enabled=1";
	private static final String SELECT_AUTHORITIES = "select username, rolename as authority from authorities where username=?";

	public WebSecurityConfig(@Qualifier("dataSource") DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		BCryptPasswordEncoder pwEncoder = new BCryptPasswordEncoder(10);

		auth.jdbcAuthentication().dataSource(dataSource)
				.usersByUsernameQuery(SELECT_USERS)
				.authoritiesByUsernameQuery(SELECT_AUTHORITIES)
				.passwordEncoder(pwEncoder);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**", "/fonts/**", "/images/**",
				"/js/**", "/libs/**", "/templates/**", "/api/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/")
				.access("hasAnyRole('" + Constants.ROLE_RO + "')").anyRequest()
				.authenticated().and().formLogin()
				.successHandler(new SimpleUrlAuthenticationSuccessHandler())
				.loginPage("/signin").permitAll().and().logout()
				.invalidateHttpSession(true).logoutUrl("/signout")
				.logoutSuccessUrl("/signin?signout").permitAll().and()
				.exceptionHandling().accessDeniedPage("/access-denied").and()
				.sessionManagement().invalidSessionUrl("/invalid-session")
				.maximumSessions(1).maxSessionsPreventsLogin(false)
				.expiredUrl("/signin");
	}

}
