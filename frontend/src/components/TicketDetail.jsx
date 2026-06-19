import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import MessageBubble from '../components/MessageBubble';
import StatusBadge from '../components/StatusBadge';
import CategoryBadge from '../components/CategoryBadge';
import { useTicket, useTicketActions } from '../hooks/useTickets';

const ALL_STATUSES = ['NEW', 'OPEN', 'PENDING', 'RESOLVED', 'CLOSED'];

export default function TicketDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { data: ticket, error, loading, reload } = useTicket(id);
    const actions = useTicketActions(ticket, reload);

    const [replyBody, setReplyBody] = useState('');
    const [noteBody, setNoteBody] = useState('');
    const [formError, setFormError] = useState(null);

    const submitReply = async (e) => {
        e.preventDefault();
        if (!replyBody.trim()) return;
        setFormError(null);
        try {
            await actions.sendReply(replyBody.trim());
            setReplyBody('');
        } catch (err) {
            setFormError(err.message || 'Failed to send reply');
        }
    };

    const submitNote = async (e) => {
        e.preventDefault();
        if (!noteBody.trim()) return;
        setFormError(null);
        try {
            await actions.sendNote(noteBody.trim());
            setNoteBody('');
        } catch (err) {
            setFormError(err.message || 'Failed to add note');
        }
    };

    const onStatusChange = async (e) => {
        try {
            await actions.changeStatus(e.target.value);
        } catch (err) {
            setFormError(err.message || 'Failed to change status');
        }
    };

    const onRetry = async (messageId) => {
        setFormError(null);
        try {
            await actions.retry(messageId);
        } catch (err) {
            setFormError(err.message || 'Retry failed');
        }
    };

    if (loading && !ticket) {
        return <div className="text-slate-500">Loading…</div>;
    }
    if (error) {
        return (
            <div className="bg-red-50 border border-red-200 text-red-700 rounded-md p-4">
                Failed to load ticket: {error.message}
            </div>
        );
    }
    if (!ticket) return null;

    return (
        <div>
            <button
                type="button"
                onClick={() => navigate('/dashboard/tickets')}
                className="text-sm text-blue-600 hover:underline mb-3"
            >
                ← Back to tickets
            </button>

            <div className="bg-white rounded-lg shadow p-5 mb-4">
                <div className="flex items-start justify-between mb-2">
                    <div>
                        <div className="text-xs text-slate-500 mb-1">Ticket #{ticket.id}</div>
                        <h1 className="text-2xl font-bold text-slate-900">{ticket.subject}</h1>
                        <div className="text-sm text-slate-600 mt-1">{ticket.customerEmail}</div>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                        <div className="flex gap-2">
                            <StatusBadge status={ticket.status} />
                            <CategoryBadge category={ticket.category} />
                        </div>
                        <select
                            value={ticket.status}
                            onChange={onStatusChange}
                            disabled={actions.busy}
                            className="text-xs border border-slate-300 rounded px-2 py-1 bg-white"
                        >
                            {ALL_STATUSES.map((s) => (
                                <option key={s} value={s}>
                                    {s}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
                {ticket.keywords && ticket.keywords.length > 0 && (
                    <div className="flex flex-wrap gap-1 mt-3">
                        {ticket.keywords.map((kw) => (
                            <span
                                key={kw}
                                className="text-xs bg-slate-100 text-slate-700 border border-slate-200 px-2 py-0.5 rounded"
                            >
                                {kw}
                            </span>
                        ))}
                    </div>
                )}
                <div className="text-xs text-slate-500 mt-3">
                    Created {formatTime(ticket.createdAt)} · Updated {formatTime(ticket.updatedAt)}
                </div>
            </div>

            {formError && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-md p-3 mb-4 text-sm">
                    {formError}
                </div>
            )}

            <div className="bg-white rounded-lg shadow p-5 mb-4">
                <h2 className="text-sm font-semibold text-slate-700 mb-3">Conversation</h2>
                <div>
                    {(ticket.messages || []).map((m) => (
                        <MessageBubble key={m.id} message={m} onRetry={onRetry} />
                    ))}
                    {(!ticket.messages || ticket.messages.length === 0) && (
                        <div className="text-sm text-slate-500 italic">No messages yet.</div>
                    )}
                </div>
            </div>

            <div className="bg-white rounded-lg shadow p-5 mb-4">
                <h2 className="text-sm font-semibold text-slate-700 mb-2">Reply to customer</h2>
                <form onSubmit={submitReply}>
                    <textarea
                        value={replyBody}
                        onChange={(e) => setReplyBody(e.target.value)}
                        rows={4}
                        maxLength={5000}
                        placeholder="Type your reply…"
                        className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        disabled={actions.busy}
                    />
                    <div className="flex justify-end mt-2">
                        <button
                            type="submit"
                            disabled={actions.busy || !replyBody.trim()}
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 text-sm font-medium"
                        >
                            {actions.busy ? 'Sending…' : 'Send reply'}
                        </button>
                    </div>
                </form>
            </div>

            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-5">
                <h2 className="text-sm font-semibold text-yellow-900 mb-2">
                    🔒 Internal note (admin only)
                </h2>
                <form onSubmit={submitNote}>
                    <textarea
                        value={noteBody}
                        onChange={(e) => setNoteBody(e.target.value)}
                        rows={3}
                        maxLength={5000}
                        placeholder="Add an internal note…"
                        className="w-full px-3 py-2 border border-yellow-300 rounded-md text-sm bg-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
                        disabled={actions.busy}
                    />
                    <div className="flex justify-end mt-2">
                        <button
                            type="submit"
                            disabled={actions.busy || !noteBody.trim()}
                            className="px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700 disabled:opacity-50 text-sm font-medium"
                        >
                            {actions.busy ? 'Saving…' : 'Add note'}
                        </button>
                    </div>
                </form>
            </div>
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