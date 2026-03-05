import { describe, it, expect } from 'vitest';
import { PRODUCTS, OVERLAY_PRIMARY_LINKS, PROCESS_STEPS } from './landing.constants';

describe('Landing Constants', () => {
    it('should have valid products matching the required schema', () => {
        expect(Array.isArray(PRODUCTS)).toBe(true);
        expect(PRODUCTS.length).toBeGreaterThan(0);

        PRODUCTS.forEach(product => {
            expect(product).toHaveProperty('id');
            expect(typeof product.id).toBe('string');

            expect(product).toHaveProperty('productName');
            expect(typeof product.productName).toBe('string');

            expect(product).toHaveProperty('price');
            expect(typeof product.price).toBe('number');
            expect(product.price).toBeGreaterThan(0);

            expect(product).toHaveProperty('imageUrl');
            expect(typeof product.imageUrl).toBe('string');
        });
    });

    it('should have non-empty navigation links', () => {
        expect(Array.isArray(OVERLAY_PRIMARY_LINKS)).toBe(true);
        expect(OVERLAY_PRIMARY_LINKS.length).toBeGreaterThan(0);

        OVERLAY_PRIMARY_LINKS.forEach(link => {
            expect(link).toHaveProperty('label');
            expect(link).toHaveProperty('href');
        });
    });

    it('should properly structure process steps', () => {
        expect(Array.isArray(PROCESS_STEPS)).toBe(true);
        expect(PROCESS_STEPS.length).toBeGreaterThan(0);

        PROCESS_STEPS.forEach(step => {
            expect(step).toHaveProperty('numeral');
            expect(step).toHaveProperty('title');
            expect(step).toHaveProperty('desc');
        });
    });
});
