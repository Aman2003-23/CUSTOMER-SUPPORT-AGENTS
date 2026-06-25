import React from 'react';

export default function KpiCard({ title, value, hint, accent = 'slate' }) {
    const accentMap = {
        slate: 'text-slate-900',
        blue: 'text-blue-700',
        emerald: 'text-emerald-700',
        amber: 'text-amber-700',
        rose: 'text-rose-700',
    };
    const valueClass = accentMap[accent] || accentMap.slate;

    return (
        <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-slate-500 uppercase tracking-wide">
                {title}
            </div>
            <div className={`mt-2 text-3xl font-bold ${valueClass}`}>{value}</div>
            {hint && <div className="mt-1 text-xs text-slate-500">{hint}</div>}
        </div>
    );
}