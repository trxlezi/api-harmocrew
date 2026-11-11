package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.UserRole;

public record AuthResponse(
	/*
	 * DTO devolvido por register/login.
	 *
	 * token autentica as proximas requisicoes.
	 * userId identifica a conta.
	 * artistId identifica o perfil artistico ligado a conta.
	 * A senha nao aparece aqui por seguranca.
	 */
	String token,
	Long userId,
	Long artistId,
	String name,
	String email,
	UserRole role
) {
}
