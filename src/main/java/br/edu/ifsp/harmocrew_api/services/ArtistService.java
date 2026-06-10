package br.edu.ifsp.harmocrew_api.services;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.ArtistRequest;
import br.edu.ifsp.harmocrew_api.dtos.ArtistResponse;
import br.edu.ifsp.harmocrew_api.dtos.mapper.ArtistMapper;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.ArtistRepository;

@Service
public class ArtistService {

	private final ArtistRepository artistRepository;

	public ArtistService(ArtistRepository artistRepository) {
		this.artistRepository = artistRepository;
	}

	@Transactional(readOnly = true)
	public List<ArtistResponse> list() {
		return artistRepository.findAll().stream().map(ArtistMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ArtistResponse get(Long id) {
		return ArtistMapper.toResponse(findEntity(id));
	}

	@Transactional
	public ArtistResponse create(ArtistRequest request) {
		Artist artist = new Artist();
		apply(artist, request);
		return ArtistMapper.toResponse(artistRepository.save(artist));
	}

	@Transactional
	public ArtistResponse update(Long id, ArtistRequest request) {
		Artist artist = findEntity(id);
		apply(artist, request);
		return ArtistMapper.toResponse(artist);
	}

	@Transactional
	public void delete(Long id) {
		Artist artist = findEntity(id);
		artistRepository.delete(artist);
	}

	Artist findEntity(Long id) {
		return artistRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Artista nao encontrado: " + id));
	}

	private void apply(Artist artist, ArtistRequest request) {
		artist.setStageName(request.stageName().trim());
		artist.setBio(request.bio());
		artist.setMainSpecialty(request.mainSpecialty().trim());
		artist.setAvailability(request.availability().trim());
		artist.setCity(request.city());
		artist.setInstruments(request.instruments() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.instruments()));
		artist.setMusicalStyles(request.musicalStyles() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.musicalStyles()));
	}
}
