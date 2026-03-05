// ── Filter tabs ───────────────────────────────────────────
export const TRANSACTION_FILTERS = [
    { label: 'All',        key: 'all' },
    { label: 'Today',      key: 'today' },
    { label: 'Yesterday',  key: 'yesterday' },
    { label: 'Last Week',  key: 'week' },
    { label: 'Last Month', key: 'month' },
    { label: 'Last Year',  key: 'year' },
];

export const TRANSACTION_PAGE_SIZE = 7;

// ── Status display metadata ───────────────────────────────
export const TRANSACTION_STATUS_META = {
    PAID:     { label: 'Paid',     cls: 'statusPaid' },
    PENDING:  { label: 'Pending',  cls: 'statusPending' },
    REFUNDED: { label: 'Refunded', cls: 'statusRefunded' },
};
