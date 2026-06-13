import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex font-sans">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-800 text-white flex flex-col">
        <div className="p-6 text-xl font-bold border-b border-slate-700">
          Support System
        </div>
        <nav className="flex-grow p-4 space-y-2">
          <Link
            to="/dashboard/home"
            className="block p-3 rounded-md hover:bg-slate-700 transition-colors"
          >
            🏠 Home
          </Link>
          <Link
            to="/dashboard/tickets"
            className="block p-3 rounded-md hover:bg-slate-700 transition-colors"
          >
            🎫 Tickets
          </Link>
        </nav>
        <div className="p-4 border-t border-slate-700">
          <div className="mb-4 text-sm text-slate-400">
            Logged in as: <span className="text-white font-medium">{user?.username}</span>
          </div>
          <button
            onClick={handleLogout}
            className="w-full py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors text-sm font-medium"
          >
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-grow p-8">
        <div className="max-w-6xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
