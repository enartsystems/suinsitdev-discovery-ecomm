package com.suinsit.web.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * This is an example of minimal configuration for ZK + Spring Security, we open as less access as possible to run a ZK-based application.
 * Please understand the configuration and modify it upon your requirement.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String ZUL_FILES = "/zkau/web/**/*.zul";
    private static final String[] ZK_RESOURCES = {
            "/zkau/web/**/js/**",
            "/zkau/web/**/zul/css/**",
            "/zkau/web/**/font/**",
            "/zkau/web/**/img/**",
            "/console/img/**",
            "/console/images/**",
            "/console/iconos/**",
            "/console/assets/**",
            "/console/css/**",
            "/console/global_assets/**"
    };
    // allow desktop cleanup after logout or when reloading login page
    private static final String REMOVE_DESKTOP_REGEX = "/zkau\\?dtid=.*&cmd_0=rmDesktop&.*";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.authorizeRequests().antMatchers(ZUL_FILES).denyAll() // block direct access to zul files
		.antMatchers(HttpMethod.GET, ZK_RESOURCES).permitAll() // allow zk resources
		.regexMatchers(HttpMethod.GET, REMOVE_DESKTOP_REGEX).permitAll() // allow desktop cleanup
		.requestMatchers(req -> "rmDesktop".equals(req.getParameter("cmd_0"))).permitAll() // allow desktop
																							// cleanup from ZATS
		.antMatchers("/","/healthz","/actuator/**", "/login.zhtml", "/login.zhtml?logout").permitAll()
		.antMatchers( "/desktop/**","/portal/**","/console/**").authenticated().and().formLogin()
		.loginPage("/login.zhtml").defaultSuccessUrl(environment.getProperty("suinsit.desktop.ctx")+"/"+environment.getProperty("suinsit.desktop.page")).and().logout()
		.logoutUrl("/login.zhtml?logout").logoutSuccessUrl("/");
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
    }

 
    
//    @Value("${jwt.key}")
//	private String secret;
	@Autowired
	private Environment environment;
//	@Autowired
//	private AuthenticationProvider authenticationProvider;
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.authenticationProvider(this.authenticationProvider);
//	}

}