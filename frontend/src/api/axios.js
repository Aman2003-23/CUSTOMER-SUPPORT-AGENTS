import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => {
    // Return only the data part of the response
    return response.data;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      console.error('Unauthorized request - redirecting to login...');
      // In a real app, we might trigger a state update via a store or an event
      // For now, we can redirect to the login page if we are not already there
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    // Normalize: surface the backend's error message on the rejected error so callers can read err.message.
    const serverMsg = error.response?.data?.message || error.response?.data?.error;
    if (serverMsg) {
      error.message = typeof serverMsg === 'string' ? serverMsg : JSON.stringify(serverMsg);
    } else if (error.message) {
      // keep default axios message
    }
    return Promise.reject(error);
  }
);

export default api;
