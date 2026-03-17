package fsoft.franchise.service;

import fsoft.franchise.dto.products.IngredientResponse;
import java.util.List;

public interface IngredientService {
    List<IngredientResponse> getAllIngredients();
}
