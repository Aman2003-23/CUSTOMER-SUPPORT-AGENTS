import React from 'react';
import { useSearchParams } from 'react-router-dom';
import TicketFilterBar from '../components/TicketFilterBar';
import TicketTable from '../components/TicketTable';
import { useTicketsList } from '../hooks/useTickets';

const ALL_STATUSES = ['NEW', 'OPEN', 'PENDING', 'RESOLVED', 'CLOSED'];

export default function TicketsPage() {
    const [searchParams, setSearchParams] = useSearchParams();

    const filters = parseFilters(searchParams);
    const { data, error, loading } = useTicketsList(filters);

    const tickets = data?.content || [];
    const totalPages = data?.totalPages ?? 0;
    const page = data?.page ?? 0;

    const setPage = (newPage) => {
        const next = new URLSearchParams(searchParams);
        if (newPage <= 0) next.delete('page');
        else next.set('page', String(newPage));
        setSearchParams(next, { replace: true });
    };

    return (
        <div>
            <div className="flex items-baseline justify-between mb-4">
                <h1 className="text-3xl font-bold text-gray-800">Tickets</h1>
                <div className="text-sm text-slate-500">
                    {data && (
                        <span>
                            {data.totalElements} {data.totalElements === 1 ? 'ticket' : 'tickets'}
                        </span>
                    )}
                </div>
            </div>

            <TicketFilterBar filters={filters} setSearchParams={setSearchParams} />

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-md p-4 mb-4 text-sm">
                    Failed to load tickets: {error.message}
                </div>
            )}

            {loading ? (
                <div className="bg-white rounded-lg shadow p-8 text-center text-slate-500">
                    Loading…
                </div>
            ) : (
                <>
                    <TicketTable tickets={tickets} />
                    {totalPages > 1 && (
                        <div className="flex items-center justify-between mt-4 text-sm text-slate-600">
                            <div>
                                Page {page + 1} of {totalPages}
                            </div>
                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    disabled={page <= 0}
                                    onClick={() => setPage(page - 1)}
                                    className="px-3 py-1 border border-slate-300 rounded disabled:opacity-50 hover:bg-slate-50"
                                >
                                    Previous
                                </button>
                                <button
                                    type="button"
                                    disabled={page + 1 >= totalPages}
                                    onClick={() => setPage(page + 1)}
                                    className="px-3 py-1 border border-slate-300 rounded disabled:opacity-50 hover:bg-slate-50"
                                >
                                    Next
                                </button>
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

function parseFilters(params) {
    const status = params.get('status');
    const sort = params.get('sort');
    const size = params.get('size');
    return {
        status: status ? status.split(',').filter((s) => ALL_STATUSES.includes(s)) : [],
        category: params.get('category') || undefined,
        q: params.get('q') || undefined,
        from: params.get('from') || undefined,
        to: params.get('to') || undefined,
        sort: sort || undefined,
        page: params.get('page') ? Math.max(0, Number(params.get('page'))) : 0,
        size: size ? Number(size) : 25,
    };
}