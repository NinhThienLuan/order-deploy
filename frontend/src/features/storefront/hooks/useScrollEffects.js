import { useEffect } from 'react';

/**
 * useScrollEffects
 * Handles parallax and reveal-on-scroll animations for the storefront.
 * 
 * Parallax: Finds all elements with '.js-parallax' and applies transform based on data-speed.
 * Reveal: Finds all elements with '.reveal-on-scroll' and adds '.is-revealed' when they enter viewport.
 */
export const useScrollEffects = () => {
    useEffect(() => {
        // --- PARALLAX LOGIC ---
        const parallaxElements = document.querySelectorAll('.js-parallax');
        
        const handleParallax = () => {
            const scrollY = window.scrollY;
            parallaxElements.forEach(el => {
                const speed = parseFloat(el.getAttribute('data-speed')) || 0;
                const yPos = -(scrollY * speed);
                el.style.transform = `translate3d(0, ${yPos}px, 0)`;
            });
        };

        // --- REVEAL LOGIC ---
        const revealElements = document.querySelectorAll('.reveal-on-scroll');
        
        const revealObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-revealed');
                    // We don't unobserve if we want it to potentially "re-reveal" 
                    // but usually storefront-style is reveal once.
                    revealObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.15,
            rootMargin: '0px 0px -50px 0px'
        });

        revealElements.forEach(el => revealObserver.observe(el));
        window.addEventListener('scroll', handleParallax);

        // Initial call
        handleParallax();

        return () => {
            window.removeEventListener('scroll', handleParallax);
            revealObserver.disconnect();
        };
    }, []);
};
