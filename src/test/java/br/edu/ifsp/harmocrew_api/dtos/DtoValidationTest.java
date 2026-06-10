package br.edu.ifsp.harmocrew_api.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class DtoValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void shouldValidateRegisterRequestRequiredFields() {
		RegisterRequest request = new RegisterRequest("", "not-an-email", "123", "", "");

		Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

		assertThat(violations)
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("name", "email", "password");
	}

	@Test
	void shouldValidateArtistRequestRequiredFields() {
		ArtistRequest request = new ArtistRequest("", null, "", Set.of(), Set.of(), "", null);

		Set<ConstraintViolation<ArtistRequest>> violations = validator.validate(request);

		assertThat(violations)
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("stageName", "mainSpecialty", "availability");
	}
}
