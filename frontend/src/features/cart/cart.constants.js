// ── Mock Products (from backend DataInit.java seed data)
// Shape matches ProductEntity: { id, productName, productDescription, imageUrl }
// Price added as frontend mock until backend adds it

export const MOCK_PRODUCTS = [
    {
        id: '1',
        productName: 'Espresso',
        productDescription: 'Rich, bold espresso shot.',
        imageUrl: 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=400&q=60',
        price: 3.5,
        category: 'Coffee',
    },
    {
        id: '2',
        productName: 'Americano',
        productDescription: 'Espresso diluted with hot water.',
        imageUrl: 'https://images.unsplash.com/photo-1521302080334-4bebac2763a6?auto=format&fit=crop&w=400&q=60',
        price: 3.8,
        category: 'Coffee',
    },
    {
        id: '3',
        productName: 'Cappuccino',
        productDescription: 'Espresso with steamed milk and foam.',
        imageUrl: 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?auto=format&fit=crop&w=400&q=60',
        price: 4.5,
        category: 'Coffee',
    },
    {
        id: '4',
        productName: 'Latte',
        productDescription: 'Smooth espresso with steamed milk.',
        imageUrl: 'https://images.unsplash.com/photo-1561882468-9110d70d2f09?auto=format&fit=crop&w=400&q=60',
        price: 4.8,
        category: 'Coffee',
    },
    {
        id: '5',
        productName: 'Mocha',
        productDescription: 'Chocolate-infused latte with espresso.',
        imageUrl: 'https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?auto=format&fit=crop&w=400&q=60',
        price: 5.2,
        category: 'Coffee',
    },
    {
        id: '6',
        productName: 'Cold Brew',
        productDescription: 'Slow-steeped coffee served cold.',
        imageUrl: 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=400&q=60',
        price: 5.5,
        category: 'Cold',
    },
    {
        id: '7',
        productName: 'Iced Latte',
        productDescription: 'Chilled latte over ice.',
        imageUrl: 'https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=400&q=60',
        price: 5.0,
        category: 'Cold',
    },
    {
        id: '8',
        productName: 'Flat White',
        productDescription: 'Velvety microfoam over espresso.',
        imageUrl: 'https://images.unsplash.com/photo-1534040385115-33dcb3acba5b?auto=format&fit=crop&w=400&q=60',
        price: 4.7,
        category: 'Coffee',
    },
];

export const CART_SESSION_KEY = 'ecoffee_cart_v2';
