import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from './Button'

/**
 * Plantilla principal para vistas autenticadas de AutoClean.
 */
export function MainLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-sky-600 text-sm font-bold text-white">
              AC
            </div>
            <div>
              <h1 className="text-base font-bold text-slate-900">AutoClean</h1>
              <p className="text-xs text-slate-500">Gestión de Lavadero y Detailing</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            {user && (
              <div className="hidden text-right sm:block">
                <p className="text-sm font-medium text-slate-800">{user.email}</p>
                <span className="inline-block rounded-full bg-sky-50 px-2 py-0.5 text-xs font-semibold text-sky-700">
                  {user.role}
                </span>
              </div>
            )}
            <Button variant="secondary" onClick={handleLogout}>
              Cerrar sesión
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  )
}
