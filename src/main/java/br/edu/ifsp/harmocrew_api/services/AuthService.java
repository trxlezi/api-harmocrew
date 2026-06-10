package br.edu.ifsp.harmocrew_api.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.AuthResponse;
import br.edu.ifsp.harmocrew_api.dtos.LoginRequest;
import br.edu.ifsp.harmocrew_api.dtos.RegisterRequest;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.User;
import br.edu.ifsp.harmocrew_api.enums.UserRole;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;
import br.edu.ifsp.harmocrew_api.exceptions.EmailAlreadyExistsException;
import br.edu.ifsp.harmocrew_api.repositories.UserRepository;
import br.edu.ifsp.harmocrew_api.security.JwtService;
import br.edu.ifsp.harmocrew_api.security.TokenBlacklistService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final TokenBlacklistService tokenBlacklistService;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		TokenBlacklistService tokenBlacklistService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException("Email ja cadastrado");
		}

		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole(UserRole.USER);

		if (hasText(request.stageName()) || hasText(request.mainSpecialty())) {
			Artist artist = new Artist();
			artist.setStageName(hasText(request.stageName()) ? request.stageName().trim() : request.name().trim());
			artist.setMainSpecialty(hasText(request.mainSpecialty()) ? request.mainSpecialty().trim() : "Artista");
			artist.setAvailability("A combinar");
			artist.setUser(user);
			user.setArtist(artist);
		}

		User saved = userRepository.save(user);
		return toAuthResponse(saved);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
			.orElseThrow(() -> new BusinessException("Credenciais invalidas"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException("Credenciais invalidas");
		}

		return toAuthResponse(user);
	}

	public void logout(String token) {
		if (token == null || token.isBlank()) {
			throw new BusinessException("Token JWT nao informado");
		}
		tokenBlacklistService.invalidate(token);
	}

	private AuthResponse toAuthResponse(User user) {
		return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getName(), user.getEmail(), user.getRole());
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
