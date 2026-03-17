package fsoft.franchise.controller;

import fsoft.franchise.client.FranchiseClient;
import fsoft.franchise.client.WarehouseClient;
import fsoft.franchise.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/clients")
@Validated
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Controller handle request api for data from other services")
public class ClientController { //controller xu ly cac request sang service khac
//chu dong fetch data khi start

    private final FranchiseClient franchiseClient; //service team 4
    private final WarehouseClient warehouseClient; //service team 2
    //No authen + author
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<?>> getStoreId(HttpServletRequest request) {
        // Call FranchiseClient to fetch franchise stores
        ApiResponse<?> franchiseData = franchiseClient.getAllStores();

        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Franchise stores fetched successfully")
                .result(franchiseData.getResult())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }


}
