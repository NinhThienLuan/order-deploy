package a_and_s_service.compile.module.service.account_role;

//import a_and_s_service.compile.module.dto.account_role.request.AccountRoleRequestDTO;
//import a_and_s_service.compile.module.dto.account_role.request.AccountRoleUpdateRequestDTO;
//import a_and_s_service.compile.module.dto.account_role.response.AccountRoleResponseDTO;
import a_and_s_service.compile.module.entity.AccountRoleEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AccountRoleService {
//    AccountRoleResponseDTO insertAccountRole(AccountRoleRequestDTO accountRoleRequestDTO);
//    AccountRoleResponseDTO updateAccountRole(AccountRoleUpdateRequestDTO accountRoleUpdateRequestDTO);
//    void deleteAccountRole(AccountRoleRequestDTO accountRoleRequestDTO);

    List<AccountRoleEntity> findByRoleId(UUID roleId);

    void saveAllAccountRole(Set<AccountRoleEntity> accountRoleEntitySet);
    void saveCustomerAccountRole(AccountRoleEntity accountRoleEntity);

}
