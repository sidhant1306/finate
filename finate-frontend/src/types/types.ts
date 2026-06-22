
// Enums
export type UserRole = 'BASIC' | 'PREMIUM' | 'ADMIN'
export const USER_ROLES: UserRole[] = ['BASIC', 'PREMIUM', 'ADMIN']

export type Category =
  | 'FOOD'
  | 'TRANSPORT'
  | 'RENT'
  | 'SALARY'
  | 'ENTERTAINMENT'
  | 'SHOPPING'
  | 'HEALTH'
  | 'EDUCATION'
  | 'INVESTMENT'
  | 'SAVING'
  | 'OTHER'

export const CATEGORIES: Category[] = [
  'FOOD',
  'TRANSPORT',
  'RENT',
  'SALARY',
  'ENTERTAINMENT',
  'SHOPPING',
  'HEALTH',
  'EDUCATION',
  'INVESTMENT',
  'SAVING',
  'OTHER',
]

export type ExpenseTransactionType = 'DEBIT' | 'CREDIT'
export const EXPENSE_TRANSACTION_TYPES: ExpenseTransactionType[] = ['DEBIT', 'CREDIT']

export type PaymentType = 'WALLET' | 'PREMIUM'
export const PAYMENT_TYPES: PaymentType[] = ['WALLET', 'PREMIUM']

export type TransactionType = 'DEBIT' | 'CREDIT' | 'DEPOSIT' | 'UPI'
export const TRANSACTION_TYPES: TransactionType[] = ['DEBIT', 'CREDIT', 'DEPOSIT', 'UPI']

export type TransactionStatus = 'SUCCESS' | 'PENDING' | 'FAILED'
export const TRANSACTION_STATUSES: TransactionStatus[] = ['SUCCESS', 'PENDING', 'FAILED']

// Auth
export interface LoginRequest {
  userEmail: string
  userPassword: string
}

export interface LoginResponse {
  token: string
  firstName: string
  lastName: string
  userEmail: string
  userRole: UserRole
  username: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  username: string
  userEmail: string
  userPassword: string
}

export interface RegisterResponse {
  firstName: string
  lastName: string
  userRole: UserRole
  username: string
}

// Dashboard
export interface DashboardResponse {
  walletBalance: number
  netWorth: number
  totalBudgetRemaining: number
  totalDebitAmountLast30Days: number
  totalCreditAmountLast30Days: number
  last30DaysExpenseTransactionList: ExpenseTransactionResponse[]
}

// Expense / Tracking
export interface ExpenseTransactionRequest {
  expenseTransactionType: ExpenseTransactionType
  expenseAmount: number
  expenseCategory: Category
  expenseDescription?: string
}

export interface ExpenseTransactionResponse {
  expenseTransactionId: number
  expenseTransactionType: ExpenseTransactionType
  expenseAmount: number
  expenseTransactionDate: string
  expenseTransactionCategory: Category
  expenseDescription?: string
}

export interface ExpenseTransactionSummary {
  expenseTransactionResponseDtoList: ExpenseTransactionResponse[]
  currentBalance: number
  totalDebitAmount: number
  totalCreditAmount: number
}

// Budget
export interface BudgetRequest {
  budgetAmount: number
  budgetCategory: Category
}

export interface BudgetResponse {
  budgetId: number
  budgetAmount: number
  budgetCategory: Category
  remainingBudget: number
  budgetSpent: number
}

export interface BudgetSummaryResponse {
  budgetResponseList: BudgetResponse[]
  totalBudgetAmount: number
  totalRemainingBudgetAmount: number
  totalBudgetSpent: number
}

// Wallet
export interface PaymentRequest {
  paymentAmount: number
  paymentType: PaymentType
}

export interface PaymentResponse {
  orderId: string
  amount: number
  currency: string
  keyId: string
}

export interface PaymentVerifyRequest {
  paymentId: string
  orderId: string
  signature: string
  paymentType: PaymentType
  amount: number
}

export interface WalletTransactionResponse {
  paymentType: PaymentType
  transactionType: TransactionType
  transactionStatus: TransactionStatus
  amount: number
  recipientId: number | null
  transactionDate: string
}

export interface WalletSummaryResponse {
  walletTransactionResponseDtoList: WalletTransactionResponse[]
  walletBalance: number
}

export interface UpiPaymentRequest {
  receiverUserid: number
  amount: number
}

export interface UpiPaymentResponse {
  transactionId: number
  receiverId: number
  senderId: number
  amount: number
  receiverName: string
}

// Stocks / Finnhub
export interface FinnhubQuoteResponse {
  c: number
  h: number
  l: number
  o: number
  pc: number
  t: number
}

export interface StockSearchResult {
  symbol: string
  companyName: string
}

export interface StockHoldingRequest {
  symbol: string
  companyName: string
  quantity: number
}

export interface StockHoldingResponse {
  holdingId: number
  symbol: string
  quantity: number
  companyName: string
  buyPrice: number
  buyDate: string
}

export interface StockWatchlistRequest {
  stockSymbol: string
  CompanyName: string
}

export interface StockWatchlistResponse {
  stockSymbol: string
  stockCompanyName: string
  priceWhenAdded: number
  watchlistDate: string
}

// Portfolio — CurrentValue matches backend record component name
export interface ActiveHoldingResponse {
  stockHolding: StockHoldingResponse
  currentPrice: number
  currentValue: number
  currentInvestedAmount: number
  unrealizedPnL: number
  unrealizedPnLPercent: number
}

export interface ClosedPositionResponse {
  soldStock: StockHoldingResponse
  sellPrice: number
  sellValue: number
  sellDate: string
  realizedPnL: number
  realizedPnLPercent: number
}

export interface PortfolioResponse {
  activeHoldings: ActiveHoldingResponse[]
  closedHoldings: ClosedPositionResponse[]
  totalCurrentAmountInvested: number
  CurrentValue: number
  lifetimeInvested: number
  unrealizedPnL: number
  realizedPnL: number
  totalPnL: number
}

export function formatCurrency(amount: number | null | undefined): string {
  const value = amount ?? 0
  return `₹${value.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
}



export function formatCategoryLabel(category: Category): string {
  return category.charAt(0) + category.slice(1).toLowerCase()
}
