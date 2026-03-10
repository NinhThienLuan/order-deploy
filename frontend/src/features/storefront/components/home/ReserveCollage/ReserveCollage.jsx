import React from 'react';
import { Link } from 'react-router-dom';
import styles from './ReserveCollage.module.css';

const ReserveCollage = ({
    number,
    title,
    titleAccent,
    description,
    featuredText,
    linkText,
    linkHref = '/menu',
    images = [],
    alts = [],
    speeds = ['-1.2', '0.5', '-0.7', '-1.8'],
    reversed = false
}) => {
    return (
        <section className={`${styles.reserveCollageSection} ${reversed ? styles.collageReversed : ''}`}>
            <div className={styles.architecturalLines}>
                <div className={styles.lineVertical}></div>
            </div>

            <div className={styles.collageContainer}>
                <div className={styles.collageImages}>
                    <div className={`${styles.imgMain} js-parallax`} data-speed={speeds[0]}>
                        <img src={images[0]} alt={alts[0]} />
                    </div>
                    <div className={`${styles.imgSecondary} js-parallax`} data-speed={speeds[1]}>
                        <img src={images[1]} alt={alts[1]} />
                    </div>
                    <div className={`${styles.imgBottom} js-parallax`} data-speed={speeds[2]}>
                        <img src={images[2]} alt={alts[2]} />
                    </div>
                    <div className={`${styles.imgAccent} js-parallax`} data-speed={speeds[3]}>
                        <img src={images[3]} alt={alts[3]} />
                    </div>
                </div>

                <div className={`${styles.collageContent} js-parallax`} data-speed="-0.3">
                    <span className={`${styles.collageNumber} reveal-on-scroll`}>{number}</span>
                    <h2 className={`${styles.collageTitle} reveal-on-scroll reveal-delay-half`}>
                        {title}<br /><span className={styles.textAccent}>{titleAccent}</span>
                    </h2>
                    <p className={`${styles.collageDesc} reveal-on-scroll reveal-delay-1`}>
                        {description}
                    </p>
                    <div className={`${styles.collageFeatured} reveal-on-scroll reveal-delay-2`}>
                        <span>TOP PICKS:</span> {featuredText}
                    </div>
                    <Link to={linkHref} className={`${styles.collageLink} reveal-on-scroll reveal-delay-3`}>
                        {linkText}
                    </Link>
                </div>
            </div>
        </section>
    );
};

export default ReserveCollage;
