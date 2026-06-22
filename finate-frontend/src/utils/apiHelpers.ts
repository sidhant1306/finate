import axios from 'axios'

export function getApiError(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data
    if (typeof data === 'string' && data.length > 0) return data
    if (data && typeof data === 'object' && 'message' in data) {
      return String((data as { message: string }).message)
    }
  }
  return fallback
}
