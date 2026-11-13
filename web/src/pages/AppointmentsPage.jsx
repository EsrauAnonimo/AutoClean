import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
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
import { getVehicleStatusMeta } from '../utils/vehicleStatus'

const STATUS_META = {
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

const INITIAL_FORM = {
  customerId: '',
  vehicleId: '',
  employeeId: '',
  serviceTypeId: '',
  dateTime: '',
}

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

/**
 * Vista de gestión de agenda de citas con creación y máquina de estados (PATCH).
 */
export function AppointmentsPage() {
  const [appointments, setAppointments] = useState([])
  const [customers, setCustomers] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [employees, setEmployees] = useState([])
  const [serviceTypes, setServiceTypes] = useState([])

  const [statusFilter, setStatusFilter] = useState('')
  const [form, setForm] = useState(INITIAL_FORM)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [updatingId, setUpdatingId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadData = useCallback(async (selectedStatus = statusFilter) => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [appointmentList, customerList, vehicleList, employeeList, serviceList] = await Promise.all([
        getAppointments(selectedStatus ? { status: selectedStatus } : {}),
        getCustomers(),
        getVehicles(),
        getEmployees(),
        getServiceTypes(),
      ])
      setAppointments(appointmentList)
      setCustomers(customerList)
      setVehicles(vehicleList)
      setEmployees(employeeList)
      setServiceTypes(serviceList)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar la agenda de citas.'))
    } finally {
      setLoading(false)
    }
  }, [statusFilter])

  useEffect(() => {
    let active = true
    Promise.all([
      getAppointments(),
      getCustomers(),
      getVehicles(),
      getEmployees(),
      getServiceTypes(),
    ])
      .then(([appointmentList, customerList, vehicleList, employeeList, serviceList]) => {
        if (!active) return
        setAppointments(appointmentList)
        setCustomers(customerList)
        setVehicles(vehicleList)
        setEmployees(employeeList)
        setServiceTypes(serviceList)
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar la agenda de citas.'))
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  const filteredVehicles = useMemo(() => {
    if (!form.customerId) return []
    return vehicles.filter((vehicle) => vehicle.customerId === form.customerId)
  }, [vehicles, form.customerId])

  const handleCustomerChange = (customerId) => {
    setForm((prev) => ({
      ...prev,
      customerId,
      vehicleId: '',
    }))
  }

  const handleCreate = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    if (!form.customerId || !form.vehicleId || !form.employeeId || !form.serviceTypeId || !form.dateTime) {
      setErrorMessage('Completa cliente, vehículo, empleado, servicio y fecha/hora para agendar la cita.')
      return
    }

    const parsedDate = new Date(form.dateTime)
    if (Number.isNaN(parsedDate.getTime())) {
      setErrorMessage('La fecha y hora ingresadas no son válidas.')
      return
    }

    setSubmitting(true)
    try {
      await createAppointment({
        customerId: form.customerId,
        vehicleId: form.vehicleId,
        employeeId: form.employeeId,
        serviceTypeId: form.serviceTypeId,
        dateTime: parsedDate.toISOString(),
        status: 'PENDING',
      })
      setForm(INITIAL_FORM)
      await loadData(statusFilter)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible crear la cita.'))
    } finally {
      setSubmitting(false)
    }
  }

  const applyAppointmentUpdate = (updated) => {
    setAppointments((prev) => {
      const mapped = prev.map((item) => (item.id === updated.id ? updated : item))
      return statusFilter ? mapped.filter((item) => item.status === statusFilter) : mapped
    })
  }

  const handleStatusChange = async (appointmentId, nextStatus) => {
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      const updated = await updateAppointmentStatus(appointmentId, nextStatus)
      applyAppointmentUpdate(updated)
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
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible iniciar el servicio.'))
    } finally {
      setUpdatingId(null)
    }
  }

  const handleVehicleStatusChange = async (appointmentId, nextVehicleStatus) => {
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      const updated = await updateVehicleStatus(appointmentId, nextVehicleStatus)
      applyAppointmentUpdate(updated)
    } catch (error) {
      setErrorMessage(
        getApiErrorMessage(error, 'No fue posible actualizar el estado del vehículo.'),
      )
    } finally {
      setUpdatingId(null)
    }
  }

  const handleDelete = async (appointmentId) => {
    if (!window.confirm('¿Deseas eliminar esta cita?')) return
    setErrorMessage('')
    setUpdatingId(appointmentId)
    try {
      await deleteAppointment(appointmentId)
      setAppointments((prev) => prev.filter((item) => item.id !== appointmentId))
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar la cita.'))
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-slate-900">Agenda de Citas</h2>
            <p className="mt-1 text-sm text-slate-500">
              Programa turnos de lavado/detailing y gestiona su estado operativo.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="statusFilter" className="text-xs font-medium text-slate-600">
              Filtrar estado:
            </label>
            <select
              id="statusFilter"
              value={statusFilter}
              onChange={(e) => {
                const nextStatus = e.target.value
                setStatusFilter(nextStatus)
                loadData(nextStatus)
              }}
              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-sky-500 focus:outline-none"
            >
              <option value="">Todos</option>
              <option value="PENDING">PENDING</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="FINISHED">FINISHED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
        </div>

        {errorMessage && (
          <div
            role="alert"
            className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
          >
            {errorMessage}
          </div>
        )}

        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
            <h3 className="text-base font-semibold text-slate-900">Nueva cita</h3>
            <span className="rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700 ring-1 ring-amber-200">
              Estado por defecto: PENDING
            </span>
          </div>

          <form onSubmit={handleCreate} className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <div className="space-y-1.5">
              <label htmlFor="customerId" className="block text-sm font-medium text-slate-700">
                Cliente
              </label>
              <select
                id="customerId"
                value={form.customerId}
                onChange={(e) => handleCustomerChange(e.target.value)}
                className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                required
              >
                <option value="">Selecciona un cliente...</option>
                {customers.map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.name} ({customer.phone})
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label htmlFor="vehicleId" className="block text-sm font-medium text-slate-700">
                Vehículo
              </label>
              <select
                id="vehicleId"
                value={form.vehicleId}
                onChange={(e) => setForm((prev) => ({ ...prev, vehicleId: e.target.value }))}
                disabled={!form.customerId}
                className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none disabled:bg-slate-100 disabled:text-slate-400"
                required
              >
                <option value="">
                  {!form.customerId
                    ? 'Primero elige un cliente'
                    : filteredVehicles.length === 0
                      ? 'Sin vehículos registrados para este cliente'
                      : 'Selecciona un vehículo...'}
                </option>
                {filteredVehicles.map((vehicle) => (
                  <option key={vehicle.id} value={vehicle.id}>
                    {vehicle.plate} — {vehicle.brand} {vehicle.model} ({vehicle.year})
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label htmlFor="serviceTypeId" className="block text-sm font-medium text-slate-700">
                Servicio
              </label>
              <select
                id="serviceTypeId"
                value={form.serviceTypeId}
                onChange={(e) => setForm((prev) => ({ ...prev, serviceTypeId: e.target.value }))}
                className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                required
              >
                <option value="">Selecciona un servicio...</option>
                {serviceTypes.map((service) => (
                  <option key={service.id} value={service.id}>
                    {service.name} (${Number(service.price).toLocaleString('es-CO')} · {service.durationMin} min)
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label htmlFor="employeeId" className="block text-sm font-medium text-slate-700">
                Empleado asignado
              </label>
              <select
                id="employeeId"
                value={form.employeeId}
                onChange={(e) => setForm((prev) => ({ ...prev, employeeId: e.target.value }))}
                className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                required
              >
                <option value="">Selecciona un empleado...</option>
                {employees.map((employee) => (
                  <option key={employee.id} value={employee.id}>
                    {employee.name} — {employee.role}
                  </option>
                ))}
              </select>
            </div>

            <Input
              id="dateTime"
              label="Fecha y hora"
              type="datetime-local"
              value={form.dateTime}
              onChange={(e) => setForm((prev) => ({ ...prev, dateTime: e.target.value }))}
              required
            />

            <div className="flex items-end">
              <Button type="submit" loading={submitting} className="w-full">
                Agendar cita
              </Button>
            </div>
          </form>
        </section>

        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
            <h3 className="text-base font-semibold text-slate-900">Listado de citas</h3>
            <Button variant="secondary" onClick={() => loadData(statusFilter)} disabled={loading}>
              Actualizar
            </Button>
          </div>

          {loading ? (
            <div className="p-8 text-center text-sm text-slate-500">Cargando agenda de citas...</div>
          ) : appointments.length === 0 ? (
            <div className="p-8 text-center text-sm text-slate-500">
              No hay citas registradas{statusFilter ? ` con estado ${statusFilter}` : ''}.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                <thead className="bg-slate-50 text-xs font-semibold tracking-wider text-slate-600 uppercase">
                  <tr>
                    <th className="px-4 py-3">Fecha y hora</th>
                    <th className="px-4 py-3">Cliente</th>
                    <th className="px-4 py-3">Vehículo</th>
                    <th className="px-4 py-3">Servicio</th>
                    <th className="px-4 py-3">Empleado</th>
                    <th className="px-4 py-3">Estado</th>
                    <th className="px-4 py-3">Vehículo (vivo)</th>
                    <th className="px-4 py-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {appointments.map((appointment) => {
                    const meta = STATUS_META[appointment.status] || {
                      label: appointment.status,
                      badgeClass: 'bg-slate-100 text-slate-700',
                    }
                    const vehicleMeta = getVehicleStatusMeta(appointment.vehicleStatus)
                    const isBusy = updatingId === appointment.id

                    return (
                      <tr key={appointment.id} className="hover:bg-slate-50/80">
                        <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                          {formatDateTime(appointment.dateTime)}
                        </td>
                        <td className="px-4 py-3 text-slate-700">{appointment.customerName || '—'}</td>
                        <td className="whitespace-nowrap px-4 py-3 font-mono text-xs text-slate-700">
                          {appointment.vehiclePlate || '—'}
                        </td>
                        <td className="px-4 py-3 text-slate-700">{appointment.serviceTypeName || '—'}</td>
                        <td className="px-4 py-3 text-slate-700">{appointment.employeeName || '—'}</td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${meta.badgeClass}`}>
                            {appointment.status}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3">
                          <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${vehicleMeta.badgeClass}`}>
                            {vehicleMeta.label}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-right">
                          <div className="inline-flex flex-wrap items-center justify-end gap-1.5">
                            {appointment.status === 'PENDING' && (
                              <>
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  onClick={() => handleStartService(appointment.id)}
                                  className="rounded-md bg-sky-600 px-2.5 py-1 text-xs font-semibold text-white transition hover:bg-sky-700 disabled:opacity-50"
                                >
                                  Iniciar Servicio
                                </button>
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  onClick={() => handleStatusChange(appointment.id, 'CANCELLED')}
                                  className="rounded-md bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 ring-1 ring-rose-200 transition hover:bg-rose-100 disabled:opacity-50"
                                >
                                  Cancelar
                                </button>
                              </>
                            )}
                            {appointment.status === 'IN_PROGRESS' && (
                              <>
                                {appointment.vehicleStatus !== 'READY' && (
                                  <button
                                    type="button"
                                    disabled={isBusy}
                                    onClick={() => handleVehicleStatusChange(appointment.id, 'READY')}
                                    className="rounded-md bg-amber-500 px-2.5 py-1 text-xs font-semibold text-white transition hover:bg-amber-600 disabled:opacity-50"
                                  >
                                    Marcar como Listo
                                  </button>
                                )}
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  onClick={() => handleStatusChange(appointment.id, 'FINISHED')}
                                  className="rounded-md bg-emerald-600 px-2.5 py-1 text-xs font-semibold text-white transition hover:bg-emerald-700 disabled:opacity-50"
                                >
                                  Finalizar
                                </button>
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  onClick={() => handleStatusChange(appointment.id, 'CANCELLED')}
                                  className="rounded-md bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 ring-1 ring-rose-200 transition hover:bg-rose-100 disabled:opacity-50"
                                >
                                  Cancelar
                                </button>
                              </>
                            )}
                            {appointment.status === 'FINISHED' &&
                              appointment.vehicleStatus !== 'DELIVERED' && (
                                <button
                                  type="button"
                                  disabled={isBusy}
                                  onClick={() => handleVehicleStatusChange(appointment.id, 'DELIVERED')}
                                  className="rounded-md bg-teal-600 px-2.5 py-1 text-xs font-semibold text-white transition hover:bg-teal-700 disabled:opacity-50"
                                >
                                  Entregar
                                </button>
                              )}
                            <button
                              type="button"
                              disabled={isBusy}
                              onClick={() => handleDelete(appointment.id)}
                              className="rounded-md px-2.5 py-1 text-xs font-medium text-slate-500 hover:bg-slate-100 hover:text-rose-600 disabled:opacity-50"
                            >
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </MainLayout>
  )
}
