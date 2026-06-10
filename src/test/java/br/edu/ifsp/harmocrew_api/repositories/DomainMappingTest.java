package br.edu.ifsp.harmocrew_api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.edu.ifsp.harmocrew_api.entities.Application;
import br.edu.ifsp.harmocrew_api.entities.Artist;
import br.edu.ifsp.harmocrew_api.entities.MusicalProject;
import br.edu.ifsp.harmocrew_api.entities.Task;
import br.edu.ifsp.harmocrew_api.entities.User;
import br.edu.ifsp.harmocrew_api.enums.ApplicationStatus;
import br.edu.ifsp.harmocrew_api.enums.ProjectStatus;
import br.edu.ifsp.harmocrew_api.enums.TaskPriority;
import br.edu.ifsp.harmocrew_api.enums.TaskStatus;
import br.edu.ifsp.harmocrew_api.enums.UserRole;

@DataJpaTest
@ActiveProfiles("test")
class DomainMappingTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ArtistRepository artistRepository;

	@Autowired
	private MusicalProjectRepository musicalProjectRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Test
	void shouldPersistCoreDomainRelationships() {
		User user = new User();
		user.setName("Marina Costa");
		user.setEmail("marina@harmocrew.app");
		user.setPassword("hashed-password");
		user.setRole(UserRole.USER);

		Artist artist = new Artist();
		artist.setStageName("Marina Voz");
		artist.setBio("Vocalista e compositora");
		artist.setMainSpecialty("Vocal principal");
		artist.getInstruments().add("Voz");
		artist.getMusicalStyles().add("Neo Soul");
		artist.setAvailability("Noites de quarta e sexta");
		artist.setCity("Sao Paulo");
		user.setArtist(artist);
		artist.setUser(user);

		User savedUser = userRepository.saveAndFlush(user);
		assertThat(userRepository.findByEmail("marina@harmocrew.app")).contains(savedUser);
		assertThat(userRepository.existsByEmail("marina@harmocrew.app")).isTrue();

		MusicalProject project = new MusicalProject();
		project.setTitle("Sessao Neo Soul");
		project.setDescription("Projeto colaborativo para ensaio gravado");
		project.setMusicalStyle("Neo Soul");
		project.setStatus(ProjectStatus.ACTIVE);
		project.getNeeds().add("Baixo");
		project.setStartDate(LocalDate.of(2026, 6, 10));
		project.getArtists().add(savedUser.getArtist());

		MusicalProject savedProject = musicalProjectRepository.saveAndFlush(project);

		Task task = new Task();
		task.setTitle("Fechar repertorio");
		task.setDescription("Definir musicas do ensaio");
		task.setStatus(TaskStatus.TODO);
		task.setPriority(TaskPriority.HIGH);
		task.setDueDate(LocalDate.of(2026, 6, 12));
		task.setResponsibleName("Marina Voz");
		task.setProject(savedProject);
		taskRepository.saveAndFlush(task);

		Application application = new Application();
		application.setMessage("Posso ajudar nos arranjos vocais.");
		application.setSpecialty("Vocal");
		application.setAvailability("Quartas a noite");
		application.setStatus(ApplicationStatus.PENDING);
		application.setArtist(savedUser.getArtist());
		application.setProject(savedProject);
		applicationRepository.saveAndFlush(application);

		assertThat(taskRepository.findByProjectId(savedProject.getId())).hasSize(1);
		assertThat(applicationRepository.findByProjectId(savedProject.getId())).hasSize(1);
		assertThat(applicationRepository.findByArtistId(savedUser.getArtist().getId())).hasSize(1);
		assertThat(artistRepository.findById(savedUser.getArtist().getId())).isPresent();
	}
}
