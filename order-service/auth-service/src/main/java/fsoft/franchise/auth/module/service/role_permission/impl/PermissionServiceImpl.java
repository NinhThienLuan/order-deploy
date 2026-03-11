package fsoft.franchise.auth.module.service.role_permission.impl;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.module.dto.permission.request.PermissionRequestDTO;
import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;
import fsoft.franchise.auth.module.entity.PermissionEntity;
import fsoft.franchise.auth.module.mapper.PermissionMapper;
import fsoft.franchise.auth.module.repository.PermissionRepository;
import fsoft.franchise.auth.module.service.role_permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;

//    @Override
//    public PermissionResponseDTO insertPermission(PermissionRequestDTO permissionRequestDTO) {
//        checkPermissionRequestDTO(permissionRequestDTO);
//        PermissionEntity permission = permissionMapper.toPermissionEntity(permissionRequestDTO);
//        permissionRepository.save(permission);
//        return permissionMapper.toPermissionResponseDTO(permission);
//    }
//
//    @Override
//    public PermissionResponseDTO updatePermission(UUID permissionId, PermissionRequestDTO permissionRequestDTO) {
//        if (permissionId == null) {
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Permission ID cannot be null");
//        }
//        PermissionEntity existingPermission = permissionRepository.findById(permissionId)
//                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found with ID: " + permissionId));
//        permissionMapper.updatePermissionFromDTO(permissionRequestDTO, existingPermission);
//        permissionRepository.save(existingPermission);
//        return permissionMapper.toPermissionResponseDTO(existingPermission);
//    }
//
//    @Override
//    public void deletePermission(UUID permissionId) {
//        if (permissionId == null) {
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Permission ID cannot be null");
//        }
//        if (!permissionRepository.existsById(permissionId)) {
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found with ID: " + permissionId);
//        }
//        permissionRepository.deleteById(permissionId);
//    }
//
//    @Override
//    public PermissionResponseDTO getPermissionById(UUID permissionId) {
//        if (permissionId == null) {
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Permission ID cannot be null");
//        }
//        PermissionEntity permission = permissionRepository.findById(permissionId)
//                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found with ID: " + permissionId));
//        return permissionMapper.toPermissionResponseDTO(permission);
//    }
//
    @Override
    public PermissionEntity getPermissionByEntityId(UUID permissionId) {
        if (permissionId == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Permission ID cannot be null");
        }
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found with ID: " + permissionId));
    }

    @Override
    public List<PermissionResponseDTO> getAllPermission() {
        List<PermissionEntity> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(permissionMapper::toPermissionResponseDTO)
                .toList();
    }

    @Override
    public List<PermissionEntity> getAllPermissionEntityByIds(List<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Permission IDs list cannot be null or empty");
        }
        return permissionRepository.findAllById(permissionIds);
    }

//    private void checkPermissionRequestDTO(PermissionRequestDTO permissionRequestDTO) {
//
//        if (permissionRequestDTO.code() == null || permissionRequestDTO.code().isEmpty()) {
//            throw new IllegalArgumentException("Permission code cannot be null or empty");
//        }
//        if (permissionRequestDTO.module() == null || permissionRequestDTO.module().isEmpty()) {
//            throw new IllegalArgumentException("Permission name cannot be null or empty");
//        }
//
//    }
}
