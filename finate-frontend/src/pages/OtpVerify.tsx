import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate, Navigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { KeyRound } from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import { getApiError } from '../utils/apiHelpers'

export default function OtpVerify() {
  const navigate = useNavigate()
  const location = useLocation()
  const userEmail = location.state?.email as string | undefined

  const [otp, setOtp] = useState('')
  const [attempts, setAttempts] = useState(0)

  if (!userEmail) {
    return <Navigate to="/register" replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!otp || otp.length !== 6) {
      toast.error('Please enter a valid 6-digit OTP')
      return
    }

    const loadingToast = toast.loading('Verifying OTP...')
    try {
      const response = await axiosInstance.post<boolean>('/auth/verify-otp', {
        otp: Number(otp),
        userEmail,
      })
      toast.dismiss(loadingToast)

      if (response.data) {
        toast.success('Email verified successfully! 🎉 Please sign in.')
        navigate('/login')
      } else {
        if (attempts >= 2) {
          toast.error('Too many incorrect attempts. Please register again.')
          navigate('/register', { replace: true })
        } else {
          toast.error(`Invalid OTP. ${2 - attempts} attempt(s) remaining.`)
          setAttempts((prev) => prev + 1)
          setOtp('')
        }
      }
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'OTP verification failed'))
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
              <KeyRound className="h-6 w-6 text-emerald-400" />
            </div>
            <h1 className="gradient-text text-3xl font-bold tracking-tight">Verify Email</h1>
            <p className="mt-2 text-sm text-gray-400">
              We sent a 6-digit code to <span className="text-gray-200">{userEmail}</span>
            </p>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label htmlFor="otp" className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400 text-center">
                Enter OTP
              </label>
              <div className="flex justify-center">
                <input
                  id="otp"
                  name="otp"
                  type="text"
                  maxLength={6}
                  required
                  value={otp}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, ''); // only numbers
                    setOtp(value);
                  }}
                  placeholder="000000"
                  className="input-dark w-48 text-center text-2xl tracking-[0.5em] placeholder:tracking-normal"
                />
              </div>
            </div>

            <button type="submit" className="btn-primary mt-4 py-3 text-base font-semibold">
              Verify
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
