import api from './axios';

// Thin wrapper around the shared axios instance. The interceptor in axios.js
// already unwraps response.data and redirects on 401.
export const fetchTickets = (params) =>
    api.get('/tickets', { params }).then((data) => data);

export const fetchTicket = (id) =>
    api.get(`/tickets/${id}`).then((data) => data);

export const changeTicketStatus = (id, status) =>
    api.patch(`/tickets/${id}/status`, { status }).then((data) => data);

export const postReply = (id, body) =>
    api.post(`/tickets/${id}/reply`, { body }).then((data) => data);

export const postNote = (id, body) =>
    api.post(`/tickets/${id}/notes`, { body }).then((data) => data);

export const retryMessage = (id, messageId) =>
    api.post(`/tickets/${id}/messages/${messageId}/retry`).then((data) => data);