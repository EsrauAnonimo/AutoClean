import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { AUTH_UNAUTHORIZED_EVENT, TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '../services/api'
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

function parseJwtPayload(jwtToken) {
  const parts = jwtToken?.split('.')
  if (!parts || parts.length !== 3) {
    throw new Error('Token JWT inválido.')
  }
  const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  const json = decodeURIComponent(
    atob(padded)
      .split('')
      .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
      .join(''),
  )
  return JSON.parse(json)
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

  const register = useCallback(
    async (payload) => {
      const data = await authService.register(payload)
      persistSession(data)
      return data
    },
    [persistSession],
  )

  const completeProfile = useCallback(
    async (payload) => {
      const data = await authService.completeProfile(payload)
      persistSession(data)
      return data
    },
    [persistSession],
  )

  const loginWithToken = useCallback(
    (rawToken) => {
      const claims = parseJwtPayload(rawToken)
      if (!claims?.sub || !claims?.role) {
        throw new Error('El token recibido no contiene correo ni rol válidos.')
      }
      if (claims.exp && claims.exp * 1000 <= Date.now()) {
        throw new Error('El token recibido ya expiró.')
      }

      const sessionData = {
        token: rawToken,
        email: claims.sub,
        role: claims.role,
        customerId: claims.customerId ?? null,
      }
      persistSession(sessionData)
      return sessionData
    },
    [persistSession],
  )

  const logout = useCallback(() => {
    persistSession(null)
  }, [persistSession])

  // Sincroniza el contexto cuando el interceptor HTTP detecta un token inválido o expirado (401)
  useEffect(() => {
    const handleUnauthorized = () => {
      persistSession(null)
    }

    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [persistSession])

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      register,
      completeProfile,
      loginWithToken,
      logout,
    }),
    [token, user, login, register, completeProfile, loginWithToken, logout],
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
