import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from './Button'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Panel principal' },
  { to: '/customers', label: 'Clientes' },
  { to: '/vehicles', label: 'Vehículos' },
  { to: '/appointments', label: 'Agenda de Citas' },
  { to: '/catalog', label: 'Catálogo' },
  { to: '/employees', label: 'Empleados' },
  { to: '/supplies', label: 'Insumos' },
  { to: '/reports', label: 'Reportes' },
]

/**
 * Plantilla principal con barra lateral de navegación para vistas autenticadas de AutoClean.
 */
export function MainLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6">
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

      <div className="mx-auto flex w-full max-w-7xl flex-1 flex-col gap-6 px-4 py-6 sm:px-6 lg:flex-row">
        <aside aria-label="Navegación principal" className="w-full shrink-0 lg:w-60">
          <nav className="flex flex-wrap gap-1.5 rounded-2xl border border-slate-200 bg-white p-2 shadow-xs lg:flex-col">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `rounded-xl px-3.5 py-2.5 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-sky-600 text-white shadow-xs'
                      : 'text-slate-700 hover:bg-slate-100 hover:text-slate-900'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </aside>

        <main className="min-w-0 flex-1">{children}</main>
      </div>
    </div>
  )
}
