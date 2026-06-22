package br.edu.ifsp.harmocrew_api.dtos;

import br.edu.ifsp.harmocrew_api.enums.UserRole;

public record AuthResponse(
	String token,
	Long userId,
	Long artistId,
	String name,
	String email,
	UserRole role
) {
}
