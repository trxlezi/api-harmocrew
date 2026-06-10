package br.edu.ifsp.harmocrew_api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final TokenBlacklistService tokenBlacklistService;

	public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
		this.jwtService = jwtService;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String token = bearerToken(request);

		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			if (tokenBlacklistService.isInvalidated(token)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalidado");
				return;
			}

			try {
				JwtService.JwtUser user = jwtService.parseAndValidate(token);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					user.email(),
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
				authentication.setDetails(user);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			catch (RuntimeException exception) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalido");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	public static String bearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return null;
		}
		return authorization.substring(7).trim();
	}
}
