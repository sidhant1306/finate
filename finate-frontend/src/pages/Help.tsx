import {
  CircleHelp,
  ExternalLink,
  Database,
  Code2,
  Cloud,
} from 'lucide-react'

const steps = [
  'Register and sign in to access your dashboard.',
  'Deposit funds to your wallet via Razorpay, or send money to other users via UPI.',
  'Track daily income and expenses on the Tracking page with categories and filters.',
  'Set category budgets and monitor remaining amounts on the Budget page.',
  'Search stocks, add to your watchlist, and buy or sell shares on the Stocks page.',
  'View your portfolio performance and P&L on the Portfolio page.',
  'Upgrade to Premium for AI insights and advanced analytics.',
]

const techStack = [
  { icon: Database, label: 'Backend', items: 'Spring Boot, PostgreSQL, Redis, JWT, Razorpay, Finnhub' },
  { icon: Code2, label: 'Frontend', items: 'React, TypeScript, Tailwind CSS, Axios, React Router' },
  { icon: Cloud, label: 'Infrastructure', items: 'Docker, WebSocket, Spring AI' },
]

export default function Help() {
  return (
    <div className="mx-auto max-w-4xl">
      <div className="animate-fade-in flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-500/10">
          <CircleHelp className="h-5 w-5 text-violet-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Help</h1>
          <p className="text-sm text-gray-500">About Finate</p>
        </div>
      </div>

      {/* About */}
      <section className="animate-fade-in-delay-1 mt-6 glass-card p-5">
        <h2 className="text-base font-semibold text-white">About</h2>
        <p className="mt-3 text-sm leading-relaxed text-gray-400">
          Finate is a full-stack personal finance and investment tracker built with Spring Boot, React,
          WebSocket, Redis, PostgreSQL, Razorpay, Spring AI, and Docker.
        </p>
        <a
          href="https://github.com/sidhant1306"
          target="_blank"
          rel="noopener noreferrer"
          className="mt-4 flex items-center gap-2 text-sm font-medium text-emerald-400 transition-colors hover:text-emerald-300"
        >
          <ExternalLink className="h-4 w-4" /> github.com/sidhant1306
        </a>
      </section>

      {/* Tech Stack */}
      <section className="animate-fade-in-delay-2 mt-6 glass-card p-5">
        <h2 className="text-base font-semibold text-white">Tech Stack</h2>
        <div className="mt-3 flex flex-col gap-3">
          {techStack.map(({ icon: Icon, label, items }) => (
            <div key={label} className="flex items-start gap-3">
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/5">
                <Icon className="h-4 w-4 text-gray-400" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-200">{label}</p>
                <p className="text-xs text-gray-500">{items}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* How to use */}
      <section className="animate-fade-in-delay-3 mt-6 glass-card p-5">
        <h2 className="text-base font-semibold text-white">How to use</h2>
        <ol className="mt-3 flex flex-col gap-3">
          {steps.map((step, index) => (
            <li key={index} className="flex items-start gap-3">
              <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-lg bg-emerald-500/10 text-xs font-bold text-emerald-400">
                {index + 1}
              </span>
              <p className="text-sm leading-relaxed text-gray-400">{step}</p>
            </li>
          ))}
        </ol>
      </section>
    </div>
  )
}
