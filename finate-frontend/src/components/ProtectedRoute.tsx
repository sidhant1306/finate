import { useCallback, useEffect, useState } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Sidebar } from './Sidebar'
import { TopBar } from './TopBar'
import { PageTransition } from './PageTransition'

const DESKTOP_BREAKPOINT = 1024
const TABLET_BREAKPOINT = 768

export function ProtectedRoute() {
  const { token } = useAuth()
  const [sidebarOpen, setSidebarOpen] = useState<boolean>(false)
  const [sidebarCollapsed, setSidebarCollapsed] = useState<boolean>(false)

  // Auto-collapse sidebar on tablet-sized screens, expand on desktop
  const handleResize = useCallback(() => {
    const width = window.innerWidth
    if (width >= DESKTOP_BREAKPOINT) {
      setSidebarCollapsed(false)
      setSidebarOpen(false)
    } else if (width >= TABLET_BREAKPOINT) {
      setSidebarCollapsed(true)
      setSidebarOpen(false)
    } else {
      setSidebarOpen(false)
    }
  }, [])

  useEffect(() => {
    handleResize()
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [handleResize])

  if (!token) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="flex min-h-screen bg-[#0B0B0F]">
      {/* Sidebar */}
      <Sidebar
        isOpen={sidebarOpen}
        isCollapsed={sidebarCollapsed}
        onClose={() => setSidebarOpen(false)}
        onToggleCollapse={() => setSidebarCollapsed((prev) => !prev)}
      />

      {/* Main content area */}
      <main
        className={`flex-1 transition-[margin] duration-300 ease-in-out
          md:ml-[72px]
          ${!sidebarCollapsed ? 'lg:ml-[260px]' : 'lg:ml-[72px]'}
        `}
      >
        {/* Mobile top bar */}
        <TopBar onMenuClick={() => setSidebarOpen(true)} />

        {/* Page content with transitions */}
        <div className="px-4 py-6 sm:px-6 lg:px-8">
          <PageTransition>
            <Outlet />
          </PageTransition>
        </div>
      </main>
    </div>
  )
}
