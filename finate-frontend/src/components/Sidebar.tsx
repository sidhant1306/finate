import { NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Wallet,
  ArrowLeftRight,
  PiggyBank,
  TrendingUp,
  Briefcase,
  Crown,
  Bot,
  CircleHelp,
  LogOut,
  ChevronLeft,
  ChevronRight,
  User,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'

interface NavItem {
  to: string
  label: string
  icon: typeof LayoutDashboard
}

interface NavSection {
  title: string
  items: NavItem[]
}

const navSections: NavSection[] = [
  {
    title: 'Overview',
    items: [
      { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    ],
  },
  {
    title: 'Finance',
    items: [
      { to: '/wallet', label: 'Wallet', icon: Wallet },
      { to: '/tracking', label: 'Tracking', icon: ArrowLeftRight },
      { to: '/budget', label: 'Budget', icon: PiggyBank },
    ],
  },
  {
    title: 'Markets',
    items: [
      { to: '/stocks', label: 'Stocks', icon: TrendingUp },
      { to: '/portfolio', label: 'Portfolio', icon: Briefcase },
    ],
  },
  {
    title: 'Account',
    items: [
      { to: '/premium', label: 'Premium', icon: Crown },
      { to: '/ai-chat', label: 'AI Chat', icon: Bot },
      { to: '/help', label: 'Help', icon: CircleHelp },
    ],
  },
]

interface SidebarProps {
  isOpen: boolean
  isCollapsed: boolean
  onClose: () => void
  onToggleCollapse: () => void
}

export function Sidebar({ isOpen, isCollapsed, onClose, onToggleCollapse }: SidebarProps) {
  const location = useLocation()
  const { firstName, lastName, userEmail, userRole, logout } = useAuth()

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to log out?')) {
      logout()
    }
  }

  const sidebarWidth = isCollapsed ? 'w-[72px]' : 'w-[260px]'

  return (
    <>
      {/* Mobile overlay backdrop */}
      {isOpen && (
        <div
          className="sidebar-overlay md:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`sidebar-glass fixed top-0 left-0 z-40 flex h-screen flex-col transition-all duration-300 ease-in-out
          ${sidebarWidth}
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
          md:translate-x-0
        `}
      >
        {/* Logo */}
        <div className="flex h-16 shrink-0 items-center justify-between px-5">
          {!isCollapsed && (
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-emerald-500/15 ring-1 ring-emerald-500/25">
                <span className="text-sm font-bold text-emerald-400">F</span>
              </div>
              <span className="gradient-text text-lg font-bold tracking-tight">Finate</span>
            </div>
          )}
          {isCollapsed && (
            <div className="mx-auto flex h-8 w-8 items-center justify-center rounded-xl bg-emerald-500/15 ring-1 ring-emerald-500/25">
              <span className="text-sm font-bold text-emerald-400">F</span>
            </div>
          )}
          <button
            type="button"
            onClick={onToggleCollapse}
            className="hidden md:flex h-6 w-6 items-center justify-center rounded-lg text-gray-500 transition-colors hover:bg-white/5 hover:text-gray-300"
          >
            {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto overflow-x-hidden px-3 py-2">
          {navSections.map((section, sectionIndex) => (
            <div key={section.title} className={sectionIndex > 0 ? 'mt-5' : ''}>
              {!isCollapsed && (
                <p className="mb-2 px-3 text-[10px] font-semibold uppercase tracking-[0.1em] text-gray-500">
                  {section.title}
                </p>
              )}
              {isCollapsed && sectionIndex > 0 && (
                <div className="mx-3 mb-3 border-t border-white/[0.06]" />
              )}
              <div className="flex flex-col gap-0.5">
                {section.items.map(({ to, label, icon: Icon }) => {
                  const isActive = location.pathname === to
                  return (
                    <NavLink
                      key={to}
                      to={to}
                      onClick={onClose}
                      className={`sidebar-link group relative flex items-center gap-3 rounded-xl px-3 py-2.5 text-[13px] font-medium transition-all duration-200
                        ${isActive
                          ? 'bg-emerald-500/10 text-emerald-400'
                          : 'text-gray-400 hover:bg-white/[0.04] hover:text-gray-200'
                        }
                        ${isCollapsed ? 'justify-center' : ''}
                      `}
                      title={isCollapsed ? label : undefined}
                    >
                      {/* Active indicator bar */}
                      {isActive && (
                        <span className="absolute left-0 top-1/2 h-5 w-[3px] -translate-y-1/2 rounded-r-full bg-emerald-400" />
                      )}
                      <Icon className="h-[18px] w-[18px] shrink-0" strokeWidth={isActive ? 2 : 1.5} />
                      {!isCollapsed && <span>{label}</span>}

                      {/* Tooltip for collapsed mode */}
                      {isCollapsed && (
                        <span className="pointer-events-none absolute left-full ml-3 rounded-lg bg-[#1A1A24] px-2.5 py-1.5 text-xs font-medium text-gray-200 opacity-0 shadow-lg ring-1 ring-white/[0.08] transition-opacity group-hover:opacity-100">
                          {label}
                        </span>
                      )}
                    </NavLink>
                  )
                })}
              </div>
            </div>
          ))}
        </nav>

        {/* User profile + logout */}
        <div className="shrink-0 border-t border-white/[0.06] p-3">
          {!isCollapsed ? (
            <>
              <div className="flex items-center gap-3 rounded-xl px-3 py-2.5">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500/20 to-teal-500/20 ring-1 ring-emerald-500/20">
                  <User className="h-4 w-4 text-emerald-400" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-gray-200">
                    {firstName} {lastName}
                  </p>
                  <p className="truncate text-[11px] text-gray-500">{userEmail}</p>
                </div>
                {userRole && (
                  <span className={`badge text-[9px] ${userRole === 'PREMIUM' ? 'badge-premium' : 'badge-accent'}`}>
                    {userRole === 'PREMIUM' ? '★' : userRole}
                  </span>
                )}
              </div>
              <button
                type="button"
                onClick={handleLogout}
                className="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-[13px] font-medium text-red-400/70 transition-all duration-200 hover:bg-red-500/[0.08] hover:text-red-400"
              >
                <LogOut className="h-[18px] w-[18px]" strokeWidth={1.5} />
                <span>Log out</span>
              </button>
            </>
          ) : (
            <div className="flex flex-col items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500/20 to-teal-500/20 ring-1 ring-emerald-500/20">
                <User className="h-4 w-4 text-emerald-400" />
              </div>
              <button
                type="button"
                onClick={handleLogout}
                className="group relative flex h-9 w-9 items-center justify-center rounded-xl text-red-400/70 transition-all hover:bg-red-500/[0.08] hover:text-red-400"
                title="Log out"
              >
                <LogOut className="h-[18px] w-[18px]" strokeWidth={1.5} />
                <span className="pointer-events-none absolute left-full ml-3 rounded-lg bg-[#1A1A24] px-2.5 py-1.5 text-xs font-medium text-gray-200 opacity-0 shadow-lg ring-1 ring-white/[0.08] transition-opacity group-hover:opacity-100">
                  Log out
                </span>
              </button>
            </div>
          )}
        </div>
      </aside>
    </>
  )
}
