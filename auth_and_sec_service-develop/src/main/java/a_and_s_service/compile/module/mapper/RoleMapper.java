package a_and_s_service.compile.module.mapper;

import a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO;
import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.request.RoleUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "permissions", expression = "java(roleEntity.getRolePermissions() == null ? null : roleEntity.getRolePermissions().stream().map(rp -> new a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO(rp.getPermission().getId(), rp.getPermission().getModule(), rp.getPermission().getCode())).toList())")
    RoleResponseDTO toRoleResponseDTO(RoleEntity roleEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    RoleEntity toRoleEntity(RoleRequestDTO roleRequestDTO);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    void updateRoleFromDTO(RoleRequestDTO dto, @MappingTarget RoleEntity roleEntity);
}
