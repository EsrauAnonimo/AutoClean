import { Link } from 'react-router-dom'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'

const MODULES = [
  { name: 'Clientes', status: 'CRUD activo (/api/customers)', to: '/customers' },
  { name: 'Vehículos', status: 'CRUD activo (/api/vehicles)', to: '/vehicles' },
  { name: 'Catálogo de Servicios', status: 'CRUD activo (/api/service-types)', to: '/catalog' },
  { name: 'Empleados', status: 'CRUD activo (/api/employees)', to: '/employees' },
  { name: 'Agenda de Citas', status: 'Agenda y estados (/api/appointments)', to: '/appointments' },
  { name: 'Insumos', status: 'Inventario CRUD (/api/supplies)', to: '/supplies' },
  { name: 'Reportes Gerenciales', status: 'Servicios e Ingresos vs Costos (/api/reports)', to: '/reports' },
  { name: 'Motor de Costeo', status: 'API lista (/api/costing/estimate)', to: null },
]

/**
 * Vista inicial autenticada tras el login JWT.
 */
export function DashboardPage() {
  const { user, token } = useAuth()

  return (
    <MainLayout>
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-xl font-bold text-slate-900">Panel principal</h2>
        <p className="mt-1 text-sm text-slate-600">
          Sesión activa como <span className="font-semibold text-slate-900">{user?.email || 'usuario'}</span>{' '}
          con rol{' '}
          <span className="rounded-md bg-sky-50 px-2 py-0.5 text-xs font-semibold text-sky-700">
            {user?.role || 'N/A'}
          </span>
          . Token JWT guardado en <code className="rounded bg-slate-100 px-1.5 py-0.5 text-xs">localStorage</code>{' '}
          ({token ? `${token.slice(0, 16)}...` : 'sin token'}).
        </p>
      </section>

      <section className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {MODULES.map((moduleItem) => {
          const cardContent = (
            <>
              <h3 className="text-sm font-semibold text-slate-900">{moduleItem.name}</h3>
              <p className="mt-1 text-xs text-slate-500">{moduleItem.status}</p>
            </>
          )

          return moduleItem.to ? (
            <Link
              key={moduleItem.name}
              to={moduleItem.to}
              className="block rounded-xl border border-slate-200 bg-white p-5 shadow-xs transition hover:border-sky-400 hover:bg-sky-50/30"
            >
              {cardContent}
            </Link>
          ) : (
            <div
              key={moduleItem.name}
              className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs"
            >
              {cardContent}
            </div>
          )
        })}
      </section>
    </MainLayout>
  )
}
