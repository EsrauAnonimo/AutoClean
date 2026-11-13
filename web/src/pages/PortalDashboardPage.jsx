import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'
import { getAppointments } from '../services/appointmentService'
import { getVehiclesByCustomer } from '../services/vehicleService'

const PORTAL_SHORTCUTS = [
  {
    to: '/portal/mis-vehiculos',
    title: 'Mis Vehículos',
    description: 'Registra, actualiza o elimina los vehículos asociados a tu cuenta.',
    ctaLabel: 'Administrar vehículos',
    accentClass: 'bg-sky-50 text-sky-700',
    buttonClass:
      'bg-white text-slate-800 ring-1 ring-inset ring-slate-300 hover:bg-slate-50',
  },
  {
    to: '/portal/agendar',
    title: 'Agendar Cita',
    description: 'Elige un servicio de detailing, tu vehículo, especialista y horario.',
    ctaLabel: 'Agendar nueva cita',
    accentClass: 'bg-sky-600 text-white',
    buttonClass: 'bg-sky-600 text-white hover:bg-sky-700',
  },
  {
    to: '/portal/historial',
    title: 'Historial de Citas',
    description: 'Consulta el estado de tus turnos o cancela citas activas.',
    ctaLabel: 'Consultar historial',
    accentClass: 'bg-emerald-50 text-emerald-700',
    buttonClass:
      'bg-white text-slate-800 ring-1 ring-inset ring-slate-300 hover:bg-slate-50',
  },
]

/**
 * Vista de inicio del Portal de Clientes con accesos directos y activación de perfil.
 */
export function PortalDashboardPage() {
  const { user } = useAuth()
  const customerId = user?.customerId ?? null

  const [vehicles, setVehicles] = useState([])
  const [appointments, setAppointments] = useState([])

  useEffect(() => {
    if (!customerId) return
    let active = true

    Promise.all([getVehiclesByCustomer(customerId), getAppointments({ customerId })])
      .then(([vehicleList, appointmentList]) => {
        if (!active) return
        setVehicles(Array.isArray(vehicleList) ? vehicleList : [])
        setAppointments(Array.isArray(appointmentList) ? appointmentList : [])
      })
      .catch(() => {
        // El resumen numérico es informativo; los módulos muestran sus errores propios
      })

    return () => {
      active = false
    }
  }, [customerId])

  const stats = useMemo(() => {
    const activeAppointments = appointments.filter(
      (item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS',
    ).length
    const finishedAppointments = appointments.filter((item) => item.status === 'FINISHED').length
    return {
      vehiclesCount: vehicles.length,
      activeAppointments,
      finishedAppointments,
    }
  }, [vehicles, appointments])

  return (
    <MainLayout>
      <div className="space-y-6">
        <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h2 className="text-xl font-bold text-slate-900">Bienvenido a tu Portal de Clientes</h2>
            <p className="mt-1 text-sm text-slate-600">
              Sesión iniciada como{' '}
              <span className="font-semibold text-slate-900">{user?.email || 'cliente'}</span>.
              {customerId
                ? ' Tu perfil está activo para gestionar vehículos y reservas.'
                : ' Completa tus datos una sola vez para activar reservas y vehículos.'}
            </p>
          </div>

          {customerId && (
            <div className="flex flex-wrap items-center gap-2.5">
              <Link
                to="/portal/mis-vehiculos"
                className="inline-flex items-center justify-center rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 transition-colors hover:bg-slate-50"
              >
                Mis Vehículos
              </Link>
              <Link
                to="/portal/agendar"
                className="inline-flex items-center justify-center rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
              >
                + Agendar Cita
              </Link>
            </div>
          )}
        </section>

        {!customerId ? (
          <CompleteCustomerProfileCard />
        ) : (
          <section className="grid gap-4 sm:grid-cols-3">
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold tracking-wider text-slate-500 uppercase">
                Vehículos registrados
              </span>
              <p className="mt-2 text-2xl font-bold text-slate-900">{stats.vehiclesCount}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold tracking-wider text-slate-500 uppercase">
                Citas activas
              </span>
              <p className="mt-2 text-2xl font-bold text-sky-700">{stats.activeAppointments}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold tracking-wider text-slate-500 uppercase">
                Servicios completados
              </span>
              <p className="mt-2 text-2xl font-bold text-emerald-700">
                {stats.finishedAppointments}
              </p>
            </div>
          </section>
        )}

        <section className="grid gap-4 sm:grid-cols-3">
          {PORTAL_SHORTCUTS.map((item) => (
            <article
              key={item.to}
              className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-xs transition hover:border-sky-300 hover:shadow-sm"
            >
              <div>
                <h3 className="text-base font-bold text-slate-900">{item.title}</h3>
                <p className="mt-2 text-sm text-slate-500">{item.description}</p>
              </div>

              <Link
                to={item.to}
                className={`mt-6 inline-flex w-full items-center justify-center rounded-lg px-4 py-2.5 text-sm font-semibold shadow-xs transition-colors ${item.buttonClass}`}
              >
                {item.ctaLabel}
              </Link>
            </article>
          ))}
        </section>
      </div>
    </MainLayout>
  )
}
