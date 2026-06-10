package br.edu.ifsp.harmocrew_api.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifsp.harmocrew_api.entities.User;
import br.edu.ifsp.harmocrew_api.exceptions.BusinessException;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final long expirationSeconds;

	public JwtService(
		@Value("${app.jwt.secret}") String secret,
		@Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds
	) {
		this.objectMapper = new ObjectMapper();
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(User user) {
		try {
			Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("sub", user.getEmail());
			payload.put("userId", user.getId());
			payload.put("name", user.getName());
			payload.put("role", user.getRole().name());
			payload.put("iat", Instant.now().getEpochSecond());
			payload.put("exp", Instant.now().plusSeconds(expirationSeconds).getEpochSecond());

			String encodedHeader = encodeJson(header);
			String encodedPayload = encodeJson(payload);
			String unsignedToken = encodedHeader + "." + encodedPayload;
			return unsignedToken + "." + sign(unsignedToken);
		}
		catch (Exception exception) {
			throw new BusinessException("Nao foi possivel gerar o token JWT");
		}
	}

	public JwtUser parseAndValidate(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				throw new BusinessException("Token JWT invalido");
			}

			String unsignedToken = parts[0] + "." + parts[1];
			String expectedSignature = sign(unsignedToken);
			if (!constantTimeEquals(expectedSignature, parts[2])) {
				throw new BusinessException("Assinatura JWT invalida");
			}

			Map<String, Object> payload = objectMapper.readValue(
				URL_DECODER.decode(parts[1]),
				new TypeReference<Map<String, Object>>() {
				});

			long expiration = ((Number) payload.get("exp")).longValue();
			if (Instant.now().getEpochSecond() >= expiration) {
				throw new BusinessException("Token JWT expirado");
			}

			return new JwtUser(
				((Number) payload.get("userId")).longValue(),
				(String) payload.get("sub"),
				(String) payload.get("name"),
				(String) payload.get("role"));
		}
		catch (BusinessException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new BusinessException("Token JWT invalido");
		}
	}

	private String encodeJson(Map<String, Object> value) throws Exception {
		return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
	}

	private String sign(String value) throws Exception {
		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
		return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
	}

	private boolean constantTimeEquals(String expected, String actual) {
		return MessageDigestSupport.constantTimeEquals(
			expected.getBytes(StandardCharsets.UTF_8),
			actual.getBytes(StandardCharsets.UTF_8));
	}

	public record JwtUser(Long userId, String email, String name, String role) {
	}
}
