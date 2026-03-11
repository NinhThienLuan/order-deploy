package a_and_s_service.compile.module.service.account_role.impl;

import a_and_s_service.compile.common.exception.ApiException;
import a_and_s_service.compile.common.exception.ErrorCode;
//import a_and_s_service.compile.module.dto.account_role.request.AccountRoleRequestDTO;
//import a_and_s_service.compile.module.dto.account_role.request.AccountRoleUpdateRequestDTO;
//import a_and_s_service.compile.module.dto.account_role.response.AccountRoleResponseDTO;
import a_and_s_service.compile.module.entity.AccountEntity;
import a_and_s_service.compile.module.entity.AccountRoleEntity;
import a_and_s_service.compile.module.entity.RoleEntity;
//import a_and_s_service.compile.module.mapper.AccountRoleMapper;
import a_and_s_service.compile.module.repository.AccountRepository;
import a_and_s_service.compile.module.repository.AccountRoleRepository;
import a_and_s_service.compile.module.repository.RoleRepository;
import a_and_s_service.compile.module.service.account_role.AccountRoleService;
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
public class AccountRoleServiceImpl implements AccountRoleService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
//    private final AccountRoleMapper accountRoleMapper;

//    @Override
//    public AccountRoleResponseDTO insertAccountRole(AccountRoleRequestDTO accountRoleRequestDTO) {
//        checkAccountRoleRequest(accountRoleRequestDTO);
//
//        boolean accountAndRoleExist = accountRoleRepository.existsByAccountIdAndRoleId(
//                accountRoleRequestDTO.accountId(), accountRoleRequestDTO.roleId());
//        if (accountAndRoleExist) {
//            log.error("Account with ID {} already has Role with ID {}", accountRoleRequestDTO.accountId(), accountRoleRequestDTO.roleId());
//            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Account already has this role");
//        }
//
//        AccountEntity accountEntity = accountRepository.findById(accountRoleRequestDTO.accountId())
//                .orElseThrow(() -> {
//                    log.error("Account with ID {} not found", accountRoleRequestDTO.accountId());
//                    return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found");
//                });
//        RoleEntity roleEntity = roleRepository.findById(accountRoleRequestDTO.roleId())
//                .orElseThrow(() -> {
//                    log.error("Role with ID {} not found", accountRoleRequestDTO.roleId());
//                    return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found");
//                });
//
//        AccountRoleEntity accountRoleEntity = new AccountRoleEntity();
//        accountRoleEntity.setAccount(accountEntity);
//        accountRoleEntity.setRole(roleEntity);
//        accountRoleRepository.save(accountRoleEntity);
//
//        return accountRoleMapper.toAccountRoleResponseDTO(accountRoleEntity);
//    }
//
//    @Override
//    @Transactional
//    public AccountRoleResponseDTO updateAccountRole(AccountRoleUpdateRequestDTO dto) {
//        if (dto == null) {
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Request cannot be null");
//        }
//
//        // Kiểm tra record cũ (accountId + oldRoleId) có tồn tại không
//        boolean oldExists = accountRoleRepository.existsByAccountIdAndRoleId(dto.accountId(), dto.oldRoleId());
//        if (!oldExists) {
//            log.error("AccountRole with Account ID {} and Role ID {} not found", dto.accountId(), dto.oldRoleId());
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "AccountRole not found");
//        }
//
//        // Kiểm tra record mới (accountId + newRoleId) đã tồn tại chưa để tránh duplicate
//        boolean newExists = accountRoleRepository.existsByAccountIdAndRoleId(dto.accountId(), dto.newRoleId());
//        if (newExists) {
//            log.error("Account with ID {} already has Role with ID {}", dto.accountId(), dto.newRoleId());
//            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Account already has the new role");
//        }
//
//        RoleEntity newRoleEntity = roleRepository.findById(dto.newRoleId())
//                .orElseThrow(() -> {
//                    log.error("Role with ID {} not found", dto.newRoleId());
//                    return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "New role not found");
//                });
//
//        AccountEntity accountEntity = accountRepository.findById(dto.accountId())
//                .orElseThrow(() -> {
//                    log.error("Account with ID {} not found", dto.accountId());
//                    return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found");
//                });
//
//        // Xóa record cũ, insert record mới
//        accountRoleRepository.deleteByAccountIdAndRoleId(dto.accountId(), dto.oldRoleId());
//
//        AccountRoleEntity newAccountRoleEntity = new AccountRoleEntity();
//        newAccountRoleEntity.setAccount(accountEntity);
//        newAccountRoleEntity.setRole(newRoleEntity);
//        accountRoleRepository.save(newAccountRoleEntity);
//
//        return accountRoleMapper.toAccountRoleResponseDTO(newAccountRoleEntity);
//    }
//
//    @Override
//    public void deleteAccountRole(AccountRoleRequestDTO accountRoleRequestDTO) {
//        checkAccountRoleRequest(accountRoleRequestDTO);
//
//        boolean checkAccountRoleExist = accountRoleRepository.existsByAccountIdAndRoleId(
//                accountRoleRequestDTO.accountId(), accountRoleRequestDTO.roleId());
//        if (!checkAccountRoleExist) {
//            log.error("AccountRole with Account ID {} and Role ID {} not found",
//                    accountRoleRequestDTO.accountId(), accountRoleRequestDTO.roleId());
//            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account Or Role not found");
//        }
//        accountRoleRepository.deleteByAccountIdAndRoleId(accountRoleRequestDTO.accountId(), accountRoleRequestDTO.roleId());
//    }

    @Override
    public List<AccountRoleEntity> findByRoleId(UUID roleId) {
        if (roleId == null) {
            log.error("Role ID is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
        }
        return accountRoleRepository.findByRoleId(roleId);
    }

    @Override
    public void saveAllAccountRole(Set<AccountRoleEntity> accountRoleEntitySet) {
        if (accountRoleEntitySet == null) {
            log.error("accountRoleEntitySet is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountRoleEntity set cannot be null");
        }
        accountRoleRepository.saveAll(accountRoleEntitySet);
    }

    @Override
    public void saveCustomerAccountRole(AccountRoleEntity accountRoleEntity) {
        if (accountRoleEntity == null) {
            log.error("accountRoleEntity is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountRoleEntity cannot be null");
        }
        accountRoleRepository.save(accountRoleEntity);
    }

//    private void checkAccountRoleRequest(AccountRoleRequestDTO accountRoleRequestDTO) {
//        if (accountRoleRequestDTO.accountId() == null) {
//            log.error("accountRequestDTO is null");
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Account ID cannot be null");
//        }
//        if (accountRoleRequestDTO.roleId() == null) {
//            log.error("accountRequestDTO is null");
//            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
//        }
//    }
}
