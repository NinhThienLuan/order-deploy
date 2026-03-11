package fsoft.franchise.auth.module.service.account_role;

//import fsoft.franchise.auth.module.dto.account_role.request.AccountRoleRequestDTO;
//import fsoft.franchise.auth.module.dto.account_role.request.AccountRoleUpdateRequestDTO;
//import fsoft.franchise.auth.module.dto.account_role.response.AccountRoleResponseDTO;
import fsoft.franchise.auth.module.entity.AccountRoleEntity;

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
