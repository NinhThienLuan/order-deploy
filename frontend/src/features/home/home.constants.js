// src/features/home/home.constants.js

// Import images from assets (Vite will handle the paths)
import prodEspresso from '@/assets/products/prod_espresso.png';
import prodAmericano from '@/assets/products/prod_americano.png';
import prodMacchiato from '@/assets/products/prod_macchiato.png';
import prodCappuccino from '@/assets/products/prod_cappuccino.png';
import prodFlatWhite from '@/assets/products/prod_flat_white.png';
import prodLatte from '@/assets/products/prod_latte.png';
import prodColdBrew from '@/assets/products/prod_cold_brew.png';
import prodMocha from '@/assets/products/prod_mocha.png';
import prodChaiLatte from '@/assets/products/prod_chai_latte.png';
import prodHotChocolate from '@/assets/products/prod_hot_chocolate.png';
import prodVanillaFrappuccino from '@/assets/products/prod_vanilla_frappuccino.png';

// Fallback images from Unsplash for missing ones or generated ones mentioned in prototype
const UNSPLASH_IMAGES = {
    icedLatte: 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=800&q=80',
    caramelFrapp: 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=800&q=80',
    matchaLatte: 'https://images.unsplash.com/photo-1515823064-d6e0c04616a7?auto=format&fit=crop&w=800&q=80',
    houseBlend: 'https://images.unsplash.com/photo-1559525839-b184a4d698c7?auto=format&fit=crop&w=600&q=80',
    decafBeans: 'https://images.unsplash.com/photo-1587734195503-904fca47e0e9?auto=format&fit=crop&w=600&q=80',
    coffeeBeans: 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80',
    singleOrigin: 'https://images.unsplash.com/photo-1611162458324-aae1eb4129a4?auto=format&fit=crop&w=600&q=80',
    milkFrother: 'https://images.unsplash.com/photo-1582216503923-a8eebeb54452?auto=format&fit=crop&w=600&q=80',
    espressoMachine: 'https://images.unsplash.com/photo-1520218508822-998633d99765?auto=format&fit=crop&w=600&q=80',
    // Seasonal
    pumpkinSpice: 'https://images.unsplash.com/photo-1508361727343-ca787442dcd7?auto=format&fit=crop&w=800&q=80',
    cherryBlossom: 'https://images.unsplash.com/photo-1522992319-0365e5f11656?auto=format&fit=crop&w=800&q=80',
    hotButterRum: 'https://images.unsplash.com/photo-1512568400610-62da28bc8a13?auto=format&fit=crop&w=800&q=80',
    mintMocha: 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=800&q=80',
    lavenderLatte: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80',
    eggnog: 'https://images.unsplash.com/photo-1481182894116-8f0718da52b2?auto=format&fit=crop&w=800&q=80',
    // Food & Pastries
    croissant: 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=800&q=80',
    blueberryMuffin: 'https://images.unsplash.com/photo-1607958996333-41aef7caefaa?auto=format&fit=crop&w=800&q=80',
    cinnamonRoll: 'https://images.unsplash.com/photo-1509365390695-33aee754301f?auto=format&fit=crop&w=800&q=80',
    avocadoToast: 'https://images.unsplash.com/photo-1541519227354-08fa5d50c820?auto=format&fit=crop&w=800&q=80',
    cheeseScone: 'https://images.unsplash.com/photo-1568254183919-78a4f43a2877?auto=format&fit=crop&w=800&q=80',
    brownie: 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=800&q=80',
    // Cold & Iced
    icedAmericano: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=800&q=80',
    icedCappuccino: 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80',
    nitroColdBrew: 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=800&q=80',
    icedMatcha: 'https://images.unsplash.com/photo-1515823064-d6e0c04616a7?auto=format&fit=crop&w=800&q=80',
    icedChaiLatte: 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?auto=format&fit=crop&w=800&q=80',
    lemonade: 'https://images.unsplash.com/photo-1523677011781-c91d1bbe2f9e?auto=format&fit=crop&w=800&q=80',
};

export const CATEGORIES = [
    { id: 'cat-01', name: 'Espresso & Classics' },
    { id: 'cat-02', name: 'Frappuccino / Blended' },
    { id: 'cat-03', name: 'Tea & Non-Coffee' },
    { id: 'cat-04', name: 'Retail & Equipment' },
    { id: 'cat-05', name: 'Seasonal Specials' },
    { id: 'cat-06', name: 'Food & Pastries' },
    { id: 'cat-07', name: 'Cold & Iced' },
];

export const PRODUCTS = [
    {
        id: 'p-01',
        name: 'Espresso',
        description: 'A single concentrated shot pulled at high pressure through finely-ground, dark-roasted Arabica beans. Expect a dense, syrupy body with notes of dark chocolate and a golden crema that lingers.',
        price: 2.50,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodEspresso
    },
    {
        id: 'p-02',
        name: 'Americano',
        description: 'A double espresso stretched with hot water to a smooth, full-cup length drink. The dilution softens bitterness and reveals subtle caramel and toasted-grain sweetness without losing depth.',
        price: 3.00,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodAmericano
    },
    {
        id: 'p-03',
        name: 'Macchiato',
        description: 'Espresso boldly "stained" with a small crown of microfoam. The contrast between intense shot and silken milk creates a layered, rounded cup — bitter up front, creamy on the finish.',
        price: 3.25,
        status: 'Available',
        categoryId: 'cat-01',
        badge: 'Must try',
        image: prodMacchiato
    },
    {
        id: 'p-04',
        name: 'Cappuccino',
        description: 'Equal thirds of espresso, steamed milk and thick dry foam. The airy milk cap insulates heat and delivers a classic bittersweet harmony prized in Italian coffee culture for over a century.',
        price: 3.50,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodCappuccino
    },
    {
        id: 'p-05',
        name: 'Flat White',
        description: 'Ristretto-based espresso poured into a small tulip of velvety microfoam. Short ratio and fine-textured milk produce a more intense, silkier drink than a latte — beloved across Australasia.',
        price: 3.60,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodFlatWhite
    },
    {
        id: 'p-06',
        name: 'Latte',
        description: 'Double espresso crowned with a generous pour of steamed whole milk and a thin layer of foam. Mild, milky and comforting — the ideal canvas for seasonal syrups or simple morning ritual.',
        price: 3.75,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodLatte
    },
    {
        id: 'p-07',
        name: 'Cold Brew',
        description: 'Coarse-ground coffee steeped in cold filtered water for 18 hours, then pressed slowly. The result is a naturally sweet, low-acid concentrate served over ice — smooth and deeply caffeinated.',
        price: 3.80,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodColdBrew
    },
    {
        id: 'p-08',
        name: 'Iced Latte',
        description: 'Freshly pulled espresso poured over a full glass of ice and chilled milk. Fast to cool, slow to dilute — a refreshing all-day drink with the same espresso depth as its hot counterpart.',
        price: 3.90,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: UNSPLASH_IMAGES.icedLatte
    },
    {
        id: 'p-09',
        name: 'Mocha',
        description: 'Espresso blended with rich dark-chocolate sauce, then finished with silky steamed milk. An indulgent meeting of coffee and cocoa that satisfies caffeine cravings and sweet tooth in a single cup.',
        price: 4.00,
        status: 'Available',
        categoryId: 'cat-01',
        badge: '',
        image: prodMocha
    },
    {
        id: 'p-10',
        name: 'Caramel Frappuccino',
        description: 'Double espresso blended with ice, whole milk and buttery caramel syrup to a thick, frosty consistency. Finished with whipped cream and a caramel drizzle — our perennial crowd favourite.',
        price: 4.50,
        status: 'Available',
        categoryId: 'cat-02',
        badge: 'Best seller',
        image: UNSPLASH_IMAGES.caramelFrapp
    },
    {
        id: 'p-11',
        name: 'Vanilla Bean Frappuccino',
        description: 'Ground pure vanilla beans blended with ice and milk into a lush, cream-forward frozen drink. No espresso — just delicate, natural vanilla sweetness in a smooth, chilled format.',
        price: 4.75,
        status: 'Available',
        categoryId: 'cat-02',
        badge: '',
        image: prodVanillaFrappuccino
    },
    {
        id: 'p-12',
        name: 'Hot Chocolate',
        description: 'Premium Belgian cocoa dissolved in hot steamed milk, topped with a pillow of foam. Rich, warming and entirely caffeine-free — a timeless comfort in a cup for all ages.',
        price: 3.50,
        status: 'Available',
        categoryId: 'cat-03',
        badge: '',
        image: prodHotChocolate
    },
    {
        id: 'p-13',
        name: 'Chai Tea Latte',
        description: 'Concentrated black tea infused with cinnamon, cardamom, ginger and star anise, then steamed with whole milk. Warming, aromatic and gently spiced — a South-Asian-inspired house favourite.',
        price: 3.95,
        status: 'Available',
        categoryId: 'cat-03',
        badge: '',
        image: prodChaiLatte
    },
    {
        id: 'p-14',
        name: 'Matcha Latte',
        description: 'Ceremonial-grade Japanese matcha whisked into a smooth paste, then finished with steamed oat milk. Earthy, gently sweet and vibrant green — a mindful alternative to espresso.',
        price: 4.25,
        status: 'Available',
        categoryId: 'cat-03',
        badge: 'Must try',
        image: UNSPLASH_IMAGES.matchaLatte
    },
    {
        id: 'p-16',
        name: 'House Blend',
        description: 'Our signature blend of high-altitude Ethiopian Yirgacheffe and Colombian Huila beans, roasted in-house to a balanced medium profile. Bright stone-fruit acidity with a honey-sweet finish.',
        price: 10.50,
        status: 'Available',
        categoryId: 'cat-04',
        badge: '',
        image: UNSPLASH_IMAGES.houseBlend
    },
    {
        id: 'p-17',
        name: 'Decaf Beans',
        description: 'Swiss Water Process decaffeinated beans sourced from a single Honduran co-operative. Full-bodied with notes of dark cherry and brown sugar — all the flavour, none of the caffeine.',
        price: 11.00,
        status: 'Available',
        categoryId: 'cat-04',
        badge: '',
        image: UNSPLASH_IMAGES.decafBeans
    },
    {
        id: 'p-18',
        name: 'Coffee Beans',
        description: 'Freshly roasted whole beans sourced from single-origin farms across three continents. Available in light, medium and dark roast — grind to order or take home for your own brewing ritual.',
        price: 12.50,
        status: 'Available',
        categoryId: 'cat-04',
        badge: '',
        image: UNSPLASH_IMAGES.coffeeBeans
    },
    {
        id: 'p-19',
        name: 'Single-Origin Beans',
        description: 'A micro-lot of hand-picked Geisha beans from a family-run Panamá farm at 1,800 m elevation. Extremely limited harvest with jasmine florals, peach sweetness and a clean, lingering finish.',
        price: 15.00,
        status: 'OutOfStock',
        categoryId: 'cat-04',
        badge: '',
        image: UNSPLASH_IMAGES.singleOrigin
    },
    {
        id: 'p-20',
        name: 'Milk Frother',
        description: 'Handheld induction frother with variable speed settings and a food-grade stainless-steel whisk. Produces café-quality microfoam in under 20 seconds — compact enough for any countertop.',
        price: 35.00,
        status: 'Available',
        categoryId: 'cat-04',
        badge: '',
        image: UNSPLASH_IMAGES.milkFrother
    },
    {
        id: 'p-21',
        name: 'Espresso Machine',
        description: 'Semi-automatic 15-bar pump machine with dual boilers and a commercial-grade steam wand. Programmable pre-infusion and shot timer — everything you need to pull barista-level espresso at home.',
        price: 249.00,
        status: 'Available',
        categoryId: 'cat-04',
        badge: '',
        image: UNSPLASH_IMAGES.espressoMachine
    },

    // ── Seasonal Specials ──────────────────────────────
    {
        id: 'p-22',
        name: 'Pumpkin Spice Latte',
        description: 'Espresso with real pumpkin purée, cinnamon, nutmeg and clove, finished with steamed milk and whipped cream. Available each autumn — our most anticipated seasonal return of the year.',
        price: 5.25,
        status: 'Available',
        categoryId: 'cat-05',
        badge: 'Limited',
        image: UNSPLASH_IMAGES.pumpkinSpice
    },
    {
        id: 'p-23',
        name: 'Cherry Blossom Latte',
        description: 'A spring-only creation of espresso, house-made sakura syrup and oat milk, dusted with dried rose petals. Delicate, floral and fleeting — as brief as the blossoms themselves.',
        price: 5.50,
        status: 'Available',
        categoryId: 'cat-05',
        badge: 'New',
        image: UNSPLASH_IMAGES.cherryBlossom
    },
    {
        id: 'p-24',
        name: 'Hot Butter Rum',
        description: 'Warm spiced cider blended with brown-sugar butter, rum extract and a cinnamon stick. Rich, aromatic and entirely non-alcoholic — winter comfort distilled into a single ceramic mug.',
        price: 4.75,
        status: 'Available',
        categoryId: 'cat-05',
        badge: '',
        image: UNSPLASH_IMAGES.hotButterRum
    },
    {
        id: 'p-25',
        name: 'Peppermint Mocha',
        description: 'Dark espresso, bittersweet chocolate and cool peppermint syrup steamed together with whole milk. A festive flavour built on sharp contrasts — a holiday staple since our founding year.',
        price: 5.00,
        status: 'Available',
        categoryId: 'cat-05',
        badge: 'Best seller',
        image: UNSPLASH_IMAGES.mintMocha
    },
    {
        id: 'p-26',
        name: 'Lavender Honey Latte',
        description: 'Cold-infused lavender simple syrup and raw wildflower honey swirled into a double espresso, finished with steamed milk. Subtly floral, naturally sweet and impossibly calming.',
        price: 5.25,
        status: 'Available',
        categoryId: 'cat-05',
        badge: '',
        image: UNSPLASH_IMAGES.lavenderLatte
    },
    {
        id: 'p-27',
        name: 'Eggnog Latte',
        description: 'Seasonal eggnog steamed with a double espresso and dusted with fresh nutmeg. Creamy, rich and indulgent — a December tradition that sells out every year without exception.',
        price: 5.50,
        status: 'OutOfStock',
        categoryId: 'cat-05',
        badge: '',
        image: UNSPLASH_IMAGES.eggnog
    },

    // ── Food & Pastries ────────────────────────────────
    {
        id: 'p-28',
        name: 'Butter Croissant',
        description: 'Laminated with 27 layers of Normandy butter and slow-proofed overnight. Baked fresh each morning for maximum flakiness — best enjoyed still warm with a double espresso on the side.',
        price: 3.50,
        status: 'Available',
        categoryId: 'cat-06',
        badge: '',
        image: UNSPLASH_IMAGES.croissant
    },
    {
        id: 'p-29',
        name: 'Blueberry Muffin',
        description: 'Plump wild blueberries folded into a buttermilk batter, baked to a domed golden top with a crunchy turbinado sugar crust. Dense, moist and bursting with tart fruit in every bite.',
        price: 3.25,
        status: 'Available',
        categoryId: 'cat-06',
        badge: 'Must try',
        image: UNSPLASH_IMAGES.blueberryMuffin
    },
    {
        id: 'p-30',
        name: 'Cinnamon Roll',
        description: 'Hand-rolled brioche dough with a dark brown-sugar and Ceylon cinnamon filling, glazed straight from the oven with vanilla cream-cheese frosting. Sticky, pillowy and deeply caramelised.',
        price: 4.00,
        status: 'Available',
        categoryId: 'cat-06',
        badge: 'Best seller',
        image: UNSPLASH_IMAGES.cinnamonRoll
    },
    {
        id: 'p-31',
        name: 'Avocado Toast',
        description: 'Thick-cut sourdough grilled on the flat-top, spread with smashed Hass avocado, sea salt flakes, chilli flakes and a squeeze of lime. Simple, seasonal and endlessly satisfying.',
        price: 6.50,
        status: 'Available',
        categoryId: 'cat-06',
        badge: '',
        image: UNSPLASH_IMAGES.avocadoToast
    },
    {
        id: 'p-32',
        name: 'Cheese Scone',
        description: 'Traditional English-style scone enriched with aged sharp cheddar and a touch of mustard powder. Light crumb, crisp shell — best split warm with a knob of salted butter.',
        price: 3.75,
        status: 'Available',
        categoryId: 'cat-06',
        badge: '',
        image: UNSPLASH_IMAGES.cheeseScone
    },
    {
        id: 'p-33',
        name: 'Dark Chocolate Brownie',
        description: '70% single-origin dark chocolate and brown butter baked into a fudgy, dense square with a crinkle-top crust. Intensely chocolatey and barely sweet — an unapologetically grown-up treat.',
        price: 3.50,
        status: 'Available',
        categoryId: 'cat-06',
        badge: '',
        image: UNSPLASH_IMAGES.brownie
    },

    // ── Cold & Iced ─────────────────────────────────────
    {
        id: 'p-34',
        name: 'Iced Americano',
        description: 'Double espresso diluted with cold filtered water and poured over a tower of ice. Clean, bracing and unadorned — the purist choice when the temperature climbs.',
        price: 3.25,
        status: 'Available',
        categoryId: 'cat-07',
        badge: '',
        image: UNSPLASH_IMAGES.icedAmericano
    },
    {
        id: 'p-35',
        name: 'Iced Cappuccino',
        description: 'Espresso shaken over ice with a splash of cold milk, then topped with airy cold foam. The familiar cappuccino ratio reimagined for warm days — bold, textured and instantly refreshing.',
        price: 3.75,
        status: 'Available',
        categoryId: 'cat-07',
        badge: '',
        image: UNSPLASH_IMAGES.icedCappuccino
    },
    {
        id: 'p-36',
        name: 'Nitro Cold Brew',
        description: 'Our 18-hour cold brew charged with nitrogen through a pressurised tap. The micro-bubbles create a cascading, Guinness-like pour with a silky mouthfeel and natural sweetness — no milk required.',
        price: 4.50,
        status: 'Available',
        categoryId: 'cat-07',
        badge: 'Must try',
        image: UNSPLASH_IMAGES.nitroColdBrew
    },
    {
        id: 'p-37',
        name: 'Iced Matcha Latte',
        description: 'Ceremonial matcha whisked with a small amount of hot water to dissolve, then poured over ice and oat milk. Vibrant, grassy and gently sweet — an antioxidant-rich cold cup for any season.',
        price: 4.75,
        status: 'Available',
        categoryId: 'cat-07',
        badge: 'New',
        image: UNSPLASH_IMAGES.icedMatcha
    },
    {
        id: 'p-38',
        name: 'Iced Chai Latte',
        description: 'House-brewed masala chai concentrate shaken with ice and whole milk into a layered, spiced cold drink. Cinnamon warmth, cardamom brightness and a hint of black pepper — served tall.',
        price: 4.25,
        status: 'Available',
        categoryId: 'cat-07',
        badge: '',
        image: UNSPLASH_IMAGES.icedChaiLatte
    },
    {
        id: 'p-39',
        name: 'Sparkling Lemonade',
        description: 'Hand-squeezed Sicilian lemons, raw cane syrup and highly carbonated mineral water. Bright, tart and bone-dry — a sharp palate cleanser and the perfect coffee-free counterpart to any meal.',
        price: 3.50,
        status: 'Available',
        categoryId: 'cat-07',
        badge: '',
        image: UNSPLASH_IMAGES.lemonade
    },
];
