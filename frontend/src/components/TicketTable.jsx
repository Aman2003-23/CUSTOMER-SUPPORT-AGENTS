import React from 'react';
import { useNavigate } from 'react-router-dom';
import StatusBadge from './StatusBadge';
import CategoryBadge from './CategoryBadge';

export default function TicketTable({ tickets }) {
    const navigate = useNavigate();

    if (!tickets || tickets.length === 0) {
        return (
            <div className="bg-white rounded-lg shadow p-8 text-center text-slate-500">
                No tickets match the current filters.
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                    <tr>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wide">
                            ID
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wide">
                            Subject
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wide">
                            Customer
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wide">
                            Status
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wide">
                            Category
                        </th>
                        <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wide">
                            Updated
                        </th>
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-slate-200">
                    {tickets.map((t) => (
                        <tr
                            key={t.id}
                            onClick={() => navigate(`/dashboard/tickets/${t.id}`)}
                            className="cursor-pointer hover:bg-slate-50"
                        >
                            <td className="px-4 py-3 text-sm text-slate-500">#{t.id}</td>
                            <td className="px-4 py-3 text-sm font-medium text-slate-900 max-w-md truncate">
                                {t.subject}
                            </td>
                            <td className="px-4 py-3 text-sm text-slate-600 max-w-xs truncate">
                                {t.customerEmail}
                            </td>
                            <td className="px-4 py-3">
                                <StatusBadge status={t.status} />
                            </td>
                            <td className="px-4 py-3">
                                <CategoryBadge category={t.category} />
                            </td>
                            <td className="px-4 py-3 text-sm text-slate-500">
                                {formatTime(t.updatedAt)}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function formatTime(iso) {
    if (!iso) return '';
    try {
        return new Date(iso).toLocaleString();
    } catch {
        return iso;
    }
}