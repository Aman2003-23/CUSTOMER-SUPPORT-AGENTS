import React from 'react';

const STYLES = {
    BILLING: 'bg-green-100 text-green-800 border-green-200',
    TECHNICAL: 'bg-red-100 text-red-800 border-red-200',
    ACCOUNT: 'bg-indigo-100 text-indigo-800 border-indigo-200',
    GENERAL: 'bg-slate-100 text-slate-700 border-slate-200',
    OTHER: 'bg-gray-100 text-gray-700 border-gray-200',
};

export default function CategoryBadge({ category }) {
    if (!category) return null;
    const cls = STYLES[category] || 'bg-gray-100 text-gray-700 border-gray-200';
    return (
        <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${cls}`}
        >
            {category}
        </span>
    );
}