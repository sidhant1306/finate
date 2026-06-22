import { Menu } from 'lucide-react'

interface TopBarProps {
  onMenuClick: () => void
}

export function TopBar({ onMenuClick }: TopBarProps) {
  return (
    <header className="topbar-glass sticky top-0 z-30 flex h-14 items-center gap-3 px-4 md:hidden">
      <button
        type="button"
        onClick={onMenuClick}
        className="flex h-9 w-9 items-center justify-center rounded-xl text-gray-400 transition-colors hover:bg-white/5 hover:text-gray-200"
      >
        <Menu className="h-5 w-5" />
      </button>
      <div className="flex items-center gap-2">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-emerald-500/15 ring-1 ring-emerald-500/25">
          <span className="text-xs font-bold text-emerald-400">F</span>
        </div>
        <span className="gradient-text text-base font-bold tracking-tight">Finate</span>
      </div>
    </header>
  )
}
