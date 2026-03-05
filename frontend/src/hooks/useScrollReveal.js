import { useEffect, useRef } from 'react';

/**
 * useScrollReveal
 * Attaches an IntersectionObserver to a ref.
 * The element fades + slides up into view when it enters the viewport.
 *
 * @param {{ threshold?: number, delay?: number }} options
 * @returns {React.RefObject}
 */
const useScrollReveal = ({ threshold = 0.1, delay = 0 } = {}) => {
    const ref = useRef(null);

    useEffect(() => {
        const el = ref.current;
        if (!el) return;

        // Initial hidden state
        el.style.opacity = '0';
        el.style.transform = 'translate3d(0, 30px, 0)';
        el.style.transition = `opacity 0.65s cubic-bezier(0.165, 0.84, 0.44, 1) ${delay}ms, transform 0.65s cubic-bezier(0.165, 0.84, 0.44, 1) ${delay}ms`;

        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) {
                        el.style.opacity = '1';
                        el.style.transform = 'translate3d(0, 0, 0)';
                        observer.unobserve(el);
                    }
                });
            },
            { threshold }
        );

        observer.observe(el);
        return () => observer.disconnect();
    }, [threshold, delay]);

    return ref;
};

export default useScrollReveal;
