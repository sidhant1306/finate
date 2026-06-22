export async function loadRazorpay(): Promise<void> {
  if (document.querySelector('script[src*="checkout.razorpay.com"]')) return
  await new Promise<void>((resolve) => {
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.async = true
    script.onload = () => resolve()
    document.head.appendChild(script)
  })
}
