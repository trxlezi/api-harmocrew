package br.edu.ifsp.harmocrew_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import br.edu.ifsp.harmocrew_api.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	/*
	 * Configuracao central de seguranca da API.
	 *
	 * A API e stateless: cada requisicao protegida precisa levar o JWT no header.
	 * Nao usamos sessao HTTP no servidor, por isso funciona bem com Docker,
	 * NGINX e multiplas instancias da API.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilter
	) throws Exception {
		http
			/*
			 * CSRF e desativado porque a API nao usa cookie de sessao para auth;
			 * usa Bearer Token. Isso e comum em APIs REST consumidas por mobile.
			 */
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					/*
					 * Rotas publicas: health e Swagger para verificacao/documentacao,
					 * register/login para permitir criar sessao. Todo o resto exige JWT.
					 */
					"/health",
					"/media/**",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/api/auth/register",
					"/api/auth/login",
					"/auth/register",
					"/auth/login")
				.permitAll()
				.anyRequest().authenticated());

		jwtAuthenticationFilter.ifAvailable(filter ->
			// O filtro JWT precisa rodar antes do filtro padrao de username/password.
			http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		// BCrypt gera hash com salt; a senha original nunca deve ser salva.
		return new BCryptPasswordEncoder();
	}
}
