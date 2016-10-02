package br.com.springTeste.security;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import br.com.springTeste.query.QueryUsuario;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableWebSecurity
@PropertySource("/META-INF/springdb.properties")
@ImportResource("/META-INF/Spring-DataSource.xml")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	DataSource dataSource;

	@Inject
	Environment env;
        
	private static String REALM = "MY_TEST_REALM";
	private static final Logger logger = LogManager.getLogger(SecurityConfiguration.class);

	@Autowired
	public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
		// auth.inMemoryAuthentication().withUser("bill").password("abc123").roles("ADMIN");
		// auth.inMemoryAuthentication().withUser("tom").password("abc123").roles("USER");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		logger.debug("*****Start Service*****");
		http.csrf().disable().authorizeRequests()
				.antMatchers("/user/**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/login**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/perfil**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/usuario**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/empresa**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/client**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/plano**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/message**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/teste**").access("hasRole('ROLE_ADMIN')").and().httpBasic().realmName(REALM)
				.authenticationEntryPoint(getBasicAuthEntryPoint());

	}

	@Bean
	public CustomBasicAuthenticationEntryPoint getBasicAuthEntryPoint() {
		return new CustomBasicAuthenticationEntryPoint();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
	}

	/*
	 * @Bean public DriverManagerDataSource dataSource() {
	 * 
	 * logger.debug("Banco de dados ; "+ env.getProperty("url")); logger.debug(
	 * "Usuario : "+ env.getProperty("user")); logger.debug("Senha : "+
	 * env.getProperty("password"));
	 * 
	 * DriverManagerDataSource driverManagerDataSource = new
	 * DriverManagerDataSource();
	 * driverManagerDataSource.setDriverClassName("com.mysql.jdbc.Driver");
	 * driverManagerDataSource.setUrl(env.getProperty("url"));
	 * driverManagerDataSource.setUsername(env.getProperty("user"));
	 * driverManagerDataSource.setPassword(env.getProperty("password")); return
	 * driverManagerDataSource; }
	 */

	@Autowired
	public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource).usersByUsernameQuery(QueryUsuario.queryUserAuthentication()).authoritiesByUsernameQuery(QueryUsuario.queryUserAndProfileAuthentication());

	}
        
        /**
         * Retorna o Usuario autenticado no sistema
         * @return 
         */
        public static UserAuthentication getPrincipal(){
            
            UserAuthentication userAuthentication = new UserAuthentication();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if ( authentication!= null ){
                userAuthentication.setNome(authentication.getName());
                userAuthentication.setPerfil(authentication.getAuthorities().toString());
                userAuthentication.setAutenticado(authentication.isAuthenticated());
                
                logger.info(" Usuario autenticado : {} " , userAuthentication.getNome()  ); 
                logger.info(" Authorities : {} " , userAuthentication.getPerfil()  ); 
                logger.info(" isAuthenticated : {} " , userAuthentication.isAutenticado()  ); 
            }
            
            return userAuthentication;
            
        }

}
