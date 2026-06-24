import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  Briefcase,
  TrendingUp,
  TrendingDown,
  DollarSign,
  BarChart3,
  Calendar,
  ArrowUpRight,
  ArrowDownRight,
  Package,
  Crown,
  Lock,
  X,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import { useAuth } from '../context/AuthContext'
import type { ActiveHoldingResponse, ClosedPositionResponse, PortfolioResponse } from '../types/types'
import { formatCurrency } from '../types/types'
import { getApiError } from '../utils/apiHelpers'

function SummaryCard({ label, value, icon: Icon, accent }: {
  label: string; value: string; icon: typeof DollarSign; accent?: 'green' | 'red'
}) {
  return (
    <div className="stat-card">
      <div className="relative z-10 flex items-start justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wider text-gray-400">{label}</p>
          <p className={`mt-2 text-lg font-bold ${
            accent === 'green' ? 'text-emerald-400' : accent === 'red' ? 'text-red-400' : 'text-white'
          }`}>
            {value}
          </p>
        </div>
        <div className={`flex h-9 w-9 items-center justify-center rounded-xl ${
          accent === 'green' ? 'bg-emerald-500/10' : accent === 'red' ? 'bg-red-500/10' : 'bg-white/5'
        }`}>
          <Icon className={`h-4 w-4 ${
            accent === 'green' ? 'text-emerald-400' : accent === 'red' ? 'text-red-400' : 'text-gray-300'
          }`} strokeWidth={1.5} />
        </div>
      </div>
    </div>
  )
}

function HoldingCard({ holding, onSell }: { holding: ActiveHoldingResponse; onSell: () => void }) {
  const isProfit = holding.unrealizedPnL >= 0
  return (
    <div className="glass-card p-4">
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500/10">
            <TrendingUp className="h-5 w-5 text-emerald-400" strokeWidth={1.5} />
          </div>
          <div>
            <p className="text-sm font-semibold text-white">{holding.stockHolding.symbol}</p>
            <p className="text-xs text-gray-500">{holding.stockHolding.companyName}</p>
          </div>
        </div>
        <button onClick={onSell} className="btn-danger text-xs py-1.5 px-3">
          Sell
        </button>
      </div>
      <div className="mt-4 grid grid-cols-3 gap-3 text-center">
        <div>
          <p className="text-[10px] font-medium uppercase tracking-wider text-gray-500">Qty</p>
          <p className="mt-0.5 text-sm font-semibold text-gray-200">{holding.stockHolding.quantity}</p>
        </div>
        <div>
          <p className="text-[10px] font-medium uppercase tracking-wider text-gray-500">Avg Buy</p>
          <p className="mt-0.5 text-sm font-semibold text-gray-200">{formatCurrency(holding.stockHolding.buyPrice)}</p>
        </div>
        <div>
          <p className="text-[10px] font-medium uppercase tracking-wider text-gray-500">Current</p>
          <p className="mt-0.5 text-sm font-semibold text-gray-200">{formatCurrency(holding.currentPrice)}</p>
        </div>
      </div>
      <div className="mt-3 flex items-center justify-between rounded-xl bg-white/[0.02] px-3 py-2">
        <div className="flex items-center gap-1.5">
          {isProfit ? (
            <ArrowUpRight className="h-4 w-4 text-emerald-400" />
          ) : (
            <ArrowDownRight className="h-4 w-4 text-red-400" />
          )}
          <span className={`text-sm font-semibold ${isProfit ? 'text-emerald-400' : 'text-red-400'}`}>
            {formatCurrency(holding.unrealizedPnL)}
          </span>
        </div>
        <span className={`badge text-[10px] ${isProfit ? 'badge-accent' : 'badge-danger'}`}>
          {isProfit ? '+' : ''}{holding.unrealizedPnLPercent.toFixed(2)}%
        </span>
      </div>
    </div>
  )
}

function ClosedCard({ position }: { position: ClosedPositionResponse }) {
  const isProfit = position.realizedPnL >= 0
  return (
    <div className="glass-card p-4">
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gray-500/10">
            <Package className="h-4 w-4 text-gray-400" strokeWidth={1.5} />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-300">{position.soldStock.symbol}</p>
            <p className="text-xs text-gray-500">{position.soldStock.companyName}</p>
          </div>
        </div>
        <div className="flex items-center gap-1 text-xs text-gray-500">
          <Calendar className="h-3 w-3" />
          {position.sellDate}
        </div>
      </div>
      <div className="mt-3 flex items-center justify-between text-sm">
        <span className="text-gray-400">
          {position.soldStock.quantity} shares @ {formatCurrency(position.sellPrice)}
        </span>
        <span className={`font-semibold ${isProfit ? 'text-emerald-400' : 'text-red-400'}`}>
          {isProfit ? '+' : ''}{formatCurrency(position.realizedPnL)}
          <span className="ml-1 text-xs opacity-60">({isProfit ? '+' : ''}{position.realizedPnLPercent.toFixed(2)}%)</span>
        </span>
      </div>
    </div>
  )
}

function SellStockModal({ 
  holding, 
  onClose, 
  onConfirm 
}: { 
  holding: ActiveHoldingResponse; 
  onClose: () => void; 
  onConfirm: (quantity: number) => Promise<void>; 
}) {
  const [sellQty, setSellQty] = useState<number>(holding.stockHolding.quantity)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleConfirm = async () => {
    if (sellQty <= 0 || sellQty > holding.stockHolding.quantity) {
      toast.error('Invalid quantity')
      return
    }
    setIsSubmitting(true)
    await onConfirm(sellQty)
    setIsSubmitting(false)
  }

  const projectedValue = sellQty * holding.currentPrice
  const originalCost = sellQty * holding.stockHolding.buyPrice
  const projectedProfit = projectedValue - originalCost
  const isProfit = projectedProfit >= 0

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="animate-fade-in relative w-full max-w-sm rounded-2xl bg-[#111118] p-6 shadow-2xl ring-1 ring-white/10">
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-bold text-white">Sell {holding.stockHolding.symbol}</h3>
            <p className="text-xs text-gray-400">{holding.stockHolding.companyName}</p>
          </div>
          <button onClick={onClose} className="rounded-lg p-1 text-gray-400 hover:bg-white/10 hover:text-white">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="mt-6 flex flex-col gap-4">
          <div>
            <label className="mb-1.5 block text-xs font-medium uppercase tracking-wider text-gray-400">
              Quantity to Sell (Max: {holding.stockHolding.quantity})
            </label>
            <input
              type="number"
              min="0.01"
              step="0.01"
              max={holding.stockHolding.quantity}
              value={sellQty || ''}
              onChange={(e) => setSellQty(Number(e.target.value))}
              className="input-dark text-lg font-semibold"
            />
          </div>

          <div className="rounded-xl bg-white/[0.03] p-4 ring-1 ring-white/5">
            <div className="flex justify-between text-sm">
              <span className="text-gray-400">Current Price</span>
              <span className="font-medium text-white">{formatCurrency(holding.currentPrice)}</span>
            </div>
            <div className="mt-2 flex justify-between text-sm">
              <span className="text-gray-400">Avg Buy Price</span>
              <span className="font-medium text-white">{formatCurrency(holding.stockHolding.buyPrice)}</span>
            </div>
            <div className="my-3 h-px w-full bg-white/10" />
            <div className="flex justify-between text-sm">
              <span className="text-gray-400">Estimated Return</span>
              <span className="font-bold text-white">{formatCurrency(projectedValue)}</span>
            </div>
            <div className="mt-2 flex justify-between text-sm">
              <span className="text-gray-400">Est. Profit/Loss</span>
              <span className={`font-bold ${isProfit ? 'text-emerald-400' : 'text-red-400'}`}>
                {isProfit ? '+' : ''}{formatCurrency(projectedProfit)}
              </span>
            </div>
          </div>

          <button
            onClick={() => void handleConfirm()}
            disabled={isSubmitting || sellQty <= 0 || sellQty > holding.stockHolding.quantity}
            className="btn-danger mt-2 w-full py-3 text-sm font-semibold"
          >
            {isSubmitting ? 'Processing...' : `Confirm Sell`}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function Portfolio() {
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null)
  const [holdingToSell, setHoldingToSell] = useState<ActiveHoldingResponse | null>(null)
  const { userRole } = useAuth()
  const navigate = useNavigate()
  const isPremium = userRole === 'PREMIUM'

  const fetchPortfolio = useCallback(async () => {
    const loadingToast = toast.loading('Loading portfolio...')
    try {
      const { data } = await axiosInstance.get<PortfolioResponse>('/api/portfolio/get-portfolio')
      setPortfolio(data)
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to load portfolio'))
    }
  }, [])

  useEffect(() => {
    if (isPremium) {
      void fetchPortfolio()
    }
  }, [fetchPortfolio, isPremium])

  const handleSell = async (quantity: number) => {
    if (!holdingToSell) return
    const loadingToast = toast.loading('Selling shares...')
    try {
      await axiosInstance.post('/api/stocks/sell-stock', {
        symbol: holdingToSell.stockHolding.symbol,
        companyName: holdingToSell.stockHolding.companyName,
        quantity: quantity,
      })
      toast.dismiss(loadingToast)
      toast.success(`${holdingToSell.stockHolding.symbol} sold`)
      setHoldingToSell(null)
      await fetchPortfolio()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Sell failed'))
    }
  }

  const pnlAccent = (val: number | undefined) => {
    if (val == null) return undefined
    return val >= 0 ? 'green' as const : 'red' as const
  }

  return (
    <div className="mx-auto max-w-4xl">
      {holdingToSell && (
        <SellStockModal
          holding={holdingToSell}
          onClose={() => setHoldingToSell(null)}
          onConfirm={handleSell}
        />
      )}

      <div className="animate-fade-in flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500/10">
          <Briefcase className="h-5 w-5 text-emerald-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Portfolio</h1>
          <p className="text-sm text-gray-500">Your investment overview</p>
        </div>
      </div>

      {!isPremium ? (
        <div className="mt-10 flex flex-col items-center justify-center py-8">
          <div className="relative">
            <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-gradient-to-br from-amber-500/15 to-orange-500/15 ring-1 ring-amber-500/20">
              <Lock className="h-10 w-10 text-amber-400" />
            </div>
            <Crown className="absolute -right-2 -top-2 h-6 w-6 text-amber-400 animate-pulse" />
          </div>
          <h2 className="mt-6 text-xl font-bold text-white">Premium Feature</h2>
          <p className="mt-2 max-w-xs text-center text-sm leading-relaxed text-gray-500">
            Unlock your full investment portfolio with real-time P&amp;L tracking, active holdings management, and closed position history.
          </p>
          <div className="mt-5 rounded-xl bg-amber-500/10 px-5 py-3 ring-1 ring-amber-500/20">
            <p className="text-xs text-amber-400 text-center">
              ⚡ Upgrade to <span className="font-semibold">Premium</span> to access Portfolio
            </p>
          </div>
          <button
            type="button"
            onClick={() => navigate('/premium')}
            className="mt-5 btn-primary flex items-center gap-2"
          >
            <Crown className="h-4 w-4" /> Upgrade to Premium
          </button>
        </div>
      ) : (
        <>

      {/* Summary Cards */}
      <div className="mt-6 grid grid-cols-2 gap-3">
        <SummaryCard label="Invested" value={formatCurrency(portfolio?.totalCurrentAmountInvested)} icon={DollarSign} />
        <SummaryCard label="Current Value" value={formatCurrency(portfolio?.CurrentValue)} icon={BarChart3} accent={pnlAccent(portfolio?.unrealizedPnL)} />
        <SummaryCard label="Unrealized P&L" value={formatCurrency(portfolio?.unrealizedPnL)} icon={TrendingUp} accent={pnlAccent(portfolio?.unrealizedPnL)} />
        <SummaryCard label="Realized P&L" value={formatCurrency(portfolio?.realizedPnL)} icon={TrendingDown} accent={pnlAccent(portfolio?.realizedPnL)} />
      </div>

      {portfolio?.totalPnL !== undefined && (
        <div className="animate-fade-in-delay-2 mt-4 glass-card p-4 text-center">
          <p className="text-xs font-medium uppercase tracking-wider text-gray-400">Total P&L</p>
          <p className={`mt-1 text-2xl font-bold ${portfolio.totalPnL >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
            {portfolio.totalPnL >= 0 ? '+' : ''}{formatCurrency(portfolio.totalPnL)}
          </p>
        </div>
      )}

      {/* Active Holdings */}
      <section className="mt-6">
        <h2 className="flex items-center gap-2 text-base font-semibold text-white">
          <TrendingUp className="h-4 w-4 text-emerald-400" />
          Active Holdings
        </h2>
        <div className="mt-3 flex flex-col gap-3">
          {portfolio?.activeHoldings.length ? (
            portfolio.activeHoldings.map((holding) => (
              <HoldingCard key={holding.stockHolding.holdingId} holding={holding} onSell={() => setHoldingToSell(holding)} />
            ))
          ) : (
            <div className="glass-card py-8 text-center">
              <Briefcase className="mx-auto h-8 w-8 text-gray-600" />
              <p className="mt-2 text-sm text-gray-500">No active holdings. Buy stocks from the Stocks page.</p>
            </div>
          )}
        </div>
      </section>

      {/* Closed Positions */}
      {portfolio?.closedHoldings && portfolio.closedHoldings.length > 0 && (
        <section className="mt-8">
          <h2 className="flex items-center gap-2 text-base font-semibold text-white">
            <Package className="h-4 w-4 text-gray-400" />
            Closed Positions
          </h2>
          <div className="mt-3 flex flex-col gap-3">
            {portfolio.closedHoldings.map((position, index) => (
              <ClosedCard key={`${position.soldStock.holdingId}-${index}`} position={position} />
            ))}
          </div>
        </section>
      )}

        </>)}
    </div>
  )
}
