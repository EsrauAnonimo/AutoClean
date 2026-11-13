import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'

/**
 * Callback del frontend que recibe ?token= tras el login con Google,
 * persiste la sesión JWT y redirige según el rol del usuario.
 */
export function OAuth2RedirectPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { loginWithToken } = useAuth()

  const token = searchParams.get('token')
  const oauthError = searchParams.get('error')
  const [tokenError, setTokenError] = useState('')

  useEffect(() => {
    if (!token) return
    try {
      const session = loginWithToken(token)
      navigate(getHomeRouteByRole(session.role), { replace: true })
    } catch (error) {
      queueMicrotask(() => {
        setTokenError(error?.message || 'El token de acceso recibido no es válido.')
      })
    }
  }, [token, loginWithToken, navigate])

  const displayError =
    tokenError ||
    (oauthError
      ? 'No se pudo completar el inicio de sesión con Google.'
      : !token
        ? 'No se recibió el token de autenticación desde Google.'
        : '')

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-sky-900 px-4 py-12">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-xl ring-1 ring-slate-200">
        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-sky-600 text-lg font-bold text-white shadow-sm">
          AC
        </div>

        {displayError ? (
          <>
            <h1 className="text-xl font-bold text-slate-900">Error al iniciar sesión</h1>
            <p className="mt-2 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {displayError}
            </p>
            <Link
              to="/login"
              className="mt-6 inline-flex w-full items-center justify-center rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
            >
              Volver al inicio de sesión
            </Link>
          </>
        ) : (
          <>
            <h1 className="text-xl font-bold text-slate-900">Conectando con Google...</h1>
            <p className="mt-2 text-sm text-slate-500">
              Validando tu sesión y redirigiendo a AutoClean.
            </p>
          </>
        )}
      </div>
    </div>
  )
}
