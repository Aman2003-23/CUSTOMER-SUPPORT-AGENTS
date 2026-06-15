import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, act } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import api from '../api/axios';
import React from 'react';

vi.mock('../api/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

// Helper component to access the hook
const AuthConsumer = () => {
  const { user, isAuthenticated, loading, login, logout, checkAuth } = useAuth();
  return (
    <div data-testid="auth-consumer">
      <span data-testid="user">{user ? user.username : 'null'}</span>
      <span data-testid="auth">{isAuthenticated.toString()}</span>
      <span data-testid="loading">{loading.toString()}</span>
      <button onClick={() => login('testuser', 'password')} data-testid="login-btn">Login</button>
      <button onClick={() => logout()} data-testid="logout-btn">Logout</button>
      <button onClick={() => checkAuth()} data-testid="check-auth-btn">CheckAuth</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with loading state and then check authentication', async () => {
    const mockUser = { username: 'testuser', id: '123' };
    api.get.mockResolvedValueOnce(mockUser);

    let wrapper;
    await act(async () => {
      wrapper = render(
        <AuthProvider>
          <AuthConsumer />
        </AuthProvider>
      );
    });

    const userEl = wrapper.getByTestId('user');
    const authEl = wrapper.getByTestId('auth');

    expect(userEl.textContent).toBe('testuser');
    expect(authEl.textContent).toBe('true');
  });

  it('should handle authentication failure during initialization', async () => {
    api.get.mockRejectedValueOnce(new Error('Unauthorized'));

    let wrapper;
    await act(async () => {
      wrapper = render(
        <AuthProvider>
          <AuthConsumer />
        </AuthProvider>
      );
    });

    const userEl = wrapper.getByTestId('user');
    const authEl = wrapper.getByTestId('auth');

    expect(userEl.textContent).toBe('null');
    expect(authEl.textContent).toBe('false');
  });

  it('should log in the user successfully', async () => {
    api.get.mockResolvedValueOnce(null); // Initial checkAuth call on mount
    api.post.mockResolvedValueOnce({}); // Login call
    api.get.mockResolvedValueOnce({ username: 'testuser' }); // Follow-up checkAuth call

    let wrapper;
    await act(async () => {
      wrapper = render(
        <AuthProvider>
          <AuthConsumer />
        </AuthProvider>
      );
    });

    await act(async () => {
      wrapper.getByTestId('login-btn').click();
    });

    expect(wrapper.getByTestId('user').textContent).toBe('testuser');
    expect(wrapper.getByTestId('auth').textContent).toBe('true');
  });

  it('should log out the user successfully', async () => {
    api.get.mockResolvedValueOnce({ username: 'testuser' }); // Initial checkAuth

    let wrapper;
    await act(async () => {
      wrapper = render(
        <AuthProvider>
          <AuthConsumer />
        </AuthProvider>
      );
    });

    await act(async () => {
      wrapper.getByTestId('logout-btn').click();
    });

    expect(wrapper.getByTestId('user').textContent).toBe('null');
    expect(wrapper.getByTestId('auth').textContent).toBe('false');
  });
});
