import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'
import { Button } from '../components/Button'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { cancelAppointment, getAppointmentsByCustomer } from '../services/appointmentService'

const FILTER_OPTIONS = [
  { value: 'ALL', label: 'Todas' },
  { value: 'PENDING', label: 'Pendientes' },
  { value: 'IN_PROGRESS', label: 'En proceso' },
  { value: 'FINISHED', label: 'Finalizadas' },
  { value: 'CANCELLED', label: 'Canceladas' },
]

const VEHICLE_STATUS_LABELS = {
  RECEIVED: 'Recibido',
  WASHING: 'En lavado',
  DETAILING: 'En detailing',
  READY: 'Listo para entrega',
  DELIVERED: 'Entregado',
}

function formatAppointmentDateTime(value) {
import { Button } from '../components/Button'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { cancelAppointment, getAppointmentById, getAppointments } from '../services/appointmentService'
import { getVehicleStatusMeta } from '../utils/vehicleStatus'

const POLLING_INTERVAL_MS = 10_000

const STATUS_BADGES = {
  PENDING: {
    label: 'Pendiente',
    badgeClass: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
  },
  IN_PROGRESS: {
    label: 'En progreso',
    badgeClass: 'bg-sky-50 text-sky-700 ring-1 ring-sky-200',
  },
  FINISHED: {
    label: 'Finalizada',
    badgeClass: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
  },
  CANCELLED: {
    label: 'Cancelada',
    badgeClass: 'bg-rose-50 text-rose-700 ring-1 ring-rose-200',
  },
}

const FILTER_TABS = [
  { key: '', label: 'Todas' },
  { key: 'PENDING', label: 'Pendientes' },
  { key: 'IN_PROGRESS', label: 'En progreso' },
  { key: 'FINISHED', label: 'Finalizadas' },
  { key: 'CANCELLED', label: 'Canceladas' },
]

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function sortByDateDesc(list) {
  return [...list].sort((a, b) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime())
}

/**
 * Pantalla "Historial de Citas" (/portal/historial) del Portal de Clientes.
 * Lista las citas del cliente ordenadas por fecha, permite filtrar por estado
 * y cancelar únicamente citas en estado PENDING mediante PATCH /api/appointments/{id}/cancel.
/**
 * Pantalla "Historial de Citas" del Portal de Clientes para consultar turnos,
 * ver el estado del vehículo en vivo y cancelar citas pendientes.
 */
export function AppointmentHistoryPage() {
  const { user } = useAuth()
  const customerId = user?.customerId ?? null

  const [appointments, setAppointments] = useState([])
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [loading, setLoading] = useState(Boolean(customerId))
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')

  const loadAppointments = useCallback(async () => {
    if (!customerId) {
      setLoading(false)
      return
    }
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getAppointmentsByCustomer(customerId)
      setAppointments(sortByDateDesc(Array.isArray(data) ? data : []))
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(Boolean(customerId))
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')
  const appointmentsRef = useRef([])

  useEffect(() => {
    appointmentsRef.current = appointments
  }, [appointments])

  const loadHistory = useCallback(async () => {
    if (!customerId) return
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getAppointments({ customerId })
      setAppointments(Array.isArray(data) ? data : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar tu historial de citas.'))
    } finally {
      setLoading(false)
    }
  }, [customerId])

  useEffect(() => {
    if (!customerId) {
      return
    }
    let active = true

    getAppointmentsByCustomer(customerId)
      .then((data) => {
        if (active) {
          setAppointments(sortByDateDesc(Array.isArray(data) ? data : []))
    if (!customerId) return
    let active = true

    getAppointments({ customerId })
      .then((data) => {
        if (active) {
          setAppointments(Array.isArray(data) ? data : [])
        }
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(
            getApiErrorMessage(error, 'No fue posible cargar tu historial de citas.'),
          )
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar tu historial de citas.'))
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [customerId])

  useEffect(() => {
    if (!customerId) return undefined
    let active = true

    const pollLiveAppointments = async () => {
      const trackedIds = appointmentsRef.current
        .filter((item) => item.status !== 'CANCELLED' && item.vehicleStatus !== 'DELIVERED')
        .map((item) => item.id)

      if (trackedIds.length === 0) return

      const results = await Promise.allSettled(trackedIds.map((id) => getAppointmentById(id)))
      if (!active) return

      const updatedById = new Map()
      for (const result of results) {
        if (result.status === 'fulfilled' && result.value?.id) {
          updatedById.set(result.value.id, result.value)
        }
      }

      if (updatedById.size > 0) {
        setAppointments((prev) => prev.map((item) => updatedById.get(item.id) || item))
      }
    }

    const intervalId = window.setInterval(pollLiveAppointments, POLLING_INTERVAL_MS)
    return () => {
      active = false
      window.clearInterval(intervalId)
    }
  }, [customerId])

  const filteredAppointments = useMemo(() => {
    if (statusFilter === 'ALL') {
      return appointments
    }
    return appointments.filter((appointment) => appointment.status === statusFilter)
  }, [appointments, statusFilter])

  const handleCancelAppointment = async (appointment) => {
    if (appointment.status !== 'PENDING') {
      return
    }

    const confirmed = window.confirm(
      `¿Seguro que deseas cancelar la cita de ${appointment.serviceTypeName} para el vehículo ${appointment.vehiclePlate}?`,
    )
    if (!confirmed) {
      return
    }

    setBusyId(appointment.id)
    setErrorMessage('')
    setActionMessage('')

    try {
      await cancelAppointment(appointment.id)
      setActionMessage('Tu cita fue cancelada correctamente.')
      await loadAppointments()
    } catch (error) {
      const fallback =
        error.response?.status === 409
          ? 'La cita cambió de estado en el sistema y ya no puede cancelarse.'
          : 'No fue posible cancelar la cita.'
      setErrorMessage(getApiErrorMessage(error, fallback))
      if (error.response?.status === 409) {
        await loadAppointments()
      }
    if (!statusFilter) return appointments
    return appointments.filter((item) => item.status === statusFilter)
  }, [appointments, statusFilter])

  const handleCancelAppointment = async (appointment) => {
    if (!window.confirm(`¿Seguro que deseas cancelar la cita de ${appointment.serviceTypeName}?`)) {
      return
    }
    setBusyId(appointment.id)
    setErrorMessage('')
    try {
      const updated = await cancelAppointment(appointment.id)
      setAppointments((prev) => prev.map((item) => (item.id === appointment.id ? updated : item)))
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cancelar la cita.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h2 className="text-xl font-bold text-slate-900">Historial de Citas</h2>
            <p className="mt-1 text-sm text-slate-600">
              Consulta el estado de tus turnos y cancela citas pendientes si lo necesitas.
            </p>
          </div>

          {customerId && (
            <div className="flex flex-wrap items-center gap-3">
              <Button variant="secondary" onClick={loadAppointments} disabled={loading}>
                Actualizar
              </Button>
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
          <div
            role="alert"
            className="rounded-2xl border border-amber-200 bg-amber-50 p-6 text-sm text-amber-800 shadow-xs"
          >
            <p className="font-semibold">Perfil de cliente no vinculado</p>
            <p className="mt-1">
              Tu cuenta aún no tiene un identificador de cliente asignado (<code>customerId</code>).
              Para ver tu historial de citas, regístrate con nombre y teléfono o comunícate con AutoClean.
            </p>
          </div>
              Revisa el estado de tus servicios programados o cancela citas pendientes.
            </p>
          </div>
          <div className="flex flex-wrap gap-2.5">
            <Button variant="secondary" onClick={loadHistory} disabled={loading || !customerId}>
              Actualizar
            </Button>
            <Link
              to="/portal/agendar"
              className="inline-flex items-center justify-center rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
            >
              + Nueva cita
            </Link>
          </div>
        </section>

        {!customerId ? (
          <CompleteCustomerProfileCard />
        ) : (
          <>
            {errorMessage && (
              <div
                role="alert"
                className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
              >
                {errorMessage}
              </div>
            )}

            {actionMessage && (
              <div
                role="status"
                className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
              >
                {actionMessage}
              </div>
            )}

            <section className="flex flex-wrap gap-2" aria-label="Filtrar citas por estado">
              {FILTER_OPTIONS.map((option) => {
                const isSelected = statusFilter === option.value
                return (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => setStatusFilter(option.value)}
                    className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition-colors ${
                      isSelected
                        ? 'bg-sky-600 text-white shadow-xs'
                        : 'bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-100'
                    }`}
                  >
                    {option.label}
                  </button>
                )
              })}
            </section>

            {loading ? (
              <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-xs">
                Cargando tu historial de citas...
              </div>
            ) : appointments.length === 0 ? (
              <section className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center shadow-xs">
                <h3 className="text-base font-semibold text-slate-900">
                  Aún no tienes citas agendadas
                </h3>
                <p className="mt-1 text-sm text-slate-500">
                  Programa tu primer servicio de lavado o detailing en pocos pasos.
                </p>
                <div className="mt-5">
                  <Link
                    to="/portal/agendar"
                    className="inline-flex items-center justify-center rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
                  >
                    Agendar mi primera cita
                  </Link>
                </div>
              </section>
            ) : filteredAppointments.length === 0 ? (
              <section className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-xs">
                No tienes citas registradas con el estado seleccionado.
              </section>
            ) : (
              <section className="space-y-4">
                {filteredAppointments.map((appointment) => {
                  const canCancel = appointment.status === 'PENDING'
                  const vehicleStepLabel = VEHICLE_STATUS_LABELS[appointment.vehicleStatus]
            <div className="flex flex-wrap gap-2">
              {FILTER_TABS.map((tab) => {
                const count = tab.key
                  ? appointments.filter((item) => item.status === tab.key).length
                  : appointments.length
                const active = statusFilter === tab.key
                return (
                  <button
                    key={tab.key || 'ALL'}
                    type="button"
                    onClick={() => setStatusFilter(tab.key)}
                    className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition ${
                      active
                        ? 'bg-sky-600 text-white shadow-xs'
                        : 'bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-50'
                    }`}
                  >
                    {tab.label} ({count})
                  </button>
                )
              })}
            </div>

            {loading ? (
              <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-xs">
                Cargando historial de citas...
              </div>
            ) : filteredAppointments.length === 0 ? (
              <section className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center shadow-xs">
                <h3 className="text-base font-semibold text-slate-900">
                  {statusFilter
                    ? 'No tienes citas en este estado'
                    : 'Aún no has agendado ninguna cita'}
                </h3>
                <p className="mt-1 text-sm text-slate-500">
                  Reserva tu primer lavado o detailing cuando quieras.
                </p>
                <Link
                  to="/portal/agendar"
                  className="mt-5 inline-flex items-center justify-center rounded-lg bg-sky-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
                >
                  Agendar mi primera cita
                </Link>
              </section>
            ) : (
              <section className="space-y-3">
                {filteredAppointments.map((appointment) => {
                  const statusMeta = STATUS_BADGES[appointment.status] || {
                    label: appointment.status,
                    badgeClass: 'bg-slate-100 text-slate-700',
                  }
                  const vehicleMeta = getVehicleStatusMeta(appointment.vehicleStatus)
                  const canCancel = appointment.status === 'PENDING'

                  return (
                    <article
                      key={appointment.id}
                      className="flex flex-col justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-xs sm:flex-row sm:items-center"
                    >
                      <div className="space-y-2">
                      className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-xs transition hover:border-sky-200"
                    >
                      <div className="space-y-1.5">
                        <div className="flex flex-wrap items-center gap-2.5">
                          <h3 className="text-base font-bold text-slate-900">
                            {appointment.serviceTypeName || 'Servicio'}
                          </h3>
                          <AppointmentStatusBadge status={appointment.status} />
                          {vehicleStepLabel && (
                            <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-700">
                              Vehículo: {vehicleStepLabel}
                            </span>
                          )}
                        </div>

                        <div className="flex flex-wrap gap-x-6 gap-y-1 text-sm text-slate-600">
                          <p>
                            <span className="font-medium text-slate-800">Vehículo:</span>{' '}
                            <span className="font-semibold text-slate-900">
                              {appointment.vehiclePlate || '—'}
                            </span>
                          </p>
                          <p>
                            <span className="font-medium text-slate-800">Especialista:</span>{' '}
                            {appointment.employeeName || '—'}
                          </p>
                          <p>
                            <span className="font-medium text-slate-800">Fecha:</span>{' '}
                            {formatAppointmentDateTime(appointment.dateTime)}
                          </p>
                        </div>
                      </div>

                      {canCancel && (
                        <div className="shrink-0">
                          <button
                            type="button"
                            onClick={() => handleCancelAppointment(appointment)}
                            disabled={busyId === appointment.id}
                            className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-2 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            {busyId === appointment.id ? 'Cancelando...' : 'Cancelar Cita'}
                          </button>
                        </div>
                      )}
                          <span
                            className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${statusMeta.badgeClass}`}
                          >
                            {statusMeta.label}
                          </span>
                          <span
                            className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${vehicleMeta.badgeClass}`}
                          >
                            Vehículo: {vehicleMeta.label}
                          </span>
                        </div>
                        <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-600">
                          <span>
                            Vehículo:{' '}
                            <strong className="font-mono text-slate-900">
                              {appointment.vehiclePlate || '—'}
                            </strong>
                          </span>
                          <span>
                            Especialista:{' '}
                            <strong className="text-slate-800">{appointment.employeeName || '—'}</strong>
                          </span>
                          <span>
                            Fecha:{' '}
                            <strong className="text-slate-800">{formatDateTime(appointment.dateTime)}</strong>
                          </span>
                        </div>
                      </div>

                      <div className="flex flex-wrap items-center gap-2">
                        {canCancel ? (
                          <button
                            type="button"
                            disabled={busyId === appointment.id}
                            onClick={() => handleCancelAppointment(appointment)}
                            className="rounded-lg border border-rose-200 bg-rose-50 px-3.5 py-2 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:opacity-50"
                          >
                            {busyId === appointment.id ? 'Cancelando...' : 'Cancelar cita'}
                          </button>
                        ) : (
                          <Link
                            to="/portal/agendar"
                            className="rounded-lg bg-white px-3.5 py-2 text-xs font-semibold text-sky-700 ring-1 ring-inset ring-sky-200 transition-colors hover:bg-sky-50"
                          >
                            Agendar de nuevo
                          </Link>
                        )}
                      </div>
                    </article>
                  )
                })}
              </section>
            )}
          </>
        )}
      </div>
    </MainLayout>
  )
}

