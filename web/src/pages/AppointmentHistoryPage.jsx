import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'
import { Button } from '../components/Button'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { MainLayout } from '../components/MainLayout'
import { VehicleStatusBadge } from '../components/VehicleStatusBadge'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import {
  cancelAppointment,
  getAppointmentById,
  getAppointmentsByCustomer,
} from '../services/appointmentService'
import { showToast } from '../utils/toast'
import { VEHICLE_STATUS_ORDER, getVehicleStatusMeta } from '../utils/vehicleStatus'

const POLLING_INTERVAL_MS = 10000

const STEP_DESCRIPTIONS = {
  RECEIVED: 'Vehículo ingresado y verificado en recepción.',
  WASHING: 'Lavado técnico exterior e interior en curso.',
  DETAILING: 'Acabado fino, brillo y protección en proceso.',
  READY: 'Tu auto está listo para entrega. Acércate cuando gustes.',
  DELIVERED: 'Servicio completado y vehículo entregado.',
}

function formatDateTime(isoValue) {
  if (!isoValue) {
    return '—'
  }
  const date = new Date(isoValue)
  if (Number.isNaN(date.getTime())) {
    return isoValue
  }
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function formatTimeOnly(date) {
  return new Intl.DateTimeFormat('es-CO', {
    hour: 'numeric',
    minute: '2-digit',
    second: '2-digit',
    hour12: true,
  }).format(date)
}

function shouldPollAppointment(appointment) {
  if (appointment.status === 'CANCELLED') {
    return false
  }
  if (appointment.status === 'FINISHED' && appointment.vehicleStatus === 'DELIVERED') {
    return false
  }
  return true
}

/**
 * Portal del Cliente: Historial de citas y seguimiento en vivo (polling cada 10s).
 * Incluye timeline vertical de 5 estados y notificación cuando el auto pasa a READY.
 */
export function AppointmentHistoryPage() {
  const { user } = useAuth()
  const customerId = user?.customerId

  const [appointments, setAppointments] = useState([])
  const [filterTab, setFilterTab] = useState('ALL')
  const [loading, setLoading] = useState(Boolean(customerId))
  const [errorMessage, setErrorMessage] = useState('')
  const [busyCancelId, setBusyCancelId] = useState(null)
  const [lastUpdated, setLastUpdated] = useState(null)

  const appointmentsRef = useRef([])
  useEffect(() => {
    appointmentsRef.current = appointments
  }, [appointments])

  const knownVehicleStatusRef = useRef(new Map())

  const syncStatusMemory = useCallback((list) => {
    const map = new Map()
    for (const item of list) {
      map.set(item.id, item.vehicleStatus ?? 'RECEIVED')
    }
    knownVehicleStatusRef.current = map
  }, [])

  const loadAppointments = useCallback(async () => {
    if (!customerId) {
      setAppointments([])
      setLoading(false)
      return
    }
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getAppointmentsByCustomer(customerId)
      const sorted = (Array.isArray(data) ? data : []).sort(
        (a, b) => new Date(b.dateTime) - new Date(a.dateTime),
      )
      setAppointments(sorted)
      syncStatusMemory(sorted)
      setLastUpdated(new Date())
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar tu historial de citas.'))
    } finally {
      setLoading(false)
    }
  }, [customerId, syncStatusMemory])

  useEffect(() => {
    loadAppointments()
  }, [loadAppointments])

  useEffect(() => {
    if (!customerId) {
      return undefined
    }

    const intervalId = window.setInterval(async () => {
      const currentList = appointmentsRef.current
      const targets = currentList.filter(shouldPollAppointment)
      if (targets.length === 0) {
        return
      }

      try {
        const refreshedList = await Promise.all(
          targets.map((appointment) => getAppointmentById(appointment.id)),
        )
        const refreshedById = new Map(refreshedList.map((item) => [item.id, item]))

        for (const refreshed of refreshedList) {
          const prevStatus = knownVehicleStatusRef.current.get(refreshed.id)
          const nextStatus = refreshed.vehicleStatus ?? 'RECEIVED'
          if (prevStatus && prevStatus !== 'READY' && nextStatus === 'READY') {
            showToast({
              type: 'success',
              title: 'Tu auto está listo para entrega',
              description: `${refreshed.vehiclePlate || 'Tu vehículo'} terminó ${refreshed.serviceName || 'su servicio'}.`,
            })
          }
          knownVehicleStatusRef.current.set(refreshed.id, nextStatus)
        }

        setAppointments((prev) =>
          prev.map((item) => refreshedById.get(item.id) ?? item),
        )
        setLastUpdated(new Date())
      } catch {
        // Silencioso en polling para no interrumpir la vista ante cortes momentáneos
      }
    }, POLLING_INTERVAL_MS)

    return () => window.clearInterval(intervalId)
  }, [customerId])

  const handleCancelAppointment = async (appointment) => {
    setBusyCancelId(appointment.id)
    setErrorMessage('')
    try {
      const updated = await cancelAppointment(appointment.id)
      setAppointments((prev) =>
        prev.map((item) => (item.id === appointment.id ? updated : item)),
      )
      showToast({
        type: 'info',
        title: 'Cita cancelada',
        description: `Se canceló la cita para la placa ${appointment.vehiclePlate || ''}.`,
      })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cancelar la cita.'))
    } finally {
      setBusyCancelId(null)
    }
  }

  const highlightedAppointment = useMemo(
    () =>
      appointments.find((item) => item.status === 'IN_PROGRESS') ||
      appointments.find(
        (item) => item.status === 'FINISHED' && (item.vehicleStatus ?? 'RECEIVED') === 'READY',
      ) ||
      appointments.find((item) => item.status === 'PENDING') ||
      null,
    [appointments],
  )

  const filteredAppointments = useMemo(() => {
    if (filterTab === 'ACTIVE') {
      return appointments.filter((item) => item.status === 'PENDING' || item.status === 'IN_PROGRESS')
    }
    if (filterTab === 'FINISHED') {
      return appointments.filter((item) => item.status === 'FINISHED')
    }
    if (filterTab === 'CANCELLED') {
      return appointments.filter((item) => item.status === 'CANCELLED')
    }
    return appointments
  }, [appointments, filterTab])

  const liveMeta = getVehicleStatusMeta(highlightedAppointment?.vehicleStatus ?? 'RECEIVED')

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-[13px] font-medium text-[#5B5F7A]">Seguimiento y turnos</p>
            <h1 className="text-[26px] font-bold tracking-tight text-[#1E1B4B]">
              Historial en Vivo
            </h1>
          </div>

          {customerId && (
            <div className="flex items-center gap-2">
              <span className="inline-flex items-center gap-1.5 rounded-full border border-[#E4E6F2] bg-white px-3 py-1.5 text-xs font-medium text-[#5B5F7A]">
                <span className="h-2 w-2 animate-pulse rounded-full bg-emerald-500" />
                En vivo cada 10s
                {lastUpdated ? ` · ${formatTimeOnly(lastUpdated)}` : ''}
              </span>
              <Button variant="secondary" size="m" onClick={loadAppointments} disabled={loading}>
                Actualizar
              </Button>
            </div>
          )}
        </div>

        {!customerId ? (
          <CompleteCustomerProfileCard />
        ) : (
          <>
            {errorMessage && (
              <div
                role="alert"
                className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
              >
                <span>{errorMessage}</span>
                <button
                  type="button"
                  onClick={loadAppointments}
                  className="rounded-lg bg-white px-3 py-1 text-xs font-semibold text-[#B91C1C]"
                >
                  Reintentar
                </button>
              </div>
            )}

            {loading ? (
              <div className="space-y-4">
                {[1, 2].map((skeleton) => (
                  <div
                    key={skeleton}
                    className="h-44 animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-6"
                  />
                ))}
              </div>
            ) : (
              <>
                {highlightedAppointment && (
                  <section className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card sm:p-6">
                    <div className="flex flex-wrap items-start justify-between gap-3 border-b border-[#E4E6F2] pb-4">
                      <div>
                        <span className="text-xs font-semibold tracking-wider text-[#4F46E5] uppercase">
                          Vehículo en proceso
                        </span>
                        <div className="mt-1 flex flex-wrap items-center gap-2.5">
                          <span className="rounded-lg border border-[#1E1B4B] bg-amber-300 px-2.5 py-0.5 font-mono text-sm font-extrabold tracking-widest text-[#1E1B4B]">
                            {highlightedAppointment.vehiclePlate}
                          </span>
                          <h2 className="text-lg font-semibold text-[#1E1B4B]">
                            {highlightedAppointment.serviceName}
                          </h2>
                        </div>
                        <p className="mt-1 text-xs text-[#5B5F7A]">
                          {highlightedAppointment.vehicleDescription} · Especialista:{' '}
                          {highlightedAppointment.employeeName} ·{' '}
                          {formatDateTime(highlightedAppointment.dateTime)}
                        </p>
                      </div>

                      <div className="flex flex-wrap items-center gap-2">
                        <AppointmentStatusBadge status={highlightedAppointment.status} />
                        <VehicleStatusBadge
                          status={highlightedAppointment.vehicleStatus ?? 'RECEIVED'}
                          prefix="Estado: "
                        />
                      </div>
                    </div>

                    <ol className="mt-5 space-y-4">
                      {VEHICLE_STATUS_ORDER.map((stepStatus, index) => {
                        const stepMeta = getVehicleStatusMeta(stepStatus)
                        const isCompleted = index + 1 < liveMeta.step
                        const isCurrent = index + 1 === liveMeta.step
                        const isLast = index === VEHICLE_STATUS_ORDER.length - 1

                        return (
                          <li key={stepStatus} className="relative flex gap-4">
                            {!isLast && (
                              <span
                                aria-hidden="true"
                                className={[
                                  'absolute top-7 left-3.5 h-[calc(100%+4px)] w-0.5',
                                  isCompleted ? 'bg-[#4F46E5]' : 'bg-[#E4E6F2]',
                                ].join(' ')}
                              />
                            )}

                            <span
                              className={[
                                'relative z-10 flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold',
                                isCompleted
                                  ? 'bg-[#4F46E5] text-white'
                                  : isCurrent
                                    ? 'bg-[#EDE9FE] text-[#6D28D9] ring-4 ring-[#8B5CF6]/20'
                                    : 'border border-[#E4E6F2] bg-[#F5F6FB] text-[#5B5F7A]',
                              ].join(' ')}
                            >
                              {isCompleted ? (
                                <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.8">
                                  <path d="M20 6L9 17l-5-5" />
                                </svg>
                              ) : (
                                index + 1
                              )}
                            </span>

                            <div className="flex-1 pb-1">
                              <div className="flex flex-wrap items-center justify-between gap-2">
                                <p
                                  className={[
                                    'text-sm font-semibold',
                                    isCurrent || isCompleted ? 'text-[#1E1B4B]' : 'text-[#5B5F7A]',
                                  ].join(' ')}
                                >
                                  {stepMeta.label}
                                </p>
                                {isCurrent && (
                                  <span className="rounded-full bg-[#EDE9FE] px-2.5 py-0.5 text-[11px] font-semibold text-[#6D28D9]">
                                    Etapa actual
                                  </span>
                                )}
                                {isCompleted && (
                                  <span className="text-[11px] font-medium text-[#15803D]">
                                    Completado
                                  </span>
                                )}
                              </div>
                              <p className="mt-0.5 text-xs text-[#5B5F7A]">
                                {STEP_DESCRIPTIONS[stepStatus]}
                              </p>
                            </div>
                          </li>
                        )
                      })}
                    </ol>
                  </section>
                )}

                <section className="space-y-4">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="inline-flex flex-wrap gap-1.5 rounded-xl border border-[#E4E6F2] bg-white p-1">
                      {[
                        { key: 'ALL', label: `Todas (${appointments.length})` },
                        { key: 'ACTIVE', label: 'Activas' },
                        { key: 'FINISHED', label: 'Finalizadas' },
                        { key: 'CANCELLED', label: 'Canceladas' },
                      ].map((tab) => (
                        <button
                          key={tab.key}
                          type="button"
                          onClick={() => setFilterTab(tab.key)}
                          className={[
                            'rounded-lg px-3 py-1.5 text-xs font-semibold transition',
                            filterTab === tab.key
                              ? 'bg-[#4F46E5] text-white'
                              : 'text-[#5B5F7A] hover:bg-[#F5F6FB] hover:text-[#1E1B4B]',
                          ].join(' ')}
                        >
                          {tab.label}
                        </button>
                      ))}
                    </div>

                    <Link
                      to="/portal/agendar"
                      className="text-xs font-semibold text-[#4F46E5] hover:text-[#4338CA]"
                    >
                      + Agendar nueva cita
                    </Link>
                  </div>

                  {filteredAppointments.length === 0 ? (
                    <div className="rounded-2xl border border-dashed border-[#E4E6F2] bg-white p-10 text-center">
                      <p className="text-base font-semibold text-[#1E1B4B]">
                        No hay citas en esta vista
                      </p>
                      <p className="mt-1 text-sm text-[#5B5F7A]">
                        Agenda tu próximo servicio indicando la placa de tu vehículo.
                      </p>
                      <div className="mt-5">
                        <Link
                          to="/portal/agendar"
                          className="inline-flex h-11 items-center justify-center rounded-xl bg-[#4F46E5] px-5 text-sm font-semibold text-white hover:bg-[#4338CA]"
                        >
                          Agendar cita
                        </Link>
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {filteredAppointments.map((appointment) => (
                        <article
                          key={appointment.id}
                          className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card"
                        >
                          <div className="space-y-1.5">
                            <div className="flex flex-wrap items-center gap-2">
                              <span className="rounded-md bg-amber-300 px-2 py-0.5 font-mono text-xs font-bold tracking-wider text-[#1E1B4B]">
                                {appointment.vehiclePlate || 'SIN PLACA'}
                              </span>
                              <AppointmentStatusBadge status={appointment.status} />
                              {appointment.status !== 'CANCELLED' && (
                                <VehicleStatusBadge
                                  status={appointment.vehicleStatus ?? 'RECEIVED'}
                                  prefix="Auto: "
                                />
                              )}
                            </div>

                            <h3 className="text-base font-semibold text-[#1E1B4B]">
                              {appointment.serviceName || 'Servicio de detailing'}
                            </h3>
                            <p className="text-xs text-[#5B5F7A]">
                              {formatDateTime(appointment.dateTime)} ·{' '}
                              {appointment.vehicleDescription || 'Vehículo'} · Especialista:{' '}
                              {appointment.employeeName || 'Asignado'}
                            </p>
                            <p className="text-[11px] font-medium text-[#5B5F7A]">
                              Pago en efectivo al finalizar
                            </p>
                          </div>

                          {appointment.status === 'PENDING' && (
                            <Button
                              variant="danger"
                              size="m"
                              loading={busyCancelId === appointment.id}
                              onClick={() => handleCancelAppointment(appointment)}
                            >
                              Cancelar cita
                            </Button>
                          )}
                        </article>
                      ))}
                    </div>
                  )}
                </section>
              </>
            )}
          </>
        )}
      </div>
    </MainLayout>
  )
}
