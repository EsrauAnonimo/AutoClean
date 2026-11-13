import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '../services/api'
import authService from '../services/authService'

const AuthContext = createContext(null)

function readStoredUser() {
  const raw = localStorage.getItem(USER_STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

/**
 * Proveedor global de estado de autenticación JWT persistido en localStorage.
 */
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_STORAGE_KEY))
  const [user, setUser] = useState(() => readStoredUser())

  const persistSession = useCallback((authPayload) => {
    const nextToken = authPayload?.token ?? null
    const nextUser = authPayload
      ? {
          email: authPayload.email,
          role: authPayload.role,
          customerId: authPayload.customerId ?? null,
        }
      : null

    if (nextToken) {
      localStorage.setItem(TOKEN_STORAGE_KEY, nextToken)
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(nextUser))
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY)
      localStorage.removeItem(USER_STORAGE_KEY)
    }

    setToken(nextToken)
    setUser(nextUser)
  }, [])

  const login = useCallback(
    async (credentials) => {
      const data = await authService.login(credentials)
      persistSession(data)
      return data
    },
    [persistSession],
  )

  const logout = useCallback(() => {
    persistSession(null)
  }, [persistSession])

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      logout,
    }),
    [token, user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * Hook de acceso al contexto de autenticación.
 */
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe utilizarse dentro de AuthProvider')
  }
  return context
}
