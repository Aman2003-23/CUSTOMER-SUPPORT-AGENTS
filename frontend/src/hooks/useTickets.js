import { useCallback, useEffect, useRef, useState } from 'react';
import {
    fetchTicket,
    fetchTickets,
    postReply,
    postNote,
    changeTicketStatus,
    retryMessage,
} from '../api/tickets';

/**
 * Hook for the ticket queue. Reads filters from the URL search params
 * via the caller (TicketsPage passes them in), and re-fetches when they change.
 */
export function useTicketsList(filters) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        fetchTickets(filters)
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
    }, [JSON.stringify(filters)]);

    return { data, error, loading };
}

export function useTicket(id) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    const reload = useCallback(() => {
        let cancelled = false;
        setLoading(true);
        fetchTicket(id)
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
    }, [id]);

    useEffect(() => {
        const cleanup = reload();
        return cleanup;
    }, [reload]);

    return { data, error, loading, reload };
}

/**
 * Hook for ticket-detail mutations. Keeps the loaded ticket in sync after
 * each optimistic update so we don't have to refetch the whole detail.
 */
export function useTicketActions(ticket, reload) {
    const [busy, setBusy] = useState(false);
    const [actionError, setActionError] = useState(null);
    const ticketRef = useRef(ticket);
    ticketRef.current = ticket;

    const updateLocal = useCallback((updater) => {
        ticketRef.current = updater(ticketRef.current);
    }, []);

    const sendReply = useCallback(
        async (body) => {
            if (!ticketRef.current) return;
            setBusy(true);
            setActionError(null);
            // Optimistic: append a placeholder agent reply with PENDING status.
            const optimisticId = `pending-${Date.now()}`;
            updateLocal((t) => ({
                ...t,
                messages: [
                    ...(t.messages || []),
                    {
                        id: optimisticId,
                        createdAt: new Date().toISOString(),
                        author: 'AGENT',
                        kind: 'REPLY',
                        sendStatus: 'PENDING',
                        body,
                    },
                ],
            }));
            try {
                const saved = await postReply(ticketRef.current.id, body);
                updateLocal((t) => ({
                    ...t,
                    messages: (t.messages || []).map((m) =>
                        m.id === optimisticId ? saved : m,
                    ),
                }));
                return saved;
            } catch (err) {
                // Mark the optimistic message as failed.
                updateLocal((t) => ({
                    ...t,
                    messages: (t.messages || []).map((m) =>
                        m.id === optimisticId
                            ? { ...m, sendStatus: 'FAILED', sendError: err?.message || 'Send failed' }
                            : m,
                    ),
                }));
                setActionError(err);
                throw err;
            } finally {
                setBusy(false);
            }
        },
        [updateLocal],
    );

    const sendNote = useCallback(
        async (body) => {
            if (!ticketRef.current) return;
            setBusy(true);
            setActionError(null);
            try {
                const saved = await postNote(ticketRef.current.id, body);
                updateLocal((t) => ({
                    ...t,
                    messages: [...(t.messages || []), saved],
                }));
                return saved;
            } catch (err) {
                setActionError(err);
                throw err;
            } finally {
                setBusy(false);
            }
        },
        [updateLocal],
    );

    const changeStatus = useCallback(
        async (status) => {
            if (!ticketRef.current) return;
            setBusy(true);
            setActionError(null);
            try {
                const updated = await changeTicketStatus(ticketRef.current.id, status);
                updateLocal((t) => ({ ...t, ...updated, messages: t.messages || [] }));
                return updated;
            } catch (err) {
                setActionError(err);
                throw err;
            } finally {
                setBusy(false);
            }
        },
        [updateLocal],
    );

    const retry = useCallback(
        async (messageId) => {
            if (!ticketRef.current) return;
            setBusy(true);
            setActionError(null);
            try {
                const updated = await retryMessage(ticketRef.current.id, messageId);
                updateLocal((t) => ({
                    ...t,
                    messages: (t.messages || []).map((m) =>
                        m.id === messageId ? updated : m,
                    ),
                }));
                return updated;
            } catch (err) {
                setActionError(err);
                throw err;
            } finally {
                setBusy(false);
            }
        },
        [updateLocal],
    );

    return {
        busy,
        error: actionError,
        sendReply,
        sendNote,
        changeStatus,
        retry,
        reload,
    };
}