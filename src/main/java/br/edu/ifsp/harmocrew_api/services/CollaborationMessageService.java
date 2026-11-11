package br.edu.ifsp.harmocrew_api.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.harmocrew_api.dtos.CollaborationMessageRequest;
import br.edu.ifsp.harmocrew_api.dtos.CollaborationMessageResponse;
import br.edu.ifsp.harmocrew_api.dtos.mapper.CollaborationMessageMapper;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.CollaborationMessage;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.exceptions.ResourceNotFoundException;
import br.edu.ifsp.harmocrew_api.repositories.CollaborationMessageRepository;

@Service
public class CollaborationMessageService {

	/*
	 * Service das mensagens de colaboracao.
	 *
	 * Uma mensagem sempre pertence a um MusicalProject e tem um Artist remetente.
	 * Por isso o create nao aceita texto solto: ele busca projeto e artista no
	 * banco antes de salvar, garantindo integridade com as chaves estrangeiras.
	 */
	private final CollaborationMessageRepository messageRepository;
	private final MusicalProjectService projectService;
	private final ArtistService artistService;

	public CollaborationMessageService(
		CollaborationMessageRepository messageRepository,
		MusicalProjectService projectService,
		ArtistService artistService
	) {
		this.messageRepository = messageRepository;
		this.projectService = projectService;
		this.artistService = artistService;
	}

	@Transactional(readOnly = true)
	public List<CollaborationMessageResponse> list(Long projectId) {
		/*
		 * A mesma regra atende dois usos:
		 * - sem projectId: lista todas as mensagens;
		 * - com projectId: lista apenas as mensagens daquele projeto em ordem.
		 */
		List<CollaborationMessage> messages = projectId == null
			? messageRepository.findAll()
			: messageRepository.findByProjectIdOrderBySentAtAsc(projectId);
		return messages.stream().map(CollaborationMessageMapper::toResponse).toList();
	}

	@Transactional
	public CollaborationMessageResponse create(Long projectId, CollaborationMessageRequest request) {
		/*
		 * findEntity centraliza a regra de "404 se nao existir".
		 * Se o mobile enviar projectId ou senderArtistId invalido, a API nao salva
		 * uma mensagem quebrada; ela responde erro.
		 */
		MusicalProject project = projectService.findEntity(projectId);
		Artist sender = artistService.findEntity(request.senderArtistId());
		CollaborationMessage message = new CollaborationMessage();
		message.setProject(project);
		message.setSenderArtist(sender);
		message.setContent(request.content().trim());
		message.setType(request.type());
		return CollaborationMessageMapper.toResponse(messageRepository.save(message));
	}

	@Transactional
	public void delete(Long id) {
		CollaborationMessage message = messageRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Mensagem nao encontrada: " + id));
		messageRepository.delete(message);
	}
}
