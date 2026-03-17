package fsoft.franchise.auth.module.service.role_permission;

import fsoft.franchise.auth.module.dto.role.request.RoleRequestDTO;
import fsoft.franchise.auth.module.dto.role.request.RoleUpdateRequestDTO;
import fsoft.franchise.auth.module.dto.role.response.RoleResponseDTO;
import fsoft.franchise.auth.module.entity.RoleEntity;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponseDTO insertRole(RoleRequestDTO roleRequestDTO);

    RoleResponseDTO updateRole(UUID roleId, RoleUpdateRequestDTO roleRequestDTO);

    void deleteRole(UUID roleId);

    RoleResponseDTO getRoleById(UUID roleId);

    RoleEntity getRoleByIdEntity(UUID roleId);

    RoleEntity getRoleByCode(String code);

    List<RoleResponseDTO> getAllRoles();

    List<RoleEntity> getAllRoleByIds(List<UUID> roleIds);
}
