package fsoft.franchise.datainit;

import fsoft.franchise.entity.*;
import fsoft.franchise.enums.*;
import fsoft.franchise.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DataInit — seeds the database with realistic franchise data on startup.
 * <p>
 * UUID naming convention (easy to copy in tests):
 * Role UUIDs: 00000000-0000-0000-0001-xxxxxxxxxxxx
 * Account UUIDs: 00000000-0000-0000-0002-xxxxxxxxxxxx
 * Category UUIDs: 00000000-0000-0000-0003-xxxxxxxxxxxx
 * Ingredient UUIDs: 00000000-0000-0000-0004-xxxxxxxxxxxx
 * Product UUIDs: 00000000-0000-0000-0005-xxxxxxxxxxxx
 * ProductImg UUIDs: 00000000-0000-0000-0006-xxxxxxxxxxxx
 * Variant UUIDs: 00000000-0000-0000-0007-xxxxxxxxxxxx
 * Order UUIDs: 00000000-0000-0000-0008-xxxxxxxxxxxx
 * Payment UUIDs: 00000000-0000-0000-0009-xxxxxxxxxxxx
 * Transaction UUIDs: 00000000-0000-0000-0010-xxxxxxxxxxxx
 * Refund UUIDs: 00000000-0000-0000-0011-xxxxxxxxxxxx
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit implements CommandLineRunner {

    // ─── Account UUIDs (for reference - accounts managed by auth-service)
    // ───────────────────────────────────────────────────────────────
    public static final UUID ACC_ADMIN_ID = UUID.fromString("00000000-0000-0000-0002-000000000001");
    public static final UUID ACC_MANAGER_HCM1_ID = UUID.fromString("00000000-0000-0000-0002-000000000002");
    public static final UUID ACC_MANAGER_HCM2_ID = UUID.fromString("00000000-0000-0000-0002-000000000003");
    public static final UUID ACC_CUSTOMER_1_ID = UUID.fromString("00000000-0000-0000-0002-000000000004");
    public static final UUID ACC_CUSTOMER_2_ID = UUID.fromString("00000000-0000-0000-0002-000000000005");
    public static final UUID ACC_CUSTOMER_3_ID = UUID.fromString("00000000-0000-0000-0002-000000000006");
    public static final UUID ACC_CUSTOMER_4_ID = UUID.fromString("00000000-0000-0000-0002-000000000007");
    public static final UUID ACC_POS_ID = UUID.fromString("00000000-0000-0000-0002-000000000008");
    // ─── Category UUIDs
    // ──────────────────────────────────────────────────────────
    public static final UUID CAT_ESPRESSO_ID = UUID.fromString("00000000-0000-0000-0003-000000000001");
    public static final UUID CAT_TEA_ID = UUID.fromString("00000000-0000-0000-0003-000000000002");
    public static final UUID CAT_SMOOTHIE_ID = UUID.fromString("00000000-0000-0000-0003-000000000003");

    // ─── Role UUIDs (removed - now managed by auth-service)
    // ──────────────────────────────────────────────────────────────
    // ─── Ingredient UUIDs
    // ────────────────────────────────────────────────────────
    public static final UUID ING_ESPRESSO_ID = UUID.fromString("00000000-0000-0000-0004-000000000001");
    public static final UUID ING_MILK_ID = UUID.fromString("00000000-0000-0000-0004-000000000002");
    public static final UUID ING_OAT_MILK_ID = UUID.fromString("00000000-0000-0000-0004-000000000003");
    public static final UUID ING_SUGAR_SYRUP_ID = UUID.fromString("00000000-0000-0000-0004-000000000004");
    public static final UUID ING_MATCHA_POWDER_ID = UUID.fromString("00000000-0000-0000-0004-000000000005");
    public static final UUID ING_BLACK_TEA_ID = UUID.fromString("00000000-0000-0000-0004-000000000006");
    public static final UUID ING_ICE_ID = UUID.fromString("00000000-0000-0000-0004-000000000007");
    public static final UUID ING_WHIPPING_CREAM_ID = UUID.fromString("00000000-0000-0000-0004-000000000008");
    public static final UUID ING_BANANA_ID = UUID.fromString("00000000-0000-0000-0004-000000000009");
    public static final UUID ING_STRAWBERRY_ID = UUID.fromString("00000000-0000-0000-0004-000000000010");
    public static final UUID ING_CARAMEL_SAUCE_ID = UUID.fromString("00000000-0000-0000-0004-000000000011");
    public static final UUID ING_CHOCOLATE_SAUCE_ID = UUID.fromString("00000000-0000-0000-0004-000000000012");
    public static final UUID ING_VANILLA_SYRUP_ID = UUID.fromString("00000000-0000-0000-0004-000000000013");
    public static final UUID ING_COCOA_POWDER_ID = UUID.fromString("00000000-0000-0000-0004-000000000014");
    // ─── Product UUIDs
    // ───────────────────────────────────────────────────────────
    public static final UUID PROD_ESPRESSO_ID = UUID.fromString("00000000-0000-0000-0005-000000000001");
    public static final UUID PROD_LATTE_ID = UUID.fromString("00000000-0000-0000-0005-000000000002");
    public static final UUID PROD_CAPPUCCINO_ID = UUID.fromString("00000000-0000-0000-0005-000000000003");
    public static final UUID PROD_AMERICANO_ID = UUID.fromString("00000000-0000-0000-0005-000000000004");
    public static final UUID PROD_MATCHA_LATTE_ID = UUID.fromString("00000000-0000-0000-0005-000000000005");
    public static final UUID PROD_CHAI_TEA_ID = UUID.fromString("00000000-0000-0000-0005-000000000006");
    public static final UUID PROD_EARL_GREY_ID = UUID.fromString("00000000-0000-0000-0005-000000000007");
    public static final UUID PROD_BANANA_SMOOTHIE_ID = UUID.fromString("00000000-0000-0000-0005-000000000008");
    public static final UUID PROD_STRAWBERRY_SMO_ID = UUID.fromString("00000000-0000-0000-0005-000000000009");
    public static final UUID PROD_COLD_BREW_ID = UUID.fromString("00000000-0000-0000-0005-000000000010");
    public static final UUID PROD_CARAMEL_MACCHIATO_ID = UUID.fromString("00000000-0000-0000-0005-000000000011");
    public static final UUID PROD_MOCHA_ID = UUID.fromString("00000000-0000-0000-0005-000000000012");
    public static final UUID PROD_FLAT_WHITE_ID = UUID.fromString("00000000-0000-0000-0005-000000000013");
    public static final UUID PROD_HOT_CHOCOLATE_ID = UUID.fromString("00000000-0000-0000-0005-000000000014");
    public static final UUID PROD_VANILLA_FRAPP_ID = UUID.fromString("00000000-0000-0000-0005-000000000015");
    // ─── Product Image UUIDs
    // ─────────────────────────────────────────────────────
    public static final UUID IMG_ESPRESSO_ID = UUID.fromString("00000000-0000-0000-0006-000000000001");
    public static final UUID IMG_LATTE_ID = UUID.fromString("00000000-0000-0000-0006-000000000002");
    public static final UUID IMG_CAPPUCCINO_ID = UUID.fromString("00000000-0000-0000-0006-000000000003");
    public static final UUID IMG_AMERICANO_ID = UUID.fromString("00000000-0000-0000-0006-000000000004");
    public static final UUID IMG_MATCHA_LATTE_ID = UUID.fromString("00000000-0000-0000-0006-000000000005");
    public static final UUID IMG_CHAI_TEA_ID = UUID.fromString("00000000-0000-0000-0006-000000000006");
    public static final UUID IMG_EARL_GREY_ID = UUID.fromString("00000000-0000-0000-0006-000000000007");
    public static final UUID IMG_BANANA_SMOOTHIE_ID = UUID.fromString("00000000-0000-0000-0006-000000000008");
    public static final UUID IMG_STRAWBERRY_SMO_ID = UUID.fromString("00000000-0000-0000-0006-000000000009");
    public static final UUID IMG_COLD_BREW_ID = UUID.fromString("00000000-0000-0000-0006-000000000010");
    public static final UUID IMG_CARAMEL_MACCHIATO_ID = UUID.fromString("00000000-0000-0000-0006-000000000011");
    public static final UUID IMG_MOCHA_ID = UUID.fromString("00000000-0000-0000-0006-000000000012");
    public static final UUID IMG_FLAT_WHITE_ID = UUID.fromString("00000000-0000-0000-0006-000000000013");
    public static final UUID IMG_HOT_CHOCOLATE_ID = UUID.fromString("00000000-0000-0000-0006-000000000014");
    public static final UUID IMG_VANILLA_FRAPP_ID = UUID.fromString("00000000-0000-0000-0006-000000000015");
    // ─── Variant UUIDs
    // ───────────────────────────────────────────────────────────────
    // Espresso variants (S/M only — espresso doesn't have L)
    public static final UUID VAR_ESPRESSO_S = UUID.fromString("00000000-0000-0000-0007-000000000001");
    public static final UUID VAR_ESPRESSO_M = UUID.fromString("00000000-0000-0000-0007-000000000002");
    // Latte variants S/M/L
    public static final UUID VAR_LATTE_S = UUID.fromString("00000000-0000-0000-0007-000000000003");
    public static final UUID VAR_LATTE_M = UUID.fromString("00000000-0000-0000-0007-000000000004");
    public static final UUID VAR_LATTE_L = UUID.fromString("00000000-0000-0000-0007-000000000005");
    // Cappuccino S/M/L
    public static final UUID VAR_CAPPUCCINO_S = UUID.fromString("00000000-0000-0000-0007-000000000006");
    public static final UUID VAR_CAPPUCCINO_M = UUID.fromString("00000000-0000-0000-0007-000000000007");
    public static final UUID VAR_CAPPUCCINO_L = UUID.fromString("00000000-0000-0000-0007-000000000008");
    // Americano S/M/L
    public static final UUID VAR_AMERICANO_S = UUID.fromString("00000000-0000-0000-0007-000000000009");
    public static final UUID VAR_AMERICANO_M = UUID.fromString("00000000-0000-0000-0007-000000000010");
    public static final UUID VAR_AMERICANO_L = UUID.fromString("00000000-0000-0000-0007-000000000011");
    // Matcha Latte S/M/L
    public static final UUID VAR_MATCHA_S = UUID.fromString("00000000-0000-0000-0007-000000000012");
    public static final UUID VAR_MATCHA_M = UUID.fromString("00000000-0000-0000-0007-000000000013");
    public static final UUID VAR_MATCHA_L = UUID.fromString("00000000-0000-0000-0007-000000000014");
    // Chai Tea S/M/L
    public static final UUID VAR_CHAI_S = UUID.fromString("00000000-0000-0000-0007-000000000015");
    public static final UUID VAR_CHAI_M = UUID.fromString("00000000-0000-0000-0007-000000000016");
    public static final UUID VAR_CHAI_L = UUID.fromString("00000000-0000-0000-0007-000000000017");
    // Earl Grey S/M/L
    public static final UUID VAR_EARL_GREY_S = UUID.fromString("00000000-0000-0000-0007-000000000018");
    public static final UUID VAR_EARL_GREY_M = UUID.fromString("00000000-0000-0000-0007-000000000019");
    public static final UUID VAR_EARL_GREY_L = UUID.fromString("00000000-0000-0000-0007-000000000020");
    // Banana Smoothie S/M/L
    public static final UUID VAR_BANANA_SMO_S = UUID.fromString("00000000-0000-0000-0007-000000000021");
    public static final UUID VAR_BANANA_SMO_M = UUID.fromString("00000000-0000-0000-0007-000000000022");
    public static final UUID VAR_BANANA_SMO_L = UUID.fromString("00000000-0000-0000-0007-000000000023");
    // Cold Brew S/M
    public static final UUID VAR_COLD_BREW_S = UUID.fromString("00000000-0000-0000-0007-000000000024");
    public static final UUID VAR_COLD_BREW_M = UUID.fromString("00000000-0000-0000-0007-000000000025");
    // New Variants
    public static final UUID VAR_MACCHIATO_S = UUID.fromString("00000000-0000-0000-0007-000000000026");
    public static final UUID VAR_MACCHIATO_M = UUID.fromString("00000000-0000-0000-0007-000000000027");
    public static final UUID VAR_MACCHIATO_L = UUID.fromString("00000000-0000-0000-0007-000000000028");
    public static final UUID VAR_MOCHA_S = UUID.fromString("00000000-0000-0000-0007-000000000029");
    public static final UUID VAR_MOCHA_M = UUID.fromString("00000000-0000-0000-0007-000000000030");
    public static final UUID VAR_MOCHA_L = UUID.fromString("00000000-0000-0000-0007-000000000031");
    public static final UUID VAR_FLAT_WHITE_S = UUID.fromString("00000000-0000-0000-0007-000000000032");
    public static final UUID VAR_FLAT_WHITE_M = UUID.fromString("00000000-0000-0000-0007-000000000033");
    public static final UUID VAR_HOT_CHOCO_S = UUID.fromString("00000000-0000-0000-0007-000000000034");
    public static final UUID VAR_HOT_CHOCO_M = UUID.fromString("00000000-0000-0000-0007-000000000035");
    public static final UUID VAR_HOT_CHOCO_L = UUID.fromString("00000000-0000-0000-0007-000000000036");
    public static final UUID VAR_VANILLA_FRAPP_S = UUID.fromString("00000000-0000-0000-0007-000000000037");
    public static final UUID VAR_VANILLA_FRAPP_M = UUID.fromString("00000000-0000-0000-0007-000000000038");
    public static final UUID VAR_VANILLA_FRAPP_L = UUID.fromString("00000000-0000-0000-0007-000000000039");
    // ─── Order UUIDs
    // ─────────────────────────────────────────────────────────────
    public static final UUID ORDER_1_ID = UUID.fromString("00000000-0000-0000-0008-000000000001");
    public static final UUID ORDER_2_ID = UUID.fromString("00000000-0000-0000-0008-000000000002");
    public static final UUID ORDER_3_ID = UUID.fromString("00000000-0000-0000-0008-000000000003");
    public static final UUID ORDER_4_ID = UUID.fromString("00000000-0000-0000-0008-000000000004");
    public static final UUID ORDER_5_ID = UUID.fromString("00000000-0000-0000-0008-000000000005");
    public static final UUID ORDER_6_ID = UUID.fromString("00000000-0000-0000-0008-000000000006");
    // ─── Payment UUIDs
    // ───────────────────────────────────────────────────────────
    public static final UUID PAY_1_ID = UUID.fromString("00000000-0000-0000-0009-000000000001");
    public static final UUID PAY_2_ID = UUID.fromString("00000000-0000-0000-0009-000000000002");
    public static final UUID PAY_3_ID = UUID.fromString("00000000-0000-0000-0009-000000000003");
    public static final UUID PAY_4_ID = UUID.fromString("00000000-0000-0000-0009-000000000004");
    public static final UUID PAY_5_ID = UUID.fromString("00000000-0000-0000-0009-000000000005");
    // ─── Transaction UUIDs
    // ───────────────────────────────────────────────────────
    public static final UUID TXN_1_ID = UUID.fromString("00000000-0000-0000-0010-000000000001");
    public static final UUID TXN_2_ID = UUID.fromString("00000000-0000-0000-0010-000000000002");
    public static final UUID TXN_3_ID = UUID.fromString("00000000-0000-0000-0010-000000000003");
    // ─── Refund UUIDs
    // ────────────────────────────────────────────────────────────
    public static final UUID REFUND_1_ID = UUID.fromString("00000000-0000-0000-0011-000000000001");
    public static final UUID REFUND_2_ID = UUID.fromString("00000000-0000-0000-0011-000000000002");
    public static final UUID REFUND_3_ID = UUID.fromString("00000000-0000-0000-0011-000000000003");
    //
    // ─────────────────────────────────────────────────────────────────────────────
    // ─── Local asset base path (served by frontend static assets)
    // ────────────────
    // Images are located at: frontend/src/assets/products/
    // The frontend exposes them via Vite's asset import or a static file server.
    // Convention: /assets/products/<filename>.png
    private static final String IMG_BASE = "/assets/products/";
    // ─── Repositories
    // ────────────────────────────────────────────────────────────
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantIngredientRepository productVariantIngredientRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final RefundRepository refundRepository;
    private CategoryEntity catEspresso;

    // ─── 1. CATEGORIES (Roles & Accounts now managed by auth-service)
    // ───────────────────────────────────────────────────────────
    private CategoryEntity catTea;
    private CategoryEntity catSmoothie;
    private IngredientEntity ingEspresso;
    private IngredientEntity ingMilk;

    // ─── 4. INGREDIENTS
    // ──────────────────────────────────────────────────────────
    private IngredientEntity ingOatMilk;
    private IngredientEntity ingSugarSyrup;
    private IngredientEntity ingMatchaPowder;
    private IngredientEntity ingBlackTea;
    private IngredientEntity ingIce;
    private IngredientEntity ingWhippingCream;
    private IngredientEntity ingBanana;
    private IngredientEntity ingStrawberry;
    private IngredientEntity ingCaramelSauce;
    private IngredientEntity ingChocolateSauce;
    private IngredientEntity ingVanillaSyrup;
    private IngredientEntity ingCocoaPowder;
    // Hold variant refs for order items
    private ProductVariantEntity varEspressoS, varEspressoM;
    private ProductVariantEntity varLatteS, varLatteM, varLatteL;
    private ProductVariantEntity varCappuccinoS, varCappuccinoM, varCappuccinoL;

    // ─── 5. PRODUCTS + IMAGES + VARIANTS + VARIANT-INGREDIENTS
    // ──────────────────
    private ProductVariantEntity varAmericanoS, varAmericanoM, varAmericanoL;
    private ProductVariantEntity varMatchaS, varMatchaM, varMatchaL;
    private ProductVariantEntity varChaiS, varChaiM, varChaiL;
    private ProductVariantEntity varEarlGreyS, varEarlGreyM, varEarlGreyL;
    private ProductVariantEntity varBananaSmS, varBananaSmM, varBananaSmL;
    private ProductVariantEntity varColdBrewS, varColdBrewM;
    private ProductVariantEntity varMacchiatoS, varMacchiatoM, varMacchiatoL;
    private ProductVariantEntity varMochaS, varMochaM, varMochaL;
    private ProductVariantEntity varFlatWhiteS, varFlatWhiteM;
    private ProductVariantEntity varHotChocoS, varHotChocoM, varHotChocoL;
    private ProductVariantEntity varVanillaFrappS, varVanillaFrappM, varVanillaFrappL;

    // Store UUIDs
    public static final UUID STORE_1_ID = UUID.fromString("00000000-0000-0000-0012-000000000001");
    public static final UUID STORE_2_ID = UUID.fromString("00000000-0000-0000-0012-000000000002");

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("[DataInit] Data already initialized — skipping.");
            return;
        }
        log.info("[DataInit] Seeding initial franchise data...");

        seedCategories();
        seedIngredients();
        seedProducts();
        seedOrders();

        log.info("[DataInit] ✅ Seed complete.");
    }

    private void seedCategories() {
        catEspresso = categoryRepository.save(CategoryEntity.builder()
                .id(CAT_ESPRESSO_ID)
                .name("Espresso & Coffee")
                .description("Hot and iced espresso-based drinks crafted from premium Arabica beans")
                .active(true)
                .build());

        catTea = categoryRepository.save(CategoryEntity.builder()
                .id(CAT_TEA_ID)
                .name("Tea & Matcha")
                .description("Freshly brewed teas and authentic Japanese matcha beverages")
                .active(true)
                .build());

        catSmoothie = categoryRepository.save(CategoryEntity.builder()
                .id(CAT_SMOOTHIE_ID)
                .name("Smoothies & Cold Brew")
                .description("Blended fresh-fruit smoothies and slow-steeped cold brew coffees")
                .active(true)
                .build());

        log.info("[DataInit] Categories seeded: Espresso & Coffee, Tea & Matcha,Smoothies & Cold Brew");
    }

    private void seedIngredients() {
        ingEspresso = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_ESPRESSO_ID).name("Espresso Shot")
                .description("Double-shot espresso from Arabica blend").build());

        ingMilk = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_MILK_ID).name("Fresh Whole Milk")
                .description("Full-fat pasteurized fresh milk").build());

        ingOatMilk = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_OAT_MILK_ID).name("Oat Milk")
                .description("Barista-grade oat milk for dairy-free options").build());

        ingSugarSyrup = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_SUGAR_SYRUP_ID).name("Sugar Syrup")
                .description("Simple syrup (1:1 sugar-water ratio)").build());

        ingMatchaPowder = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_MATCHA_POWDER_ID).name("Matcha Powder")
                .description("Ceremonial-grade Japanese matcha from Uji, Kyoto").build());

        ingBlackTea = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_BLACK_TEA_ID).name("Black Tea Blend")
                .description("Premium Assam and Ceylon loose-leaf black tea blend").build());

        ingIce = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_ICE_ID).name("Ice Cubes")
                .description("Filtered water ice cubes").build());

        ingWhippingCream = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_WHIPPING_CREAM_ID).name("Whipping Cream")
                .description("Heavy cream whipped to soft peaks for topping").build());

        ingBanana = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_BANANA_ID).name("Banana")
                .description("Ripe Cavendish bananas").build());

        ingStrawberry = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_STRAWBERRY_ID).name("Strawberry")
                .description("Fresh or frozen strawberries").build());

        ingCaramelSauce = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_CARAMEL_SAUCE_ID).name("Caramel Sauce")
                .description("Rich and buttery caramel drizzle").build());

        ingChocolateSauce = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_CHOCOLATE_SAUCE_ID).name("Chocolate Sauce")
                .description("Dark chocolate syrup for drinks").build());

        ingVanillaSyrup = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_VANILLA_SYRUP_ID).name("Vanilla Syrup")
                .description("Aromatic Madagascar vanilla syrup").build());

        ingCocoaPowder = ingredientRepository.save(IngredientEntity.builder()
                .id(ING_COCOA_POWDER_ID).name("Cocoa Powder")
                .description("Premium unsweetened Dutch-process cocoa").build());

        log.info("[DataInit] Ingredients seeded: 14 items");
    }

    private void seedProducts() {
// ── ESPRESSO & COFFEE CATEGORY ──────────────────────────────────────────

// 1. Espresso
        ProductEntity espresso = productRepository.save(ProductEntity.builder()
                .id(PROD_ESPRESSO_ID).name("Espresso")
                .description(
                        "Intense, full-bodied espresso shot served in a demitasse. Rich crema, balanced bitterness and sweetness.")
                .type(ProductType.HOT).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_ESPRESSO_ID).product(espresso)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052588/products/ng4xocgcanrcy15gtkhr.png").isPrimary(true)
                .build());
        varEspressoS = saveVariant(VAR_ESPRESSO_S, espresso, "Single Shot (S)", new
                BigDecimal("35000"));
        varEspressoM = saveVariant(VAR_ESPRESSO_M, espresso, "Double Shot (M)", new
                BigDecimal("45000"));
        linkIngredient(varEspressoS, ingEspresso, new BigDecimal("1"), "shot");
        linkIngredient(varEspressoM, ingEspresso, new BigDecimal("2"), "shot");

// 2. Latte
        ProductEntity latte = productRepository.save(ProductEntity.builder()
                .id(PROD_LATTE_ID).name("Caffè Latte")
                .description(
                        "Smooth espresso combined with velvety steamed milk and a thin layer of foam. A classic café favourite.")
                .type(ProductType.HOT).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_LATTE_ID).product(latte)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052754/products/mj0aeucicnhstpxkev81.png").isPrimary(true)
                .build());
        varLatteS = saveVariant(VAR_LATTE_S, latte, "Small (S)", new
                BigDecimal("55000"));
        varLatteM = saveVariant(VAR_LATTE_M, latte, "Medium (M)", new
                BigDecimal("65000"));
        varLatteL = saveVariant(VAR_LATTE_L, latte, "Large (L)", new
                BigDecimal("75000"));
        linkIngredient(varLatteS, ingEspresso, new BigDecimal("1"), "shot");
        linkIngredient(varLatteS, ingMilk, new BigDecimal("180"), "ml");
        linkIngredient(varLatteM, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varLatteM, ingMilk, new BigDecimal("250"), "ml");
        linkIngredient(varLatteL, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varLatteL, ingMilk, new BigDecimal("350"), "ml");

// 3. Cappuccino
        ProductEntity cappuccino = productRepository.save(ProductEntity.builder()
                .id(PROD_CAPPUCCINO_ID).name("Cappuccino")
                .description(
                        "Equal parts espresso, steamed milk, and thick microfoam. Bold yet balanced — the Italian classic.")
                .type(ProductType.HOT).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_CAPPUCCINO_ID).product(cappuccino)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052870/products/r94b557vvhtabmbpuemz.png").isPrimary(true)
                .build());
        varCappuccinoS = saveVariant(VAR_CAPPUCCINO_S, cappuccino, "Small (S)", new
                BigDecimal("55000"));
        varCappuccinoM = saveVariant(VAR_CAPPUCCINO_M, cappuccino, "Medium (M)", new
                BigDecimal("65000"));
        varCappuccinoL = saveVariant(VAR_CAPPUCCINO_L, cappuccino, "Large (L)", new
                BigDecimal("75000"));
        linkIngredient(varCappuccinoS, ingEspresso, new BigDecimal("1"), "shot");
        linkIngredient(varCappuccinoS, ingMilk, new BigDecimal("120"), "ml");
        linkIngredient(varCappuccinoM, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varCappuccinoM, ingMilk, new BigDecimal("180"), "ml");
        linkIngredient(varCappuccinoL, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varCappuccinoL, ingMilk, new BigDecimal("240"), "ml");
        linkIngredient(varCappuccinoL, ingWhippingCream, new BigDecimal("30"), "ml");

// 4. Americano
        ProductEntity americano = productRepository.save(ProductEntity.builder()
                .id(PROD_AMERICANO_ID).name("Americano")
                .description("Espresso diluted with hot water, creating a milder but still robust coffee experience.")
                .type(ProductType.HOT_ICED).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_AMERICANO_ID).product(americano)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052901/products/uryifm9viuoduvwfrw7m.png").isPrimary(true)
                .build());
        varAmericanoS = saveVariant(VAR_AMERICANO_S, americano, "Small (S)", new
                BigDecimal("45000"));
        varAmericanoM = saveVariant(VAR_AMERICANO_M, americano, "Medium (M)", new
                BigDecimal("55000"));
        varAmericanoL = saveVariant(VAR_AMERICANO_L, americano, "Large (L)", new
                BigDecimal("65000"));
        linkIngredient(varAmericanoS, ingEspresso, new BigDecimal("1"), "shot");
        linkIngredient(varAmericanoM, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varAmericanoL, ingEspresso, new BigDecimal("3"), "shot");

// ── TEA & MATCHA CATEGORY ───────────────────────────────────────────────

// 5. Matcha Latte
// ℹ️ Không có prod_matcha_latte.png trong assets → dùng prod_mocha.png làm
// (màu xanh tương tự). Thay bằng ảnh thật khi có.
        ProductEntity matchaLatte = productRepository.save(ProductEntity.builder()
                .id(PROD_MATCHA_LATTE_ID).name("Matcha Latte")
                .description(
                        "Vibrant ceremonial-grade matcha whisked with oat milk. Earthy, creamy, and naturally energizing.")
                .type(ProductType.HOT_ICED).active(true).category(catTea).build());
        productImageRepository.save(ProductImageEntity.builder()
                        .id(IMG_MATCHA_LATTE_ID).product(matchaLatte)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052934/products/bcmmrcedtm63u3euqp1i.png").isPrimary(true)
                .build());
        varMatchaS = saveVariant(VAR_MATCHA_S, matchaLatte, "Small (S)", new
                BigDecimal("65000"));
        varMatchaM = saveVariant(VAR_MATCHA_M, matchaLatte, "Medium (M)", new
                BigDecimal("75000"));
        varMatchaL = saveVariant(VAR_MATCHA_L, matchaLatte, "Large (L)", new
                BigDecimal("85000"));
        linkIngredient(varMatchaS, ingMatchaPowder, new BigDecimal("5"), "g");
        linkIngredient(varMatchaS, ingOatMilk, new BigDecimal("180"), "ml");
        linkIngredient(varMatchaM, ingMatchaPowder, new BigDecimal("7"), "g");
        linkIngredient(varMatchaM, ingOatMilk, new BigDecimal("250"), "ml");
        linkIngredient(varMatchaL, ingMatchaPowder, new BigDecimal("10"), "g");
        linkIngredient(varMatchaL, ingOatMilk, new BigDecimal("350"), "ml");

// 6. Chai Tea Latte
        ProductEntity chaiTea = productRepository.save(ProductEntity.builder()
                .id(PROD_CHAI_TEA_ID).name("Chai Tea Latte")
                .description(
                        "Spiced black tea blend with hints of cinnamon, cardamom and ginger, steamed with velvety milk.")
                                .type(ProductType.HOT_ICED).active(true).category(catTea).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_CHAI_TEA_ID).product(chaiTea)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052959/products/qb5b9himqiamu1c4ayz4.png").isPrimary(true)
                .build());
        varChaiS = saveVariant(VAR_CHAI_S, chaiTea, "Small (S)", new
                BigDecimal("55000"));
        varChaiM = saveVariant(VAR_CHAI_M, chaiTea, "Medium (M)", new
                BigDecimal("65000"));
        varChaiL = saveVariant(VAR_CHAI_L, chaiTea, "Large (L)", new
                BigDecimal("75000"));
        linkIngredient(varChaiS, ingBlackTea, new BigDecimal("5"), "g");
        linkIngredient(varChaiS, ingMilk, new BigDecimal("150"), "ml");
        linkIngredient(varChaiM, ingBlackTea, new BigDecimal("7"), "g");
        linkIngredient(varChaiM, ingMilk, new BigDecimal("220"), "ml");
        linkIngredient(varChaiL, ingBlackTea, new BigDecimal("10"), "g");
        linkIngredient(varChaiL, ingMilk, new BigDecimal("300"), "ml");
        linkIngredient(varChaiL, ingSugarSyrup, new BigDecimal("15"), "ml");

// 7. Earl Grey
// ℹ️ Không có prod_earl_grey.png trong assets → dùng prod_flat_white.png làm
// Thay bằng ảnh thật khi có.
                ProductEntity earlGrey = productRepository.save(ProductEntity.builder()
                .id(PROD_EARL_GREY_ID).name("Earl Grey Milk Tea")
                .description(
                        "Classic bergamot-scented Earl Grey brewed strong and blended with fresh milk. Refined and aromatic.")
                .type(ProductType.HOT_ICED).active(true).category(catTea).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_EARL_GREY_ID).product(earlGrey)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773052991/products/iefutlimczgruf3n2mhk.png").isPrimary(true)
                .build());
        varEarlGreyS = saveVariant(VAR_EARL_GREY_S, earlGrey, "Small (S)", new
                BigDecimal("55000"));
        varEarlGreyM = saveVariant(VAR_EARL_GREY_M, earlGrey, "Medium (M)", new
                BigDecimal("65000"));
        varEarlGreyL = saveVariant(VAR_EARL_GREY_L, earlGrey, "Large (L)", new
                BigDecimal("75000"));
        linkIngredient(varEarlGreyS, ingBlackTea, new BigDecimal("4"), "g");
        linkIngredient(varEarlGreyS, ingMilk, new BigDecimal("150"), "ml");
        linkIngredient(varEarlGreyM, ingBlackTea, new BigDecimal("6"), "g");
        linkIngredient(varEarlGreyM, ingMilk, new BigDecimal("200"), "ml");
        linkIngredient(varEarlGreyL, ingBlackTea, new BigDecimal("8"), "g");
        linkIngredient(varEarlGreyL, ingMilk, new BigDecimal("280"), "ml");

// ── SMOOTHIES & COLD BREW CATEGORY ────────────────────────────────────

// 8. Banana Smoothie
// Thay bằng ảnh thật khi có.
                ProductEntity bananaSmoothie = productRepository.save(ProductEntity.builder()
                .id(PROD_BANANA_SMOOTHIE_ID).name("Banana Smoothie")
                .description(
                        "Creamy blend of ripe Cavendish bananas, fresh milk and a hint of honey.Naturally sweet and filling.")
                                .type(ProductType.COLD).active(true).category(catSmoothie).build());
        productImageRepository.save(ProductImageEntity.builder()
                        .id(IMG_BANANA_SMOOTHIE_ID).product(bananaSmoothie)
                        .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773053021/products/d7hk16yysn5ip7d9euoq.png").isPrimary(true)
                        .build());
        varBananaSmS = saveVariant(VAR_BANANA_SMO_S, bananaSmoothie, "Small (S)", new
                BigDecimal("55000"));
        varBananaSmM = saveVariant(VAR_BANANA_SMO_M, bananaSmoothie, "Medium (M)",
                new BigDecimal("65000"));
        varBananaSmL = saveVariant(VAR_BANANA_SMO_L, bananaSmoothie, "Large (L)", new
                BigDecimal("75000"));
        linkIngredient(varBananaSmS, ingBanana, new BigDecimal("1"), "pcs");
        linkIngredient(varBananaSmS, ingMilk, new BigDecimal("150"), "ml");
        linkIngredient(varBananaSmM, ingBanana, new BigDecimal("2"), "pcs");
        linkIngredient(varBananaSmM, ingMilk, new BigDecimal("200"), "ml");
        linkIngredient(varBananaSmL, ingBanana, new BigDecimal("2"), "pcs");
        linkIngredient(varBananaSmL, ingMilk, new BigDecimal("280"), "ml");
        linkIngredient(varBananaSmL, ingWhippingCream, new BigDecimal("30"), "ml");

// 9. Strawberry Smoothie
// ℹ️ Không có prod_strawberry_smoothie.png trong assets → dùng
// Thay bằng ảnh thật khi có.
                ProductEntity strawberrySmoothie =
                productRepository.save(ProductEntity.builder()
                        .id(PROD_STRAWBERRY_SMO_ID).name("Strawberry Smoothie")
                        .description(
                                "Vibrant blend of fresh strawberries with oat milk and a drizzle of sugar syrup.Refreshingly fruity.")
                                        .type(ProductType.COLD).active(true).category(catSmoothie).build());
        productImageRepository.save(ProductImageEntity.builder()
                        .id(IMG_STRAWBERRY_SMO_ID).product(strawberrySmoothie)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773053052/products/ddpwubysvnej06o3opzc.png").isPrimary(true)
                .build());
        ProductVariantEntity varStrawberrySmS = saveVariant(
                UUID.fromString("00000000-0000-0000-0007-100000000001"),
                strawberrySmoothie, "Small (S)", new BigDecimal("55000"));
        ProductVariantEntity varStrawberrySmM = saveVariant(
                UUID.fromString("00000000-0000-0000-0007-100000000002"),
                strawberrySmoothie, "Medium (M)", new BigDecimal("65000"));
        ProductVariantEntity varStrawberrySmL = saveVariant(
                UUID.fromString("00000000-0000-0000-0007-100000000003"),
                strawberrySmoothie, "Large (L)", new BigDecimal("75000"));
        linkIngredient(varStrawberrySmS, ingStrawberry, new BigDecimal("100"), "g");
        linkIngredient(varStrawberrySmS, ingOatMilk, new BigDecimal("100"), "ml");
        linkIngredient(varStrawberrySmM, ingStrawberry, new BigDecimal("150"), "g");
        linkIngredient(varStrawberrySmM, ingOatMilk, new BigDecimal("150"), "ml");
        linkIngredient(varStrawberrySmL, ingStrawberry, new BigDecimal("200"), "g");
        linkIngredient(varStrawberrySmL, ingOatMilk, new BigDecimal("200"), "ml");
        linkIngredient(varStrawberrySmL, ingSugarSyrup, new BigDecimal("10"), "ml");

// 10. Cold Brew
        ProductEntity coldBrew = productRepository.save(ProductEntity.builder()
                .id(PROD_COLD_BREW_ID).name("Cold Brew Coffee")
                .description(
                        "Slow-steeped for 18 hours in cold water. Smooth, mellow and naturally low - acid.Served over ice.")
                                .type(ProductType.COLD).active(true).category(catSmoothie).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_COLD_BREW_ID).product(coldBrew)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773053082/products/at2ltjfcrqpeqaylszm8.png").isPrimary(true)
                .build());
        varColdBrewS = saveVariant(VAR_COLD_BREW_S, coldBrew, "250ml (S)", new
                BigDecimal("55000"));
        varColdBrewM = saveVariant(VAR_COLD_BREW_M, coldBrew, "350ml (M)", new
                BigDecimal("65000"));
        linkIngredient(varColdBrewS, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varColdBrewS, ingIce, new BigDecimal("200"), "g");
        linkIngredient(varColdBrewM, ingEspresso, new BigDecimal("3"), "shot");
        linkIngredient(varColdBrewM, ingIce, new BigDecimal("280"), "g");

// 11. Caramel Macchiato
        ProductEntity caramelMacchiato = productRepository.save(ProductEntity.builder()
                .id(PROD_CARAMEL_MACCHIATO_ID).name("Caramel Macchiato")
                .description("Freshly steamed milk with vanilla-flavored syrup marked with espresso and topped with a caramel drizzle.")
                .type(ProductType.HOT_ICED).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_CARAMEL_MACCHIATO_ID).product(caramelMacchiato)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773326773/products/ecqe0srwwrjwqllhscmq.png").isPrimary(true)
                .build());
        varMacchiatoS = saveVariant(VAR_MACCHIATO_S, caramelMacchiato, "Small (S)", new BigDecimal("65000"));
        varMacchiatoM = saveVariant(VAR_MACCHIATO_M, caramelMacchiato, "Medium (M)", new BigDecimal("75000"));
        varMacchiatoL = saveVariant(VAR_MACCHIATO_L, caramelMacchiato, "Large (L)", new BigDecimal("85000"));
        linkIngredient(varMacchiatoS, ingEspresso, new BigDecimal("1"), "shot");
        linkIngredient(varMacchiatoS, ingMilk, new BigDecimal("150"), "ml");
        linkIngredient(varMacchiatoS, ingVanillaSyrup, new BigDecimal("10"), "ml");
        linkIngredient(varMacchiatoS, ingCaramelSauce, new BigDecimal("10"), "ml");
        linkIngredient(varMacchiatoM, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varMacchiatoL, ingEspresso, new BigDecimal("3"), "shot");

// 12. Caffè Mocha
        ProductEntity mocha = productRepository.save(ProductEntity.builder()
                .id(PROD_MOCHA_ID).name("Caffè Mocha")
                .description("Our rich, full-bodied espresso combined with bittersweet mocha sauce and steamed milk, then topped with sweetened whipped cream.")
                .type(ProductType.HOT_ICED).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_MOCHA_ID).product(mocha)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773326850/products/nibnxaw2nhaecmrspems.png").isPrimary(true)
                .build());
        varMochaS = saveVariant(VAR_MOCHA_S, mocha, "Small (S)", new BigDecimal("65000"));
        varMochaM = saveVariant(VAR_MOCHA_M, mocha, "Medium (M)", new BigDecimal("75000"));
        varMochaL = saveVariant(VAR_MOCHA_L, mocha, "Large (L)", new BigDecimal("85000"));
        linkIngredient(varMochaS, ingEspresso, new BigDecimal("1"), "shot");
        linkIngredient(varMochaS, ingMilk, new BigDecimal("150"), "ml");
        linkIngredient(varMochaS, ingChocolateSauce, new BigDecimal("20"), "ml");
        linkIngredient(varMochaS, ingWhippingCream, new BigDecimal("20"), "ml");

// 13. Flat White
        ProductEntity flatWhite = productRepository.save(ProductEntity.builder()
                .id(PROD_FLAT_WHITE_ID).name("Flat White")
                .description("Smooth ristretto shots of espresso and steamed whole milk, creating a balanced, coffee-forward cup.")
                .type(ProductType.HOT).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_FLAT_WHITE_ID).product(flatWhite)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773326895/products/cvkn0ycmx98o0xlk5us4.png").isPrimary(true)
                .build());
        varFlatWhiteS = saveVariant(VAR_FLAT_WHITE_S, flatWhite, "Small (S)", new BigDecimal("55000"));
        varFlatWhiteM = saveVariant(VAR_FLAT_WHITE_M, flatWhite, "Medium (M)", new BigDecimal("65000"));
        linkIngredient(varFlatWhiteS, ingEspresso, new BigDecimal("2"), "shot");
        linkIngredient(varFlatWhiteS, ingMilk, new BigDecimal("120"), "ml");

// 14. Hot Chocolate
        ProductEntity hotChoco = productRepository.save(ProductEntity.builder()
                .id(PROD_HOT_CHOCOLATE_ID).name("Classic Hot Chocolate")
                .description("Steamed milk with chocolate sauce and a touch of vanilla, topped with whipped cream.")
                .type(ProductType.HOT).active(true).category(catEspresso).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_HOT_CHOCOLATE_ID).product(hotChoco)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773326962/products/jmdzdohw3hbbzkrallwy.png").isPrimary(true)
                .build());
        varHotChocoS = saveVariant(VAR_HOT_CHOCO_S, hotChoco, "Small (S)", new BigDecimal("50000"));
        varHotChocoM = saveVariant(VAR_HOT_CHOCO_M, hotChoco, "Medium (M)", new BigDecimal("60000"));
        varHotChocoL = saveVariant(VAR_HOT_CHOCO_L, hotChoco, "Large (L)", new BigDecimal("70000"));
        linkIngredient(varHotChocoS, ingMilk, new BigDecimal("180"), "ml");
        linkIngredient(varHotChocoS, ingChocolateSauce, new BigDecimal("30"), "ml");
        linkIngredient(varHotChocoS, ingCocoaPowder, new BigDecimal("5"), "g");

// 15. Vanilla Frappuccino
        ProductEntity vanillaFrapp = productRepository.save(ProductEntity.builder()
                .id(PROD_VANILLA_FRAPP_ID).name("Vanilla Bean Frappuccino")
                .description("A creamy blend of vanilla bean, milk and ice topped with whipped cream.")
                .type(ProductType.COLD).active(true).category(catSmoothie).build());
        productImageRepository.save(ProductImageEntity.builder()
                .id(IMG_VANILLA_FRAPP_ID).product(vanillaFrapp)
                .imageUrl("https://res.cloudinary.com/dbi35fapj/image/upload/v1773327035/products/uaztqngojdpjmmrbnuo3.png").isPrimary(true)
                .build());
        varVanillaFrappS = saveVariant(VAR_VANILLA_FRAPP_S, vanillaFrapp, "Small (S)", new BigDecimal("65000"));
        varVanillaFrappM = saveVariant(VAR_VANILLA_FRAPP_M, vanillaFrapp, "Medium (M)", new BigDecimal("75000"));
        varVanillaFrappL = saveVariant(VAR_VANILLA_FRAPP_L, vanillaFrapp, "Large (L)", new BigDecimal("85000"));
        linkIngredient(varVanillaFrappS, ingMilk, new BigDecimal("150"), "ml");
        linkIngredient(varVanillaFrappS, ingVanillaSyrup, new BigDecimal("20"), "ml");
        linkIngredient(varVanillaFrappS, ingIce, new BigDecimal("200"), "g");
        linkIngredient(varVanillaFrappS, ingWhippingCream, new BigDecimal("30"), "ml");

        log.info("[DataInit] Products seeded: 15 products, with variants, images & ingredients");
    }

    private ProductVariantEntity saveVariant(UUID id, ProductEntity product,
                                             String sizeName, BigDecimal price) {
        return productVariantRepository.save(ProductVariantEntity.builder()
                .id(id)
                .product(product)
                .sizeName(sizeName)
                .price(price)
                .active(true)
                .build());
    }

    private void linkIngredient(ProductVariantEntity variant, IngredientEntity ingredient,
                                BigDecimal quantity, String unit) {
        productVariantIngredientRepository.save(ProductVariantIngredientEntity.builder()
                .variant(variant)
                .ingredient(ingredient)
                .quantity(quantity)
                .unit(unit)
                .build());
    }

    // ─── 6. ORDERS + ORDER ITEMS + PAYMENTS + TRANSACTIONS

    private void seedOrders() {
// ── Order 1: PENDING by Customer 1 ─────────────────────────────────────
        OrderEntity order1 = orderRepository.save(OrderEntity.builder()
                .id(ORDER_1_ID)
                .customerId(ACC_CUSTOMER_1_ID)
                .status(OrderStatus.PENDING)
                .orderType("ONLINE")
                .orderNumber("ORD-2026-0001")
                .deliveryAddress("123 Nguyen Hue Blvd, District 1, HCMC")
                .storeId(STORE_1_ID)
                .orderTime(LocalDateTime.of(2026, 2, 28, 9, 0))
                .totalAmount(new BigDecimal("100000"))
                .build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order1).productVariant(varLatteM)
                .quantity(1).unitPrice(new BigDecimal("65000")).build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order1).productVariant(varEspressoS)
                .quantity(1).unitPrice(new BigDecimal("35000")).build());
// No payment for PENDING order

// ── Order 2: PAID by Customer 2 — VNPay ────────────────────────────────
        OrderEntity order2 = orderRepository.save(OrderEntity.builder()
                .id(ORDER_2_ID)
                .customerId(ACC_CUSTOMER_2_ID)
                .status(OrderStatus.PAID)
                .orderType("ONLINE")
                .orderNumber("ORD-2026-0002")
                .deliveryAddress("456 Le Loi St, District 1, HCMC")
                .storeId(STORE_1_ID)
                .orderTime(LocalDateTime.of(2026, 2, 28, 10, 15))
                .totalAmount(new BigDecimal("120000"))
                .build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order2).productVariant(varMatchaM)
                .quantity(1).unitPrice(new BigDecimal("75000")).build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order2).productVariant(varEspressoM)
                .quantity(1).unitPrice(new BigDecimal("45000")).build());
// Payment 1 — PAID via VNPay
        PaymentEntity pay1 = paymentRepository.save(PaymentEntity.builder()
                .id(PAY_1_ID)
                .order(order2)
                .paymentType(PaymentType.PRODUCT)
                .transactionId("VNP20260228-00120001")
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PAID)
                .amountPaid(new BigDecimal("120000"))
                .paymentDate(LocalDateTime.parse("2026-02-28T03:16:00"))
                .vnpResponseCode("00")
                .vnpBankCode("NCB")
                .build());
        transactionRepository.save(TransactionEntity.builder()
                .id(TXN_1_ID)
                .payment(pay1)
                .vnpTxnRef("ORD-2026-0002")
                .vnpTransactionNo("14523876")
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal("120000"))
                .build());

// ── Order 3: PREPARING by Customer 3 — MOMO ────────────────────────────
        OrderEntity order3 = orderRepository.save(OrderEntity.builder()
                .id(ORDER_3_ID)
                .customerId(ACC_CUSTOMER_3_ID)
                .status(OrderStatus.PREPARING)
                .orderType("DINE_IN")
                .orderNumber("ORD-2026-0003")
                .storeId(STORE_2_ID)
                .orderTime(LocalDateTime.of(2026, 2, 28, 11, 30))
                .totalAmount(new BigDecimal("195000"))
                .build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order3).productVariant(varCappuccinoL)
                .quantity(1).unitPrice(new BigDecimal("75000")).build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order3).productVariant(varChaiL)
                .quantity(1).unitPrice(new BigDecimal("75000")).build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order3).productVariant(varAmericanoS)
                .quantity(1).unitPrice(new BigDecimal("45000")).build());
// Payment 2 — PAID via MOMO
        PaymentEntity pay2 = paymentRepository.save(PaymentEntity.builder()
                .id(PAY_2_ID)
                .order(order3)
                .paymentType(PaymentType.PRODUCT)
                .transactionId("MOMO20260228-00195003")
                .paymentMethod(PaymentMethod.MOMO)
                .status(PaymentStatus.PAID)
                .amountPaid(new BigDecimal("195000"))
                .paymentDate(LocalDateTime.parse("2026-02-28T04:31:00"))
                .vnpResponseCode("0")
                .vnpBankCode("MOMO")
                .build());
        transactionRepository.save(TransactionEntity.builder()
                .id(TXN_2_ID)
                .payment(pay2)
                .vnpTxnRef("ORD-2026-0003")
                .vnpTransactionNo("MOMO-95471234")
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal("195000"))
                .build());

// ── Order 4: COMPLETED by Customer 4 — CASH ────────────────────────────
        OrderEntity order4 = orderRepository.save(OrderEntity.builder()
                .id(ORDER_4_ID)
                .customerId(ACC_CUSTOMER_4_ID)
                .status(OrderStatus.COMPLETED)
                .orderType("DINE_IN")
                .orderNumber("ORD-2026-0004")
                .storeId(STORE_1_ID)
                .orderTime(LocalDateTime.of(2026, 2, 27, 14, 0))
                .totalAmount(new BigDecimal("120000"))
                .build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order4).productVariant(varBananaSmM)
                .quantity(1).unitPrice(new BigDecimal("65000")).build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order4).productVariant(varColdBrewS)
                .quantity(1).unitPrice(new BigDecimal("55000")).build());
// Payment 3 — PAID via CASH
        PaymentEntity pay3 = paymentRepository.save(PaymentEntity.builder()
                .id(PAY_3_ID)
                .order(order4)
                .paymentMethod(PaymentMethod.CASH)
                .paymentType(PaymentType.PRODUCT)
                .status(PaymentStatus.PAID)
                .amountPaid(new BigDecimal("120000"))
                .paymentDate(LocalDateTime.parse("2026-02-27T07:05:00"))
                .vnpResponseCode("00")
                .vnpBankCode("CASH")
                .build());
        transactionRepository.save(TransactionEntity.builder()
                .id(TXN_3_ID)
                .payment(pay3)
                .vnpTxnRef("ORD-2026-0004")
                .vnpTransactionNo("CASH-LOCAL-0004")
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal("120000"))
                .build());

// ── Order 5: CANCELED by Customer 1 (was PAID, now canceled) ───────────
        OrderEntity order5 = orderRepository.save(OrderEntity.builder()
                .id(ORDER_5_ID)
                .customerId(ACC_CUSTOMER_1_ID)
                .status(OrderStatus.CANCELED)
                .orderType("ONLINE")
                .orderNumber("ORD-2026-0005")
                .deliveryAddress("123 Nguyen Hue Blvd, District 1, HCMC")
                .storeId(STORE_2_ID)
                .orderTime(LocalDateTime.of(2026, 2, 26, 16, 45))
                .totalAmount(new BigDecimal("65000"))
                .build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order5).productVariant(varEarlGreyM)
                .quantity(1).unitPrice(new BigDecimal("65000")).build());
// Payment 4 — REFUNDED
        paymentRepository.save(PaymentEntity.builder()
                .id(PAY_4_ID)
                .order(order5)
                .transactionId("VNP20260226-00065005")
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.REFUNDED)
                .amountPaid(new BigDecimal("65000"))
                .paymentDate(LocalDateTime.parse("2026-02-26T09:46:00"))
                .paymentType(PaymentType.PRODUCT)
                .build());

// ── Order 6: READY by Customer 2 — PAYOS ─────────────────────────────
        OrderEntity order6 = orderRepository.save(OrderEntity.builder()
                .id(ORDER_6_ID)
                .customerId(ACC_CUSTOMER_2_ID)
                .status(OrderStatus.READY)
                .orderType("TAKE_AWAY")
                .orderNumber("ORD-2026-0006")
                .storeId(STORE_1_ID)
                .orderTime(LocalDateTime.of(2026, 3, 1, 8, 0))
                .totalAmount(new BigDecimal("130000"))
                .build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order6).productVariant(varColdBrewM)
                .quantity(1).unitPrice(new BigDecimal("65000")).build());
        orderItemRepository.save(OrderItemEntity.builder()
                .order(order6).productVariant(varMatchaS)
                .quantity(1).unitPrice(new BigDecimal("65000")).build());
// Payment 5 — PAID via PAYOS
        paymentRepository.save(PaymentEntity.builder()
                .id(PAY_5_ID)
                .order(order6)
                .transactionId("PAYOS20260301-00140006")
                .paymentMethod(PaymentMethod.PAYOS)
                .status(PaymentStatus.PAID)
                .amountPaid(new BigDecimal("130000"))
                .paymentDate(LocalDateTime.parse("2026-03-01T01:01:00"))
                .paymentType(PaymentType.PRODUCT)
                .build());

// ─── REFUND DATA ──────────────────────────────────────────────────────────
// Refund 1: PENDING - Customer 2 wants refund for Order 2 (PAID)
        refundRepository.save(RefundEntity.builder()
                .id(REFUND_1_ID)
                .order(order2)
                .payment(pay1)
                .amount(new BigDecimal("75000"))
                .reason("Sản phẩm không đúng như mô tả")
                .status(RefundStatus.PENDING)
                .build());

// Refund 2: PENDING - Customer 3 wants refund for Order 3 (PREPARING)
        refundRepository.save(RefundEntity.builder()
                .id(REFUND_2_ID)
                .order(order3)
                .payment(pay2)
                .amount(new BigDecimal("195000"))
                .reason("Đổi ý không muốn mua nữa")
                .status(RefundStatus.PENDING)
                .build());

// Refund 3: APPROVED - Customer 4's refund for Order 4 (COMPLETED) was
        refundRepository.save(RefundEntity.builder()
                .id(REFUND_3_ID)
                .order(order4)
                .payment(pay3)
                .amount(new BigDecimal("120000"))
                .reason("Chất lượng sản phẩm không tốt")
                .status(RefundStatus.APPROVED)
                .build());

        log.info(
                "[DataInit] Orders seeded: 6 orders (PENDING / PAID / PREPARING / READY / COMPLETED / CANCELED), 5 payments, 3 transactions, 3 refunds(2PENDING, 1APPROVED) ");
    }
}

