import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'

const MODULES = [
  { name: 'Clientes y Vehículos', status: 'API lista (/api/customers, /api/vehicles)' },
  { name: 'Catálogo y Empleados', status: 'API lista (/api/service-types, /api/employees)' },
  { name: 'Agenda de Citas', status: 'API lista (/api/appointments)' },
  { name: 'Insumos', status: 'API lista (/api/supplies)' },
  { name: 'Motor de Costeo', status: 'API lista (/api/costing/estimate)' },
  { name: 'Reportes Gerenciales', status: 'API lista (/api/reports/services, /api/reports/revenue)' },
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
        {MODULES.map((moduleItem) => (
          <div
            key={moduleItem.name}
            className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs transition hover:border-sky-300"
          >
            <h3 className="text-sm font-semibold text-slate-900">{moduleItem.name}</h3>
            <p className="mt-1 text-xs text-slate-500">{moduleItem.status}</p>
          </div>
        ))}
      </section>
    </MainLayout>
  )
}
