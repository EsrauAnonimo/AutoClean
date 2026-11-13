import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { useAuth } from '../context/AuthContext'

/**
 * Pantalla de acceso que autentica contra POST /api/auth/login y persiste el token JWT.
 */
export function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
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
      await login({ email: email.trim(), password })
      navigate('/dashboard', { replace: true })
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
            Inicia sesión para gestionar citas, clientes y detailing
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
            placeholder="admin@autoclean.com"
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
      </div>
    </div>
  )
}
