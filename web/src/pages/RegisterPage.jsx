import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/**
 * Pantalla de registro público de clientes contra POST /api/auth/register.
 * Exige nombre y teléfono para asegurar la vinculación automática con la tabla clientes (customerId).
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
    if (!name.trim()) {
      errors.name = 'El nombre completo es obligatorio.'
    }
    if (!phone.trim()) {
      errors.phone = 'El teléfono de contacto es obligatorio.'
    }
    if (!email.trim()) {
      errors.email = 'El correo electrónico es obligatorio.'
    } else if (!EMAIL_PATTERN.test(email.trim())) {
      errors.email = 'El formato del correo es inválido.'
    }
    if (!password) {
      errors.password = 'La contraseña es obligatoria.'
    } else if (password.length < 6) {
      errors.password = 'La contraseña debe tener al menos 6 caracteres.'
    }
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
      if (responseData?.message) {
        setErrorMessage(responseData.message)
      } else {
        setErrorMessage('No fue posible completar el registro. Verifica tus datos o intenta más tarde.')
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
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Crear cuenta</h1>
          <p className="mt-1 text-sm text-slate-500">
            Regístrate en el Portal de Clientes de AutoClean
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

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <Input
            id="name"
            label="Nombre completo"
            type="text"
            autoComplete="name"
            placeholder="Juan Pérez"
            value={name}
            onChange={(e) => setName(e.target.value)}
            error={fieldErrors.name}
            required
          />

          <Input
            id="phone"
            label="Teléfono"
            type="tel"
            autoComplete="tel"
            placeholder="3001234567"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            error={fieldErrors.phone}
            required
          />

          <Input
            id="email"
            label="Correo electrónico"
            type="email"
            autoComplete="email"
            placeholder="cliente@correo.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={fieldErrors.email}
            required
          />

          <Input
            id="password"
            label="Contraseña"
            type="password"
            autoComplete="new-password"
            placeholder="Mínimo 6 caracteres"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={fieldErrors.password}
            required
          />

          <Button type="submit" loading={submitting} className="w-full">
            Registrarme
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          ¿Ya tienes una cuenta?{' '}
          <Link to="/login" className="font-semibold text-sky-600 hover:text-sky-700">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}

