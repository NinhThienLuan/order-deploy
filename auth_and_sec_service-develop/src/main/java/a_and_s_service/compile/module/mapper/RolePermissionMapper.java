package a_and_s_service.compile.module.mapper;

import a_and_s_service.compile.module.dto.role_permission.response.RolePermissionResponseDTO;
import a_and_s_service.compile.module.entity.RolePermissionEntity;
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
