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

	/*
	 * Este service concentra a regra de autenticacao.
	 *
	 * O controller recebe JSON da requisicao, transforma em DTO e chama esta
	 * camada. Aqui acontecem as validacoes de email, criptografia de senha,
	 * criacao do Artist vinculado ao User e emissao do JWT.
	 *
	 * Ponto importante para explicar: o backend nunca retorna a senha. A senha
	 * e salva com BCrypt e a resposta de login/cadastro volta apenas com dados
	 * publicos, token JWT e ids necessarios para o mobile operar.
	 */
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
		/*
		 * Normalizamos o email antes de salvar para evitar duplicidade como
		 * "User@Email.com" e "user@email.com". A validacao de formato vem das
		 * anotacoes do DTO; aqui fica a regra de negocio.
		 */
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException("Email ja cadastrado");
		}

		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole(UserRole.USER);

		/*
		 * No fluxo mobile, o cadastro ja envia stageName/mainSpecialty.
		 * Por isso o backend cria User e Artist juntos na mesma transacao.
		 * O relacionamento User 1:1 Artist permite que o app saiba qual artista
		 * pertence ao usuario logado sem escolher um artista aleatorio da lista.
		 */
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
		/*
		 * Login valida duas coisas:
		 * 1. se existe usuario com o email normalizado;
		 * 2. se a senha enviada bate com o hash BCrypt salvo no banco.
		 */
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
			.orElseThrow(() -> new BusinessException("Credenciais invalidas"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException("Credenciais invalidas");
		}

		return toAuthResponse(user);
	}

	public void logout(String token) {
		/*
		 * Como JWT e stateless, o servidor nao guarda sessao tradicional.
		 * Para permitir logout, o token atual entra em uma blacklist em memoria.
		 * Enquanto a aplicacao estiver rodando, esse token passa a ser recusado
		 * pelo JwtAuthenticationFilter.
		 */
		if (token == null || token.isBlank()) {
			throw new BusinessException("Token JWT nao informado");
		}
		tokenBlacklistService.invalidate(token);
	}

	private AuthResponse toAuthResponse(User user) {
		/*
		 * artistId e essencial para o mobile criar mensagens/candidaturas em nome
		 * do artista correto. Se o usuario nao tiver Artist vinculado, retornamos
		 * null em vez de inventar ou buscar outro artista.
		 */
		Long artistId = user.getArtist() == null ? null : user.getArtist().getId();
		return new AuthResponse(jwtService.generateToken(user), user.getId(), artistId, user.getName(), user.getEmail(), user.getRole());
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
