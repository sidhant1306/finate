import { useCallback, useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import toast from 'react-hot-toast'
import {
  PiggyBank,
  Plus,
  X,
  Edit3,
  Trash2,
  Save,
  Search,
  AlertTriangle,
} from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import type { BudgetRequest, BudgetResponse, BudgetSummaryResponse, Category } from '../types/types'
import { CATEGORIES, formatCategoryLabel, formatCurrency } from '../types/types'
import { getApiError } from '../utils/apiHelpers'
import { categoryEmoji } from '../utils/categories'

interface EditBudgetState {
  budgetAmount: number
  budgetCategory: Category
}

export default function Budget() {
  const [summary, setSummary] = useState<BudgetSummaryResponse | null>(null)
  const [showAddForm, setShowAddForm] = useState<boolean>(false)
  const [newBudget, setNewBudget] = useState<BudgetRequest>({
    budgetAmount: 0,
    budgetCategory: 'FOOD',
  })
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<EditBudgetState | null>(null)
  const [lookupCategory, setLookupCategory] = useState<Category>('FOOD')
  const [lookupId, setLookupId] = useState<string>('')
  const [lookupResult, setLookupResult] = useState<BudgetResponse | null>(null)

  const fetchBudgets = useCallback(async () => {
    const loadingToast = toast.loading('Loading budgets...')
    try {
      const { data } = await axiosInstance.get<BudgetSummaryResponse>('/api/budget/get-all-budgets')
      setSummary(data)
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to load budgets'))
    }
  }, [])

  useEffect(() => { void fetchBudgets() }, [fetchBudgets])

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const loadingToast = toast.loading('Creating budget...')
    try {
      await axiosInstance.post('/api/budget/create', newBudget)
      toast.dismiss(loadingToast)
      toast.success('Budget created')
      setNewBudget({ budgetAmount: 0, budgetCategory: 'FOOD' })
      setShowAddForm(false)
      await fetchBudgets()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to create budget'))
    }
  }

  const startEdit = (budget: BudgetResponse) => {
    setEditingId(budget.budgetId)
    setEditForm({ budgetAmount: budget.budgetAmount, budgetCategory: budget.budgetCategory })
  }

  const cancelEdit = () => { setEditingId(null); setEditForm(null) }

  const handleSaveEdit = async (budgetId: number) => {
    if (!editForm) return
    const loadingToast = toast.loading('Saving budget...')
    try {
      await axiosInstance.put('/api/budget/edit-budget-by-id', editForm, { params: { budgetId } })
      toast.dismiss(loadingToast)
      toast.success('Budget updated')
      cancelEdit()
      await fetchBudgets()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to update budget'))
    }
  }

  const handleDelete = async (budgetId: number) => {
    if (!window.confirm('Delete this budget?')) return
    const loadingToast = toast.loading('Deleting budget...')
    try {
      await axiosInstance.delete('/api/budget/delete-budget-by-id', { params: { budgetId } })
      toast.dismiss(loadingToast)
      toast.success('Budget deleted')
      await fetchBudgets()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to delete budget'))
    }
  }

  const handleDeleteAll = async () => {
    if (!window.confirm('Delete all budgets? This cannot be undone.')) return
    const loadingToast = toast.loading('Deleting all budgets...')
    try {
      await axiosInstance.delete('/api/budget/delete-all-budgets')
      toast.dismiss(loadingToast)
      toast.success('All budgets deleted')
      await fetchBudgets()
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Failed to delete budgets'))
    }
  }

  const handleLookupByCategory = async () => {
    const loadingToast = toast.loading('Looking up budget...')
    try {
      const { data } = await axiosInstance.get<BudgetResponse>('/api/budget/get-budget-by-category', {
        params: { category: lookupCategory },
      })
      setLookupResult(data)
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Budget not found for category'))
    }
  }

  const handleLookupById = async () => {
    if (!lookupId) return
    const loadingToast = toast.loading('Looking up budget...')
    try {
      const { data } = await axiosInstance.get<BudgetResponse>('/api/budget/get-budget-by-id', {
        params: { budgetId: Number(lookupId) },
      })
      setLookupResult(data)
      toast.dismiss(loadingToast)
    } catch (error) {
      toast.dismiss(loadingToast)
      toast.error(getApiError(error, 'Budget not found'))
    }
  }

  return (
    <div className="mx-auto max-w-4xl">
      <div className="animate-fade-in flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-pink-500/10">
            <PiggyBank className="h-5 w-5 text-pink-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white">Budget</h1>
            <p className="text-sm text-gray-500">Track spending by category</p>
          </div>
        </div>
        <button type="button" onClick={() => setShowAddForm((prev) => !prev)}
          className={showAddForm ? 'btn-danger flex items-center gap-1' : 'btn-primary flex items-center gap-1'}>
          {showAddForm ? <><X className="h-4 w-4" /> Cancel</> : <><Plus className="h-4 w-4" /> Add</>}
        </button>
      </div>

      {/* Summary */}
      {summary && (
        <div className="animate-fade-in-delay-1 mt-6 grid grid-cols-3 gap-3">
          <div className="stat-card">
            <p className="text-[10px] font-medium uppercase tracking-wider text-gray-400">Total</p>
            <p className="mt-1 text-sm font-bold text-white">{formatCurrency(summary.totalBudgetAmount)}</p>
          </div>
          <div className="stat-card">
            <p className="text-[10px] font-medium uppercase tracking-wider text-gray-400">Remaining</p>
            <p className="mt-1 text-sm font-bold text-emerald-400">{formatCurrency(summary.totalRemainingBudgetAmount)}</p>
          </div>
          <div className="stat-card">
            <p className="text-[10px] font-medium uppercase tracking-wider text-gray-400">Spent</p>
            <p className="mt-1 text-sm font-bold text-red-400">{formatCurrency(summary.totalBudgetSpent)}</p>
          </div>
        </div>
      )}

      {/* Lookup */}
      <section className="animate-fade-in-delay-2 mt-6 glass-card p-5">
        <h2 className="flex items-center gap-2 text-sm font-semibold text-white">
          <Search className="h-4 w-4 text-gray-400" /> Lookup Budget
        </h2>
        <div className="mt-3 flex flex-col gap-3">
          <div className="flex gap-2">
            <select value={lookupCategory}
              onChange={(e: ChangeEvent<HTMLSelectElement>) => setLookupCategory(e.target.value as Category)}
              className="select-dark flex-1 text-xs">
              {CATEGORIES.map((cat) => <option key={cat} value={cat}>{categoryEmoji[cat]} {formatCategoryLabel(cat)}</option>)}
            </select>
            <button type="button" onClick={() => void handleLookupByCategory()} className="btn-secondary text-xs py-2 px-3">By Category</button>
          </div>
          <div className="flex gap-2">
            <input type="number" placeholder="Budget ID" value={lookupId}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setLookupId(e.target.value)}
              className="input-dark flex-1 text-xs" />
            <button type="button" onClick={() => void handleLookupById()} className="btn-secondary text-xs py-2 px-3">By ID</button>
          </div>
          {lookupResult && (
            <div className="rounded-xl bg-white/[0.03] p-3 ring-1 ring-white/[0.06]">
              <p className="text-sm font-medium text-white">
                {categoryEmoji[lookupResult.budgetCategory]} {formatCategoryLabel(lookupResult.budgetCategory)} (ID {lookupResult.budgetId})
              </p>
              <p className="mt-1 text-xs text-gray-400">
                {formatCurrency(lookupResult.budgetSpent)} spent · {formatCurrency(lookupResult.remainingBudget)} remaining
              </p>
            </div>
          )}
        </div>
      </section>

      {/* Add Form */}
      {showAddForm && (
        <form onSubmit={handleCreate} className="animate-fade-in mt-6 glass-card p-5">
          <h2 className="text-base font-semibold text-white">New budget</h2>
          <div className="mt-4 flex flex-col gap-3">
            <select value={newBudget.budgetCategory}
              onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                setNewBudget((prev) => ({ ...prev, budgetCategory: e.target.value as Category }))}
              className="select-dark">
              {CATEGORIES.map((cat) => <option key={cat} value={cat}>{categoryEmoji[cat]} {formatCategoryLabel(cat)}</option>)}
            </select>
            <input type="number" min="1" step="0.01" placeholder="Budget amount" required
              value={newBudget.budgetAmount || ''}
              onChange={(e) => setNewBudget((prev) => ({ ...prev, budgetAmount: Number(e.target.value) }))}
              className="input-dark" />
            <button type="submit" className="btn-primary">Create budget</button>
          </div>
        </form>
      )}

      {/* Budget Cards */}
      <section className="animate-fade-in-delay-3 mt-6 flex flex-col gap-4">
        {summary?.budgetResponseList.length ? (
          summary.budgetResponseList.map((budget) => {
            const overspent = budget.remainingBudget < 0
            const spentPercent = Math.min(100, budget.budgetAmount > 0 ? (budget.budgetSpent / budget.budgetAmount) * 100 : 0)

            if (editingId === budget.budgetId && editForm) {
              return (
                <div key={budget.budgetId} className="glass-card p-5">
                  <select value={editForm.budgetCategory}
                    onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                      setEditForm((prev) => prev ? { ...prev, budgetCategory: e.target.value as Category } : prev)}
                    className="select-dark text-xs">
                    {CATEGORIES.map((cat) => <option key={cat} value={cat}>{categoryEmoji[cat]} {formatCategoryLabel(cat)}</option>)}
                  </select>
                  <input type="number" min="1" step="0.01" value={editForm.budgetAmount}
                    onChange={(e) => setEditForm((prev) => prev ? { ...prev, budgetAmount: Number(e.target.value) } : prev)}
                    className="input-dark mt-2 text-xs" />
                  <div className="mt-3 flex gap-2">
                    <button type="button" onClick={() => void handleSaveEdit(budget.budgetId)}
                      className="btn-primary flex items-center gap-1 py-1.5 px-3 text-xs">
                      <Save className="h-3 w-3" /> Save
                    </button>
                    <button type="button" onClick={cancelEdit}
                      className="rounded-lg px-3 py-1.5 text-xs text-gray-400 hover:bg-white/5">Cancel</button>
                  </div>
                </div>
              )
            }

            return (
              <div key={budget.budgetId}
                className={`glass-card p-5 ${overspent ? 'ring-1 ring-red-500/20' : ''}`}>
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{categoryEmoji[budget.budgetCategory] || '📌'}</span>
                    <div>
                      <p className="text-sm font-semibold text-white">{formatCategoryLabel(budget.budgetCategory)}</p>
                      <p className="text-xs text-gray-500">
                        Spent {formatCurrency(budget.budgetSpent)} of {formatCurrency(budget.budgetAmount)}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-1">
                    <button type="button" onClick={() => startEdit(budget)}
                      className="rounded-lg p-1.5 text-gray-500 transition-colors hover:bg-white/5 hover:text-gray-300">
                      <Edit3 className="h-3.5 w-3.5" />
                    </button>
                    <button type="button" onClick={() => void handleDelete(budget.budgetId)}
                      className="rounded-lg p-1.5 text-gray-500 transition-colors hover:bg-red-500/10 hover:text-red-400">
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>

                {/* Progress Bar */}
                <div className="progress-track mt-3">
                  <div
                    className={`progress-fill ${overspent ? 'progress-fill-danger' : 'progress-fill-accent'}`}
                    style={{ width: `${spentPercent}%` }}
                  />
                </div>

                <div className="mt-2 flex items-center gap-1">
                  {overspent && <AlertTriangle className="h-3.5 w-3.5 text-red-400" />}
                  <p className={`text-sm font-medium ${overspent ? 'text-red-400' : 'text-emerald-400'}`}>
                    {overspent ? 'Overspent by' : 'Remaining'} {formatCurrency(Math.abs(budget.remainingBudget))}
                  </p>
                </div>
              </div>
            )
          })
        ) : (
          <div className="glass-card py-8 text-center">
            <PiggyBank className="mx-auto h-8 w-8 text-gray-600" />
            <p className="mt-2 text-sm text-gray-500">No budgets yet. Create one to get started.</p>
          </div>
        )}
      </section>

      {summary && summary.budgetResponseList.length > 0 && (
        <button type="button" onClick={() => void handleDeleteAll()}
          className="btn-danger mt-6 w-full py-2.5">
          <Trash2 className="mr-1 inline h-4 w-4" /> Delete all budgets
        </button>
      )}
    </div>
  )
}
