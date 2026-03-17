package fsoft.franchise.client;

import fsoft.franchise.dto.integration.ExternalApiResponse;
import fsoft.franchise.dto.integration.FranchiseStoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for Franchise Store Service.
 * Base URL is resolved via the 'url' attribute pointing to the configured property.
 */
@FeignClient(name = "franchise-store-client", url = "${integration.franchise-store.base-url}")
public interface FranchiseStoreClient {

    /**
     * GET /api/v1/franchise-stores/{storeId}
     * Returns store details wrapped in a standard ApiResponse.
     */
    @GetMapping("/api/v1/franchise-stores/{storeId}")
    ExternalApiResponse<FranchiseStoreResponse> getStoreById(@PathVariable("storeId") UUID storeId);
}
