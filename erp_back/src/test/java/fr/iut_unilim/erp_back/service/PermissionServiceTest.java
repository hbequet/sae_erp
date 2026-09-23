package fr.iut_unilim.erp_back.service;

import fr.iut_unilim.erp_back.dto.EditCreateRoleRequest;
import fr.iut_unilim.erp_back.dto.EditRolePermissionRequest;
import fr.iut_unilim.erp_back.dto.RolePermissionResponse;
import fr.iut_unilim.erp_back.dto.RoleResponse;
import fr.iut_unilim.erp_back.entity.PermissionDefinition;
import fr.iut_unilim.erp_back.entity.Role;
import fr.iut_unilim.erp_back.entity.RolePermission;
import fr.iut_unilim.erp_back.repository.PermissionDefinitionRepository;
import fr.iut_unilim.erp_back.repository.PermissionRepository;
import fr.iut_unilim.erp_back.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionDefinitionRepository permissionDefinitionRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Captor
    private ArgumentCaptor<RolePermission> rolePermissionCaptor;

    private Role standardRole;
    private Role adminRole;
    private PermissionDefinition testPermissionDef;
    private RolePermission standardRolePermission;

    @BeforeEach
    void setUp() {
        standardRole = new Role();
        standardRole.setId(2L);
        standardRole.setRoleName("Utilisateur");

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("Admin");

        testPermissionDef = new PermissionDefinition();
        testPermissionDef.setPermissionDefinitionID(10L);
        testPermissionDef.setPermissionKey("CAN_EDIT");
        testPermissionDef.setPermissionDefinitionBitIndex(3);

        BitSet standardBitSet = new BitSet();
        standardBitSet.set(3, true);

        standardRolePermission = new RolePermission();
        standardRolePermission.setPermissionID(100L);
        standardRolePermission.setRole(standardRole);
        standardRolePermission.setBitSet(standardBitSet);
    }

    @Test
    void getAllRolePermissions_shouldReturnMappedResponses() {
        RoleResponse mockRoleResponse = mock(RoleResponse.class);
        
        when(permissionRepository.findAll()).thenReturn(List.of(standardRolePermission));
        when(permissionDefinitionRepository.findAll()).thenReturn(List.of(testPermissionDef));
        when(roleService.convertEntityToResponse(standardRole)).thenReturn(mockRoleResponse);

        List<RolePermissionResponse> responses = permissionService.getAllRolePermissions();

        assertEquals(1, responses.size());
        RolePermissionResponse response = responses.get(0);
        assertEquals(100L, response.id());
        assertEquals(mockRoleResponse, response.role());
        
        assertTrue(response.permissions().containsKey(10L));
        assertTrue(response.permissions().get(10L));
    }

    @Test
    void hasPermission_shouldReturnTrue_whenRoleExistsAndHasPrivilege() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(standardRole));
        when(permissionDefinitionRepository.findByPermissionKey("CAN_EDIT")).thenReturn(testPermissionDef);
        when(permissionRepository.findByRole(standardRole)).thenReturn(Optional.of(standardRolePermission));

        boolean result = permissionService.hasPermission(2L, "CAN_EDIT");

        assertTrue(result);
    }

    @Test
    void hasPermission_shouldReturnFalse_whenRoleDoesNotExist() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = permissionService.hasPermission(99L, "CAN_EDIT");

        assertFalse(result);
    }

    @Test
    void createEditPermission_shouldUpdateExistingRoleAndSavePermissions() {
        Map<Long, Boolean> permsMap = Map.of(10L, true);
        EditCreateRoleRequest request = new EditCreateRoleRequest(2L, "NouveauNom", permsMap);

        when(roleRepository.findById(2L)).thenReturn(Optional.of(standardRole));
        when(permissionDefinitionRepository.findById(10L)).thenReturn(Optional.of(testPermissionDef));

        boolean result = permissionService.createEditPermission(request);

        assertTrue(result);
        assertEquals("NouveauNom", standardRole.getRoleName());
        
        verify(permissionRepository).save(rolePermissionCaptor.capture());
        RolePermission savedPermission = rolePermissionCaptor.getValue();
        assertEquals(standardRole, savedPermission.getRole());
        assertTrue(savedPermission.getBitSet().get(3));
    }

    @Test
    void createEditPermission_shouldReturnFalse_whenUpdatingNonExistentRole() {
        EditCreateRoleRequest request = new EditCreateRoleRequest(99L, "Fantome", new HashMap<>());
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = permissionService.createEditPermission(request);

        assertFalse(result);
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void createEditPermission_shouldCreateNewRoleAndSavePermissions_whenIdIsNull() {
        Map<Long, Boolean> permsMap = Map.of(10L, false);
        EditCreateRoleRequest request = new EditCreateRoleRequest(null, "SuperUser", permsMap);

        Role newRole = new Role();
        newRole.setRoleName("SuperUser");
        
        when(roleService.createOrAccessRoleByRoleName("SuperUser")).thenReturn(newRole);
        when(permissionDefinitionRepository.findById(10L)).thenReturn(Optional.of(testPermissionDef));

        boolean result = permissionService.createEditPermission(request);

        assertTrue(result);
        verify(permissionRepository).save(rolePermissionCaptor.capture());
        
        RolePermission savedPermission = rolePermissionCaptor.getValue();
        assertEquals(newRole, savedPermission.getRole());
        assertFalse(savedPermission.getBitSet().get(3));
    }

    @Test
    void editRolePermission_shouldUpdateBitSet_whenEntitiesFound() {
        EditRolePermissionRequest request = new EditRolePermissionRequest(100L, 10L, false);

        when(permissionRepository.findById(100L)).thenReturn(Optional.of(standardRolePermission));
        when(permissionDefinitionRepository.findById(10L)).thenReturn(Optional.of(testPermissionDef));

        boolean result = permissionService.editRolePermission(request);

        assertTrue(result);
        verify(permissionRepository).save(standardRolePermission);
        assertFalse(standardRolePermission.getBitSet().get(3));
    }

    @Test
    void editRolePermission_shouldReturnFalse_whenPermissionRoleNotFound() {
        EditRolePermissionRequest request = new EditRolePermissionRequest(999L, 10L, false);
        
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());
        when(permissionDefinitionRepository.findById(10L)).thenReturn(Optional.of(testPermissionDef));

        boolean result = permissionService.editRolePermission(request);

        assertFalse(result);
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void hasPrivilege_shouldReturnTrue_whenRoleIsAdmin_noMatterThePermission() {
        boolean result = permissionService.hasPrivilege(adminRole, "IMPOSSIBLE_KEY");

        assertTrue(result);
        verifyNoInteractions(permissionDefinitionRepository);
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void hasPrivilege_shouldReturnFalse_whenPermissionDefinitionDoesNotExist() {
        when(permissionDefinitionRepository.findByPermissionKey("UNKNOWN")).thenReturn(null);

        boolean result = permissionService.hasPrivilege(standardRole, "UNKNOWN");

        assertFalse(result);
    }

    @Test
    void hasPrivilege_shouldReturnFalse_whenRoleHasNoPermissionEntryInDatabase() {
        when(permissionRepository.findByRole(standardRole)).thenReturn(Optional.empty());

        boolean result = permissionService.hasPrivilege(standardRole, testPermissionDef);

        assertFalse(result);
    }
}
