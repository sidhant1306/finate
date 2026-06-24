import { useEffect, useRef, useState } from 'react'
import toast from 'react-hot-toast'
import { useLocation } from 'react-router-dom'
import {
  Wallet,
  TrendingUp,
  PiggyBank,
  ArrowDownCircle,
  ArrowUpCircle,
  Sparkles,
  Crown,
  Calendar,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import { useAuth } from '../context/AuthContext'
import type { DashboardResponse, ExpenseTransactionResponse } from '../types/types'
import { formatCategoryLabel, formatCurrency } from '../types/types'
import { getApiError } from '../utils/apiHelpers'
import { categoryEmoji } from '../utils/categories'

function StatCard({ label, value, icon: Icon, accent, delay }: {
  label: string; value: string; icon: typeof Wallet; accent?: 'green' | 'red'; delay: string
}) {
  return (
    <div className={`stat-card ${delay}`}>
      <div className="relative z-10 flex items-start justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wider text-gray-400">{label}</p>
          <p className={`mt-2 text-xl font-bold ${
            accent === 'green' ? 'text-emerald-400' : accent === 'red' ? 'text-red-400' : 'text-white'
          }`}>
            {value}
          </p>
        </div>
        <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${
          accent === 'green' ? 'bg-emerald-500/10' : accent === 'red' ? 'bg-red-500/10' : 'bg-white/5'
        }`}>
          <Icon className={`h-5 w-5 ${
            accent === 'green' ? 'text-emerald-400' : accent === 'red' ? 'text-red-400' : 'text-gray-300'
          }`} strokeWidth={1.5} />
        </div>
      </div>
    </div>
  )
}

function TransactionRow({ transaction }: { transaction: ExpenseTransactionResponse }) {
  const isDebit = transaction.expenseTransactionType === 'DEBIT'
  const emoji = categoryEmoji[transaction.expenseTransactionCategory] || '📌'
  return (
    <div className="flex items-center gap-3 border-b border-white/[0.04] py-3.5 last:border-0">
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-white/5 text-sm">
        {emoji}
      </div>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-gray-200">
          {formatCategoryLabel(transaction.expenseTransactionCategory)}
        </p>
        <p className="truncate text-xs text-gray-500">
          {transaction.expenseTransactionDate}
          {transaction.expenseDescription ? ` · ${transaction.expenseDescription}` : ''}
        </p>
      </div>
      <span className={`shrink-0 text-sm font-semibold ${isDebit ? 'text-red-400' : 'text-emerald-400'}`}>
        {isDebit ? '-' : '+'}
        {formatCurrency(transaction.expenseAmount)}
      </span>
    </div>
  )
}

export default function Dashboard() {
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null)
  const [weeklyInsight, setWeeklyInsight] = useState<string | null>(null)
  const [insightLoading, setInsightLoading] = useState<boolean>(true)
  const [insightRequested, setInsightRequested] = useState<boolean>(false)
  const { firstName, userRole } = useAuth()
  const location = useLocation()
  const fromLogin = location.state?.fromLogin
  const toastFired = useRef(false)
  const firstNameRef = useRef(firstName)
  const userRoleRef = useRef(userRole)
  firstNameRef.current = firstName
  userRoleRef.current = userRole

  useEffect(() => {
    const fetchDashboard = async () => {
      const loadingToast = toast.loading('Loading dashboard...')
      try {
        const dashboardRes = await axiosInstance.get<DashboardResponse>('/api/dashboard/get-dashboard')
        setDashboard(dashboardRes.data)
        toast.dismiss(loadingToast)
        if (!toastFired.current && fromLogin) {
          toastFired.current = true
          toast.success(`Welcome back, ${firstNameRef.current || 'there'}!`)
        }
      } catch (error) {
        toast.dismiss(loadingToast)
        toast.error(getApiError(error, 'Failed to load dashboard'))
      }
    }

      const fetchWeeklyInsight = async () => {
      if (insightRequested) {
        return
      }
      if (userRoleRef.current !== 'PREMIUM') {
        setInsightLoading(false)
        return
      }
      setInsightRequested(true)
      try {
        const res = await axiosInstance.get<{ insight: string }>('/api/ai/get-weekly-insight')
        setWeeklyInsight(res.data.insight)
      } catch {
        // Non-critical — silently skip
        setInsightRequested(false)
      } finally {
        setInsightLoading(false)
      }
    }

    void fetchDashboard()
    void fetchWeeklyInsight()
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleGenerateInsight = async () => {
    const loadingToast = toast.loading('Generating AI insight... This may take a moment.')
    try {
      await axiosInstance.post('/api/ai/generate-weekly-insight')
      toast.dismiss(loadingToast)
      toast.success('Insight generated & emailed successfully!')
      
      // Fetch the newly generated insight to update the UI
      setInsightLoading(true)
      const res = await axiosInstance.get<{ insight: string }>('/api/ai/get-weekly-insight')
      setWeeklyInsight(res.data.insight)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to generate insight'))
    } finally {
      setInsightLoading(false)
    }
  }

  const transactions = dashboard?.last30DaysExpenseTransactionList ?? []
  const today = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long' })

  return (
    <div className="mx-auto max-w-4xl">
      {/* Header */}
      <div className="animate-fade-in flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-500">
            <Calendar className="mr-1 inline h-3.5 w-3.5" />{today}
          </p>
          <h1 className="mt-1 text-2xl font-bold text-white">
            Hey, <span className="gradient-text">{firstName || 'there'}</span> 👋
          </h1>
        </div>
        {userRole && (
          <span className={`badge ${userRole === 'PREMIUM' ? 'badge-premium' : 'badge-accent'}`}>
            {userRole === 'PREMIUM' && <Crown className="h-3 w-3" />}
            {userRole}
          </span>
        )}
      </div>

      {/* Stat Cards */}
      <div className="mt-6 grid grid-cols-2 gap-3">
        <StatCard label="Wallet Balance" value={formatCurrency(dashboard?.walletBalance)} icon={Wallet} delay="animate-fade-in-delay-1" />
        <StatCard label="Net Worth" value={formatCurrency(dashboard?.netWorth)} icon={TrendingUp} delay="animate-fade-in-delay-2" />
        <StatCard label="Budget Left" value={formatCurrency(dashboard?.totalBudgetRemaining)} icon={PiggyBank} delay="animate-fade-in-delay-3" />
        <StatCard
          label="Debit · 30d"
          value={formatCurrency(dashboard?.totalDebitAmountLast30Days)}
          icon={ArrowDownCircle}
          accent="red"
          delay="animate-fade-in-delay-3"
        />
        <div className="col-span-2 animate-fade-in-delay-4">
          <StatCard
            label="Credit · 30d"
            value={formatCurrency(dashboard?.totalCreditAmountLast30Days)}
            icon={ArrowUpCircle}
            accent="green"
            delay=""
          />
        </div>
      </div>

      {/* AI Insight */}
      <section className="animate-fade-in-delay-3 mt-6 glass-card p-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="h-4 w-4 text-amber-400" />
            <p className="text-xs font-semibold uppercase tracking-wider text-amber-400">AI Insight · This Week</p>
          </div>
          {userRole === 'PREMIUM' && (
            <button
              onClick={() => void handleGenerateInsight()}
              disabled={insightLoading}
              className="rounded-lg bg-amber-500/10 px-3 py-1.5 text-xs font-semibold text-amber-400 transition-colors hover:bg-amber-500/20 disabled:opacity-50"
            >
              {weeklyInsight ? 'Regenerate Insight' : 'Generate Insight'}
            </button>
          )}
        </div>
        <div className="mt-3">
          {insightLoading ? (
            <div className="flex flex-col gap-2">
              <div className="h-4 w-3/4 rounded bg-white/5 animate-pulse" />
              <div className="h-4 w-1/2 rounded bg-white/5 animate-pulse" />
            </div>
          ) : weeklyInsight ? (
            <div className="text-sm leading-relaxed text-gray-300">
              {weeklyInsight.split('\n').map((line, i) => (
                <span key={i}>
                  {line}
                  <br />
                </span>
              ))}
            </div>
          ) : userRole !== 'PREMIUM' ? (
            <p className="text-sm text-gray-500">Upgrade to <span className="font-medium text-amber-400">Premium</span> to unlock AI-powered weekly insights.</p>
          ) : (
            <p className="text-sm text-gray-500">No insight available this week. Click 'Generate Insight' to get your latest financial analysis.</p>
          )}
        </div>
      </section>

      {/* Recent Transactions */}
      <section className="animate-fade-in-delay-4 mt-6 glass-card p-5">
        <h2 className="text-base font-semibold text-white">Recent expenses</h2>
        <p className="text-xs text-gray-500">Last 30 days</p>
        <div className="mt-3">
          {transactions.length > 0 ? (
            [...transactions].reverse().slice(0, 10).map((transaction) => (
              <TransactionRow key={transaction.expenseTransactionId} transaction={transaction} />
            ))
          ) : (
            <p className="py-6 text-center text-sm text-gray-500">No transactions in the last 30 days.</p>
          )}
        </div>
      </section>
    </div>
  )
}
