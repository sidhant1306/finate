import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './context/AuthContext'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Toaster
          position="top-center"
          toastOptions={{
            style: {
              background: '#1A1A24',
              color: '#F5F5F5',
              border: '1px solid rgba(255,255,255,0.06)',
              fontSize: '14px',
              borderRadius: '12px',
              boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
            },
            success: {
              iconTheme: { primary: '#10B981', secondary: '#1A1A24' },
            },
            error: {
              iconTheme: { primary: '#EF4444', secondary: '#1A1A24' },
            },
          }}
        />
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
