package br.edu.ifsp.harmocrew_api.enums;

/*
 * Ciclo de vida da candidatura. Os estados intermediarios (UNDER_REVIEW,
 * SHORTLISTED) dao retorno ao musico enquanto o contratante avalia.
 */
public enum ApplicationStatus {
	PENDING,
	UNDER_REVIEW,
	SHORTLISTED,
	APPROVED,
	REJECTED
}
