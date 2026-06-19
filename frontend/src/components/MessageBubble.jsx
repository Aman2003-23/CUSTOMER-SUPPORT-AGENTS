import React from 'react';

/**
 * Renders a single ticket message bubble. Replies and notes are visually
 * distinct. For agent replies, surfaces the send status with a retry button
 * when delivery failed.
 */
export default function MessageBubble({ message, onRetry }) {
    const isAgent = message.author === 'AGENT';
    const isNote = message.kind === 'INTERNAL_NOTE';
    const isCustomer = message.author === 'CUSTOMER';

    const bubbleBase = 'rounded-lg px-4 py-3 max-w-2xl border';
    const bubbleStyle = isNote
        ? 'bg-yellow-50 border-yellow-300 text-yellow-900'
        : isAgent
            ? 'bg-blue-50 border-blue-200 text-blue-900'
            : 'bg-white border-slate-200 text-slate-900';

    return (
        <div className={`flex ${isAgent ? 'justify-end' : 'justify-start'} mb-3`}>
            <div className={`${bubbleBase} ${bubbleStyle}`}>
                <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-semibold uppercase tracking-wide">
                        {isAgent ? (isNote ? 'Internal Note' : 'Agent') : 'Customer'}
                    </span>
                    {isNote && (
                        <span className="text-xs bg-yellow-200 text-yellow-900 px-1.5 py-0.5 rounded">
                            🔒 Internal
                        </span>
                    )}
                    <span className="text-xs text-slate-500">
                        {formatTime(message.createdAt)}
                    </span>
                </div>
                <div className="whitespace-pre-wrap break-words text-sm leading-relaxed">
                    {message.body}
                </div>
                {isAgent && !isNote && (
                    <SendStatusRow message={message} onRetry={onRetry} />
                )}
            </div>
        </div>
    );
}

function SendStatusRow({ message, onRetry }) {
    const status = message.sendStatus;
    if (!status || status === 'NOT_APPLICABLE') return null;

    if (status === 'PENDING') {
        return <div className="mt-2 text-xs text-slate-500">⏳ Sending…</div>;
    }
    if (status === 'SENT') {
        return <div className="mt-2 text-xs text-emerald-600">✓ Sent</div>;
    }
    if (status === 'FAILED') {
        return (
            <div className="mt-2 flex items-center gap-2">
                <span className="text-xs text-red-700">⚠️ Failed to send</span>
                {message.sendError && (
                    <span className="text-xs text-red-500 truncate max-w-xs" title={message.sendError}>
                        {message.sendError}
                    </span>
                )}
                {onRetry && (
                    <button
                        type="button"
                        onClick={() => onRetry(message.id)}
                        className="text-xs px-2 py-0.5 bg-red-100 text-red-700 border border-red-300 rounded hover:bg-red-200"
                    >
                        Retry
                    </button>
                )}
            </div>
        );
    }
    if (status === 'SKIPPED') {
        return (
            <div className="mt-2 text-xs text-slate-500" title={message.sendError || ''}>
                ⏭ Email sending disabled
            </div>
        );
    }
    return null;
}

function formatTime(iso) {
    if (!iso) return '';
    try {
        const d = new Date(iso);
        return d.toLocaleString();
    } catch {
        return iso;
    }
}