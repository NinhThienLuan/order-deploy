package fsoft.franchise.client;

import fsoft.franchise.dto.external.team2.IngredientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "WAREHOUSE-API") //name get from eureka
public interface WarehouseClient {

    @GetMapping("api/ingredients")
    List<IngredientResponse> getAllAvailableIngredients();

}
