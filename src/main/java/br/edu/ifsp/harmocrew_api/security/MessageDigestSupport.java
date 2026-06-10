package br.edu.ifsp.harmocrew_api.security;

import java.security.MessageDigest;

final class MessageDigestSupport {

	private MessageDigestSupport() {
	}

	static boolean constantTimeEquals(byte[] expected, byte[] actual) {
		return MessageDigest.isEqual(expected, actual);
	}
}
