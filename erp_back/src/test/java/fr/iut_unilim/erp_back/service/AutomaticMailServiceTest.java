package fr.iut_unilim.erp_back.service;

import fr.iut_unilim.erp_back.builder.ConnectionBuilder;
import fr.iut_unilim.erp_back.entity.AutomaticMailConfig;
import fr.iut_unilim.erp_back.entity.Connection;
import fr.iut_unilim.erp_back.repository.AutomaticMailRepository;
import fr.iut_unilim.erp_back.repository.ConnectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AutomaticMailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private AutomaticMailRepository configRepository;

    @InjectMocks
    private AutomaticMailService mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "from", "noreply@iut.fr");
        ReflectionTestUtils.setField(mailService, "subject", "Rappel fiche ressource");
        ReflectionTestUtils.setField(mailService, "content", "Merci de compléter vos fiches.");
    }

    @Test
    @DisplayName("sendAutomaticMails send mails only to targeted roles only one time")
    void sendAutomaticMails_shouldSendOnlyToTargetRolesWithoutDuplicates() {
        Connection prof1 = ConnectionBuilder.aConnection()
                .withId(1L)
                .withEmail("prof1@unilim.fr")
                .withRoleName("Professeur")
                .build();

        Connection prof1Doublon = ConnectionBuilder.aConnection()
                .withId(2L)
                .withEmail("prof1@unilim.fr")
                .withRoleName("Professeur")
                .build();

        Connection vacataire = ConnectionBuilder.aConnection()
                .withId(3L)
                .withEmail("vacataire@unilim.fr")
                .withRoleName("Vacataire")
                .build();

        Connection etudiant = ConnectionBuilder.aConnection()
                .withId(4L)
                .withEmail("etudiant@unilim.fr")
                .withRoleName("Etudiant")
                .build();

        Connection sansEmail = ConnectionBuilder.aConnection()
                .withId(5L)
                .withEmail("")
                .withRoleName("Professeur")
                .build();

        Connection sansRole = ConnectionBuilder.aConnection()
                .withId(6L)
                .withEmail("inconnu@unilim.fr")
                .build();

        when(connectionRepository.findAll()).thenReturn(
                List.of(prof1, prof1Doublon, vacataire, etudiant, sansEmail, sansRole)
        );

        mailService.sendAutomaticMails();

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        List<SimpleMailMessage> sentMessages = messageCaptor.getAllValues();
        List<String> recipients = sentMessages.stream()
                .map(m -> Objects.requireNonNull(m.getTo())[0])
                .toList();

        assertThat(recipients).containsExactlyInAnyOrder("prof1@unilim.fr", "vacataire@unilim.fr");

        SimpleMailMessage firstMessage = sentMessages.get(0);
        assertThat(firstMessage.getFrom()).isEqualTo("noreply@iut.fr");
        assertThat(firstMessage.getSubject()).isEqualTo("Rappel fiche ressource");
        assertThat(firstMessage.getText()).isEqualTo("Merci de compléter vos fiches.");
    }

    @Test
    @DisplayName("sendAutomaticMails continue sending mails even if one mail fails")
    void sendAutomaticMails_shouldContinueSendingWhenOneFails() {
        Connection user1 = ConnectionBuilder.aConnection()
                .withId(1L)
                .withEmail("fail@unilim.fr")
                .withRoleName("Professeur")
                .build();

        Connection user2 = ConnectionBuilder.aConnection()
                .withId(2L)
                .withEmail("success@unilim.fr")
                .withRoleName("Vacataire")
                .build();

        when(connectionRepository.findAll()).thenReturn(List.of(user1, user2));

        doThrow(new MailSendException("SMTP timeout"))
                .doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

        mailService.sendAutomaticMails();

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("saveConfig saves the config and reload the scheduled tasks")
    void saveConfig_shouldSaveAndReloadSchedules() {
        mailService.initScheduler();

        AutomaticMailConfig config = new AutomaticMailConfig();
        config.setId(10L);
        config.setTime("08:30");
        config.setDayOfMonth(1);
        config.setMonthsFromList(List.of(9, 10, 11));

        when(configRepository.save(config)).thenReturn(config);
        when(configRepository.findAll()).thenReturn(List.of(config));

        AutomaticMailConfig result = mailService.saveConfig(config);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(configRepository).save(config);
        verify(configRepository, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("deleteConfig delete the configuration and update the scheduler")
    void deleteConfig_shouldDeleteAndReloadSchedules() {
        mailService.initScheduler();

        when(configRepository.findAll()).thenReturn(List.of());

        mailService.deleteConfig(5L);

        verify(configRepository).deleteById(5L);
        verify(configRepository, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("reloadSchedules ignore les configurations ayant une heure invalide sans planter")
    void reloadSchedules_shouldHandleInvalidCronConfigurationGracefully() {
        mailService.initScheduler();

        AutomaticMailConfig invalidConfig = new AutomaticMailConfig();
        invalidConfig.setId(99L);
        invalidConfig.setTime("invalid_format");
        invalidConfig.setDayOfMonth(1);

        when(configRepository.findAll()).thenReturn(List.of(invalidConfig));

        mailService.reloadSchedules();

        verify(configRepository, atLeastOnce()).findAll();
    }
}
