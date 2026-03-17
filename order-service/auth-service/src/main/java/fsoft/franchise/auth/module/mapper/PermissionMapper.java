package fsoft.franchise.auth.module.mapper;

import fsoft.franchise.auth.module.dto.permission.request.PermissionRequestDTO;
import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;
import fsoft.franchise.auth.module.entity.PermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionResponseDTO toPermissionResponseDTO(PermissionEntity permissionEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    PermissionEntity toPermissionEntity(PermissionRequestDTO permissionRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    PermissionEntity updatePermissionFromDTO(PermissionRequestDTO dto, @MappingTarget PermissionEntity permissionEntity);
}
