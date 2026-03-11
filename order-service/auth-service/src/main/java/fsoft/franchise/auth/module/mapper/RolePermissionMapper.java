package fsoft.franchise.auth.module.mapper;

import fsoft.franchise.auth.module.dto.role_permission.response.RolePermissionResponseDTO;
import fsoft.franchise.auth.module.entity.RolePermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolePermissionMapper {

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleCode", source = "role.code")
    @Mapping(target = "permissionId", source = "permission.id")
    @Mapping(target = "permissionModule", source = "permission.module")
    @Mapping(target = "permissionCode", source = "permission.code")
    RolePermissionResponseDTO toResponseDTO(RolePermissionEntity entity);
}
