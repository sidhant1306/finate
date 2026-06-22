import { useLocation } from 'react-router-dom'
import { type ReactNode } from 'react'

interface PageTransitionProps {
  children: ReactNode
}

export function PageTransition({ children }: PageTransitionProps) {
  const location = useLocation()

  return (
    <div key={location.pathname} className="page-enter">
      {children}
    </div>
  )
}
