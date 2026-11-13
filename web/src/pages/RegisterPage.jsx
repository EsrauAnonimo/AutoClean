import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { AuthHeroPanel } from '../components/AuthHeroPanel'
import { AutoCleanLogo } from '../components/AutoCleanLogo'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/**
 * Pantalla de registro de clientes con estética móvil/desktop AutoClean.
 */
export function RegisterPage() {
  const { isAuthenticated, user, register } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [errorMessage, setErrorMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={getHomeRouteByRole(user?.role)} replace />
  }

  const validateForm = () => {
    const errors = {}
    if (!name.trim()) errors.name = 'El nombre completo es obligatorio.'
    if (!phone.trim()) errors.phone = 'El teléfono de contacto es obligatorio.'
    if (!email.trim()) errors.email = 'El correo electrónico es obligatorio.'
    else if (!EMAIL_PATTERN.test(email.trim())) errors.email = 'El formato del correo es inválido.'
    if (!password) errors.password = 'La contraseña es obligatoria.'
    else if (password.length < 6) errors.password = 'La contraseña debe tener al menos 6 caracteres.'
    return errors
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')
    setFieldErrors({})

    const localErrors = validateForm()
    if (Object.keys(localErrors).length > 0) {
      setFieldErrors(localErrors)
      return
    }

    setSubmitting(true)
    try {
      const authData = await register({
        name: name.trim(),
        phone: phone.trim(),
        email: email.trim(),
        password,
      })
      navigate(getHomeRouteByRole(authData?.role), { replace: true })
    } catch (error) {
      const responseData = error.response?.data
      if (responseData?.validationErrors) {
        setFieldErrors(responseData.validationErrors)
      }
      setErrorMessage(
        responseData?.message ||
          'No fue posible completar el registro. Verifica tus datos o intenta más tarde.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#F5F6FB] lg:grid lg:grid-cols-2">
      <AuthHeroPanel headline="Tu auto, siempre impecable." />

      <div className="flex min-h-screen flex-col items-center justify-center bg-[#F5F6FB] px-6 py-10">
        <div className="mb-8 lg:hidden">
          <AutoCleanLogo size="lg" tone="light" />
        </div>

        <div className="w-full max-w-[400px] rounded-2xl border border-[#E4E6F2] bg-white p-7 shadow-card sm:p-8">
          <div className="mb-6">
            <h1 className="text-[24px] leading-tight font-semibold text-[#1E1B4B]">Crea tu cuenta</h1>
            <p className="mt-1.5 text-[15px] font-normal text-[#5B5F7A]">
              Completa tus datos para comenzar
            </p>
          </div>

          {errorMessage && (
            <div
              role="alert"
              className="mb-5 rounded-xl border border-[#FECACA] bg-[#FEE2E2] px-4 py-3 text-sm font-normal text-[#B91C1C]"
            >
              {errorMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <Input
              id="name"
              label="Nombre completo"
              size="l"
              placeholder="Camila Restrepo"
              value={name}
              onChange={(e) => setName(e.target.value)}
              error={fieldErrors.name}
              required
            />
            <Input
              id="phone"
              label="Teléfono"
              type="tel"
              size="l"
              placeholder="+57 310 458 9210"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              error={fieldErrors.phone}
              required
            />
            <Input
              id="email"
              label="Correo electrónico"
              type="email"
              size="l"
              placeholder="camila@correo.co"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={fieldErrors.email}
              required
            />
            <Input
              id="password"
              label="Contraseña"
              type="password"
              size="l"
              placeholder="Mínimo 6 caracteres"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={fieldErrors.password}
              required
            />

            <Button type="submit" size="l" loading={submitting} className="w-full">
              Crear cuenta
            </Button>
          </form>

          <p className="mt-6 text-center text-[14px] font-normal text-[#5B5F7A]">
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="font-semibold text-[#4F46E5] hover:text-[#4338CA]">
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
