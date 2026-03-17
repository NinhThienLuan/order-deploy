package fsoft.franchise.client;

import fsoft.franchise.common.config.FeignClientConfig;
import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.dto.external.team4.FranchiseStoreResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "BE-Fanchisee", url = "https://isodimorphous-basilia-controvertible.ngrok-free.dev")
//@FeignClient(name = "BE-Fanchisee", configuration = FeignClientConfig.class) if team 4 add authen. This config pass the auth header.
public interface FranchiseClient {

    @GetMapping("/api/v1/franchise-stores")
    ApiResponse<List<FranchiseStoreResponseDTO>> getAllStores();
    
}
