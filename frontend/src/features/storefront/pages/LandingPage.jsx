import Navbar from '@/layouts/Navbar';
import Footer from '@/layouts/Footer';
import HeroSection from '@/features/storefront/components/landing/HeroSection/HeroSection';
import LocationsBar from '@/features/storefront/components/landing/LocationsBar/LocationsBar';
import AtmosphereSection from '@/features/storefront/components/landing/AtmosphereSection/AtmosphereSection';
import NarrativeSection from '@/features/storefront/components/landing/NarrativeSection/NarrativeSection';
import ProcessSection from '@/features/storefront/components/landing/ProcessSection/ProcessSection';
import CatalogSection from '@/features/storefront/components/landing/CatalogSection/CatalogSection';
import VisitSection from '@/features/storefront/components/landing/VisitSection/VisitSection';

const LandingPage = () => (
    <>
        <Navbar />
        <main>
            <HeroSection />
            <LocationsBar />
            <AtmosphereSection />
            <NarrativeSection />
            <ProcessSection />
            <CatalogSection />
            <VisitSection />
        </main>
        <Footer />
    </>
);

export default LandingPage;
