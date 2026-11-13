import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'
import { Button } from './Button'

const PORTAL_NAV_ITEMS = [
  { to: '/portal/dashboard', label: 'Inicio' },
  { to: '/portal/mis-vehiculos', label: 'Mis Vehículos' },
  { to: '/portal/agendar', label: 'Agendar Cita' },
  { to: '/portal/historial', label: 'Historial' },
]

/**
 * Plantilla principal para vistas autenticadas de AutoClean (panel operativo y portal de clientes).
 */
export function MainLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const homeRoute = getHomeRouteByRole(user?.role)
  const isClient = user?.role === 'CLIENTE'

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <Link to={homeRoute} className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-sky-600 text-sm font-bold text-white">
              AC
            </div>
            <div>
              <h1 className="text-base font-bold text-slate-900">AutoClean</h1>
              <p className="text-xs text-slate-500">
                {isClient ? 'Portal de Clientes' : 'Gestión de Lavadero y Detailing'}
              </p>
            </div>
          </Link>

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

        {isClient && (
          <nav aria-label="Navegación del portal de clientes" className="border-t border-slate-100 bg-slate-50/70">
            <div className="mx-auto flex max-w-6xl gap-2 overflow-x-auto px-4 py-2 sm:px-6">
              {PORTAL_NAV_ITEMS.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `whitespace-nowrap rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-sky-600 text-white shadow-xs'
                        : 'text-slate-600 hover:bg-slate-200/70 hover:text-slate-900'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </div>
          </nav>
        )}
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  )
}
