import React from 'react';

const STYLES = {
    NEW: 'bg-blue-100 text-blue-800 border-blue-200',
    OPEN: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    PENDING: 'bg-amber-100 text-amber-800 border-amber-200',
    RESOLVED: 'bg-purple-100 text-purple-800 border-purple-200',
    CLOSED: 'bg-slate-200 text-slate-700 border-slate-300',
};

export default function StatusBadge({ status }) {
    if (!status) return null;
    const cls = STYLES[status] || 'bg-gray-100 text-gray-800 border-gray-200';
    return (
        <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${cls}`}
        >
            {status}
        </span>
    );
}