import { useState, useRef, useEffect, type FormEvent, type KeyboardEvent } from 'react'
import toast from 'react-hot-toast'
import { Bot, Send, Trash2, Sparkles, User } from 'lucide-react'
import axiosInstance from '../api/axiosInstance'
import { useAuth } from '../context/AuthContext'
import DOMPurify from 'dompurify'
import { getApiError } from '../utils/apiHelpers'

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const SUGGESTIONS = [
  'How can I reduce my monthly spending?',
  'Analyze my budget this month',
  'Where am I overspending?',
  'Give me a savings plan for this month',
]

export default function AiChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [isTyping, setIsTyping] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const idCounter = useRef(0)
  const { firstName, userRole } = useAuth()

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isTyping])

  // Auto-focus input on mount
  useEffect(() => {
    inputRef.current?.focus()
  }, [])

  const sendMessage = async (text?: string) => {
    const messageText = (text ?? input).trim()
    if (!messageText || isTyping) return

    const userMessage: ChatMessage = {
      id: ++idCounter.current,
      role: 'user',
      content: messageText,
      timestamp: new Date(),
    }

    setMessages((prev) => [...prev, userMessage])
    setInput('')
    setIsTyping(true)

    // Reset textarea height
    if (inputRef.current) inputRef.current.style.height = 'auto'

    try {
      const { data } = await axiosInstance.post<{ aiResponse: string }>('/api/ai/chat', {
        message: messageText,
      })

      const assistantMessage: ChatMessage = {
        id: ++idCounter.current,
        role: 'assistant',
        content: data.aiResponse,
        timestamp: new Date(),
      }

      setMessages((prev) => [...prev, assistantMessage])
    } catch (error) {
      toast.error(getApiError(error, 'Failed to get AI response'))
      // Add error message to chat
      const errorMessage: ChatMessage = {
        id: ++idCounter.current,
        role: 'assistant',
        content: 'Sorry, I encountered an error. Please try again.',
        timestamp: new Date(),
      }
      setMessages((prev) => [...prev, errorMessage])
    } finally {
      setIsTyping(false)
    }
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    void sendMessage()
  }

  const handleKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void sendMessage()
    }
  }

  const clearChat = () => {
    setMessages([])
    toast.success('Chat cleared')
  }

  const formatTime = (date: Date) =>
    date.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })

  // Simple markdown-like formatting for AI responses
  const formatContent = (text: string) => {
    return text.split('\n').map((line, i) => {
      // Bold **text**
      const formatted = line.replace(/\*\*(.*?)\*\*/g, '<strong class="text-white font-semibold">$1</strong>')
      // Numbered lists
      const isListItem = /^\d+[\.\)]\s/.test(line)
      // Bullet points
      const isBullet = /^[-•]\s/.test(line)

      if (isListItem || isBullet) {
        return (
          <p key={i} className="ml-3 mt-1.5 text-sm leading-relaxed text-gray-300"
            dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(formatted) }}
          />
        )
      }

      return (
        <p key={i} className={`text-sm leading-relaxed text-gray-300 ${i > 0 ? 'mt-2' : ''}`}
          dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(formatted) }}
        />
      )
    })
  }

  const isPremium = userRole === 'PREMIUM'

  return (
    <div className="flex h-[calc(100dvh-24px)] flex-col lg:h-[calc(100dvh-48px)]">
      {/* Header */}
      <div className="shrink-0 border-b border-white/[0.04] px-4 py-4">
        <div className="mx-auto flex max-w-3xl items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500/20 to-blue-500/20 ring-1 ring-violet-500/20">
              <Bot className="h-5 w-5 text-violet-400" />
              <span className="absolute -right-0.5 -top-0.5 h-2.5 w-2.5 rounded-full bg-emerald-400 ring-2 ring-[#0B0B0F]" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-white">Finate AI</h1>
              <p className="text-[11px] text-gray-500">Personal finance advisor</p>
            </div>
          </div>
          {messages.length > 0 && (
            <button type="button" onClick={clearChat}
              className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-white/5 hover:text-red-400">
              <Trash2 className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        <div className="mx-auto max-w-3xl">
          {messages.length === 0 ? (
            /* Empty state */
            <div className="flex h-full flex-col items-center justify-center py-12">
              <div className="relative">
                <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-500/15 to-blue-500/15 ring-1 ring-violet-500/20">
                  <Bot className="h-8 w-8 text-violet-400" />
                </div>
                <Sparkles className="absolute -right-2 -top-2 h-5 w-5 text-amber-400 animate-pulse" />
              </div>
              <h2 className="mt-5 text-lg font-bold text-white">
                Hey {firstName || 'there'}! 👋
              </h2>
              <p className="mt-1.5 max-w-xs text-center text-sm text-gray-500">
                I&apos;m your AI finance advisor. Ask me anything about your spending, budgets, or savings.
              </p>

              {!isPremium && (
                <div className="mt-4 rounded-xl bg-amber-500/10 px-4 py-2.5 ring-1 ring-amber-500/20">
                  <p className="text-xs text-amber-400">
                    ⚡ Premium feature — AI chat requires a Premium subscription.
                  </p>
                </div>
              )}

              {isPremium && (
                <div className="mt-6 flex flex-wrap justify-center gap-2">
                  {SUGGESTIONS.map((suggestion) => (
                    <button key={suggestion} type="button"
                      onClick={() => void sendMessage(suggestion)}
                      className="rounded-xl bg-white/[0.04] px-3.5 py-2 text-xs text-gray-400 ring-1 ring-white/[0.06] transition-all hover:bg-white/[0.08] hover:text-gray-200 hover:ring-white/[0.12]">
                      {suggestion}
                    </button>
                  ))}
                </div>
              )}
            </div>
          ) : (
            /* Chat messages */
            <div className="flex flex-col gap-4 pb-2">
              {messages.map((msg) => (
                <div key={msg.id}
                  className={`flex gap-2.5 ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'}`}>
                  {/* Avatar */}
                  <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-lg ${
                    msg.role === 'user'
                      ? 'bg-emerald-500/15 ring-1 ring-emerald-500/20'
                      : 'bg-violet-500/15 ring-1 ring-violet-500/20'
                  }`}>
                    {msg.role === 'user'
                      ? <User className="h-3.5 w-3.5 text-emerald-400" />
                      : <Bot className="h-3.5 w-3.5 text-violet-400" />
                    }
                  </div>

                  {/* Bubble */}
                  <div className={`max-w-[80%] rounded-2xl px-4 py-3 ${
                    msg.role === 'user'
                      ? 'rounded-tr-md bg-emerald-500/15 ring-1 ring-emerald-500/15'
                      : 'rounded-tl-md bg-white/[0.04] ring-1 ring-white/[0.06]'
                  }`}>
                    {msg.role === 'user' ? (
                      <p className="text-sm text-gray-200">{msg.content}</p>
                    ) : (
                      <div>{formatContent(msg.content)}</div>
                    )}
                    <p className={`mt-1.5 text-[10px] ${
                      msg.role === 'user' ? 'text-right text-emerald-500/50' : 'text-violet-500/40'
                    }`}>
                      {formatTime(msg.timestamp)}
                    </p>
                  </div>
                </div>
              ))}

              {/* Typing indicator */}
              {isTyping && (
                <div className="flex gap-2.5">
                  <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-violet-500/15 ring-1 ring-violet-500/20">
                    <Bot className="h-3.5 w-3.5 text-violet-400" />
                  </div>
                  <div className="rounded-2xl rounded-tl-md bg-white/[0.04] px-4 py-3 ring-1 ring-white/[0.06]">
                    <div className="flex gap-1">
                      <span className="h-2 w-2 rounded-full bg-gray-500 animate-bounce" style={{ animationDelay: '0ms' }} />
                      <span className="h-2 w-2 rounded-full bg-gray-500 animate-bounce" style={{ animationDelay: '150ms' }} />
                      <span className="h-2 w-2 rounded-full bg-gray-500 animate-bounce" style={{ animationDelay: '300ms' }} />
                    </div>
                  </div>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>
          )}
        </div>
      </div>

      {/* Input area */}
      <div className="shrink-0 border-t border-white/[0.04] px-4 py-3">
        <form onSubmit={handleSubmit} className="mx-auto flex max-w-3xl items-end gap-2">
          <div className="relative flex-1">
            <textarea
              ref={inputRef}
              value={input}
              onChange={(e) => {
                setInput(e.target.value)
                // Auto-resize
                e.target.style.height = 'auto'
                e.target.style.height = Math.min(e.target.scrollHeight, 120) + 'px'
              }}
              onKeyDown={handleKeyDown}
              placeholder={isPremium ? 'Ask about your finances...' : 'Premium feature — upgrade to chat'}
              disabled={!isPremium || isTyping}
              rows={1}
              className="w-full resize-none rounded-xl bg-white/[0.04] px-4 py-3 pr-10 text-sm text-gray-200 placeholder-gray-600 ring-1 ring-white/[0.06] transition-all focus:outline-none focus:ring-emerald-500/30 disabled:cursor-not-allowed disabled:opacity-50"
              style={{ maxHeight: '120px' }}
            />
          </div>
          <button
            type="submit"
            disabled={!input.trim() || isTyping || !isPremium}
            className="flex h-[44px] w-[44px] shrink-0 items-center justify-center rounded-xl bg-emerald-500/15 text-emerald-400 ring-1 ring-emerald-500/20 transition-all hover:bg-emerald-500/25 disabled:cursor-not-allowed disabled:opacity-30"
          >
            <Send className="h-4 w-4" />
          </button>
        </form>
      </div>
    </div>
  )
}
