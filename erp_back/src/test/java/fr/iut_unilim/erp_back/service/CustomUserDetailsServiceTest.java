package fr.iut_unilim.erp_back.service;

import fr.iut_unilim.erp_back.entity.Connection;
import fr.iut_unilim.erp_back.entity.Role;
import fr.iut_unilim.erp_back.repository.ConnectionRepository;
import fr.iut_unilim.erp_back.tools.utils.HashGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private ConnectionRepository connectionRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserIsFound() {
        String username = "test_user";
        String hashedUsername = "hashed_test_user_123";
        String expectedRole = "Admin";

        Connection mockConnection = mock(Connection.class);
        Role mockRole = mock(Role.class);
        
        when(mockConnection.getIdentifier()).thenReturn(username);
        when(mockConnection.getRole()).thenReturn(mockRole);
        when(mockRole.getRoleName()).thenReturn(expectedRole);

        when(connectionRepository.findByHashedIdentifier(hashedUsername))
                .thenReturn(Optional.of(mockConnection));

        try (MockedStatic<HashGenerator> mockedHashGenerator = mockStatic(HashGenerator.class)) {
            mockedHashGenerator.when(() -> HashGenerator.generateBlindIndex(username))
                    .thenReturn(hashedUsername);

            UserDetails result = customUserDetailsService.loadUserByUsername(username);

            assertNotNull(result);
            assertEquals(username, result.getUsername());
            assertEquals("", result.getPassword());
            assertEquals(1, result.getAuthorities().size());
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(expectedRole)));
        }
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFoundInDb() {
        String username = "unknown_user";
        String hashedUsername = "hashed_unknown";

        when(connectionRepository.findByHashedIdentifier(hashedUsername))
                .thenReturn(Optional.empty());

        try (MockedStatic<HashGenerator> mockedHashGenerator = mockStatic(HashGenerator.class)) {
            mockedHashGenerator.when(() -> HashGenerator.generateBlindIndex(username))
                    .thenReturn(hashedUsername);

            UsernameNotFoundException exception = assertThrows(
                    UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername(username)
            );

            assertEquals("User not found with username: " + username, exception.getMessage());
        }
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenHashGenerationFails() {
        String username = "error_user";

        try (MockedStatic<HashGenerator> mockedHashGenerator = mockStatic(HashGenerator.class)) {
            mockedHashGenerator.when(() -> HashGenerator.generateBlindIndex(username))
                    .thenThrow(new RuntimeException("Erreur de hash"));

            UsernameNotFoundException exception = assertThrows(
                    UsernameNotFoundException.class,
                    () -> customUserDetailsService.loadUserByUsername(username)
            );

            assertEquals("User not found with username: " + username, exception.getMessage());
            
            verifyNoInteractions(connectionRepository);
        }
    }
}
