package fsoft.franchise.auth.module.service.role_permission.impl;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.module.dto.role_permission.request.RolePermissionRequestDTO;
import fsoft.franchise.auth.module.dto.role_permission.request.RolePermissionUpdateRequestDTO;
import fsoft.franchise.auth.module.dto.role_permission.response.RolePermissionResponseDTO;
import fsoft.franchise.auth.module.entity.PermissionEntity;
import fsoft.franchise.auth.module.entity.RoleEntity;
import fsoft.franchise.auth.module.entity.RolePermissionEntity;
import fsoft.franchise.auth.module.mapper.RolePermissionMapper;
import fsoft.franchise.auth.module.repository.RolePermissionRepository;
import fsoft.franchise.auth.module.service.role_permission.PermissionService;
import fsoft.franchise.auth.module.service.role_permission.RolePermissionService;
import fsoft.franchise.auth.module.service.role_permission.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void deleteByRoleId(UUID roleId) {
        if (roleId == null) {
            log.error("roleId is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
        }
        rolePermissionRepository.deleteByRoleId(roleId);
    }

    @Override
    public void saveAllRolePermission(Set<RolePermissionEntity> rolePermissionEntitySet) {
        if(rolePermissionEntitySet == null || rolePermissionEntitySet.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role-Permission association list cannot be null or empty");
        }
        rolePermissionRepository.saveAll(rolePermissionEntitySet);
    }

//    @Override
//    public RolePermissionResponseDTO insertRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO) {
//        checkRolePermissionRequestDTO(rolePermissionRequestDTO);
//
//        RoleEntity foundedRole = roleService.getRoleByIdEntity(rolePermissionRequestDTO.roleId());
//        if (foundedRole == null) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with ID: " + rolePermissionRequestDTO.roleId());
//        }
//        PermissionEntity foundedPermission = permissionService.getPermissionByEntityId(rolePermissionRequestDTO.permissionId());
//        if(foundedPermission == null) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found with ID: " + rolePermissionRequestDTO.permissionId());
//        }
//
//        boolean checkExisted  = rolePermissionRepository.existByRoleIdAndPermissionId(rolePermissionRequestDTO.roleId(), rolePermissionRequestDTO.permissionId());
//        if (checkExisted) {
//            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Role-Permission association already exists for Role ID: " + rolePermissionRequestDTO.roleId() + " and Permission ID: " + rolePermissionRequestDTO.permissionId());
//        }
//        RolePermissionEntity rolePermissionEntity = new RolePermissionEntity();
//        rolePermissionEntity.setRole(foundedRole);
//        rolePermissionEntity.setPermission(foundedPermission);
//        rolePermissionRepository.save(rolePermissionEntity);
//        return rolePermissionMapper.toResponseDTO(rolePermissionEntity);
//    }
//
//    @Override
//    @Transactional
//    public RolePermissionResponseDTO updateRolePermission(RolePermissionUpdateRequestDTO rolePermissionUpdateRequestDTO) {
//       if (rolePermissionUpdateRequestDTO == null) {
//           throw new ApiException(ErrorCode.INVALID_INPUT, "Role-Permission association ID cannot be null");
//       }
////       Check rolePermission trong DB đã tồn tại chưa, nếu tồn tại thì mới update
//        boolean checkExisted  = rolePermissionRepository.existByRoleIdAndPermissionId(rolePermissionUpdateRequestDTO.roleId(), rolePermissionUpdateRequestDTO.oldPermissionId());
//        if(!checkExisted) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role-Permission association not found for Role ID: " + rolePermissionUpdateRequestDTO.roleId() + " and Permission ID: " + rolePermissionUpdateRequestDTO.oldPermissionId());
//        }
//        RoleEntity newRole = roleService.getRoleByIdEntity(rolePermissionUpdateRequestDTO.roleId());
//        if (newRole == null) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with ID: " + rolePermissionUpdateRequestDTO.roleId());
//        }
//        PermissionEntity newPermission = permissionService.getPermissionByEntityId(rolePermissionUpdateRequestDTO.newPermissionId());
//        if(newPermission == null) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Old Permission not found with ID: " + rolePermissionUpdateRequestDTO.oldPermissionId());
//        }
//
////        Sau đó sẽ xóa bản ghi cũ đi và thêm bản ghi mới vào
//        rolePermissionRepository.deleteByRoleIdAndPermissionId(rolePermissionUpdateRequestDTO.roleId(), rolePermissionUpdateRequestDTO.oldPermissionId());
//        RolePermissionEntity newRolePermissionEntity = new RolePermissionEntity();
//        newRolePermissionEntity.setRole(newRole);
//        newRolePermissionEntity.setPermission(newPermission);
//        rolePermissionRepository.save(newRolePermissionEntity);
//        return rolePermissionMapper.toResponseDTO(newRolePermissionEntity);
//    }
//
//    @Override
//    @Transactional
//    public void deleteRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO) {
//        checkRolePermissionRequestDTO(rolePermissionRequestDTO);
//
//        boolean checkExisted  = rolePermissionRepository.existByRoleIdAndPermissionId(rolePermissionRequestDTO.roleId(), rolePermissionRequestDTO.permissionId());
//        if(!checkExisted) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role-Permission association not found for Role ID: " + rolePermissionRequestDTO.roleId() + " and Permission ID: " + rolePermissionRequestDTO.permissionId());
//        }
//        rolePermissionRepository.deleteByRoleIdAndPermissionId(rolePermissionRequestDTO.roleId(), rolePermissionRequestDTO.permissionId());
//    }
//
//    private void checkRolePermissionRequestDTO(RolePermissionRequestDTO rolePermissionRequestDTO) {
//        if (rolePermissionRequestDTO.roleId() == null) {
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
//        }
//        if (rolePermissionRequestDTO.permissionId() == null) {
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Permission ID cannot be null");
//        }
//    }


}
