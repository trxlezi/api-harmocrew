package br.edu.ifsp.harmocrew_api.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.repositories.UserRepository;

/*
 * Descobre qual artista esta executando a acao (autor do upload, votante...).
 * Usa o artistId enviado explicitamente ou, se ausente, o artista ligado ao
 * usuario do token JWT (o principal do token e o email).
 */
@Component
public class CurrentArtistResolver {

	private final ArtistService artistService;
	private final UserRepository userRepository;

	public CurrentArtistResolver(ArtistService artistService, UserRepository userRepository) {
		this.artistService = artistService;
		this.userRepository = userRepository;
	}

	public Artist resolve(Long explicitArtistId) {
		if (explicitArtistId != null) {
			return artistService.findEntity(explicitArtistId);
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return null;
		}
		return userRepository.findByEmail(authentication.getName()).map(user -> user.getArtist()).orElse(null);
	}
}
