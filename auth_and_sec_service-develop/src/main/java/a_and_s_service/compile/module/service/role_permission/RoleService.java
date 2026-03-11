package a_and_s_service.compile.module.service.role_permission;

import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.request.RoleUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.entity.RoleEntity;

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
