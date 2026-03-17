package fsoft.franchise.serviceImpl;

import fsoft.franchise.dto.products.IngredientResponse;
import fsoft.franchise.repository.IngredientRepository;
import fsoft.franchise.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    @Override
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .filter(i -> i.getDeleteAt() == null)
                .map(i -> IngredientResponse.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .build())
                .toList();
    }
}
