import axios from 'axios'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL,
})

// Attach JWT token to every outgoing request
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// If any API returns 401 or 403 (expired/invalid token), clear all auth data and redirect
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const originalRequest = error.config

    if (
      originalRequest &&
      (originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/register'))
    ) {
      return Promise.reject(error)
    }

    if (axios.isAxiosError(error) && error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('firstName')
      localStorage.removeItem('lastName')
      localStorage.removeItem('userEmail')
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default axiosInstance
