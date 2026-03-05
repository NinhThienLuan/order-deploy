import Navbar from '../../../layouts/Navbar';
import Footer from '../../../layouts/Footer';
import HeroSection from '../components/HeroSection/HeroSection';
import LocationsBar from '../components/LocationsBar/LocationsBar';
import AtmosphereSection from '../components/AtmosphereSection/AtmosphereSection';
import NarrativeSection from '../components/NarrativeSection/NarrativeSection';
import ProcessSection from '../components/ProcessSection/ProcessSection';
import CatalogSection from '../components/CatalogSection/CatalogSection';
import VisitSection from '../components/VisitSection/VisitSection';

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
