import { useCallback, useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import toast from 'react-hot-toast'
import {
  ArrowLeftRight,
  Plus,
  Filter,
  Edit3,
  Trash2,
  Save,
  X,
  Mail,
  Loader2,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import type {
  Category,
  ExpenseTransactionRequest,
  ExpenseTransactionResponse,
  ExpenseTransactionSummary,
  ExpenseTransactionType,
} from '../types/types'
import {
  CATEGORIES,
  EXPENSE_TRANSACTION_TYPES,
  formatCategoryLabel,
  formatCurrency,
} from '../types/types'
import { getApiError } from '../utils/apiHelpers'
import { categoryEmoji } from '../utils/categories'

type FilterMode = 'all' | 'type' | 'category'

interface EditState {
  expenseTransactionType: ExpenseTransactionType
  expenseAmount: number
  expenseCategory: Category
  expenseDescription: string
}

const emptyForm: ExpenseTransactionRequest = {
  expenseTransactionType: 'DEBIT',
  expenseAmount: 0,
  expenseCategory: 'FOOD',
  expenseDescription: '',
}

export default function Tracking() {
  const [summary, setSummary] = useState<ExpenseTransactionSummary | null>(null)
  const [transactions, setTransactions] = useState<ExpenseTransactionResponse[]>([])
  const [filterMode, setFilterMode] = useState<FilterMode>('all')
  const [startDate, setStartDate] = useState<string>('')
  const [endDate, setEndDate] = useState<string>('')
  const [typeFilter, setTypeFilter] = useState<ExpenseTransactionType>('DEBIT')
  const [categoryFilter, setCategoryFilter] = useState<Category>('FOOD')
  const [showAddForm, setShowAddForm] = useState<boolean>(false)
  const [newTransaction, setNewTransaction] = useState<ExpenseTransactionRequest>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<EditState | null>(null)
  const [emailSending, setEmailSending] = useState<boolean>(false)

  const fetchAll = useCallback(async () => {
    const params: Record<string, string> = {}
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    const { data } = await axiosInstance.get<ExpenseTransactionSummary>('/api/tracking/get-all-expenses', { params })
    setSummary(data)
    setTransactions(data.expenseTransactionResponseDtoList)
  }, [startDate, endDate])

  const fetchByType = useCallback(async () => {
    const { data } = await axiosInstance.get<ExpenseTransactionResponse[]>(
      '/api/tracking/get-all-expenses-by-type',
      { params: { expenseTransactionType: typeFilter } },
    )
    setTransactions(data)
  }, [typeFilter])

  const fetchByCategory = useCallback(async () => {
    const { data } = await axiosInstance.get<ExpenseTransactionResponse[]>(
      '/api/tracking/get-all-expenses-by-category',
      { params: { category: categoryFilter } },
    )
    setTransactions(data)
  }, [categoryFilter])

  const loadData = useCallback(async () => {
    const loadingToast = toast.loading('Loading transactions...')
    try {
      if (filterMode === 'all') await fetchAll()
      else if (filterMode === 'type') await fetchByType()
      else await fetchByCategory()
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to load transactions'))
    }
  }, [filterMode, fetchAll, fetchByType, fetchByCategory])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const loadingToast = toast.loading('Adding transaction...')
    try {
      await axiosInstance.post('/api/tracking/create', newTransaction)
      toast.dismiss(loadingToast)
      toast.success('Transaction added')
      setNewTransaction(emptyForm)
      setShowAddForm(false)
      await loadData()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to add transaction'))
    }
  }

  const startEdit = (transaction: ExpenseTransactionResponse) => {
    setEditingId(transaction.expenseTransactionId)
    setEditForm({
      expenseTransactionType: transaction.expenseTransactionType,
      expenseAmount: transaction.expenseAmount,
      expenseCategory: transaction.expenseTransactionCategory,
      expenseDescription: transaction.expenseDescription ?? '',
    })
  }

  const cancelEdit = () => { setEditingId(null); setEditForm(null) }

  const handleSaveEdit = async (id: number) => {
    if (!editForm) return
    const loadingToast = toast.loading('Saving...')
    try {
      const payload: ExpenseTransactionRequest = {
        expenseTransactionType: editForm.expenseTransactionType,
        expenseAmount: editForm.expenseAmount,
        expenseCategory: editForm.expenseCategory,
        expenseDescription: editForm.expenseDescription || undefined,
      }
      await axiosInstance.put('/api/tracking/edit-expense-transaction-by-id', payload, {
        params: { expenseTransactionId: id },
      })
      toast.dismiss(loadingToast)
      toast.success('Transaction updated')
      cancelEdit()
      await loadData()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to update transaction'))
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this transaction?')) return
    const loadingToast = toast.loading('Deleting...')
    try {
      await axiosInstance.delete('/api/tracking/delete-expense-transaction-by-id', {
        params: { expenseTransactionId: id },
      })
      toast.dismiss(loadingToast)
      toast.success('Transaction deleted')
      await loadData()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to delete transaction'))
    }
  }

  const handleEmailHistory = async () => {
    setEmailSending(true)
    const loadingToast = toast.loading('Sending transaction history to your email...')
    try {
      const { data } = await axiosInstance.post<string>('/api/tracking/email-transaction-history')
      toast.dismiss(loadingToast)
      toast.success(data)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to send transaction history'))
    } finally {
      setEmailSending(false)
    }
  }

  return (
    <div className="mx-auto max-w-4xl">
      <div className="animate-fade-in flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-500/10">
            <ArrowLeftRight className="h-5 w-5 text-blue-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white">Tracking</h1>
            <p className="text-sm text-gray-500">Income and expenses</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button type="button" onClick={() => void handleEmailHistory()}
            disabled={emailSending}
            className="btn-secondary flex items-center gap-1"
            title="Email last 30 days transaction history">
            {emailSending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Mail className="h-4 w-4" />}
            {emailSending ? 'Sending...' : 'Mail'}
          </button>
          <button type="button" onClick={() => setShowAddForm((prev) => !prev)}
            className={showAddForm ? 'btn-danger flex items-center gap-1' : 'btn-primary flex items-center gap-1'}>
            {showAddForm ? <><X className="h-4 w-4" /> Cancel</> : <><Plus className="h-4 w-4" /> Add</>}
          </button>
        </div>
      </div>

      {/* Summary Stats */}
      {summary && filterMode === 'all' && (
        <div className="animate-fade-in-delay-1 mt-6 grid grid-cols-3 gap-3">
          <div className="stat-card">
            <p className="text-[10px] font-medium uppercase tracking-wider text-gray-400">Balance</p>
            <p className="mt-1 text-sm font-bold text-white">{formatCurrency(summary.currentBalance)}</p>
          </div>
          <div className="stat-card">
            <p className="text-[10px] font-medium uppercase tracking-wider text-gray-400">Debit</p>
            <p className="mt-1 text-sm font-bold text-red-400">{formatCurrency(summary.totalDebitAmount)}</p>
          </div>
          <div className="stat-card">
            <p className="text-[10px] font-medium uppercase tracking-wider text-gray-400">Credit</p>
            <p className="mt-1 text-sm font-bold text-emerald-400">{formatCurrency(summary.totalCreditAmount)}</p>
          </div>
        </div>
      )}

      {/* Add Form */}
      {showAddForm && (
        <form onSubmit={handleCreate} className="animate-fade-in mt-6 glass-card p-5">
          <h2 className="text-base font-semibold text-white">New transaction</h2>
          <div className="mt-4 flex flex-col gap-3">
            <select value={newTransaction.expenseTransactionType}
              onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                setNewTransaction((prev) => ({ ...prev, expenseTransactionType: e.target.value as ExpenseTransactionType }))}
              className="select-dark">
              {EXPENSE_TRANSACTION_TYPES.map((type) => <option key={type} value={type}>{type}</option>)}
            </select>
            <input type="number" min="0.01" step="0.01" placeholder="Amount" required
              value={newTransaction.expenseAmount || ''}
              onChange={(e) => setNewTransaction((prev) => ({ ...prev, expenseAmount: Number(e.target.value) }))}
              className="input-dark" />
            <select value={newTransaction.expenseCategory}
              onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                setNewTransaction((prev) => ({ ...prev, expenseCategory: e.target.value as Category }))}
              className="select-dark">
              {CATEGORIES.map((cat) => <option key={cat} value={cat}>{categoryEmoji[cat]} {formatCategoryLabel(cat)}</option>)}
            </select>
            <input placeholder="Description (optional)" value={newTransaction.expenseDescription ?? ''}
              onChange={(e) => setNewTransaction((prev) => ({ ...prev, expenseDescription: e.target.value }))}
              className="input-dark" />
            <button type="submit" className="btn-primary">Save transaction</button>
          </div>
        </form>
      )}

      {/* Filters */}
      <section className="animate-fade-in-delay-2 mt-6 glass-card p-5">
        <h2 className="flex items-center gap-2 text-sm font-semibold text-white">
          <Filter className="h-4 w-4 text-gray-400" /> Filters
        </h2>
        <div className="mt-3 flex flex-col gap-3">
          <div className="flex gap-2">
            {(['all', 'type', 'category'] as const).map((mode) => (
              <button key={mode} type="button" onClick={() => setFilterMode(mode)}
                className={`flex-1 rounded-xl px-3 py-2 text-xs font-medium transition-all ${
                  filterMode === mode
                    ? 'bg-emerald-500/15 text-emerald-400 ring-1 ring-emerald-500/30'
                    : 'bg-white/[0.03] text-gray-400 hover:bg-white/[0.06]'
                }`}>
                {mode === 'all' ? 'All' : mode === 'type' ? 'By Type' : 'By Category'}
              </button>
            ))}
          </div>

          {filterMode === 'all' && (
            <div className="grid grid-cols-2 gap-3">
              <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="input-dark text-xs" />
              <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} className="input-dark text-xs" />
            </div>
          )}

          {filterMode === 'type' && (
            <select value={typeFilter}
              onChange={(e: ChangeEvent<HTMLSelectElement>) => setTypeFilter(e.target.value as ExpenseTransactionType)}
              className="select-dark">
              {EXPENSE_TRANSACTION_TYPES.map((type) => <option key={type} value={type}>{type}</option>)}
            </select>
          )}

          {filterMode === 'category' && (
            <select value={categoryFilter}
              onChange={(e: ChangeEvent<HTMLSelectElement>) => setCategoryFilter(e.target.value as Category)}
              className="select-dark">
              {CATEGORIES.map((cat) => <option key={cat} value={cat}>{categoryEmoji[cat]} {formatCategoryLabel(cat)}</option>)}
            </select>
          )}
        </div>
      </section>

      {/* Transaction List */}
      <section className="animate-fade-in-delay-3 mt-6 glass-card p-5">
        <h2 className="text-base font-semibold text-white">Transactions</h2>
        <div className="mt-3">
          {transactions.length ? (
            [...transactions].reverse().map((transaction) =>
              editingId === transaction.expenseTransactionId && editForm ? (
                <div key={transaction.expenseTransactionId} className="mb-3 flex flex-col gap-2 rounded-xl bg-white/[0.03] p-3 ring-1 ring-white/[0.06]">
                  <select value={editForm.expenseTransactionType}
                    onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                      setEditForm((prev) => prev ? { ...prev, expenseTransactionType: e.target.value as ExpenseTransactionType } : prev)}
                    className="select-dark text-xs" >
                    {EXPENSE_TRANSACTION_TYPES.map((type) => <option key={type} value={type}>{type}</option>)}
                  </select>
                  <input type="number" min="0.01" step="0.01" value={editForm.expenseAmount}
                    onChange={(e) => setEditForm((prev) => prev ? { ...prev, expenseAmount: Number(e.target.value) } : prev)}
                    className="input-dark text-xs" />
                  <select value={editForm.expenseCategory}
                    onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                      setEditForm((prev) => prev ? { ...prev, expenseCategory: e.target.value as Category } : prev)}
                    className="select-dark text-xs">
                    {CATEGORIES.map((cat) => <option key={cat} value={cat}>{categoryEmoji[cat]} {formatCategoryLabel(cat)}</option>)}
                  </select>
                  <input value={editForm.expenseDescription}
                    onChange={(e) => setEditForm((prev) => prev ? { ...prev, expenseDescription: e.target.value } : prev)}
                    className="input-dark text-xs" placeholder="Description" />
                  <div className="flex gap-2">
                    <button type="button" onClick={() => void handleSaveEdit(transaction.expenseTransactionId)}
                      className="btn-primary flex items-center gap-1 py-1.5 px-3 text-xs">
                      <Save className="h-3 w-3" /> Save
                    </button>
                    <button type="button" onClick={cancelEdit}
                      className="rounded-lg px-3 py-1.5 text-xs text-gray-400 hover:bg-white/5">Cancel</button>
                  </div>
                </div>
              ) : (
                <div key={transaction.expenseTransactionId}
                  className="flex items-center gap-3 border-b border-white/[0.04] py-3 last:border-0">
                  <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/5 text-sm">
                    {categoryEmoji[transaction.expenseTransactionCategory] || '📌'}
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-gray-200">
                      {formatCategoryLabel(transaction.expenseTransactionCategory)}
                    </p>
                    <p className="truncate text-xs text-gray-500">
                      {transaction.expenseTransactionDate} · {transaction.expenseTransactionType}
                      {transaction.expenseDescription ? ` · ${transaction.expenseDescription}` : ''}
                    </p>
                  </div>
                  <span className={`shrink-0 text-sm font-semibold ${
                    transaction.expenseTransactionType === 'DEBIT' ? 'text-red-400' : 'text-emerald-400'}`}>
                    {formatCurrency(transaction.expenseAmount)}
                  </span>
                  <div className="flex shrink-0 gap-1">
                    <button type="button" onClick={() => startEdit(transaction)}
                      className="rounded-lg p-1.5 text-gray-500 transition-colors hover:bg-white/5 hover:text-gray-300">
                      <Edit3 className="h-3.5 w-3.5" />
                    </button>
                    <button type="button" onClick={() => void handleDelete(transaction.expenseTransactionId)}
                      className="rounded-lg p-1.5 text-gray-500 transition-colors hover:bg-red-500/10 hover:text-red-400">
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>
              ),
            )
          ) : (
            <p className="py-6 text-center text-sm text-gray-500">No transactions found.</p>
          )}
        </div>
      </section>
    </div>
  )
}
