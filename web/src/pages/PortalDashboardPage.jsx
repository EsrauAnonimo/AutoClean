import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { MainLayout } from '../components/MainLayout'
import { VehicleStatusBadge } from '../components/VehicleStatusBadge'
import { useAuth } from '../context/AuthContext'
import { getAppointmentsByCustomer } from '../services/appointmentService'
import { getVehiclesByCustomer } from '../services/vehicleService'
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

function LockIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
      <rect x="4" y="11" width="16" height="10" rx="2.5" />
      <path d="M8 11V7a4 4 0 1 1 8 0v4" />
    </svg>
  )
}

function LockedPortalCard({ title, description }) {
  return (
    <article className="flex flex-col justify-between rounded-2xl border border-dashed border-[#E4E6F2] bg-white/80 p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-[17px] font-semibold text-[#1E1B4B]">{title}</h2>
          <p className="mt-1 text-xs font-normal text-[#5B5F7A]">{description}</p>
        </div>
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[#F5F6FB] text-[#5B5F7A]">
          <LockIcon />
        </span>
      </div>
      <div className="mt-5 inline-flex w-fit items-center gap-1.5 rounded-full bg-[#F5F6FB] px-3 py-1 text-xs font-semibold text-[#5B5F7A]">
        <LockIcon />
        <span>Se activa al completar tu perfil</span>
      </div>
    </article>
  )
}

/**
 * Inicio del Portal del Cliente:
 * - Si el perfil está incompleto: banner compacto con progreso + 3 tarjetas grandes bloqueadas.
 * - Si el perfil está completo: datos reales (Estado en vivo, Próxima cita, Mis vehículos) y botón 52px.
 */
export function PortalDashboardPage() {
  const { user } = useAuth()
  const customerId = user?.customerId
  const [appointments, setAppointments] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(Boolean(customerId))

  useEffect(() => {
    let cancelled = false

    async function load() {
      if (!customerId) {
        setAppointments([])
        setVehicles([])
        setLoading(false)
        return
      }
      setLoading(true)
      try {
        const [appointmentList, vehicleList] = await Promise.all([
          getAppointmentsByCustomer(customerId),
          getVehiclesByCustomer(customerId),
        ])
        if (!cancelled) {
          setAppointments(Array.isArray(appointmentList) ? appointmentList : [])
          setVehicles(Array.isArray(vehicleList) ? vehicleList : [])
        }
      } catch {
        if (!cancelled) {
          setAppointments([])
          setVehicles([])
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
        {!customerId ? (
          <>
            <CompleteCustomerProfileCard />

            <section className="grid gap-4 lg:grid-cols-3">
              <LockedPortalCard
                title="Estado de tu auto en vivo"
                description="Observa en tiempo real cuando tu vehículo pasa por lavado, detailing y entrega."
              />
              <LockedPortalCard
                title="Próxima cita"
                description="Consulta la hora reservada, especialista asignado y confirmación de efectivo."
              />
              <LockedPortalCard
                title="Mis vehículos"
                description="Guarda tus placas una sola vez para agendar en segundos."
              />
            </section>
          </>
        ) : (
          <>
            {/* Única acción primaria de la pantalla: Agendar nueva cita (52px) */}
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h1 className="text-[22px] font-semibold text-[#1E1B4B]">Tu resumen</h1>
                <p className="text-sm font-normal text-[#5B5F7A]">
                  Estado de tus vehículos y próximas reservas
                </p>
              </div>
              <Link
                to="/portal/agendar"
                className="inline-flex h-[52px] w-full items-center justify-center gap-2 rounded-xl bg-[#4F46E5] px-6 text-[15px] font-semibold text-white transition hover:bg-[#4338CA] sm:w-auto"
              >
                Agendar nueva cita
              </Link>
            </div>

            {loading ? (
              <div className="grid gap-4 lg:grid-cols-3">
                {[1, 2, 3].map((n) => (
                  <div
                    key={n}
                    className="h-44 animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-6"
                  />
                ))}
              </div>
            ) : (
              <section className="grid gap-4 lg:grid-cols-3">
                {/* Tarjeta 1: Estado de tu auto en vivo */}
                <article className="flex flex-col justify-between rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
                  <div>
                    <div className="flex items-center justify-between gap-2">
                      <h2 className="text-[17px] font-semibold text-[#1E1B4B]">
                        Estado de tu auto en vivo
                      </h2>
                      {activeLiveAppointment && (
                        <VehicleStatusBadge
                          status={activeLiveAppointment.vehicleStatus ?? 'RECEIVED'}
                        />
                      )}
                    </div>

                    {activeLiveAppointment ? (
                      <>
                        <p className="mt-3 text-sm font-semibold text-[#1E1B4B]">
                          {activeLiveAppointment.vehiclePlate} · {liveMeta.label}
                        </p>
                        <p className="mt-0.5 text-xs font-normal text-[#5B5F7A]">
                          {activeLiveAppointment.serviceName || 'Servicio activo'}
                        </p>

                        <div className="mt-4 grid grid-cols-5 gap-1.5">
                          {VEHICLE_STATUS_ORDER.map((stepStatus, idx) => (
                            <div
                              key={stepStatus}
                              className={`h-2 rounded-full ${
                                idx + 1 <= liveMeta.step ? 'bg-[#8B5CF6]' : 'bg-[#E4E6F2]'
                              }`}
                            />
                          ))}
                        </div>
                      </>
                    ) : (
                      <p className="mt-3 text-sm font-normal text-[#5B5F7A]">
                        Sin servicios en curso en este momento.
                      </p>
                    )}
                  </div>

                  <Link
                    to="/portal/historial"
                    className="mt-5 inline-flex h-[52px] w-full items-center justify-center rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] px-4 text-sm font-semibold text-[#1E1B4B] transition hover:bg-[#E4E6F2]/60"
                  >
                    Ver historial en vivo
                  </Link>
                </article>

                {/* Tarjeta 2: Próxima cita */}
                <article className="flex flex-col justify-between rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
                  <div>
                    <div className="flex items-center justify-between gap-2">
                      <h2 className="text-[17px] font-semibold text-[#1E1B4B]">Próxima cita</h2>
                      {nextAppointment && <AppointmentStatusBadge status={nextAppointment.status} />}
                    </div>

                    {nextAppointment ? (
                      <div className="mt-3 space-y-1">
                        <p className="text-sm font-semibold text-[#1E1B4B]">
                          {nextAppointment.serviceName}
                        </p>
                        <p className="text-xs font-normal text-[#5B5F7A]">
                          {formatShortDateTime(nextAppointment.dateTime)}
                        </p>
                        <p className="text-xs font-normal text-[#5B5F7A]">
                          Placa {nextAppointment.vehiclePlate} · {nextAppointment.employeeName}
                        </p>
                      </div>
                    ) : (
                      <p className="mt-3 text-sm font-normal text-[#5B5F7A]">
                        No tienes citas próximas programadas.
                      </p>
                    )}
                  </div>

                  <Link
                    to="/portal/historial"
                    className="mt-5 inline-flex h-[52px] w-full items-center justify-center rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] px-4 text-sm font-semibold text-[#1E1B4B] transition hover:bg-[#E4E6F2]/60"
                  >
                    Gestionar citas
                  </Link>
                </article>

                {/* Tarjeta 3: Mis vehículos */}
                <article className="flex flex-col justify-between rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card">
                  <div>
                    <div className="flex items-center justify-between gap-2">
                      <h2 className="text-[17px] font-semibold text-[#1E1B4B]">Mis vehículos</h2>
                      <span className="rounded-full bg-[#EDE9FE] px-2.5 py-0.5 text-xs font-semibold tabular-nums text-[#6D28D9]">
                        {vehicles.length}
                      </span>
                    </div>

                    {vehicles.length > 0 ? (
                      <div className="mt-3 space-y-2">
                        {vehicles.slice(0, 2).map((vehicle) => (
                          <div
                            key={vehicle.id}
                            className="flex items-center justify-between rounded-xl bg-[#F5F6FB] px-3 py-2 text-xs"
                          >
                            <span className="font-semibold text-[#1E1B4B]">
                              {vehicle.brand} {vehicle.model}
                            </span>
                            <span className="rounded bg-[#FACC15] px-2 py-0.5 font-mono font-semibold text-[#1E1B4B]">
                              {vehicle.plate}
                            </span>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="mt-3 text-sm font-normal text-[#5B5F7A]">
                        Aún no has guardado vehículos en tu cuenta.
                      </p>
                    )}
                  </div>

                  <Link
                    to="/portal/mis-vehiculos"
                    className="mt-5 inline-flex h-[52px] w-full items-center justify-center rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] px-4 text-sm font-semibold text-[#1E1B4B] transition hover:bg-[#E4E6F2]/60"
                  >
                    Administrar vehículos
                  </Link>
                </article>
              </section>
            )}
          </>
        )}
      </div>
    </MainLayout>
  )
}
