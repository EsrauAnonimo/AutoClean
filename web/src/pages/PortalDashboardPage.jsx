import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { MainLayout } from '../components/MainLayout'
import { VehicleStatusBadge } from '../components/VehicleStatusBadge'
import { useAuth } from '../context/AuthContext'
import { getAppointmentsByCustomer } from '../services/appointmentService'
import { VEHICLE_STATUS_ORDER, getVehicleStatusMeta } from '../utils/vehicleStatus'

function formatShortDateTime(isoValue) {
  if (!isoValue) {
    return 'Por definir'
  }
  const date = new Date(isoValue)
  if (Number.isNaN(date.getTime())) {
    return isoValue
  }
  return new Intl.DateTimeFormat('es-CO', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  }).format(date)
}

function getGreetingName(email) {
  if (!email) {
    return 'conductor'
  }
  const prefix = email.split('@')[0] || 'conductor'
  return prefix.charAt(0).toUpperCase() + prefix.slice(1)
}

/**
 * Pantalla de inicio del Portal del Cliente (mobile-first).
 * Destaca el progreso en vivo del vehículo, acceso rápido a agendar y próxima cita.
 */
export function PortalDashboardPage() {
  const { user } = useAuth()
  const customerId = user?.customerId
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(Boolean(customerId))

  useEffect(() => {
    let cancelled = false

    async function load() {
      if (!customerId) {
        setAppointments([])
        setLoading(false)
        return
      }
      setLoading(true)
      try {
        const data = await getAppointmentsByCustomer(customerId)
        if (!cancelled) {
          setAppointments(Array.isArray(data) ? data : [])
        }
      } catch {
        if (!cancelled) {
          setAppointments([])
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [customerId])

  const activeLiveAppointment =
    appointments.find((item) => item.status === 'IN_PROGRESS') ||
    appointments.find(
      (item) => item.status === 'FINISHED' && (item.vehicleStatus ?? 'RECEIVED') === 'READY',
    ) ||
    appointments.find((item) => item.status === 'PENDING') ||
    null

  const upcomingAppointments = appointments
    .filter((item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS')
    .sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime))

  const nextAppointment = upcomingAppointments[0] || null
  const liveMeta = getVehicleStatusMeta(activeLiveAppointment?.vehicleStatus ?? 'RECEIVED')

  return (
    <MainLayout>
      <div className="space-y-5">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <p className="text-[13px] font-medium text-[#5B5F7A]">Tu resumen en AutoClean</p>
            <h1 className="text-[26px] font-bold tracking-tight text-[#1E1B4B]">
              Hola, {getGreetingName(user?.email)}
            </h1>
          </div>
          {customerId && (
            <Link
              to="/portal/agendar"
              className="hidden h-11 items-center gap-2 rounded-xl bg-[#4F46E5] px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4338CA] lg:inline-flex"
            >
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Agendar nueva cita
            </Link>
          )}
        </div>

        {!customerId && <CompleteCustomerProfileCard />}

        {customerId && (
          <>
            {loading ? (
              <div className="animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-6">
                <div className="h-4 w-32 rounded bg-[#E4E6F2]" />
                <div className="mt-3 h-7 w-56 rounded bg-[#E4E6F2]" />
                <div className="mt-5 grid grid-cols-5 gap-2">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <div key={n} className="h-2 rounded-full bg-[#E4E6F2]" />
                  ))}
                </div>
              </div>
            ) : activeLiveAppointment ? (
              <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
                <div className="bg-linear-to-r from-[#1E1B4B] via-[#312E81] to-[#4F46E5] p-5 text-white sm:p-6">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold text-white backdrop-blur-xs">
                      <span className="h-2 w-2 animate-pulse rounded-full bg-emerald-300" />
                      Seguimiento en vivo
                    </span>
                    <span className="rounded-lg border border-amber-300/60 bg-amber-300 px-2.5 py-0.5 font-mono text-xs font-extrabold tracking-widest text-[#1E1B4B]">
                      {activeLiveAppointment.vehiclePlate || 'VEHÍCULO'}
                    </span>
                  </div>

                  <p className="mt-4 text-xs font-medium uppercase tracking-wider text-indigo-200">
                    Tu auto está en
                  </p>
                  <div className="mt-1 flex flex-wrap items-center justify-between gap-3">
                    <h2 className="text-2xl font-bold text-white">{liveMeta.label}</h2>
                    <VehicleStatusBadge status={activeLiveAppointment.vehicleStatus ?? 'RECEIVED'} />
                  </div>
                  <p className="mt-1.5 text-sm text-indigo-100">
                    {activeLiveAppointment.serviceName || 'Servicio de detailing'} ·{' '}
                    {activeLiveAppointment.vehicleDescription || 'Vehículo registrado'}
                  </p>

                  <div className="mt-5">
                    <div className="grid grid-cols-5 gap-1.5">
                      {VEHICLE_STATUS_ORDER.map((stepStatus, idx) => {
                        const completed = idx + 1 <= liveMeta.step
                        return (
                          <div key={stepStatus} className="space-y-1">
                            <div
                              className={[
                                'h-2 rounded-full transition-all',
                                completed ? 'bg-emerald-300' : 'bg-white/20',
                              ].join(' ')}
                            />
                            <p className="truncate text-[10px] font-medium text-indigo-100/90">
                              {getVehicleStatusMeta(stepStatus).label}
                            </p>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-between gap-3 bg-white px-5 py-3.5 sm:px-6">
                  <span className="text-xs text-[#5B5F7A]">
                    Especialista:{' '}
                    <strong className="font-semibold text-[#1E1B4B]">
                      {activeLiveAppointment.employeeName || 'Por asignar'}
                    </strong>
                  </span>
                  <Link
                    to="/portal/historial"
                    className="text-xs font-semibold text-[#4F46E5] hover:text-[#4338CA]"
                  >
                    Ver timeline completo →
                  </Link>
                </div>
              </section>
            ) : (
              <section className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card">
                <span className="inline-flex items-center rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-[#4F46E5]">
                  Sin servicios en curso
                </span>
                <h2 className="mt-3 text-lg font-semibold text-[#1E1B4B]">
                  ¿Listo para consentir tu vehículo?
                </h2>
                <p className="mt-1 text-sm text-[#5B5F7A]">
                  Agenda en segundos ingresando la placa de tu auto y sigue cada etapa en tiempo real.
                </p>
              </section>
            )}

            <Link
              to="/portal/agendar"
              className="flex h-[52px] w-full items-center justify-center gap-2 rounded-xl bg-[#4F46E5] px-6 text-base font-semibold text-white shadow-sm transition hover:bg-[#4338CA] lg:hidden"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Agendar nueva cita
            </Link>

            <section className="grid gap-4 lg:grid-cols-3">
              <div className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card lg:col-span-2">
                <div className="flex items-center justify-between gap-2">
                  <h3 className="text-[18px] font-semibold text-[#1E1B4B]">Próxima cita</h3>
                  <Link
                    to="/portal/historial"
                    className="text-xs font-semibold text-[#4F46E5] hover:text-[#4338CA]"
                  >
                    Historial
                  </Link>
                </div>

                {nextAppointment ? (
                  <div className="mt-4 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 p-4">
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="rounded-md bg-amber-300 px-2 py-0.5 font-mono text-xs font-bold tracking-wider text-[#1E1B4B]">
                          {nextAppointment.vehiclePlate}
                        </span>
                        <AppointmentStatusBadge status={nextAppointment.status} />
                      </div>
                      <p className="text-sm font-semibold text-[#1E1B4B]">
                        {nextAppointment.serviceName}
                      </p>
                      <p className="text-xs text-[#5B5F7A]">
                        {formatShortDateTime(nextAppointment.dateTime)} · {nextAppointment.employeeName}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-[#5B5F7A]">Pago en efectivo al finalizar</p>
                      <Link
                        to="/portal/historial"
                        className="mt-2 inline-flex rounded-lg border border-[#E4E6F2] bg-white px-3 py-1.5 text-xs font-semibold text-[#1E1B4B] hover:bg-[#F5F6FB]"
                      >
                        Gestionar cita
                      </Link>
                    </div>
                  </div>
                ) : (
                  <div className="mt-4 rounded-xl border border-dashed border-[#E4E6F2] p-6 text-center">
                    <p className="text-sm font-medium text-[#1E1B4B]">No tienes citas programadas</p>
                    <p className="mt-1 text-xs text-[#5B5F7A]">
                      Atendemos de 7:00 a. m. a 12:00 p. m. y de 2:30 p. m. a 9:00 p. m.
                    </p>
                  </div>
                )}
              </div>

              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
                <Link
                  to="/portal/mis-vehiculos"
                  className="flex items-center justify-between rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card transition hover:border-[#4F46E5]/40"
                >
                  <div>
                    <p className="text-sm font-semibold text-[#1E1B4B]">Mis Vehículos</p>
                    <p className="mt-0.5 text-xs text-[#5B5F7A]">Administra tus placas y autos</p>
                  </div>
                  <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 text-[#4F46E5]">
                    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9">
                      <path d="M5 17h14v-5l-2-5H7l-2 5v5z" />
                      <circle cx="7.5" cy="17" r="1.5" />
                      <circle cx="16.5" cy="17" r="1.5" />
                    </svg>
                  </span>
                </Link>

                <Link
                  to="/portal/historial"
                  className="flex items-center justify-between rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card transition hover:border-[#8B5CF6]/40"
                >
                  <div>
                    <p className="text-sm font-semibold text-[#1E1B4B]">Historial y Estado</p>
                    <p className="mt-0.5 text-xs text-[#5B5F7A]">Sigue el lavado en vivo</p>
                  </div>
                  <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-50 text-[#6D28D9]">
                    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9">
                      <circle cx="12" cy="12" r="9" />
                      <path d="M12 7v5l3 2" />
                    </svg>
                  </span>
                </Link>
              </div>
            </section>
          </>
        )}
      </div>
    </MainLayout>
  )
}
