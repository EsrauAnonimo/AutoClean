import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getHomeRouteByRole } from '../utils/authRedirect'
import { Button } from './Button'

const ADMIN_NAV_ITEMS = [
  { to: '/dashboard', label: 'Panel principal', icon: 'grid' },
  { to: '/customers', label: 'Clientes', icon: 'users' },
  { to: '/vehicles', label: 'Vehículos', icon: 'car' },
  { to: '/appointments', label: 'Agenda de Citas', icon: 'calendar' },
  { to: '/catalog', label: 'Catálogo', icon: 'sparkles' },
  { to: '/employees', label: 'Empleados', icon: 'badge' },
  { to: '/supplies', label: 'Insumos', icon: 'box' },
  { to: '/reports', label: 'Reportes', icon: 'chart' },
]

const PORTAL_NAV_ITEMS = [
  { to: '/portal/dashboard', label: 'Inicio', icon: 'home' },
  { to: '/portal/mis-vehiculos', label: 'Mis Vehículos', icon: 'car' },
  { to: '/portal/agendar', label: 'Agendar Cita', icon: 'calendar' },
  { to: '/portal/historial', label: 'Historial', icon: 'clock' },
]

function NavIcon({ name }) {
  const paths = {
    home: 'M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5.5v-6h-5v6H4a1 1 0 0 1-1-1v-9.5Z',
    grid: 'M4 4h7v7H4V4Zm9 0h7v7h-7V4ZM4 13h7v7H4v-7Zm9 0h7v7h-7v-7Z',
    users: 'M16 11a4 4 0 1 0-8 0 4 4 0 0 0 8 0Zm-12 9a8 8 0 1 1 16 0',
    car: 'M5 11l1.5-4.5A2 2 0 0 1 8.4 5h7.2a2 2 0 0 1 1.9 1.5L19 11m-14 0h14v6a1 1 0 0 1-1 1h-1a1 1 0 0 1-1-1v-1H8v1a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1v-6Zm2.5 3h.01M16.5 14h.01',
    calendar: 'M8 2v3m8-3v3M3 9h18M5 5h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z',
    clock: 'M12 6v6l4 2m6-2a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z',
    sparkles: 'M12 3l1.8 4.2L18 9l-4.2 1.8L12 15l-1.8-4.2L6 9l4.2-1.8L12 3Zm6 11 1 2.2 2.2 1-2.2 1-1 2.2-1-2.2-2.2-1 2.2-1 1-2.2Z',
    badge: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8a7 7 0 0 1 14 0',
    box: 'M20 7 12 3 4 7m16 0v10l-8 4m8-14-8 4m0 10-8-4V7m8 4v10',
    chart: 'M4 20h16M7 16v-5m5 5V7m5 9v-8',
  }

  return (
    <svg
      className="h-4 w-4 shrink-0"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={paths[name] || paths.grid} />
    </svg>
  )
}

/**
 * Plantilla principal con barra lateral de navegación para vistas autenticadas
 * del panel administrativo y del Portal de Clientes de AutoClean.
 */
export function MainLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const isClient = user?.role === 'CLIENTE'
  const homeRoute = getHomeRouteByRole(user?.role)
  const navItems = isClient ? PORTAL_NAV_ITEMS : ADMIN_NAV_ITEMS

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-3 px-4 py-4 sm:px-6">
          <Link to={homeRoute} className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-sky-600 text-sm font-bold text-white shadow-xs">
              AC
            </div>
            <div>
              <h1 className="text-base font-bold text-slate-900">AutoClean</h1>
              <p className="text-xs text-slate-500">
                {isClient ? 'Portal de Clientes' : 'Gestión de Lavadero y Detailing'}
              </p>
            </div>
          </Link>

          <div className="flex items-center gap-3">
            {isClient && user?.customerId && (
              <Link
                to="/portal/agendar"
                className="hidden rounded-lg bg-sky-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs transition-colors hover:bg-sky-700 sm:inline-flex"
              >
                + Agendar cita
              </Link>
            )}
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

      <div className="mx-auto flex w-full max-w-7xl flex-1 flex-col gap-6 px-4 py-6 sm:px-6 lg:flex-row">
        <aside aria-label="Navegación principal" className="w-full shrink-0 lg:w-64">
          <nav className="grid grid-cols-2 gap-1.5 rounded-2xl border border-slate-200 bg-white p-2.5 shadow-xs sm:flex sm:flex-wrap lg:flex-col">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-2.5 rounded-xl px-3.5 py-2.5 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-sky-600 text-white shadow-xs'
                      : 'text-slate-700 hover:bg-slate-100 hover:text-slate-900'
                  }`
                }
              >
                <NavIcon name={item.icon} />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </aside>

        <main className="min-w-0 flex-1">{children}</main>
      </div>
    </div>
  )
}
