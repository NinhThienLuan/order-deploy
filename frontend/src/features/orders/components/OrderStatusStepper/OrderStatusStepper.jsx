import styles from './OrderStatusStepper.module.css';
import { STEPPER_STEPS } from '../../orders.constants';

/**
 * OrderStatusStepper
 * @param {{ currentStatus: string }} props
 */
const OrderStatusStepper = ({ currentStatus }) => {
    const currentIndex = STEPPER_STEPS.findIndex((s) => s.key === currentStatus);

    return (
        <div className={styles.stepper}>
            {STEPPER_STEPS.map((step, index) => {
                const isDone = index < currentIndex;
                const isActive = index === currentIndex;

                let stepClass = styles.step;
                if (isDone) stepClass += ` ${styles.done}`;
                if (isActive) stepClass += ` ${styles.active}`;

                return (
                    <div key={step.key} className={stepClass}>
                        <div className={styles.dot}>
                            {isDone ? (
                                <svg className={styles.checkIcon} viewBox="0 0 12 12" fill="none">
                                    <path
                                        d="M2 6l3 3 5-5"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                </svg>
                            ) : (
                                <span>{index + 1}</span>
                            )}
                        </div>
                        <span className={styles.label}>{step.label}</span>
                    </div>
                );
            })}
        </div>
    );
};

export default OrderStatusStepper;
