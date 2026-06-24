import { useCallback, useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import toast from 'react-hot-toast'
import {
  Wallet as WalletIcon,
  Plus,
  Send,
  ArrowDownCircle,
  ArrowUpCircle,
  Clock,
  Crown,
  Zap,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import type {
  PaymentResponse,
  PaymentVerifyRequest,
  UpiPaymentRequest,
  UpiPaymentResponse,
  WalletSummaryResponse,
  WalletTransactionResponse,
} from '../types/types'
import { formatCurrency } from '../types/types'
import { getApiError } from '../utils/apiHelpers'
import { loadRazorpay } from '../utils/razorpay'

function getTransactionLabel(tx: WalletTransactionResponse): string {
  if (tx.transactionType === 'DEPOSIT') return 'Wallet Deposit'
  if (tx.transactionType === 'UPI') {
    if (tx.recipientId) return `UPI Transfer · To User #${tx.recipientId}`
    return 'UPI Transfer'
  }
  if (tx.paymentType === 'PREMIUM') return 'Premium Subscription'
  if (tx.transactionType === 'DEBIT') {
    if (tx.recipientId) return `Sent to User #${tx.recipientId}`
    return 'Wallet Debit'
  }
  if (tx.transactionType === 'CREDIT') {
    if (tx.recipientId) return `Received from User #${tx.recipientId}`
    return 'Wallet Credit'
  }
  return tx.transactionType
}

function getTransactionIcon(tx: WalletTransactionResponse) {
  if (tx.paymentType === 'PREMIUM') return <Crown className="h-4 w-4 text-amber-400" />
  if (tx.transactionType === 'DEPOSIT') return <ArrowDownCircle className="h-4 w-4 text-emerald-400" />
  if (tx.transactionType === 'UPI') return <Zap className="h-4 w-4 text-blue-400" />
  if (tx.transactionType === 'CREDIT') return <ArrowDownCircle className="h-4 w-4 text-emerald-400" />
  return <ArrowUpCircle className="h-4 w-4 text-red-400" />
}

function getStatusBadgeClass(status: string): string {
  if (status === 'SUCCESS') return 'badge-accent'
  if (status === 'PENDING') return 'badge-premium'
  return 'badge-danger'
}

export default function Wallet() {
  const [wallet, setWallet] = useState<WalletSummaryResponse | null>(null)
  const [depositAmount, setDepositAmount] = useState<string>('')
  const [withdrawForm, setWithdrawForm] = useState<UpiPaymentRequest>({
    receiverUserid: 0,
    amount: 0,
  })

  const fetchWallet = useCallback(async () => {
    const { data } = await axiosInstance.get<WalletSummaryResponse>('/api/wallet/get-wallet-details')
    setWallet(data)
  }, [])

  const loadData = useCallback(async () => {
    const loadingToast = toast.loading('Loading wallet...')
    try {
      await fetchWallet()
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to load wallet'))
    }
  }, [fetchWallet])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const handleDeposit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const amount = Number(depositAmount)
    if (!amount || amount <= 0) {
      toast.error('Enter a valid amount')
      return
    }

    await loadRazorpay()

    const loadingToast = toast.loading('Creating payment order...')
    try {
      const { data: order } = await axiosInstance.post<PaymentResponse>('/api/wallet/create-order', {
        paymentAmount: amount,
        paymentType: 'WALLET',
      })
      toast.dismiss(loadingToast)

      const rzp = new window.Razorpay({
        key: order.keyId,
        amount: Math.round(order.amount * 100),
        currency: order.currency,
        name: 'Finate',
        description: 'Wallet deposit',
        order_id: order.orderId,
        handler: async (response) => {
          const verifyToast = toast.loading('Verifying payment...')
          try {
            const verifyPayload: PaymentVerifyRequest = {
              paymentId: response.razorpay_payment_id,
              orderId: response.razorpay_order_id,
              signature: response.razorpay_signature,
              paymentType: 'WALLET',
              amount: order.amount,
            }
            await axiosInstance.post<string>('/api/wallet/verify-payment', verifyPayload)
            toast.dismiss(verifyToast)
            toast.success('Deposit successful')
            setDepositAmount('')
            await fetchWallet()
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
    }
  }

  const handleWithdraw = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const loadingToast = toast.loading('Sending money...')
    try {
      const { data } = await axiosInstance.post<UpiPaymentResponse>('/api/upi/send-money', withdrawForm)
      toast.dismiss(loadingToast)
      toast.success(`Sent ${formatCurrency(withdrawForm.amount)} to ${data.receiverName}`)
      setWithdrawForm({ receiverUserid: 0, amount: 0 })
      await fetchWallet()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Transfer failed'))
    }
  }

  return (
    <div className="mx-auto max-w-4xl">
      <div className="animate-fade-in flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500/10">
          <WalletIcon className="h-5 w-5 text-emerald-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Wallet</h1>
          <p className="text-sm text-gray-500">Manage your balance</p>
        </div>
      </div>

      {/* Balance Card */}
      <div className="animate-fade-in-delay-1 mt-6 glass-card p-6 text-center">
        <p className="text-xs font-medium uppercase tracking-wider text-gray-400">Current Balance</p>
        <p className="mt-2 gradient-text text-4xl font-bold">
          {formatCurrency(wallet?.walletBalance)}
        </p>
      </div>

      <div className="mt-6 grid gap-5">
        {/* Deposit */}
        <section className="animate-fade-in-delay-2 glass-card p-5">
          <h2 className="flex items-center gap-2 text-base font-semibold text-white">
            <Plus className="h-4 w-4 text-emerald-400" /> Add Money
          </h2>
          <p className="mt-1 text-xs text-gray-500">Deposit via Razorpay</p>
          <form onSubmit={handleDeposit} className="mt-4 flex gap-3">
            <input
              type="number"
              min="1"
              step="0.01"
              placeholder="Amount"
              value={depositAmount}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setDepositAmount(e.target.value)}
              className="input-dark flex-1"
            />
            <button type="submit" className="btn-primary">Deposit</button>
          </form>
        </section>

        {/* Send Money */}
        <section className="animate-fade-in-delay-3 glass-card p-5">
          <h2 className="flex items-center gap-2 text-base font-semibold text-white">
            <Send className="h-4 w-4 text-blue-400" /> Send Money
          </h2>
          <p className="mt-1 text-xs text-gray-500">Transfer via UPI to another user</p>
          <form onSubmit={handleWithdraw} className="mt-4 flex flex-col gap-3">
            <input
              type="number"
              min="1"
              placeholder="Receiver user ID"
              value={withdrawForm.receiverUserid || ''}
              onChange={(e: ChangeEvent<HTMLInputElement>) =>
                setWithdrawForm((prev) => ({ ...prev, receiverUserid: Number(e.target.value) }))
              }
              className="input-dark"
            />
            <input
              type="number"
              min="1"
              step="0.01"
              placeholder="Amount"
              value={withdrawForm.amount || ''}
              onChange={(e: ChangeEvent<HTMLInputElement>) =>
                setWithdrawForm((prev) => ({ ...prev, amount: Number(e.target.value) }))
              }
              className="input-dark"
            />
            <button type="submit" className="btn-secondary">Send</button>
          </form>
        </section>
      </div>

      {/* Transaction History */}
      <section className="animate-fade-in-delay-4 mt-6 glass-card p-5">
        <h2 className="flex items-center gap-2 text-base font-semibold text-white">
          <Clock className="h-4 w-4 text-gray-400" /> Transaction History
        </h2>
        <div className="mt-3">
          {wallet?.walletTransactionResponseDtoList.length ? (
            [...wallet.walletTransactionResponseDtoList].reverse().map((tx, index) => (
              <WalletTransactionRow key={`${tx.transactionDate}-${index}`} transaction={tx} />
            ))
          ) : (
            <p className="py-6 text-center text-sm text-gray-500">No wallet transactions yet.</p>
          )}
        </div>
      </section>
    </div>
  )
}

function WalletTransactionRow({ transaction }: { transaction: WalletTransactionResponse }) {
  const isCredit = transaction.transactionType === 'CREDIT' || transaction.transactionType === 'DEPOSIT'
  const label = getTransactionLabel(transaction)
  const icon = getTransactionIcon(transaction)
  const statusClass = getStatusBadgeClass(transaction.transactionStatus)

  return (
    <div className="flex items-center gap-3 border-b border-white/[0.04] py-3.5 last:border-0">
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/5">
        {icon}
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-sm font-medium text-gray-200">{label}</p>
        <div className="mt-1 flex items-center gap-2">
          <span className="text-[11px] text-gray-500">{transaction.transactionDate}</span>
          <span className={`badge text-[9px] ${statusClass}`}>{transaction.transactionStatus}</span>
          <span className="rounded-md bg-white/[0.04] px-1.5 py-0.5 text-[9px] font-medium text-gray-400">
            {transaction.paymentType}
          </span>
        </div>
      </div>
      <span className={`shrink-0 text-sm font-semibold ${isCredit ? 'text-emerald-400' : 'text-red-400'}`}>
        {isCredit ? '+' : '-'}
        {formatCurrency(transaction.amount)}
      </span>
    </div>
  )
}
