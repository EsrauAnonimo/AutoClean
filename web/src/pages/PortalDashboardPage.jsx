import { Link } from 'react-router-dom'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'

const PORTAL_SHORTCUTS = [
  {
    to: '/portal/mis-vehiculos',
    title: 'Mis Vehículos',
    description: 'Registra, actualiza o elimina los vehículos asociados a tu cuenta.',
  },
  {
    to: '/portal/agendar',
    title: 'Agendar Cita',
    description: 'Elige un servicio de detailing, tu vehículo, especialista y horario.',
  },
  {
    to: '/portal/historial',
    title: 'Historial de Citas',
    description: 'Consulta el estado de tus turnos o cancela citas activas.',
  },
]

/**
 * Vista de inicio del Portal de Clientes tras autenticarse con rol CLIENTE.
 */
export function PortalDashboardPage() {
  const { user } = useAuth()

  return (
    <MainLayout>
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-xl font-bold text-slate-900">Bienvenido a tu Portal de Clientes</h2>
        <p className="mt-1 text-sm text-slate-600">
          Sesión iniciada como{' '}
          <span className="font-semibold text-slate-900">{user?.email || 'cliente'}</span>.
          {user?.customerId
            ? ' Tu perfil de cliente está vinculado y listo para gestionar vehículos y citas.'
            : ' Tu cuenta aún no tiene un perfil de cliente vinculado.'}
        </p>
      </section>

      <section className="mt-6 grid gap-4 sm:grid-cols-3">
        {PORTAL_SHORTCUTS.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className="group rounded-xl border border-slate-200 bg-white p-5 shadow-xs transition hover:border-sky-400 hover:shadow-sm"
          >
            <h3 className="text-base font-semibold text-slate-900 group-hover:text-sky-700">
              {item.title}
            </h3>
            <p className="mt-1.5 text-sm text-slate-500">{item.description}</p>
            <span className="mt-4 inline-block text-xs font-semibold text-sky-600">
              Ir al módulo →
            </span>
          </Link>
        ))}
      </section>
    </MainLayout>
  )
}

