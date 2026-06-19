import React, { useEffect, useState } from 'react';

const ALL_STATUSES = ['NEW', 'OPEN', 'PENDING', 'RESOLVED', 'CLOSED'];
const CATEGORIES = ['BILLING', 'TECHNICAL', 'ACCOUNT', 'GENERAL', 'OTHER'];
const SORTS = [
    { value: 'createdAt,desc', label: 'Newest first' },
    { value: 'createdAt,asc', label: 'Oldest first' },
    { value: 'updatedAt,desc', label: 'Recently updated' },
];
const PAGE_SIZES = [10, 25, 50];

/**
 * URL-driven filter bar. All filter changes update the search params via
 * {@code setSearchParams} on the parent; this component is controlled.
 */
export default function TicketFilterBar({ filters, setSearchParams }) {
    const [searchInput, setSearchInput] = useState(filters.q || '');

    // Debounce the search input to avoid hammering the API on every keystroke.
    useEffect(() => {
        const id = setTimeout(() => {
            if (searchInput !== (filters.q || '')) {
                patch(setSearchParams, { q: searchInput || null, page: 0 });
            }
        }, 300);
        return () => clearTimeout(id);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [searchInput]);

    const toggleStatus = (status) => {
        const current = new Set(filters.status || []);
        if (current.has(status)) current.delete(status);
        else current.add(status);
        patch(setSearchParams, { status: [...current], page: 0 });
    };

    const setCategory = (e) => patch(setSearchParams, { category: e.target.value || null, page: 0 });

    const setSort = (e) => patch(setSearchParams, { sort: e.target.value });

    const setPageSize = (e) => patch(setSearchParams, { size: Number(e.target.value), page: 0 });

    const setDate = (key, value) => patch(setSearchParams, { [key]: value || null, page: 0 });

    const clearAll = () => setSearchParams({});

    const activeStatusSet = new Set(filters.status || []);

    return (
        <div className="bg-white rounded-lg shadow p-4 mb-4 space-y-3">
            {/* Status chips */}
            <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm font-medium text-slate-700 mr-1">Status:</span>
                {ALL_STATUSES.map((s) => {
                    const active = activeStatusSet.has(s);
                    return (
                        <button
                            key={s}
                            type="button"
                            onClick={() => toggleStatus(s)}
                            className={`px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
                                active
                                    ? 'bg-blue-600 text-white border-blue-600'
                                    : 'bg-white text-slate-700 border-slate-300 hover:bg-slate-50'
                            }`}
                        >
                            {s}
                        </button>
                    );
                })}
                {activeStatusSet.size > 0 && (
                    <button
                        type="button"
                        onClick={() => patch(setSearchParams, { status: null, page: 0 })}
                        className="text-xs text-blue-600 hover:underline ml-1"
                    >
                        Clear
                    </button>
                )}
            </div>

            {/* Other filters */}
            <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
                <div className="md:col-span-2">
                    <label className="block text-xs font-medium text-slate-600 mb-1">
                        Search (subject or email)
                    </label>
                    <input
                        type="text"
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        placeholder="e.g. invoice or alice@example.com"
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">Category</label>
                    <select
                        value={filters.category || ''}
                        onChange={setCategory}
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white"
                    >
                        <option value="">All</option>
                        {CATEGORIES.map((c) => (
                            <option key={c} value={c}>
                                {c}
                            </option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">Sort</label>
                    <select
                        value={filters.sort || 'createdAt,desc'}
                        onChange={setSort}
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white"
                    >
                        {SORTS.map((o) => (
                            <option key={o.value} value={o.value}>
                                {o.label}
                            </option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">Page size</label>
                    <select
                        value={filters.size || 25}
                        onChange={setPageSize}
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white"
                    >
                        {PAGE_SIZES.map((n) => (
                            <option key={n} value={n}>
                                {n}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3 items-end">
                <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">From</label>
                    <input
                        type="date"
                        value={filters.from || ''}
                        onChange={(e) => setDate('from', e.target.value)}
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm"
                    />
                </div>
                <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">To</label>
                    <input
                        type="date"
                        value={filters.to || ''}
                        onChange={(e) => setDate('to', e.target.value)}
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm"
                    />
                </div>
                <div className="md:text-right">
                    <button
                        type="button"
                        onClick={clearAll}
                        className="px-4 py-2 text-sm text-slate-700 border border-slate-300 rounded-md hover:bg-slate-50"
                    >
                        Clear all filters
                    </button>
                </div>
            </div>
        </div>
    );
}

// Update URL search params, preserving other params, removing nulls.
function patch(setSearchParams, patch) {
    setSearchParams(
        (prev) => {
            const next = new URLSearchParams(prev);
            Object.entries(patch).forEach(([k, v]) => {
                if (v === null || v === undefined || v === '') {
                    next.delete(k);
                } else {
                    next.set(k, String(v));
                }
            });
            return next;
        },
        { replace: true },
    );
}