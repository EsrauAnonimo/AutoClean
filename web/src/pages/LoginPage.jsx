import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { BACKEND_BASE_URL } from '../services/api'
import { getHomeRouteByRole } from '../utils/authRedirect'

const GOOGLE_AUTH_URL = `${BACKEND_BASE_URL}/oauth2/authorization/google`

/**
 * Pantalla de acceso que autentica contra POST /api/auth/login,
 * persiste el JWT y redirige según el rol (ADMIN -> /dashboard, CLIENTE -> /portal/dashboard).
 */
export function LoginPage() {
  const { isAuthenticated, user, login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={getHomeRouteByRole(user?.role)} replace />
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    if (!email.trim() || !password.trim()) {
      setErrorMessage('Ingresa tu correo electrónico y contraseña.')
      return
    }

    setSubmitting(true)
    try {
      const authData = await login({ email: email.trim(), password })
      navigate(getHomeRouteByRole(authData?.role), { replace: true })
    } catch (error) {
      const apiMessage = error.response?.data?.message
      if (apiMessage) {
        setErrorMessage(apiMessage)
      } else if (error.response?.status === 401 || error.response?.status === 403) {
        setErrorMessage('Credenciales inválidas. Verifica tu correo y contraseña.')
      } else {
        setErrorMessage('No fue posible conectar con el servidor. Verifica que el backend esté activo.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-sky-900 px-4 py-12">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl ring-1 ring-slate-200">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-sky-600 text-lg font-bold text-white shadow-sm">
            AC
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">AutoClean</h1>
          <p className="mt-1 text-sm text-slate-500">
            Inicia sesión para acceder a tu cuenta
          </p>
        </div>

        {errorMessage && (
          <div
            role="alert"
            className="mb-5 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
          >
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          <Input
            id="email"
            label="Correo electrónico"
            type="email"
            autoComplete="email"
            placeholder="correo@autoclean.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <Input
            id="password"
            label="Contraseña"
            type="password"
            autoComplete="current-password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <Button type="submit" loading={submitting} className="w-full">
            Ingresar
          </Button>
        </form>

        <div className="my-5 flex items-center gap-3">
          <div className="h-px flex-1 bg-slate-200" />
          <span className="text-xs font-medium text-slate-400 uppercase">o</span>
          <div className="h-px flex-1 bg-slate-200" />
        </div>

        <a
          href={GOOGLE_AUTH_URL}
          className="inline-flex w-full items-center justify-center gap-2.5 rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 transition-colors hover:bg-slate-50"
        >
          <svg className="h-4 w-4" viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="#4285F4"
              d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17Z"
            />
            <path
              fill="#34A853"
              d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.11-6.72-4.96H1.29v3.14C3.26 21.3 7.31 24 12 24Z"
            />
            <path
              fill="#FBBC05"
              d="M5.28 14.24c-.24-.72-.38-1.49-.38-2.24s.14-1.52.38-2.24V6.62H1.29C.47 8.24 0 10.06 0 12s.47 3.76 1.29 5.38l3.99-3.14Z"
            />
            <path
              fill="#EA4335"
              d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.62l3.99 3.14c.95-2.85 3.6-4.96 6.72-4.96Z"
            />
          </svg>
          Continuar con Google
        </a>

        <p className="mt-6 text-center text-sm text-slate-600">
          ¿Aún no tienes cuenta?{' '}
          <Link to="/register" className="font-semibold text-sky-600 hover:text-sky-700">
            Regístrate aquí
          </Link>
        </p>
      </div>
    </div>
  )
}
