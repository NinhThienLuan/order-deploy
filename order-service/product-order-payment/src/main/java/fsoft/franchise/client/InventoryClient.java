package fsoft.franchise.client;

import fsoft.franchise.dto.integration.ExternalApiResponse;
import fsoft.franchise.dto.integration.StoreInventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Feign client for Inventory Service.
 */
@FeignClient(name = "inventory-client", url = "${integration.inventory.base-url}")
public interface InventoryClient {

    /**
     * GET /api/v1/store-inventories/store/{storeId}
     * Returns inventory list for the given store.
     */
    @GetMapping("/api/v1/store-inventories/store/{storeId}")
    ExternalApiResponse<List<StoreInventoryResponse>> getStoreInventory(@PathVariable("storeId") UUID storeId);
}
