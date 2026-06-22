import { useState } from 'react'
import toast from 'react-hot-toast'
import {
  Crown,
  Sparkles,
  Shield,
  BarChart3,
  Zap,
  CheckCircle2,
  Star,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import { useAuth } from '../context/AuthContext'
import type { PaymentResponse, PaymentVerifyRequest } from '../types/types'
import { getApiError } from '../utils/apiHelpers'
import { loadRazorpay } from '../utils/razorpay'

const premiumFeatures = [
  { icon: Sparkles, label: 'AI-Powered Weekly Insights', description: 'Get personalized financial advice every week' },
  { icon: BarChart3, label: 'Advanced Analytics', description: 'Deep dive into your spending patterns' },
  { icon: Shield, label: 'Priority Support', description: 'Get help when you need it, faster' },
  { icon: Zap, label: 'Real-Time Alerts', description: 'Instant notifications for budget limits' },
]

export default function Premium() {
  const { userRole, setUserRole } = useAuth()
  const [loading, setLoading] = useState(false)
  const isPremium = userRole === 'PREMIUM'

  const handleUpgrade = async () => {
    await loadRazorpay()

    setLoading(true)
    const loadingToast = toast.loading('Creating payment order...')
    try {
      const { data: order } = await axiosInstance.post<PaymentResponse>('/wallet/create-order', {
        paymentAmount: 99,
        paymentType: 'PREMIUM',
      })
      toast.dismiss(loadingToast)

      const rzp = new window.Razorpay({
        key: order.keyId,
        amount: Math.round(order.amount * 100),
        currency: order.currency,
        name: 'Finate Premium',
        description: 'Upgrade to Premium',
        order_id: order.orderId,
        handler: async (response) => {
          const verifyToast = toast.loading('Verifying payment...')
          try {
            const verifyPayload: PaymentVerifyRequest = {
              paymentId: response.razorpay_payment_id,
              orderId: response.razorpay_order_id,
              signature: response.razorpay_signature,
              paymentType: 'PREMIUM',
              amount: order.amount,
            }
            await axiosInstance.post<string>('/wallet/verify-payment', verifyPayload)
            toast.dismiss(verifyToast)
            toast.success('🎉 Welcome to Premium!')
            setUserRole('PREMIUM')
          } catch (error) {
            toast.dismiss(verifyToast)
            toast.error(getApiError(error, 'Payment verification failed'))
          }
        },
        theme: { color: '#10B981' },
      })
      rzp.open()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to create order'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-4xl">
      <div className="animate-fade-in flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-500/10">
          <Crown className="h-5 w-5 text-amber-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Premium</h1>
          <p className="text-sm text-gray-500">Unlock the full Finate experience</p>
        </div>
      </div>

      {/* Current Plan */}
      <div className="animate-fade-in-delay-1 mt-6 glass-card p-5">
        <p className="text-xs font-medium uppercase tracking-wider text-gray-400">Current Plan</p>
        <div className="mt-3 flex items-center gap-3">
          {isPremium ? (
            <>
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-amber-500/20 to-orange-500/20 ring-1 ring-amber-500/30">
                <Crown className="h-6 w-6 text-amber-400" />
              </div>
              <div>
                <p className="premium-gradient text-xl font-bold">Premium</p>
                <p className="text-sm text-gray-400">You have access to all features</p>
              </div>
            </>
          ) : (
            <>
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/5 ring-1 ring-white/10">
                <Star className="h-6 w-6 text-gray-400" />
              </div>
              <div>
                <p className="text-xl font-bold text-white">Basic</p>
                <p className="text-sm text-gray-400">Upgrade to unlock premium features</p>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Features List */}
      <div className="animate-fade-in-delay-2 mt-6">
        <h2 className="text-base font-semibold text-white">Premium Features</h2>
        <div className="mt-3 flex flex-col gap-3">
          {premiumFeatures.map(({ icon: Icon, label, description }) => (
            <div key={label} className="glass-card flex items-start gap-3 p-4">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-emerald-500/10">
                <Icon className="h-4 w-4 text-emerald-400" />
              </div>
              <div>
                <p className="text-sm font-medium text-white">{label}</p>
                <p className="mt-0.5 text-xs text-gray-400">{description}</p>
              </div>
              {isPremium && <CheckCircle2 className="ml-auto h-5 w-5 shrink-0 text-emerald-400" />}
            </div>
          ))}
        </div>
      </div>

      {/* Upgrade Button */}
      {!isPremium && (
        <div className="animate-fade-in-delay-3 mt-8">
          <div className="glass-card overflow-hidden">
            <div className="bg-gradient-to-r from-emerald-500/10 via-amber-500/10 to-purple-500/10 p-6 text-center">
              <p className="text-3xl font-bold text-white">
                ₹99
                <span className="ml-1 text-base font-normal text-gray-400">one-time</span>
              </p>
              <p className="mt-1 text-sm text-gray-400">Unlock all premium features forever</p>
              <button
                onClick={() => void handleUpgrade()}
                disabled={loading}
                className="btn-primary mt-5 w-full py-3.5 text-base font-semibold disabled:opacity-50"
              >
                {loading ? 'Processing...' : '✨ Upgrade to Premium'}
              </button>
            </div>
          </div>
        </div>
      )}

      {isPremium && (
        <div className="animate-fade-in-delay-3 mt-8 glass-card p-6 text-center">
          <CheckCircle2 className="mx-auto h-12 w-12 text-emerald-400" />
          <p className="mt-3 text-lg font-semibold text-white">You're all set!</p>
          <p className="mt-1 text-sm text-gray-400">Enjoy your premium features across Finate.</p>
        </div>
      )}
    </div>
  )
}
