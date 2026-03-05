// ── Locations ────────────────────────────────────────────
export const LOCATIONS = [
    { label: 'URI Palace', href: '/stores/uri-palace' },
    { label: 'Bac Ninh', href: '/stores/bac-ninh' },
    { label: 'Hanoi', href: '/stores/hanoi' },
    { label: 'Saigon', href: '/stores/saigon' },
];

// ── Overlay Nav Links ─────────────────────────────────────
export const OVERLAY_PRIMARY_LINKS = [
    { label: 'Visit Us', href: '/stores' },
    { label: 'Menus', href: '/menu' },
    { label: 'About', href: '/about' },
];

export const OVERLAY_PAGE_LINKS = [
    { label: 'Home', href: '#' },
    { label: 'Atmosphere', href: '#atmosphere' },
    { label: 'Our Narrative', href: '#narrative' },
    { label: 'The Process', href: '#process' },
    { label: 'The Catalog', href: '#coffees' },
    { label: 'Visit Us', href: '#visit' },
];

// ── Products ──────────────────────────────────────────────
export const PRODUCTS = [
    {
        id: '1',
        productName: 'Espresso',
        productDescription: 'Rich, bold espresso shot.',
        archiveName: 'E-Coffee Archive',
        category: 'Coffee',
        price: 3.5,
        imageUrl: 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=400&q=60',
        detailHref: '/menu/1',
    },
    {
        id: '4',
        productName: 'Latte',
        productDescription: 'Smooth espresso with steamed milk.',
        archiveName: 'E-Coffee Archive',
        category: 'Coffee',
        price: 4.8,
        imageUrl: 'https://images.unsplash.com/photo-1561882468-9110d70d2f09?auto=format&fit=crop&w=400&q=60',
        detailHref: '/menu/4',
    },
    {
        id: '6',
        productName: 'Cold Brew',
        productDescription: 'Slow-steeped coffee served cold.',
        archiveName: 'E-Coffee Archive',
        category: 'Cold',
        price: 5.5,
        imageUrl: 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=400&q=60',
        detailHref: '/menu/6',
    },
];

// ── Process Steps ─────────────────────────────────────────
export const PROCESS_STEPS = [
    {
        numeral: 'I.',
        title: 'Selection',
        desc: 'Architectural screening of high-altitude Arabica & Robusta beans.',
    },
    {
        numeral: 'II.',
        title: 'Extraction',
        desc: 'Precision brewing at exact temperatures for visceral depth.',
    },
    {
        numeral: 'III.',
        title: 'Calibrage',
        desc: 'Manual balancing of crema density and palate weight.',
    },
];

// ── Atmosphere Images ─────────────────────────────────────
export const ATMOSPHERE_IMAGES = [
    {
        src: 'https://images.unsplash.com/photo-1453614512568-c4024d13c247?auto=format&fit=crop&w=600&q=60',
        alt: 'Venue 1',
    },
    {
        src: 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=600&q=60',
        alt: 'Venue 2',
    },
    {
        src: 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=60',
        alt: 'Venue 3',
    },
];

// ── Footer ────────────────────────────────────────────────
export const FOOTER_LINKS = {
    Discovery: [
        { label: 'The Venue', href: '/stores' },
        { label: 'Menus', href: '/menu' },
        { label: 'About E-Coffee', href: '/about' },
    ],
    'The Venue': [
        { label: 'URI Palace • Bac Ninh', href: '/stores' },
        { label: 'Hanoi Gallery', href: '/stores' },
        { label: 'Saigon Atelier', href: '/stores' },
    ],
    Support: [
        { label: 'Contact', href: '/contact' },
        { label: 'Security', href: '#' },
        { label: 'Legal', href: '#' },
    ],
};
