package a_and_s_service.compile.module.controller;

import a_and_s_service.compile.common.exception.ErrorCode;
import a_and_s_service.compile.common.response.ApiResponse;
import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.request.RoleUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.service.account_role.AccountRoleService;
import a_and_s_service.compile.module.service.role_permission.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('role:write')")
    @PostMapping()
    public ResponseEntity<ApiResponse<RoleResponseDTO>> insertRole(HttpServletRequest request, @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO roleResponseDTO = roleService.insertRole(roleRequestDTO);

        return ResponseEntity.ok(
                ApiResponse.<RoleResponseDTO>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Role created successfully")
                        .result(roleResponseDTO)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('role:write')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(HttpServletRequest request, @PathVariable("id") UUID id, @RequestBody RoleUpdateRequestDTO roleRequestDTO) {
        RoleResponseDTO roleResponseDTO = roleService.updateRole(id, roleRequestDTO);

        return ResponseEntity.ok(
                ApiResponse.<RoleResponseDTO>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Role updated successfully")
                        .result(roleResponseDTO)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('role:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(HttpServletRequest request, @PathVariable("id") UUID id) {
        roleService.deleteRole(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Role deleted successfully")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }


    @PreAuthorize("hasRole('ADMIN') or hasAuthority('role:read')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> getRoleById(HttpServletRequest request, @PathVariable("id") UUID id) {
        RoleResponseDTO roleResponseDTO = roleService.getRoleById(id);

        return ResponseEntity.ok(
                ApiResponse.<RoleResponseDTO>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Role retrieved successfully")
                        .result(roleResponseDTO)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('role:read')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getAllRoles(HttpServletRequest request) {
        List<RoleResponseDTO> roleResponseDTOList = roleService.getAllRoles();
        return ResponseEntity.ok(
                ApiResponse.<List<RoleResponseDTO>>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Roles retrieved successfully")
                        .result(roleResponseDTOList)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }
}
