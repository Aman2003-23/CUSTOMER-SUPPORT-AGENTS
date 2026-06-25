import React from 'react';

// Colors mirror CategoryBadge.jsx for visual consistency.
const COLOR_MAP = {
    BILLING: 'bg-green-500',
    TECHNICAL: 'bg-red-500',
    ACCOUNT: 'bg-indigo-500',
    GENERAL: 'bg-slate-400',
    OTHER: 'bg-gray-400',
};

export default function CategoryBars({ data = [] }) {
    const max = data.reduce((acc, d) => Math.max(acc, d.count), 0);
    return (
        <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold text-slate-800 mb-4">
                Tickets by Category
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