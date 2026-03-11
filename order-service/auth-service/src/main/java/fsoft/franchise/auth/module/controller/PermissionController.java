package fsoft.franchise.auth.module.controller;

import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.common.response.ApiResponse;
import fsoft.franchise.auth.module.dto.permission.request.PermissionRequestDTO;
import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;
import fsoft.franchise.auth.module.service.role_permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

//    @PostMapping()
//    public ResponseEntity<ApiResponse<PermissionResponseDTO>> createPermission(HttpServletRequest request, @RequestBody PermissionRequestDTO permissionRequestDTO) {
//        PermissionResponseDTO responseDTO = permissionService.insertPermission(permissionRequestDTO);
//        return ResponseEntity.ok(
//                ApiResponse.<PermissionResponseDTO>builder()
//                        .code(ErrorCode.SUCCESS.getCode())
//                        .message("Permission created successfully")
//                        .result(responseDTO)
//                        .path(request.getRequestURI())
//                        .timestamp(Instant.now())
//                        .build()
//        );
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<PermissionResponseDTO>> updatePermission(HttpServletRequest request, @PathVariable("id") UUID id, @RequestBody PermissionRequestDTO permissionRequestDTO) {
//        PermissionResponseDTO responseDTO = permissionService.updatePermission(id, permissionRequestDTO);
//        return ResponseEntity.ok(
//                ApiResponse.<PermissionResponseDTO>builder()
//                        .code(ErrorCode.SUCCESS.getCode())
//                        .message("Permission updated successfully")
//                        .result(responseDTO)
//                        .path(request.getRequestURI())
//                        .timestamp(Instant.now())
//                        .build()
//        );
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> deletePermission(HttpServletRequest request, @PathVariable("id") UUID id) {
//        permissionService.deletePermission(id);
//        return ResponseEntity.ok(
//                ApiResponse.<Void>builder()
//                        .code(ErrorCode.SUCCESS.getCode())
//                        .message("Permission deleted successfully")
//                        .path(request.getRequestURI())
//                        .timestamp(Instant.now())
//                        .build()
//        );
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<PermissionResponseDTO>> getPermissionById(HttpServletRequest request, @PathVariable("id") UUID id) {
//        PermissionResponseDTO responseDTO = permissionService.getPermissionById(id);
//        return ResponseEntity.ok(
//                ApiResponse.<PermissionResponseDTO>builder()
//                        .code(ErrorCode.SUCCESS.getCode())
//                        .message("Permission retrieved successfully")
//                        .result(responseDTO)
//                        .path(request.getRequestURI())
//                        .timestamp(Instant.now())
//                        .build()
//        );
//    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('permission:read')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getAllPermissions(HttpServletRequest request) {
        List<PermissionResponseDTO> responseDTOs = permissionService.getAllPermission();
        return ResponseEntity.ok(
                ApiResponse.<List<PermissionResponseDTO>>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Permissions retrieved successfully")
                        .result(responseDTOs)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

}
