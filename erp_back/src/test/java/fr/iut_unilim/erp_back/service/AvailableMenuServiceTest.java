package fr.iut_unilim.erp_back.service;

import fr.iut_unilim.erp_back.builder.AvailableMenuBuilder;
import fr.iut_unilim.erp_back.builder.ConnectionBuilder;
import fr.iut_unilim.erp_back.dto.MenuResponse;
import fr.iut_unilim.erp_back.entity.AvailableMenu;
import fr.iut_unilim.erp_back.entity.Connection;
import fr.iut_unilim.erp_back.repository.AvailableMenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AvailableMenuServiceTest {

    private static final String USERNAME = "prof";

    private static final String ROLE_PROFESSEUR = "Professeur";
    private static final String ROLE_VACATAIRE = "Vacataire";

    private static final String PERM_RESOURCE_MANAGEMENT = "RESOURCE_MANAGEMENT";
    private static final String PERM_USER_MANAGEMENT = "USER_MANAGEMENT";
    private static final String PERM_RESOURCE_SHEET_HISTORY = "RESOURCE_SHEET_HISTORY";
    private static final String PERM_SAE_MANAGEMENT = "SAE_MANAGEMENT";
    private static final String PERM_MCCC_MANAGEMENT = "MCCC_MANAGEMENT";
    private static final String PERM_DEPARTMENT_MANAGEMENT = "DEPARTMENT_MANAGEMENT";

    @Mock
    private ConnectionService connectionService;

    @Mock
    private AvailableMenuRepository availableMenuRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private AvailableMenuService availableMenuService;

    @Test
    void getAvailableMenusFromUser_shouldReturnOnlyAvailableMenus() {
        Connection newUser = ConnectionBuilder.aConnection()
                .withId(1L)
                .withIdentifier(USERNAME)
                .withEmail("prof1@unilim.fr")
                .withRoleName(ROLE_PROFESSEUR)
                .build();

        AvailableMenu subMenu = new AvailableMenuBuilder()
                .id(2L)
                .label("Fiche ressource")
                .permissionKey(PERM_RESOURCE_SHEET_HISTORY)
                .children(List.of())
                .build();

        AvailableMenu availableMenu = new AvailableMenuBuilder()
                .id(1L)
                .label("Ressource")
                .permissionKey(PERM_RESOURCE_MANAGEMENT)
                .children(List.of(subMenu))
                .build();
        subMenu.setParent(availableMenu);

        when(connectionService.findByIdentifier(USERNAME))
                .thenReturn(Optional.of(newUser));

        when(availableMenuRepository.findByParentIdIsNull())
                .thenReturn(List.of(availableMenu));

        when(permissionService.hasPrivilege(newUser.getRole(), subMenu.getPermissionKey()))
                .thenReturn(true);

        when(permissionService.hasPrivilege(newUser.getRole(), availableMenu.getPermissionKey()))
                .thenReturn(true);

        List<MenuResponse> availableMenus = availableMenuService.getAvailableMenusFromUser(USERNAME);

        assertEquals(
                Stream.of(availableMenu).map(AvailableMenuBuilder::buildDto).toList(),
                availableMenus
        );
    }

    @Test
    void getAvailableMenusFromUser_shouldReturnEmptyList_whenUserNotFound() {
        when(connectionService.findByIdentifier(USERNAME)).thenReturn(Optional.empty());

        List<MenuResponse> result = availableMenuService.getAvailableMenusFromUser(USERNAME);

        assertTrue(result.isEmpty());
        verifyNoInteractions(availableMenuRepository);
        verifyNoInteractions(permissionService);
    }

    @Test
    void getAvailableMenusFromUser_shouldReturnEmptyList_whenUserHasNoMatchingPermissions() {
        Connection user = ConnectionBuilder.aConnection()
                .withId(1L)
                .withIdentifier(USERNAME)
                .withRoleName(ROLE_VACATAIRE)
                .build();

        AvailableMenu rootMenu = new AvailableMenuBuilder()
                .id(1L)
                .label("Gestion des utilisateurs")
                .permissionKey(PERM_USER_MANAGEMENT)
                .children(List.of())
                .build();

        when(connectionService.findByIdentifier(USERNAME)).thenReturn(Optional.of(user));
        when(availableMenuRepository.findByParentIdIsNull()).thenReturn(List.of(rootMenu));
        when(permissionService.hasPrivilege(user.getRole(), rootMenu.getPermissionKey())).thenReturn(false);

        List<MenuResponse> result = availableMenuService.getAvailableMenusFromUser(USERNAME);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableMenusFromUser_shouldIncludeParent_whenParentHasNoPermission_butChildHasPermission() {
        Connection user = ConnectionBuilder.aConnection()
                .withId(1L)
                .withIdentifier(USERNAME)
                .withRoleName(ROLE_PROFESSEUR)
                .build();

        AvailableMenu authorizedChild = new AvailableMenuBuilder()
                .id(2L)
                .label("Afficher les fiches ressources")
                .permissionKey(PERM_RESOURCE_SHEET_HISTORY)
                .children(List.of())
                .build();

        AvailableMenu parentMenu = new AvailableMenuBuilder()
                .id(1L)
                .label("Gestion des ressources")
                .permissionKey(PERM_RESOURCE_MANAGEMENT)
                .children(List.of(authorizedChild))
                .build();

        when(connectionService.findByIdentifier(USERNAME)).thenReturn(Optional.of(user));
        when(availableMenuRepository.findByParentIdIsNull()).thenReturn(List.of(parentMenu));

        when(permissionService.hasPrivilege(user.getRole(), authorizedChild.getPermissionKey())).thenReturn(true);
        when(permissionService.hasPrivilege(user.getRole(), parentMenu.getPermissionKey())).thenReturn(false);

        List<MenuResponse> result = availableMenuService.getAvailableMenusFromUser(USERNAME);

        assertEquals(1, result.size());
        MenuResponse parentResponse = result.get(0);
        assertEquals(1L, parentResponse.id());
        assertEquals(1, parentResponse.children().size());
        assertEquals(2L, parentResponse.children().get(0).id());
    }

    @Test
    void getAvailableMenusFromUser_shouldFilterOutUnauthorizedChildren() {
        Connection user = ConnectionBuilder.aConnection()
                .withId(1L)
                .withIdentifier(USERNAME)
                .withRoleName(ROLE_PROFESSEUR)
                .build();

        AvailableMenu authorizedChild = new AvailableMenuBuilder()
                .id(2L)
                .label("Gestion des SAE")
                .permissionKey(PERM_SAE_MANAGEMENT)
                .children(List.of())
                .build();

        AvailableMenu unauthorizedChild = new AvailableMenuBuilder()
                .id(3L)
                .label("Gestion des MCCC")
                .permissionKey(PERM_MCCC_MANAGEMENT)
                .children(List.of())
                .build();

        AvailableMenu parentMenu = new AvailableMenuBuilder()
                .id(1L)
                .label("Gestion des départements")
                .permissionKey(PERM_DEPARTMENT_MANAGEMENT)
                .children(List.of(authorizedChild, unauthorizedChild))
                .build();

        when(connectionService.findByIdentifier(USERNAME)).thenReturn(Optional.of(user));
        when(availableMenuRepository.findByParentIdIsNull()).thenReturn(List.of(parentMenu));

        when(permissionService.hasPrivilege(user.getRole(), authorizedChild.getPermissionKey())).thenReturn(true);
        when(permissionService.hasPrivilege(user.getRole(), unauthorizedChild.getPermissionKey())).thenReturn(false);
        when(permissionService.hasPrivilege(user.getRole(), parentMenu.getPermissionKey())).thenReturn(true);

        List<MenuResponse> result = availableMenuService.getAvailableMenusFromUser(USERNAME);

        assertEquals(1, result.size());
        List<MenuResponse> childrenResponse = result.get(0).children();
        assertEquals(1, childrenResponse.size());
        assertEquals(authorizedChild.getId(), childrenResponse.get(0).id());
    }
}
