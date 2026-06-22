import { useState, type FormEvent, type ChangeEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { LogIn, Mail, Lock, Eye, EyeOff } from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import { useAuth } from '../context/AuthContext'
import type { LoginRequest, LoginResponse } from '../types/types'
import { getApiError } from '../utils/apiHelpers'

export default function Login() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState<LoginRequest>({
    userEmail: '',
    userPassword: '',
  })
  const [showPassword, setShowPassword] = useState<boolean>(false)

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const loadingToast = toast.loading('Signing in...')
    try {
      const { data } = await axiosInstance.post<LoginResponse>('/auth/login', form)
      login(data.token, data.firstName, data.lastName, data.userEmail, data.username, data.userRole)
      toast.dismiss(loadingToast)
      toast.success(`Welcome back, ${data.firstName}! 🎉`)
      navigate('/dashboard', { state: { fromLogin: true } })
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Login failed'))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-[#0B0B0F] px-4 py-6">
      {/* Subtle radial gradient background */}
      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute left-1/2 top-0 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-emerald-500/[0.04] blur-3xl" />
      </div>

      <div className="animate-fade-in relative w-full max-w-md">
        <div className="glass-card p-8">
          {/* Brand */}
          <div className="mb-8 text-center">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-500/10 ring-1 ring-emerald-500/20">
              <LogIn className="h-6 w-6 text-emerald-400" />
            </div>
            <h1 className="gradient-text text-3xl font-bold tracking-tight">Finate</h1>
            <p className="mt-2 text-sm text-gray-400">Sign in to your account</p>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <div>
              <label htmlFor="userEmail" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
                Email
              </label>
              <div className="relative">
                <Mail className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
                <input
                  id="userEmail"
                  name="userEmail"
                  type="email"
                  required
                  value={form.userEmail}
                  onChange={handleChange}
                  placeholder="you@example.com"
                  className="input-dark pl-10"
                />
              </div>
            </div>

            <div>
              <label htmlFor="userPassword" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
                Password
              </label>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
                <input
                  id="userPassword"
                  name="userPassword"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={form.userPassword}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="input-dark pl-10 pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 transition-colors hover:text-gray-300"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn-primary mt-1 py-3 text-base font-semibold">
              Sign in
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-500">
            No account?{' '}
            <Link to="/register" className="font-medium text-emerald-400 transition-colors hover:text-emerald-300">
              Register
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
