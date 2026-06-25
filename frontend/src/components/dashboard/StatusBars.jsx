import React from 'react';

// Colors mirror StatusBadge.jsx for visual consistency.
const COLOR_MAP = {
    NEW: 'bg-blue-500',
    OPEN: 'bg-emerald-500',
    PENDING: 'bg-amber-500',
    RESOLVED: 'bg-purple-500',
    CLOSED: 'bg-slate-400',
};

export default function StatusBars({ data = [] }) {
    const max = data.reduce((acc, d) => Math.max(acc, d.count), 0);
    return (
        <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold text-slate-800 mb-4">
                Tickets by Status
            </h2>
            <div className="space-y-3">
                {data.map((row) => {
                    const widthPct = max > 0 ? (row.count / max) * 100 : 0;
                    const color = COLOR_MAP[row.key] || 'bg-slate-400';
                    return (
                        <div key={row.key}>
                            <div className="flex items-baseline justify-between text-sm mb-1">
                                <span className="font-medium text-slate-700">
                                    {row.label || row.key}
                                </span>
                                <span className="text-slate-500 tabular-nums">
                                    {row.count}
                                </span>
                            </div>
                            <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                                <div
                                    className={`h-full ${color} transition-all`}
                                    style={{ width: `${widthPct}%` }}
                                />
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}