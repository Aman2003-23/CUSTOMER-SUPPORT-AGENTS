import React from 'react';

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

// SVG bar chart for daily ticket volume. Pure SVG, no chart library.
// Props: data = [{ date: 'YYYY-MM-DD', count: number }]
export default function DailyVolumeChart({ data = [] }) {
    const safe = Array.isArray(data) ? data : [];
    const counts = safe.map((d) => Number(d.count) || 0);
    const max = Math.max(1, ...counts);
    const total = counts.reduce((acc, n) => acc + n, 0);

    // SVG layout dimensions
    const width = 720;
    const height = 220;
    const padLeft = 36;
    const padRight = 12;
    const padTop = 16;
    const padBottom = 32;
    const chartW = width - padLeft - padRight;
    const chartH = height - padTop - padBottom;

    const barGap = safe.length > 0 ? Math.min(6, chartW / safe.length * 0.2) : 0;
    const barWidth = safe.length > 0
        ? Math.max(2, (chartW - barGap * (safe.length - 1)) / safe.length)
        : 0;

    return (
        <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-baseline justify-between mb-4">
                <h2 className="text-lg font-semibold text-slate-800">
                    Daily Ticket Volume
                </h2>
                <span className="text-sm text-slate-500 tabular-nums">
                    {total} in last {safe.length} {safe.length === 1 ? 'day' : 'days'}
                </span>
            </div>
            {safe.length === 0 ? (
                <div className="text-center text-slate-500 py-12 text-sm">
                    No volume data.
                </div>
            ) : (
                <svg
                    viewBox={`0 0 ${width} ${height}`}
                    className="w-full h-auto"
                    role="img"
                    aria-label="Daily ticket volume"
                >
                    {/* Horizontal gridlines at 0, 25%, 50%, 75%, 100% of max */}
                    {[0, 0.25, 0.5, 0.75, 1].map((p) => {
                        const y = padTop + chartH - chartH * p;
                        return (
                            <g key={p}>
                                <line
                                    x1={padLeft}
                                    x2={padLeft + chartW}
                                    y1={y}
                                    y2={y}
                                    stroke="#e2e8f0"
                                    strokeWidth="1"
                                />
                                <text
                                    x={padLeft - 6}
                                    y={y + 3}
                                    textAnchor="end"
                                    fontSize="10"
                                    fill="#94a3b8"
                                >
                                    {Math.round(max * p)}
                                </text>
                            </g>
                        );
                    })}

                    {safe.map((d, i) => {
                        const count = Number(d.count) || 0;
                        const x = padLeft + i * (barWidth + barGap);
                        const h = (count / max) * chartH;
                        const y = padTop + chartH - h;
                        return (
                            <g key={d.date || i}>
                                <rect
                                    x={x}
                                    y={y}
                                    width={barWidth}
                                    height={Math.max(h, count > 0 ? 2 : 0)}
                                    rx="2"
                                    fill="#3b82f6"
                                >
                                    <title>
                                        {d.date}: {count} ticket{count === 1 ? '' : 's'}
                                    </title>
                                </rect>
                                {/* Show date label every ~2 bars to avoid overlap */}
                                {i % Math.ceil(safe.length / 7) === 0 && (
                                    <text
                                        x={x + barWidth / 2}
                                        y={height - padBottom + 16}
                                        textAnchor="middle"
                                        fontSize="10"
                                        fill="#64748b"
                                    >
                                        {formatLabel(d.date)}
                                    </text>
                                )}
                            </g>
                        );
                    })}
                </svg>
            )}
        </div>
    );
}

function formatLabel(iso) {
    if (!iso) return '';
    // Accept 'YYYY-MM-DD' without timezone gymnastics.
    const parts = iso.split('-');
    if (parts.length !== 3) return iso;
    const month = MONTHS[Number(parts[1]) - 1] || parts[1];
    return `${month} ${Number(parts[2])}`;
}