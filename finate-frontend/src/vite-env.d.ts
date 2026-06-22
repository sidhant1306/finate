/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_BACKEND_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

interface RazorpaySuccessResponse {
  razorpay_payment_id: string
  razorpay_order_id: string
  razorpay_signature: string
}

interface RazorpayOptions {
  key: string
  amount: number
  currency: string
  name: string
  description: string
  order_id: string
  handler: (response: RazorpaySuccessResponse) => void
  prefill?: {
    name?: string
    email?: string
  }
  theme?: {
    color?: string
  }
}

interface RazorpayInstance {
  open: () => void
}

interface RazorpayConstructor {
  new (options: RazorpayOptions): RazorpayInstance
}

interface Window {
  Razorpay: RazorpayConstructor
}
