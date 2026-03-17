package fsoft.franchise.dto.external.team4;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FranchiseStoreResponseDTO(

        UUID id,
        String storeName,
        String address,
        String status,
        LocalDate openDate,
        LocalDateTime createdAt
) {
}
