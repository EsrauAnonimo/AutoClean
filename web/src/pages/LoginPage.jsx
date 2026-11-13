import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'

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
