import { useEffect } from 'react';

/**
 * Hook to handle Reveal on Scroll (IntersectionObserver)
 * and Inertia Parallax (requestAnimationFrame)
 */
export const useScrollEffects = () => {
    useEffect(() => {
        // --- 1. Reveal on Scroll ---
        const revealCallback = (entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                } else {
                    entry.target.classList.remove('is-visible');
                }
            });
        };

        const revealObserver = new IntersectionObserver(revealCallback, {
            root: null,
            threshold: 0.15,
            rootMargin: '0px 0px -50px 0px'
        });

        const revealElements = document.querySelectorAll('.reveal-on-scroll');
        revealElements.forEach(el => revealObserver.observe(el));


        // --- 2. Inertia Parallax ---
        const parallaxEls = document.querySelectorAll('.js-parallax');
        if (parallaxEls.length === 0) return;

        let currentScroll = window.pageYOffset;
        let targetScroll = window.pageYOffset;
        let ease = 0.08;
        let animationFrameId;

        const parallaxData = Array.from(parallaxEls).map(el => ({
            el: el,
            speed: parseFloat(el.getAttribute('data-speed')) || 0,
            section: el.closest('section') || el.parentElement
        }));

        const onScroll = () => {
            targetScroll = window.pageYOffset;
        };

        const updateParallax = () => {
            currentScroll += (targetScroll - currentScroll) * ease;
            const windowHeight = window.innerHeight;

            parallaxData.forEach(item => {
                if (!item.section) return;

                const sectionTop = item.section.offsetTop;
                const sectionHeight = item.section.offsetHeight;

                // Check if section is visible in screen
                if (currentScroll + windowHeight > sectionTop && currentScroll < sectionTop + sectionHeight) {
                    const distanceScrolledIntoView = (currentScroll + windowHeight) - sectionTop;
                    const yTranslate = (distanceScrolledIntoView * item.speed) * 0.12;

                    item.el.style.transform = `translate3d(0, ${yTranslate}px, 0)`;
                }
            });

            animationFrameId = requestAnimationFrame(updateParallax);
        };

        window.addEventListener('scroll', onScroll, { passive: true });
        animationFrameId = requestAnimationFrame(updateParallax);

        return () => {
            revealObserver.disconnect();
            window.removeEventListener('scroll', onScroll);
            cancelAnimationFrame(animationFrameId);
        };
    }, []);
};
