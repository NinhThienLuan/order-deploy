package fsoft.franchise.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * product_variant_ingredient — join table with extra payload (quantity, unit).
 *
 * Uses a surrogate UUID PK for simplicity (the deleted composite-key embeddable
 * was overkill — a unique constraint on (variant_id, ingredient_id) enforces
 * the business rule at the DB level without the Persistable complexity).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_variant_ingredient", uniqueConstraints = @UniqueConstraint(name = "uq_variant_ingredient", columnNames = {
        "variant_id", "ingredient_id" }))
public class ProductVariantIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariantEntity variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientEntity ingredient;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit")
    private String unit;
}
