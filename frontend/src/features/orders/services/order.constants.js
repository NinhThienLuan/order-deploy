export const ORDER_STATUSES = {
    PENDING: "PENDING",
    PAID: "PAID",
    PREPARING: "PREPARING",
    READY: "READY",
    COMPLETED: "COMPLETED",
    CANCELED: "CANCELED",
};

export const STEPPER_STEPS = [
    {
        key: ORDER_STATUSES.PENDING,
        label: "Order Placed",
    },
    {
        key: ORDER_STATUSES.PAID,
        label: "Payment Confirmed",
    },
    {
        key: ORDER_STATUSES.PREPARING,
        label: "Preparing",
    },
    {
        key: ORDER_STATUSES.READY,
        label: "Ready",
    },
    {
        key: ORDER_STATUSES.COMPLETED,
        label: "Completed",
    },
];

// Statuses where cancellation is NO LONGER allowed
export const CANCEL_BLOCKED_STATUSES = [
    ORDER_STATUSES.PREPARING,
    ORDER_STATUSES.READY,
    ORDER_STATUSES.COMPLETED,
];

export const STATUS_LABELS = {
    [ORDER_STATUSES.PENDING]: "Order Placed",
    [ORDER_STATUSES.PAID]: "Payment Confirmed",
    [ORDER_STATUSES.PREPARING]: "In Progress",
    [ORDER_STATUSES.READY]: "Ready for Pickup",
    [ORDER_STATUSES.COMPLETED]: "Completed",
    [ORDER_STATUSES.CANCELED]: "Cancelled",
};

export const ORDER_FILTERS = [
    { key: 'all',       label: 'All' },
    { key: 'active',    label: 'Active',    statuses: ['PENDING', 'PAID', 'PREPARING', 'READY'] },
    { key: 'completed', label: 'Completed', statuses: ['COMPLETED'] },
    { key: 'cancelled', label: 'Cancelled', statuses: ['CANCELED'] },
];
