import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [health, setHealth] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then(response => {
        if (!response.ok) {
          throw new Error('Backend server is not responding')
        }
        return response.json()
      })
      .then(data => {
        setHealth(data)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center font-sans">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md border border-gray-200">
        <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
          Customer Support System
        </h1>

        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-md border border-gray-100">
            <span className="text-gray-600 font-medium">Backend Status</span>
            {loading ? (
              <span className="text-sm text-gray-400 italic">Checking...</span>
            ) : error ? (
              <span className="text-sm font-bold text-red-500">🔴 {error}</span>
            ) : (
              <span className={`text-sm font-bold ${health.status === 'UP' ? 'text-green-600' : 'text-red-500'}`}>
                {health.status === 'UP' ? '🟢 Operational' : '🔴 Down'}
              </span>
            )}
          </div>

          {!loading && health && (
            <div className="text-xs text-gray-400 text-right italic">
              Last checked: {new Date(health.timestamp).toLocaleString()}
            </div>
          )}
        </div>

        <div className="mt-8 pt-6 border-t border-gray-100 text-center">
          <p className="text-gray-500 text-sm">
            System monitoring dashboard
          </p>
        </div>
      </div>
    </div>
  )
}

export default App