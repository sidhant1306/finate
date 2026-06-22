import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { UserRole } from '../types/types'

interface AuthContextType {
  token: string | null
  firstName: string | null
  lastName: string | null
  userEmail: string | null
  username: string | null
  userRole: UserRole | null
  login: (token: string, firstName: string, lastName: string, userEmail: string, username: string, userRole: UserRole) => void
  logout: () => void
  setUserRole: (role: UserRole) => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  const [firstName, setFirstName] = useState<string | null>(() => localStorage.getItem('firstName'))
  const [lastName, setLastName] = useState<string | null>(() => localStorage.getItem('lastName'))
  const [userEmail, setUserEmail] = useState<string | null>(() => localStorage.getItem('userEmail'))
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem('username'))
  const [userRole, setUserRoleState] = useState<UserRole | null>(
    () => (localStorage.getItem('userRole') as UserRole | null)
  )

  const login = useCallback((
    newToken: string,
    newFirstName: string,
    newLastName: string,
    newUserEmail: string,
    newUsername: string,
    newUserRole: UserRole
  ) => {
    setToken(newToken)
    setFirstName(newFirstName)
    setLastName(newLastName)
    setUserEmail(newUserEmail)
    setUsername(newUsername)
    setUserRoleState(newUserRole)
    localStorage.setItem('token', newToken)
    localStorage.setItem('firstName', newFirstName)
    localStorage.setItem('lastName', newLastName)
    localStorage.setItem('userEmail', newUserEmail)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('userRole', newUserRole)
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setFirstName(null)
    setLastName(null)
    setUserEmail(null)
    setUsername(null)
    setUserRoleState(null)
    localStorage.removeItem('token')
    localStorage.removeItem('firstName')
    localStorage.removeItem('lastName')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('username')
    localStorage.removeItem('userRole')
    window.location.href = '/login'
  }, [])

  const setUserRole = useCallback((role: UserRole) => {
    setUserRoleState(role)
    localStorage.setItem('userRole', role)
  }, [])

  const value = useMemo(
    () => ({ token, firstName, lastName, userEmail, username, userRole, login, logout, setUserRole }),
    [token, firstName, lastName, userEmail, username, userRole, login, logout, setUserRole],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
