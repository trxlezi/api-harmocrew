package br.edu.ifsp.harmocrew_api.security;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

	private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

	public void invalidate(String token) {
		invalidatedTokens.add(token);
	}

	public boolean isInvalidated(String token) {
		return invalidatedTokens.contains(token);
	}
}
