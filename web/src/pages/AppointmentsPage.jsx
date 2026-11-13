import { useCallback, useEffect, useMemo, useState } from 'react'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { VehicleStatusBadge } from '../components/VehicleStatusBadge'
import { getApiErrorMessage } from '../services/api'
import {
  createAppointment,
  deleteAppointment,
  getAppointments,
  getCustomers,
  getVehicles,
  updateAppointmentStatus,
  updateVehicleStatus,
} from '../services/appointmentService'
import { getEmployees, getServiceTypes } from '../services/catalogService'
import { showToast } from '../utils/toast'

const INITIAL_FORM = {
  customerId: '',
  vehicleId: '',
  employeeId: '',
  serviceTypeId: '',
  dateTime: '',
}

const KANBAN_COLUMNS = [
  { key: 'PENDING', title: 'Pendiente', pillClass: 'bg-[#FEF3C7] text-[#B45309]' },
  { key: 'IN_PROGRESS', title: 'En proceso', pillClass: 'bg-[#EDE9FE] text-[#6D28D9]' },
  { key: 'FINISHED', title: 'Finalizada', pillClass: 'bg-[#DCFCE7] text-[#15803D]' },
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

function formatCop(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0)
}

function isWithinOperatingHours(dateTimeString) {
  if (!dateTimeString) return true
  const d = new Date(dateTimeString)
  if (Number.isNaN(d.getTime())) return false
  const mins = d.getHours() * 60 + d.getMinutes()
  return (mins >= 420 && mins <= 720) || (mins >= 870 && mins <= 1260)
}

/**
 * Agenda de Citas Admin: tablero Kanban (PENDIENTE -> EN PROCESO -> FINALIZADA)
 * con toggle a vista de tabla, filtros, bloqueo horario y confirmación de efectivo al entregar.
 */
export function AppointmentsPage() {
  const [appointments, setAppointments] = useState([])
  const [customers, setCustomers] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [employees, setEmployees] = useState([])
  const [serviceTypes, setServiceTypes] = useState([])

  const [viewMode, setViewMode] = useState('kanban')
  const [dateFilter, setDateFilter] = useState('')
  const [employeeFilter, setEmployeeFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  const [showCreateModal, setShowCreateModal] = useState(false)
  const [deliverConfirmTarget, setDeliverConfirmTarget] = useState(null)
  const [form, setForm] = useState(INITIAL_FORM)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [updatingId, setUpdatingId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadData = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [appointmentList, customerList, vehicleList, employeeList, serviceList] =
        await Promise.all([
          getAppointments(),
          getCustomers(),
          getVehicles(),
          getEmployees(),
          getServiceTypes(),
        ])
      setAppointments(Array.isArray(appointmentList) ? appointmentList : [])
      setCustomers(Array.isArray(customerList) ? customerList : [])
      setVehicles(Array.isArray(vehicleList) ? vehicleList : [])
      setEmployees(Array.isArray(employeeList) ? employeeList : [])
      setServiceTypes(Array.isArray(serviceList) ? serviceList : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar la agenda de citas.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const serviceById = useMemo(() => {
    const map = new Map()
    for (const s of serviceTypes) map.set(s.id, s)
    return map
  }, [serviceTypes])

  const vehicleById = useMemo(() => {
    const map = new Map()
    for (const v of vehicles) map.set(v.id, v)
    return map
  }, [vehicles])

  const filteredAppointments = useMemo(() => {
    return appointments.filter((item) => {
      if (statusFilter && item.status !== statusFilter) return false
      if (employeeFilter && item.employeeId !== employeeFilter) return false
      if (dateFilter) {
        const d = new Date(item.dateTime)
        if (!Number.isNaN(d.getTime())) {
          const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
            d.getDate(),
          ).padStart(2, '0')}`
          if (key !== dateFilter) return false
        }
      }
      return true
    })
  }, [appointments, dateFilter, employeeFilter, statusFilter])

  const filteredVehicles = useMemo(() => {
    if (!form.customerId) return []
    return vehicles.filter((vehicle) => vehicle.customerId === form.customerId)
  }, [vehicles, form.customerId])

  const applyAppointmentUpdate = (updated) => {
    setAppointments((prev) => prev.map((item) => (item.id === updated.id ? updated : item)))
  }

  const handleStatusChange = async (appointmentId, nextStatus) => {
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      const updated = await updateAppointmentStatus(appointmentId, nextStatus)
      applyAppointmentUpdate(updated)
      showToast({
        type: 'info',
        title: 'Estado de cita actualizado',
        description: `La cita pasó a ${nextStatus}.`,
      })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible actualizar el estado de la cita.'))
    } finally {
      setUpdatingId(null)
    }
  }

  const handleStartService = async (appointmentId) => {
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      await updateAppointmentStatus(appointmentId, 'IN_PROGRESS')
      const updated = await updateVehicleStatus(appointmentId, 'WASHING')
      applyAppointmentUpdate(updated)
      showToast({
        type: 'success',
        title: 'Servicio iniciado',
        description: `${updated.vehiclePlate} ahora está en proceso (Lavando).`,
      })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible iniciar el servicio.'))
    } finally {
      setUpdatingId(null)
    }
  }

  const handleVehicleStatusChange = async (appointmentId, nextVehicleStatus, customMsg) => {
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      const updated = await updateVehicleStatus(appointmentId, nextVehicleStatus)
      applyAppointmentUpdate(updated)
      showToast({
        type: 'success',
        title: customMsg || 'Estado del vehículo actualizado',
        description: `${updated.vehiclePlate} actualizado correctamente.`,
      })
    } catch (error) {
      setErrorMessage(
        getApiErrorMessage(error, 'No fue posible actualizar el estado del vehículo.'),
      )
    } finally {
      setUpdatingId(null)
    }
  }

  const handleConfirmDelivery = async () => {
    if (!deliverConfirmTarget) return
    const target = deliverConfirmTarget
    setDeliverConfirmTarget(null)
    await handleVehicleStatusChange(
      target.id,
      'DELIVERED',
      `Entrega y pago en efectivo confirmados (${target.vehiclePlate})`,
    )
  }

  const handleDelete = async (appointmentId) => {
    if (!window.confirm('¿Deseas eliminar esta cita?')) return
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      await deleteAppointment(appointmentId)
      setAppointments((prev) => prev.filter((item) => item.id !== appointmentId))
      showToast({ type: 'info', title: 'Cita eliminada' })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar la cita.'))
    } finally {
      setUpdatingId(null)
    }
  }

  const handleCreate = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    if (
      !form.customerId ||
      !form.vehicleId ||
      !form.employeeId ||
      !form.serviceTypeId ||
      !form.dateTime
    ) {
      setErrorMessage('Completa cliente, vehículo, empleado, servicio y fecha/hora.')
      return
    }
    if (!isWithinOperatingHours(form.dateTime)) {
      setErrorMessage('Horario de atención: 7:00 a. m.–12:00 p. m. y 2:30 p. m.–9:00 p. m.')
      return
    }

    setSubmitting(true)
    try {
      const created = await createAppointment({
        customerId: form.customerId,
        vehicleId: form.vehicleId,
        employeeId: form.employeeId,
        serviceTypeId: form.serviceTypeId,
        dateTime: new Date(form.dateTime).toISOString(),
        status: 'PENDING',
      })
      setAppointments((prev) => [created, ...prev])
      setForm(INITIAL_FORM)
      setShowCreateModal(false)
      showToast({
        type: 'success',
        title: 'Cita programada',
        description: `${created.vehiclePlate} agendado en estado Pendiente.`,
      })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible crear la cita.'))
    } finally {
      setSubmitting(false)
    }
  }

  const renderPrimaryButton = (appointment, compact = false) => {
    const isBusy = updatingId === appointment.id
    const sizeCls = compact ? 'h-[36px] px-3 text-xs' : 'h-[40px] w-full text-[13px]'

    if (appointment.status === 'PENDING') {
      return (
        <button
          type="button"
          disabled={isBusy}
          onClick={() => handleStartService(appointment.id)}
          className={`${sizeCls} rounded-xl bg-[#4F46E5] font-semibold text-white transition hover:bg-[#4338CA] disabled:opacity-50`}
        >
          Iniciar Servicio
        </button>
      )
    }
    if (appointment.status === 'IN_PROGRESS' && appointment.vehicleStatus !== 'READY') {
      return (
        <button
          type="button"
          disabled={isBusy}
          onClick={() =>
            handleVehicleStatusChange(appointment.id, 'READY', 'Vehículo marcado como Listo')
          }
          className={`${sizeCls} rounded-xl bg-[#8B5CF6] font-semibold text-white transition hover:bg-[#7C3AED] disabled:opacity-50`}
        >
          Marcar como Listo
        </button>
      )
    }
    if (appointment.status === 'IN_PROGRESS') {
      return (
        <button
          type="button"
          disabled={isBusy}
          onClick={() => handleStatusChange(appointment.id, 'FINISHED')}
          className={`${sizeCls} rounded-xl bg-[#15803D] font-semibold text-white transition hover:bg-emerald-800 disabled:opacity-50`}
        >
          Finalizar
        </button>
      )
    }
    if (appointment.status === 'FINISHED' && appointment.vehicleStatus !== 'DELIVERED') {
      return (
        <button
          type="button"
          disabled={isBusy}
          onClick={() => setDeliverConfirmTarget(appointment)}
          className={`${sizeCls} rounded-xl bg-[#15803D] font-semibold text-white transition hover:bg-emerald-800 disabled:opacity-50`}
        >
          Entregar
        </button>
      )
    }
    return null
  }

  return (
    <MainLayout>
      <div className="space-y-5">
        {/* Cabecera + Toggle Kanban/Tabla + Nueva Cita */}
        <section className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] leading-[34px] font-bold tracking-tight text-[#1E1B4B]">
              Agenda de Citas
            </h1>
            <p className="mt-1 text-[14px] text-[#5B5F7A]">
              Horario: 7:00 a. m.–12:00 p. m. y 2:30 p. m.–9:00 p. m. · Pago 100% en efectivo al entregar.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2.5">
            <div className="inline-flex rounded-xl border border-[#E4E6F2] bg-white p-1">
              <button
                type="button"
                onClick={() => setViewMode('kanban')}
                className={`rounded-lg px-3.5 py-1.5 text-[13px] font-semibold transition ${
                  viewMode === 'kanban'
                    ? 'bg-[#4F46E5] text-white'
                    : 'text-[#5B5F7A] hover:text-[#1E1B4B]'
                }`}
              >
                Kanban
              </button>
              <button
                type="button"
                onClick={() => setViewMode('table')}
                className={`rounded-lg px-3.5 py-1.5 text-[13px] font-semibold transition ${
                  viewMode === 'table'
                    ? 'bg-[#4F46E5] text-white'
                    : 'text-[#5B5F7A] hover:text-[#1E1B4B]'
                }`}
              >
                Tabla
              </button>
            </div>
            <Button onClick={() => setShowCreateModal(true)}>+ Nueva cita</Button>
          </div>
        </section>

        {/* Filtros + franjas bloqueadas visibles */}
        <section className="flex flex-wrap items-center gap-3 rounded-2xl border border-[#E4E6F2] bg-white p-4 shadow-card">
          <input
            type="date"
            aria-label="Filtrar por fecha"
            value={dateFilter}
            onChange={(e) => setDateFilter(e.target.value)}
            className="h-[40px] rounded-xl border border-[#E4E6F2] px-3 text-[13px] text-[#1E1B4B] outline-none focus:border-[#4F46E5]"
          />
          <select
            aria-label="Filtrar por especialista"
            value={employeeFilter}
            onChange={(e) => setEmployeeFilter(e.target.value)}
            className="h-[40px] rounded-xl border border-[#E4E6F2] bg-white px-3 text-[13px] text-[#1E1B4B] outline-none focus:border-[#4F46E5]"
          >
            <option value="">Especialista: Todos</option>
            {employees.map((emp) => (
              <option key={emp.id} value={emp.id}>
                {emp.name}
              </option>
            ))}
          </select>
          <select
            aria-label="Filtrar por estado"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-[40px] rounded-xl border border-[#E4E6F2] bg-white px-3 text-[13px] text-[#1E1B4B] outline-none focus:border-[#4F46E5]"
          >
            <option value="">Estado: Todos</option>
            <option value="PENDING">Pendiente</option>
            <option value="IN_PROGRESS">En proceso</option>
            <option value="FINISHED">Finalizada</option>
            <option value="CANCELLED">Cancelada</option>
          </select>
          {(dateFilter || employeeFilter || statusFilter) && (
            <Button
              variant="ghost"
              onClick={() => {
                setDateFilter('')
                setEmployeeFilter('')
                setStatusFilter('')
              }}
            >
              Limpiar filtros
            </Button>
          )}

          <div className="ml-auto hidden flex-wrap gap-2 text-[12px] font-medium text-[#5B5F7A] xl:flex">
            <span className="rounded-lg bg-[#F5F6FB] px-2.5 py-1">12:00–14:30 Bloqueado (Almuerzo)</span>
            <span className="rounded-lg bg-[#F5F6FB] px-2.5 py-1">21:00–07:00 Cerrado</span>
          </div>
        </section>

        {errorMessage && (
          <div
            role="alert"
            className="rounded-xl border border-[#FECACA] bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
          >
            {errorMessage}
          </div>
        )}

        {loading ? (
          <div className="grid gap-4 lg:grid-cols-3">
            {[1, 2, 3].map((col) => (
              <div key={col} className="h-64 animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-4" />
            ))}
          </div>
        ) : viewMode === 'kanban' ? (
          <section className="grid gap-4 lg:grid-cols-3">
            {KANBAN_COLUMNS.map((col) => {
              const items = filteredAppointments.filter((a) => a.status === col.key)
              return (
                <div
                  key={col.key}
                  className="flex flex-col gap-3 rounded-2xl border border-[#E4E6F2] bg-white/75 p-4"
                >
                  <div className="flex items-center justify-between">
                    <span className={`rounded-full px-3 py-1 text-xs font-bold uppercase ${col.pillClass}`}>
                      {col.title}
                    </span>
                    <span className="text-xs font-semibold text-[#5B5F7A] tabular-nums">
                      {items.length}
                    </span>
                  </div>

                  {items.length === 0 ? (
                    <div className="rounded-xl border border-dashed border-[#E4E6F2] p-6 text-center text-xs text-[#5B5F7A]">
                      Sin citas en esta etapa
                    </div>
                  ) : (
                    items.map((appointment) => {
                      const srv = serviceById.get(appointment.serviceTypeId)
                      const veh = vehicleById.get(appointment.vehicleId)
                      const vehicleLabel = veh ? `${veh.brand} ${veh.model}` : 'Vehículo'
                      const isBusy = updatingId === appointment.id

                      return (
                        <article
                          key={appointment.id}
                          className="rounded-2xl border border-[#E4E6F2] bg-white p-4 shadow-card transition hover:border-[#4F46E5]/35"
                        >
                          <div className="flex items-center justify-between gap-2">
                            <span className="rounded-lg border border-[#1E1B4B] bg-[#FEF08A] px-2.5 py-0.5 font-mono text-xs font-extrabold tracking-wider text-[#1E1B4B]">
                              {appointment.vehiclePlate || '---'}
                            </span>
                            <VehicleStatusBadge status={appointment.vehicleStatus} />
                          </div>

                          <h3 className="mt-2.5 text-[15px] font-bold text-[#1E1B4B]">
                            {vehicleLabel} · {appointment.serviceTypeName}
                          </h3>
                          <p className="mt-0.5 text-[13px] text-[#5B5F7A]">
                            {appointment.customerName}
                            {srv?.price != null && (
                              <>
                                {' · '}
                                <strong className="text-[#1E1B4B] tabular-nums">
                                  {formatCop(srv.price)}
                                </strong>
                              </>
                            )}
                          </p>
                          <p className="mt-1 text-[12px] text-[#5B5F7A]">
                            {formatDateTime(appointment.dateTime)} · {appointment.employeeName}
                          </p>

                          <div className="mt-3 space-y-2">
                            {renderPrimaryButton(appointment)}
                            <div className="flex items-center justify-between gap-2 pt-1 text-xs">
                              {appointment.status !== 'FINISHED' &&
                              appointment.status !== 'CANCELLED' ? (
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  onClick={() => handleStatusChange(appointment.id, 'CANCELLED')}
                                  className="font-semibold text-[#B91C1C] hover:underline disabled:opacity-50"
                                >
                                  Cancelar cita
                                </button>
                              ) : (
                                <span className="text-[#5B5F7A]">
                                  {appointment.vehicleStatus === 'DELIVERED'
                                    ? 'Pago en efectivo recibido'
                                    : 'Finalizada'}
                                </span>
                              )}
                              <button
                                type="button"
                                disabled={isBusy}
                                onClick={() => handleDelete(appointment.id)}
                                className="font-medium text-[#5B5F7A] hover:text-[#B91C1C] disabled:opacity-50"
                              >
                                Eliminar
                              </button>
                            </div>
                          </div>
                        </article>
                      )
                    })
                  )}
                </div>
              )
            })}
          </section>
        ) : (
          <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
            {filteredAppointments.length === 0 ? (
              <div className="p-10 text-center text-sm text-[#5B5F7A]">
                No hay citas para los filtros seleccionados.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                  <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                    <tr>
                      <th className="px-4 py-3">Fecha y hora</th>
                      <th className="px-4 py-3">Placa</th>
                      <th className="px-4 py-3">Cliente</th>
                      <th className="px-4 py-3">Servicio</th>
                      <th className="px-4 py-3">Especialista</th>
                      <th className="px-4 py-3">Cita</th>
                      <th className="px-4 py-3">Vehículo</th>
                      <th className="px-4 py-3 text-right">Acción</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#E4E6F2] bg-white">
                    {filteredAppointments.map((appointment) => (
                      <tr key={appointment.id} className="hover:bg-[#F5F6FB]/60">
                        <td className="whitespace-nowrap px-4 py-3 font-medium text-[#1E1B4B]">
                          {formatDateTime(appointment.dateTime)}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <span className="rounded bg-[#FEF08A] px-2 py-0.5 font-mono text-xs font-bold text-[#1E1B4B]">
                            {appointment.vehiclePlate || '—'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-[#5B5F7A]">{appointment.customerName || '—'}</td>
                        <td className="px-4 py-3 text-[#1E1B4B]">{appointment.serviceTypeName || '—'}</td>
                        <td className="px-4 py-3 text-[#5B5F7A]">{appointment.employeeName || '—'}</td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <AppointmentStatusBadge status={appointment.status} />
                        </td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <VehicleStatusBadge status={appointment.vehicleStatus} />
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-right">
                          <div className="inline-flex items-center gap-2">
                            {renderPrimaryButton(appointment, true)}
                            <button
                              type="button"
                              disabled={updatingId === appointment.id}
                              onClick={() => handleDelete(appointment.id)}
                              className="text-xs font-medium text-[#5B5F7A] hover:text-[#B91C1C]"
                            >
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}
      </div>

      {/* Modal Nueva Cita */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#1E1B4B]/45 p-4 backdrop-blur-xs">
          <div className="w-full max-w-lg rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-2xl">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-[18px] font-bold text-[#1E1B4B]">Nueva cita operativa</h2>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="text-sm font-semibold text-[#5B5F7A] hover:text-[#1E1B4B]"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreate} className="space-y-4">
              <div className="space-y-1.5">
                <label htmlFor="modal-customer" className="block text-[13px] font-semibold">
                  Cliente
                </label>
                <select
                  id="modal-customer"
                  value={form.customerId}
                  onChange={(e) => setForm((p) => ({ ...p, customerId: e.target.value, vehicleId: '' }))}
                  className="h-[44px] w-full rounded-xl border border-[#E4E6F2] bg-white px-3 text-sm"
                  required
                >
                  <option value="">Selecciona un cliente...</option>
                  {customers.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-1.5">
                <label htmlFor="modal-vehicle" className="block text-[13px] font-semibold">
                  Vehículo
                </label>
                <select
                  id="modal-vehicle"
                  value={form.vehicleId}
                  onChange={(e) => setForm((p) => ({ ...p, vehicleId: e.target.value }))}
                  disabled={!form.customerId}
                  className="h-[44px] w-full rounded-xl border border-[#E4E6F2] bg-white px-3 text-sm disabled:bg-[#F5F6FB]"
                  required
                >
                  <option value="">
                    {form.customerId ? 'Selecciona un vehículo...' : 'Elige primero el cliente'}
                  </option>
                  {filteredVehicles.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.plate} — {v.brand} {v.model}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <label htmlFor="modal-service" className="block text-[13px] font-semibold">
                    Servicio
                  </label>
                  <select
                    id="modal-service"
                    value={form.serviceTypeId}
                    onChange={(e) => setForm((p) => ({ ...p, serviceTypeId: e.target.value }))}
                    className="h-[44px] w-full rounded-xl border border-[#E4E6F2] bg-white px-3 text-sm"
                    required
                  >
                    <option value="">Selecciona servicio...</option>
                    {serviceTypes.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name} ({formatCop(s.price)})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label htmlFor="modal-employee" className="block text-[13px] font-semibold">
                    Especialista
                  </label>
                  <select
                    id="modal-employee"
                    value={form.employeeId}
                    onChange={(e) => setForm((p) => ({ ...p, employeeId: e.target.value }))}
                    className="h-[44px] w-full rounded-xl border border-[#E4E6F2] bg-white px-3 text-sm"
                    required
                  >
                    <option value="">Selecciona especialista...</option>
                    {employees.map((emp) => (
                      <option key={emp.id} value={emp.id}>
                        {emp.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <Input
                id="modal-datetime"
                label="Fecha y hora (7:00–12:00 o 14:30–21:00)"
                type="datetime-local"
                value={form.dateTime}
                onChange={(e) => setForm((p) => ({ ...p, dateTime: e.target.value }))}
                error={
                  form.dateTime && !isWithinOperatingHours(form.dateTime)
                    ? 'Franja bloqueada. Solo atendemos de 7:00 a. m.–12:00 p. m. y 2:30 p. m.–9:00 p. m.'
                    : ''
                }
                required
              />

              <div className="flex justify-end gap-2.5 pt-2">
                <Button variant="ghost" onClick={() => setShowCreateModal(false)}>
                  Cancelar
                </Button>
                <Button type="submit" loading={submitting}>
                  Guardar cita
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Confirmación Pago 100% en Efectivo al Entregar */}
      {deliverConfirmTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#1E1B4B]/45 p-4 backdrop-blur-xs">
          <div className="w-full max-w-md rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-2xl">
            <h2 className="text-[18px] font-bold text-[#1E1B4B]">
              Confirmar entrega y pago en efectivo
            </h2>
            <p className="mt-2 text-[14px] text-[#5B5F7A]">
              Confirma que recibiste el pago 100% en efectivo por{' '}
              <strong className="text-[#1E1B4B]">
                {formatCop(serviceById.get(deliverConfirmTarget.serviceTypeId)?.price || 0)}
              </strong>{' '}
              del vehículo{' '}
              <strong className="font-mono text-[#1E1B4B]">
                {deliverConfirmTarget.vehiclePlate}
              </strong>{' '}
              ({deliverConfirmTarget.customerName}).
            </p>
            <div className="mt-6 flex justify-end gap-2.5">
              <Button variant="ghost" onClick={() => setDeliverConfirmTarget(null)}>
                Cancelar
              </Button>
              <Button onClick={handleConfirmDelivery}>Confirmar efectivo y entregar</Button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  )
}
