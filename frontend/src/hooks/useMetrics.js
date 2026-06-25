import { useEffect, useState } from 'react';
import { fetchDashboardMetrics } from '../api/metrics';

/**
 * Fetches aggregated dashboard metrics. Cancels in-flight requests when the
 * component unmounts or `days` changes, matching the pattern in useTickets.js.
 */
export function useDashboardMetrics(days = 14) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        fetchDashboardMetrics(days)
            .then((res) => {
                if (!cancelled) {
                    setData(res);
                    setError(null);
                }
            })
            .catch((err) => {
                if (!cancelled) setError(err);
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => {
            cancelled = true;
        };
    }, [days]);

    return { data, error, loading };
}