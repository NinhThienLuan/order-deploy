import atmosphere_1 from '@/assets/images/atmosphere/atmosphere_1.png';
import atmosphere_2 from '@/assets/images/atmosphere/atmosphere_2.png';
import atmosphere_4 from '@/assets/images/atmosphere/atmosphere_4.png';

export const LOCATIONS = [
    { label: 'URI PALACE • BAC NINH', href: '/stores/bac-ninh' },
    { label: 'HANOI GALLERY', href: '/stores/hanoi' },
    { label: 'SAIGON ATELIER', href: '/stores/saigon' },
    { label: 'DA NANG STUDIO', href: '/stores/danang' },
];

export const ATMOSPHERE_IMAGES = [
    {
        src: atmosphere_1,
        alt: 'Atmosphere 1'
    },
    {
        src: atmosphere_2,
        alt: 'Atmosphere 2'
    },
    {
        src: atmosphere_4,
        alt: 'Atmosphere 4'
    }
];

export const PROCESS_STEPS = [
    {
        numeral: '01',
        title: 'Curation',
        desc: 'Sourcing the finest Arabica and Robusta beans from the highlands of Central Vietnam.'
    },
    {
        numeral: '02',
        title: 'Roasting',
        desc: 'Small-batch artisanal roasting to highlight the unique flavor profile of each harvest.'
    },
    {
        numeral: '03',
        title: 'Brewing',
        desc: 'Meticulous extraction methods, from traditional Phin to modern espresso techniques.'
    }
];

export const FOOTER_LINKS = {
    'The Archive': [
        { label: 'Narrative', href: '#narrative' },
        { label: 'Atmosphere', href: '#atmosphere' },
        { label: 'Process', href: '#process' },
        { label: 'Locations', href: '/stores' }
    ],
    'Experience': [
        { label: 'Menu', href: '/menu' },
        { label: 'Order Online', href: '/menu' },
        { label: 'Reservations', href: '#visit' }
    ],
    'Collectives': [
        { label: 'Membership', href: '/login' },
        { label: 'Franchise', href: '/contact' },
        { label: 'Careers', href: '/careers' }
    ]
};

export const OVERLAY_PRIMARY_LINKS = [
    { label: 'THE MENU', href: '/menu' },
    { label: 'THE STORES', href: '/stores' },
    { label: 'THE EXPERIENCE', href: '#narrative' },
    { label: 'THE ARCHIVE', href: '#atmosphere' },
];

export const OVERLAY_PAGE_LINKS = [
    { label: 'Order Online', href: '/menu' },
    { label: 'Membership', href: '/login' },
    { label: 'Franchise', href: '/contact' },
    { label: 'Careers', href: '/careers' },
    { label: 'About Us', href: '#narrative' },
    { label: 'Legal & Privacy', href: '/privacy' },
];
