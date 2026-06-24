import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { UserPlus, User, Mail, Lock, AtSign, Eye, EyeOff } from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import type { RegisterRequest, RegisterResponse } from '../types/types'
import { getApiError } from '../utils/apiHelpers'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState<RegisterRequest>({
    firstName: '',
    lastName: '',
    username: '',
    userEmail: '',
    userPassword: '',
  })
  const [confirmPassword, setConfirmPassword] = useState<string>('')
  const [showPassword, setShowPassword] = useState<boolean>(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false)

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (form.userPassword !== confirmPassword) {
      toast.error('Passwords do not match')
      return
    }

    const loadingToast = toast.loading('Creating account...')
    try {
      await axiosInstance.post<RegisterResponse>('/api/auth/register', form)
      toast.dismiss(loadingToast)
      toast.success('Account created! Please verify your email.')
      navigate('/verify-otp', { state: { email: form.userEmail } })
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Registration failed'))
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
              <UserPlus className="h-6 w-6 text-emerald-400" />
            </div>
            <h1 className="gradient-text text-3xl font-bold tracking-tight">Create account</h1>
            <p className="mt-2 text-sm text-gray-400">Start tracking your finances with Finate</p>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor="firstName" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
                  First name
                </label>
                <div className="relative">
                  <User className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
                  <input
                    id="firstName"
                    name="firstName"
                    required
                    value={form.firstName}
                    onChange={handleChange}
                    placeholder="John"
                    className="input-dark pl-10"
                  />
                </div>
              </div>
              <div>
                <label htmlFor="lastName" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
                  Last name
                </label>
                <div className="relative">
                  <User className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
                  <input
                    id="lastName"
                    name="lastName"
                    required
                    value={form.lastName}
                    onChange={handleChange}
                    placeholder="Doe"
                    className="input-dark pl-10"
                  />
                </div>
              </div>
            </div>

            <div>
              <label htmlFor="username" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
                Username
              </label>
              <div className="relative">
                <AtSign className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
                <input
                  id="username"
                  name="username"
                  required
                  value={form.username}
                  onChange={handleChange}
                  placeholder="johndoe"
                  className="input-dark pl-10"
                />
              </div>
            </div>

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
                  minLength={8}
                  value={form.userPassword}
                  onChange={handleChange}
                  placeholder="Min. 8 characters"
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

            <div>
              <label htmlFor="confirmPassword" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
                Confirm Password
              </label>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  required
                  minLength={8}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter your password"
                  className="input-dark pl-10 pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword((prev) => !prev)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 transition-colors hover:text-gray-300"
                  tabIndex={-1}
                >
                  {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn-primary mt-1 py-3 text-base font-semibold">
              Register
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-500">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-emerald-400 transition-colors hover:text-emerald-300">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
