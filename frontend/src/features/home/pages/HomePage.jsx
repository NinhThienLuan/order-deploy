import React from 'react';
import styles from './HomePage.module.css';
import Navbar from '@/layouts/Navbar';
import HeroSection from '../components/HeroSection/HeroSection';
import ReserveCollage from '../components/ReserveCollage/ReserveCollage';
import ParallaxReveal from '../components/ParallaxReveal/ParallaxReveal';
import { useScrollEffects } from '../hooks/useScrollEffects';
import { useCategories } from '../hooks/useCategories';

import prodColdBrew from '@/assets/products/prod_cold_brew.png';
import prodMacchiato from '@/assets/products/prod_macchiato.png';
import prodLatte from '@/assets/products/prod_latte.png';
import prodEspresso from '@/assets/products/prod_espresso.png';
import prodVanillaFrappuccino from '@/assets/products/prod_vanilla_frappuccino.png';
import prodMocha from '@/assets/products/prod_mocha.png';
import prodCappuccino from '@/assets/products/prod_cappuccino.png';
import prodHotChocolate from '@/assets/products/prod_hot_chocolate.png';
import prodChaiLatte from '@/assets/products/prod_chai_latte.png';

const categoryHref = (id) => id ? `/menu?category=${id}` : '/menu';

const HomePage = () => {
    useScrollEffects();
    const { getCategoryId } = useCategories();

    const espressoId  = getCategoryId('espresso');
    const smoothieId  = getCategoryId('smoothie');
    const teaId       = getCategoryId('tea');

    return (
        <div className={styles.page}>
            <Navbar />
            <HeroSection />

            {/* Collage Section 1 — Espresso & Coffee → real category ID */}
            <ReserveCollage
                number="01"
                title="The Precision Of"
                titleAccent="Classics"
                description="Precision-extracted espresso and timeless favorites. Discover the pinnacle of coffee artistry in every drop."
                featuredText="ESPRESSO • MACCHIATO • COLD BREW • LATTE"
                linkText="VIEW CLASSICS"
                linkHref={categoryHref(espressoId)}
                images={[prodColdBrew, prodMacchiato, prodLatte, prodEspresso]}
                alts={['Cold Brew', 'Macchiato', 'Latte', 'Espresso']}
                speeds={['-1.2', '0.5', '-0.7', '-1.8']}
            />

            {/* Collage Section 2 — Smoothie / Blended → real category ID */}
            <ReserveCollage
                number="02"
                title="Ice-Blended"
                titleAccent="Indulgence"
                description="Indulge in our signature Frappuccinos—a harmonious blend of premium coffee, silky milk, and velvet cream."
                featuredText="CARAMEL • VANILLA BEAN • VELVET CREAM"
                linkText="VIEW BLENDED"
                linkHref={categoryHref(smoothieId)}
                reversed={true}
                images={[prodVanillaFrappuccino, prodMocha, prodCappuccino, prodLatte]}
                alts={['Vanilla Frappuccino', 'Mocha', 'Cappuccino', 'Latte']}
                speeds={['-1.2', '0.5', '-0.7', '-1.8']}
            />

            {/* Collage Section 3 — Tea & Matcha → real category ID */}
            <ReserveCollage
                number="03"
                title="Beyond The"
                titleAccent="Bean"
                description="Refined alternatives for the discerning palate. Savor moments of pure serenity with our hand-selected tea collection."
                featuredText="MATCHA • SPICED CHAI • HOT CHOCOLATE"
                linkText="VIEW NON-COFFEE"
                linkHref={categoryHref(teaId)}
                images={[prodHotChocolate, prodChaiLatte, prodMocha, prodMacchiato]}
                alts={['Hot Chocolate', 'Chai Latte', 'Mocha', 'Macchiato']}
                speeds={['-1.6', '0.9', '-0.4', '-2.2']}
            />

            <ParallaxReveal />
        </div>
    );
};

export default HomePage;
