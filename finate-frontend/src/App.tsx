import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import OtpVerify from './pages/OtpVerify'
import Dashboard from './pages/Dashboard'
import Wallet from './pages/Wallet'
import Tracking from './pages/Tracking'
import Budget from './pages/Budget'
import Stocks from './pages/Stocks'
import Portfolio from './pages/Portfolio'
import Premium from './pages/Premium'
import AiChat from './pages/AiChat'
import Help from './pages/Help'

export default function App() {
  const { token } = useAuth()

  return (
    <Routes>
      {/* Public routes — redirect to dashboard if already logged in */}
      <Route
        path="/login"
        element={token ? <Navigate to="/dashboard" replace /> : <Login />}
      />
      <Route
        path="/register"
        element={token ? <Navigate to="/dashboard" replace /> : <Register />}
      />
      <Route
        path="/verify-otp"
        element={token ? <Navigate to="/dashboard" replace /> : <OtpVerify />}
      />

      {/* Protected routes — ProtectedRoute checks token and redirects to /login if missing */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/wallet" element={<Wallet />} />
        <Route path="/tracking" element={<Tracking />} />
        <Route path="/budget" element={<Budget />} />
        <Route path="/stocks" element={<Stocks />} />
        <Route path="/portfolio" element={<Portfolio />} />
        <Route path="/premium" element={<Premium />} />
        <Route path="/ai-chat" element={<AiChat />} />
        <Route path="/help" element={<Help />} />
      </Route>

      {/* Any unknown URL → send to login (not dashboard) */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
