import { describe, it, expect, vi, beforeEach } from 'vitest';
import MockAdapter from 'axios-mock-adapter';
import api from './axios';

const mock = new MockAdapter(api);

describe('Axios API Instance', () => {
  beforeEach(() => {
    mock.reset();
    vi.clearAllMocks();
  });

  it('should return response data directly on successful requests', async () => {
    const mockData = { id: 1, name: 'Test User' };
    mock.onGet('/test').reply(200, mockData);

    const result = await api.get('/test');
    expect(result).toEqual(mockData);
  });

  it('should redirect to /login when a 401 error occurs', async () => {
    // Mock window.location
    const originalLocation = window.location;

    // Use Object.defineProperty to mock window.location.href since it's read-only
    const locationMock = {
      pathname: '/dashboard',
      href: '/dashboard'
    };

    vi.stubGlobal('location', locationMock);

    mock.onGet('/protected').reply(401);

    try {
      await api.get('/protected');
    } catch (error) {
      // Error is expected
    }

    expect(locationMock.href).toBe('/login');
    vi.unstubAllGlobals();
  });

  it('should not redirect to /login if already on the login page', async () => {
    const locationMock = {
      pathname: '/login',
      href: '/login'
    };

    vi.stubGlobal('location', locationMock);

    mock.onGet('/protected').reply(401);

    try {
      await api.get('/protected');
    } catch (error) {
      // Error is expected
    }

    expect(locationMock.href).toBe('/login'); // It should remain /login
    vi.unstubAllGlobals();
  });
});
