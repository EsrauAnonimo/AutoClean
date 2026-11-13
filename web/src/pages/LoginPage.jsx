import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { BACKEND_BASE_URL } from '../services/api'
import { getHomeRouteByRole } from '../utils/authRedirect'

const GOOGLE_AUTH_URL = `${BACKEND_BASE_URL}/oauth2/authorization/google`

/**
 * Pantalla de acceso dividida (Desktop) / Mobile-first para Admin y Clientes.
 */
export function LoginPage() {
  const { isAuthenticated, user, login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
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
        setErrorMessage('No fue posible conectar con el servidor. Verifica que el servicio esté activo.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#F5F6FB] lg:grid lg:grid-cols-12">
      {/* Panel izquierdo Índigo oscuro (Desktop) + Header gradiente (Móvil) */}
      <div className="flex flex-col justify-between bg-gradient-to-br from-[#1E1B4B] via-[#312E81] to-[#4F46E5] px-6 py-8 text-white lg:col-span-5 lg:p-12">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15 text-base font-bold">
            AC
          </div>
          <span className="text-lg font-bold tracking-tight">AutoClean</span>
        </div>

        <div className="my-6 space-y-4 lg:my-0">
          <span className="inline-flex rounded-full bg-[#8B5CF6]/30 px-3 py-1 text-xs font-semibold text-indigo-100">
            Lavado y Detailing Premium
          </span>
          <h1 className="text-[28px] leading-[34px] font-bold tracking-tight lg:text-4xl lg:leading-tight">
            Tu vehículo impecable, con seguimiento en tiempo real.
          </h1>
          <p className="text-[15px] text-indigo-100/90">
            Agenda en menos de un minuto con tu placa, revisa el avance de tu servicio en vivo y paga en efectivo al finalizar.
          </p>
        </div>

        <p className="hidden text-[13px] text-indigo-200 lg:block">
          Horario de atención: 7:00 a. m. – 12:00 p. m. y 2:30 p. m. – 9:00 p. m.
        </p>
      </div>

      {/* Formulario derecho */}
      <div className="flex items-center justify-center px-4 py-8 sm:px-8 lg:col-span-7">
        <div className="w-full max-w-[440px] rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card sm:p-8">
          <div className="mb-6">
            <h2 className="text-[22px] font-semibold text-[#1E1B4B]">Inicia sesión</h2>
            <p className="mt-1 text-[14px] text-[#5B5F7A]">
              Ingresa a tu cuenta de AutoClean para continuar
            </p>
          </div>

          {errorMessage && (
            <div
              role="alert"
              className="mb-5 rounded-xl border border-[#FECACA] bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
            >
              {errorMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <Input
              id="email"
              label="Correo electrónico"
              type="email"
              size="l"
              autoComplete="email"
              placeholder="correo@autoclean.co"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <div className="space-y-1.5">
              <label htmlFor="password" className="block text-[13px] font-semibold text-[#1E1B4B]">
                Contraseña
              </label>
              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="block h-[52px] w-full rounded-xl border border-[#E4E6F2] bg-white pl-3.5 pr-20 text-[15px] text-[#1E1B4B] placeholder-[#5B5F7A]/60 transition hover:border-[#8B5CF6]/50 focus:border-[#4F46E5] focus:ring-4 focus:ring-[#4F46E5]/15 focus:outline-none"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg px-2 py-1 text-xs font-semibold text-[#4F46E5] hover:bg-[#EDE9FE]/60"
                >
                  {showPassword ? 'Ocultar' : 'Mostrar'}
                </button>
              </div>
            </div>

            <Button type="submit" size="l" loading={submitting} className="w-full shadow-float">
              Ingresar
            </Button>
          </form>

          <div className="my-5 flex items-center gap-3">
            <span className="h-px flex-1 bg-[#E4E6F2]" />
            <span className="text-xs font-medium text-[#5B5F7A]">o continúa con</span>
            <span className="h-px flex-1 bg-[#E4E6F2]" />
          </div>

          <a
            href={GOOGLE_AUTH_URL}
            className="inline-flex h-[52px] w-full items-center justify-center gap-2.5 rounded-xl border border-[#E4E6F2] bg-white px-4 text-[15px] font-semibold text-[#1E1B4B] transition hover:bg-[#F5F6FB]"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="#4285F4"
                d="M21.8 12.23c0-.68-.06-1.36-.18-2.02H12v3.83h5.5a4.7 4.7 0 0 1-2.04 3.08v2.56h3.3c1.93-1.78 3.04-4.4 3.04-7.45Z"
              />
              <path
                fill="#34A853"
                d="M12 22c2.76 0 5.08-.91 6.77-2.47l-3.3-2.56c-.91.61-2.08.98-3.47.98-2.67 0-4.93-1.8-5.74-4.22H2.85v2.64A10 10 0 0 0 12 22Z"
              />
              <path
                fill="#FBBC05"
                d="M6.26 13.73A6.01 6.01 0 0 1 5.95 12c0-.6.11-1.18.31-1.73V7.63H2.85A10 10 0 0 0 2 12c0 1.61.39 3.14 1.07 4.37l3.19-2.64Z"
              />
              <path
                fill="#EA4335"
                d="M12 6.05c1.5 0 2.85.52 3.91 1.53l2.93-2.93C17.07 3 14.76 2 12 2A10 10 0 0 0 2.85 7.63l3.41 2.64C7.07 7.85 9.33 6.05 12 6.05Z"
              />
            </svg>
            Continuar con Google
          </a>

          <p className="mt-6 text-center text-[14px] text-[#5B5F7A]">
            ¿Aún no tienes cuenta?{' '}
            <Link to="/register" className="font-semibold text-[#4F46E5] hover:text-[#4338CA]">
              Regístrate aquí
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
