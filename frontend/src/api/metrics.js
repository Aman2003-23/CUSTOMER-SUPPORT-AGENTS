import api from './axios';

// Thin wrapper around the shared axios instance. The interceptor in axios.js
// already unwraps response.data and redirects on 401.
export const fetchDashboardMetrics = (days = 14) =>
    api.get('/metrics/dashboard', { params: { days } }).then((data) => data);