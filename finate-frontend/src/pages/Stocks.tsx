import { useCallback, useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import toast from 'react-hot-toast'
import {
  Search,
  TrendingUp,
  Eye,
  X,
  ShoppingCart,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import type {
  FinnhubQuoteResponse,
  StockHoldingRequest,
  StockSearchResult,
  StockWatchlistResponse,
} from '../types/types'
import { formatCurrency } from '../types/types'
import { getApiError } from '../utils/apiHelpers'

export default function Stocks() {
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [searchResults, setSearchResults] = useState<StockSearchResult[]>([])
  const [selectedQuote, setSelectedQuote] = useState<FinnhubQuoteResponse | null>(null)
  const [selectedSymbol, setSelectedSymbol] = useState<string>('')
  const [selectedCompany, setSelectedCompany] = useState<string>('')
  const [watchlist, setWatchlist] = useState<StockWatchlistResponse[]>([])
  const [buyForm, setBuyForm] = useState<StockHoldingRequest>({
    symbol: '',
    companyName: '',
    quantity: 0,
  })

  const fetchWatchlist = useCallback(async () => {
    const { data } = await axiosInstance.get<StockWatchlistResponse[]>('/stocks/get-entire-watchlist')
    setWatchlist(data)
  }, [])

  const loadData = useCallback(async () => {
    try {
      await fetchWatchlist()
    } catch (error) {
      toast.error(getApiError(error, 'Failed to load watchlist'))
    }
  }, [fetchWatchlist])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const handleSearch = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!searchQuery.trim()) return
    const loadingToast = toast.loading('Searching stocks...')
    try {
      const { data } = await axiosInstance.get<StockSearchResult[]>(
        `/finnhub/search/${encodeURIComponent(searchQuery.trim())}`,
      )
      setSearchResults(data)
      toast.dismiss(loadingToast)
      if (!data.length) toast.error('No stocks found')
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Search failed'))
    }
  }

  const handleSelectStock = async (result: StockSearchResult) => {
    const loadingToast = toast.loading('Fetching quote...')
    try {
      const { data } = await axiosInstance.get<FinnhubQuoteResponse>(
        `/finnhub/quote/${encodeURIComponent(result.symbol)}`,
      )
      setSelectedQuote(data)
      setSelectedSymbol(result.symbol)
      setSelectedCompany(result.companyName)
      setBuyForm((prev) => ({
        symbol: result.symbol,
        companyName: result.companyName,
        quantity: prev.quantity,
      }))
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to fetch quote'))
    }
  }

  const handleAddToWatchlist = async () => {
    if (!selectedSymbol) return
    const loadingToast = toast.loading('Adding to watchlist...')
    try {
      await axiosInstance.post('/stocks/add-stock-to-watchlist', {
        stockSymbol: selectedSymbol,
        CompanyName: selectedCompany,
      })
      toast.dismiss(loadingToast)
      toast.success(`${selectedSymbol} added to watchlist`)
      await fetchWatchlist()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to add to watchlist'))
    }
  }

  const handleRemoveFromWatchlist = async (symbol: string) => {
    const loadingToast = toast.loading('Removing from watchlist...')
    try {
      await axiosInstance.delete('/stocks/remove-watchlist', {
        data: { stockSymbol: symbol },
      })
      toast.dismiss(loadingToast)
      toast.success(`${symbol} removed`)
      await fetchWatchlist()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to remove from watchlist'))
    }
  }

  const handleBuy = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const loadingToast = toast.loading('Buying shares...')
    try {
      await axiosInstance.post('/stocks/buy-stock', buyForm)
      toast.dismiss(loadingToast)
      toast.success(`${buyForm.symbol} purchased`)
      setBuyForm({ symbol: '', companyName: '', quantity: 0 })
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Purchase failed'))
    }
  }

  const priceChange = selectedQuote ? selectedQuote.c - selectedQuote.pc : 0
  const priceChangePercent = selectedQuote && selectedQuote.pc ? (priceChange / selectedQuote.pc) * 100 : 0
  const isUp = priceChange >= 0

  return (
    <div className="mx-auto max-w-4xl">
      <div className="animate-fade-in flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500/10">
          <TrendingUp className="h-5 w-5 text-emerald-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Stocks</h1>
          <p className="text-sm text-gray-500">Search, watch, and trade</p>
        </div>
      </div>

      {/* Search */}
      <form onSubmit={handleSearch} className="animate-fade-in-delay-1 mt-6 flex gap-2">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
          <input
            placeholder="Search by symbol or name..."
            value={searchQuery}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchQuery(e.target.value)}
            className="input-dark pl-10"
          />
        </div>
        <button type="submit" className="btn-primary">Search</button>
      </form>

      {/* Search Results */}
      {searchResults.length > 0 && (
        <section className="animate-fade-in mt-4 glass-card p-4">
          <h2 className="text-sm font-semibold text-gray-300">Search results</h2>
          <div className="mt-3 flex flex-col gap-2">
            {searchResults.map((result) => (
              <button
                key={result.symbol}
                type="button"
                onClick={() => void handleSelectStock(result)}
                className="flex items-center justify-between rounded-xl bg-white/[0.02] px-3 py-2.5 text-left transition-colors hover:bg-white/[0.05]"
              >
                <div>
                  <span className="text-sm font-semibold text-white">{result.symbol}</span>
                  <span className="ml-2 text-xs text-gray-400">{result.companyName}</span>
                </div>
                <ArrowUpRight className="h-4 w-4 text-gray-500" />
              </button>
            ))}
          </div>
        </section>
      )}

      {/* Quote Display */}
      {selectedQuote && (
        <section className="animate-fade-in mt-4 glass-card p-5">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-lg font-bold text-white">{selectedSymbol}</h2>
              <p className="text-xs text-gray-400">{selectedCompany}</p>
            </div>
            <button onClick={() => void handleAddToWatchlist()} className="btn-secondary flex items-center gap-1 py-1.5 px-3 text-xs">
              <Eye className="h-3.5 w-3.5" /> Watch
            </button>
          </div>

          <div className="mt-4 flex items-end gap-3">
            <span className="text-3xl font-bold text-white">{formatCurrency(selectedQuote.c)}</span>
            <span className={`flex items-center gap-0.5 text-sm font-semibold ${isUp ? 'text-emerald-400' : 'text-red-400'}`}>
              {isUp ? <ArrowUpRight className="h-4 w-4" /> : <ArrowDownRight className="h-4 w-4" />}
              {formatCurrency(Math.abs(priceChange))} ({isUp ? '+' : ''}{priceChangePercent.toFixed(2)}%)
            </span>
          </div>

          <div className="mt-4 grid grid-cols-4 gap-2">
            {[
              { label: 'Open', value: selectedQuote.o },
              { label: 'High', value: selectedQuote.h },
              { label: 'Low', value: selectedQuote.l },
              { label: 'Prev Close', value: selectedQuote.pc },
            ].map(({ label, value }) => (
              <div key={label} className="rounded-xl bg-white/[0.02] p-2 text-center">
                <p className="text-[10px] font-medium uppercase tracking-wider text-gray-500">{label}</p>
                <p className="mt-0.5 text-xs font-semibold text-gray-200">{formatCurrency(value)}</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Buy Form */}
      <form onSubmit={handleBuy} className="animate-fade-in-delay-2 mt-6 glass-card p-5">
        <h2 className="flex items-center gap-2 text-base font-semibold text-white">
          <ShoppingCart className="h-4 w-4 text-emerald-400" /> Buy Shares
        </h2>
        <div className="mt-4 flex flex-col gap-3">
          <input
            placeholder="Symbol"
            required
            value={buyForm.symbol}
            onChange={(e) => setBuyForm((prev) => ({ ...prev, symbol: e.target.value }))}
            className="input-dark"
          />
          <input
            placeholder="Company name"
            required
            value={buyForm.companyName}
            onChange={(e) => setBuyForm((prev) => ({ ...prev, companyName: e.target.value }))}
            className="input-dark"
          />
          <input
            type="number"
            min="0.01"
            step="0.01"
            placeholder="Quantity"
            required
            value={buyForm.quantity || ''}
            onChange={(e) => setBuyForm((prev) => ({ ...prev, quantity: Number(e.target.value) }))}
            className="input-dark"
          />
          <button type="submit" className="btn-primary">Buy</button>
        </div>
      </form>

      {/* Watchlist */}
      <section className="animate-fade-in-delay-3 mt-6">
        <h2 className="flex items-center gap-2 text-base font-semibold text-white">
          <Eye className="h-4 w-4 text-amber-400" /> Watchlist
        </h2>
        <div className="mt-3 flex flex-col gap-2">
          {watchlist.length ? (
            watchlist.map((item) => (
              <div key={item.stockSymbol} className="glass-card flex items-center justify-between p-3.5">
                <div className="flex items-center gap-3">
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-500/10">
                    <TrendingUp className="h-3.5 w-3.5 text-amber-400" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-white">{item.stockSymbol}</p>
                    <p className="text-xs text-gray-500">
                      {item.stockCompanyName} · Added {new Date(item.watchlistDate).toLocaleDateString()} @ {formatCurrency(item.priceWhenAdded)}
                    </p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => void handleSelectStock({ symbol: item.stockSymbol, companyName: item.stockCompanyName })}
                    className="flex h-7 w-7 items-center justify-center rounded-lg transition-colors hover:bg-emerald-500/10"
                    title="Get Quote"
                  >
                    <ArrowUpRight className="h-4 w-4 text-emerald-400" />
                  </button>
                  <button
                    type="button"
                    onClick={() => void handleRemoveFromWatchlist(item.stockSymbol)}
                    className="flex h-7 w-7 items-center justify-center rounded-lg transition-colors hover:bg-red-500/10"
                    title="Remove from Watchlist"
                  >
                    <X className="h-4 w-4 text-red-400" />
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="glass-card py-6 text-center">
              <Eye className="mx-auto h-8 w-8 text-gray-600" />
              <p className="mt-2 text-sm text-gray-500">Watchlist is empty.</p>
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
