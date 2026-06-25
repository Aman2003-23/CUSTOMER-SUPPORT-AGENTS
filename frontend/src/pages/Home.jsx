import React from 'react';
import { useDashboardMetrics } from '../hooks/useMetrics';
import KpiCard from '../components/dashboard/KpiCard';
import StatusBars from '../components/dashboard/StatusBars';
import CategoryBars from '../components/dashboard/CategoryBars';
import DailyVolumeChart from '../components/dashboard/DailyVolumeChart';

const DAILY_DAYS = 14;
const OPEN_STATUSES = new Set(['NEW', 'OPEN', 'PENDING']);

export default function Home() {
    const { data, error, loading } = useDashboardMetrics(DAILY_DAYS);

    if (loading) {
        return (
            <div className="bg-white rounded-lg shadow p-8 text-center text-slate-500">
                Loading dashboard…
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 border border-red-200 text-red-700 rounded-md p-4 text-sm">
                Failed to load metrics: {error.message}
            </div>
        );
    }

    const openTickets = (data.statusDistribution || [])
        .filter((s) => OPEN_STATUSES.has(s.key))
        .reduce((acc, s) => acc + (s.count || 0), 0);

    const avgHint =
        data.responseTimeSampleCount > 0
            ? `Across ${data.responseTimeSampleCount} ticket${data.responseTimeSampleCount === 1 ? '' : 's'}`
            : 'No agent replies yet';

    return (
        <div>
            <h1 className="text-3xl font-bold text-slate-800 mb-4">Dashboard</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                <KpiCard
                    title="Total Tickets"
                    value={data.totalTickets}
                    accent="slate"
                />
                <KpiCard
                    title="Open Tickets"
                    value={openTickets}
                    accent="amber"
                    hint="New, open, or pending"
                />
                <KpiCard
                    title="Avg Response Time"
                    value={data.averageResponseTime || '—'}
                    accent="emerald"
                    hint={avgHint}
                />
                <KpiCard
                    title="Today's Tickets"
                    value={data.todayTickets}
                    accent="blue"
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                <StatusBars data={data.statusDistribution || []} />
                <CategoryBars data={data.categoryDistribution || []} />
            </div>

            <DailyVolumeChart data={data.dailyVolume || []} />
        </div>
    );
}