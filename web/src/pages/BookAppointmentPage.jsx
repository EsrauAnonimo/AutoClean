import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button } from '../components/Button'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { createAppointment } from '../services/appointmentService'
import { getEmployees, getServiceTypes } from '../services/catalogService'
import { getVehiclesByCustomer } from '../services/vehicleService'

function getDefaultDateTimeLocal() {
  const nextHour = new Date(Date.now() + 60 * 60 * 1000)
  nextHour.setMinutes(0, 0, 0)
  const year = nextHour.getFullYear()
  const month = String(nextHour.getMonth() + 1).padStart(2, '0')
  const day = String(nextHour.getDate()).padStart(2, '0')
  const hours = String(nextHour.getHours()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:00`
}

function formatPrice(price) {
  const numeric = Number(price)
  if (Number.isNaN(numeric)) return '$0'
  return `$${numeric.toLocaleString('es-CO')}`
}

/**
 * Pantalla "Agendar Cita" del Portal de Clientes con flujo guiado por tarjetas
 * y resumen en tiempo real antes de confirmar.
 */
export function BookAppointmentPage() {
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const customerId = user?.customerId ?? null
  const preselectedVehicleId = searchParams.get('vehicleId') || ''

  const [vehicles, setVehicles] = useState([])
  const [services, setServices] = useState([])
  const [employees, setEmployees] = useState([])

  const [vehicleId, setVehicleId] = useState(preselectedVehicleId)
  const [serviceTypeId, setServiceTypeId] = useState('')
  const [employeeId, setEmployeeId] = useState('')
  const [dateTime, setDateTime] = useState(() => getDefaultDateTimeLocal())

  const [loading, setLoading] = useState(Boolean(customerId))
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [createdAppointment, setCreatedAppointment] = useState(null)

  useEffect(() => {
    if (!customerId) return
    let active = true

    Promise.all([getVehiclesByCustomer(customerId), getServiceTypes(), getEmployees()])
      .then(([vehicleList, serviceList, employeeList]) => {
        if (!active) return
        const safeVehicles = Array.isArray(vehicleList) ? vehicleList : []
        const safeServices = Array.isArray(serviceList) ? serviceList : []
        const safeEmployees = Array.isArray(employeeList) ? employeeList : []

        setVehicles(safeVehicles)
        setServices(safeServices)
        setEmployees(safeEmployees)

        if (preselectedVehicleId && safeVehicles.some((v) => v.id === preselectedVehicleId)) {
          setVehicleId(preselectedVehicleId)
        } else if (safeVehicles.length === 1) {
          setVehicleId(safeVehicles[0].id)
        }
        if (safeServices.length === 1) {
          setServiceTypeId(safeServices[0].id)
        }
        if (safeEmployees.length === 1) {
          setEmployeeId(safeEmployees[0].id)
        }
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar las opciones para agendar.'))
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [customerId, preselectedVehicleId])

  const selectedVehicle = useMemo(
    () => vehicles.find((item) => item.id === vehicleId) || null,
    [vehicles, vehicleId],
  )
  const selectedService = useMemo(
    () => services.find((item) => item.id === serviceTypeId) || null,
    [services, serviceTypeId],
  )
  const selectedEmployee = useMemo(
    () => employees.find((item) => item.id === employeeId) || null,
    [employees, employeeId],
  )

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    if (!customerId || !vehicleId || !serviceTypeId || !employeeId || !dateTime) {
      setErrorMessage('Selecciona vehículo, servicio, especialista y fecha/hora para confirmar tu cita.')
      return
    }

    const parsedDate = new Date(dateTime)
    if (Number.isNaN(parsedDate.getTime())) {
      setErrorMessage('La fecha y hora seleccionadas no son válidas.')
      return
    }

    setSubmitting(true)
    try {
      const created = await createAppointment({
        customerId,
        vehicleId,
        employeeId,
        serviceTypeId,
        dateTime: parsedDate.toISOString(),
        status: 'PENDING',
      })
      setCreatedAppointment(created)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible agendar la cita.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h2 className="text-xl font-bold text-slate-900">Agendar Cita</h2>
            <p className="mt-1 text-sm text-slate-600">
              Reserva tu servicio de lavado o detailing en tres pasos rápidos.
            </p>
          </div>
          <div className="flex flex-wrap gap-2.5">
            <Link
              to="/portal/mis-vehiculos"
              className="inline-flex items-center justify-center rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 transition-colors hover:bg-slate-50"
            >
              Mis Vehículos
            </Link>
            <Link
              to="/portal/historial"
              className="inline-flex items-center justify-center rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 transition-colors hover:bg-slate-50"
            >
              Ver historial
            </Link>
          </div>
        </section>

        {!customerId ? (
          <CompleteCustomerProfileCard />
        ) : createdAppointment ? (
          <section className="rounded-2xl border border-emerald-200 bg-emerald-50/70 p-8 text-center shadow-sm">
            <span className="inline-flex rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-800">
              Reserva confirmada ({createdAppointment.status})
            </span>
            <h3 className="mt-3 text-xl font-bold text-slate-900">
              ¡Tu cita ha sido agendada!
            </h3>
            <p className="mt-1 text-sm text-slate-600">
              Servicio <span className="font-semibold text-slate-900">{createdAppointment.serviceTypeName}</span> para{' '}
              <span className="font-mono font-semibold text-slate-900">{createdAppointment.vehiclePlate}</span> con{' '}
              <span className="font-semibold text-slate-900">{createdAppointment.employeeName}</span>.
            </p>
            <div className="mt-6 flex flex-wrap items-center justify-center gap-3">
              <Link
                to="/portal/historial"
                className="inline-flex items-center justify-center rounded-lg bg-sky-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
              >
                Ir a mi historial
              </Link>
              <Button variant="secondary" onClick={() => setCreatedAppointment(null)}>
                Agendar otra cita
              </Button>
            </div>
          </section>
        ) : loading ? (
          <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-xs">
            Cargando vehículos, catálogo de servicios y especialistas...
          </div>
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

            {vehicles.length === 0 ? (
              <section className="rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center shadow-xs">
                <h3 className="text-base font-semibold text-slate-900">
                  Primero registra al menos un vehículo
                </h3>
                <p className="mt-1 text-sm text-slate-500">
                  Para agendar una cita necesitamos saber a qué vehículo se le realizará el servicio.
                </p>
                <Link
                  to="/portal/mis-vehiculos"
                  className="mt-5 inline-flex items-center justify-center rounded-lg bg-sky-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
                >
                  + Registrar vehículo ahora
                </Link>
              </section>
            ) : (
              <form onSubmit={handleSubmit} className="grid gap-6 lg:grid-cols-3">
                <div className="space-y-6 lg:col-span-2">
                  <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="mb-4 flex items-center justify-between gap-2">
                      <h3 className="text-base font-bold text-slate-900">1. Elige tu vehículo</h3>
                      <Link
                        to="/portal/mis-vehiculos"
                        className="text-xs font-semibold text-sky-600 hover:text-sky-700"
                      >
                        + Gestionar vehículos
                      </Link>
                    </div>
                    <div className="grid gap-3 sm:grid-cols-2">
                      {vehicles.map((vehicle) => {
                        const selected = vehicle.id === vehicleId
                        return (
                          <button
                            key={vehicle.id}
                            type="button"
                            onClick={() => setVehicleId(vehicle.id)}
                            className={`rounded-xl border p-4 text-left transition ${
                              selected
                                ? 'border-sky-600 bg-sky-50/70 ring-2 ring-sky-500/20'
                                : 'border-slate-200 bg-white hover:border-sky-300'
                            }`}
                          >
                            <div className="flex items-center justify-between">
                              <span className="rounded-md bg-slate-900 px-2 py-0.5 font-mono text-xs font-bold text-white">
                                {vehicle.plate}
                              </span>
                              <span className="text-xs font-medium text-slate-500">{vehicle.year}</span>
                            </div>
                            <p className="mt-2 font-semibold text-slate-900">
                              {vehicle.brand} {vehicle.model}
                            </p>
                          </button>
                        )
                      })}
                    </div>
                  </section>

                  <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="mb-4 text-base font-bold text-slate-900">2. Elige el servicio</h3>
                    {services.length === 0 ? (
                      <p className="text-sm text-slate-500">
                        No hay servicios disponibles en este momento.
                      </p>
                    ) : (
                      <div className="grid gap-3 sm:grid-cols-2">
                        {services.map((service) => {
                          const selected = service.id === serviceTypeId
                          return (
                            <button
                              key={service.id}
                              type="button"
                              onClick={() => setServiceTypeId(service.id)}
                              className={`flex flex-col justify-between rounded-xl border p-4 text-left transition ${
                                selected
                                  ? 'border-sky-600 bg-sky-50/70 ring-2 ring-sky-500/20'
                                  : 'border-slate-200 bg-white hover:border-sky-300'
                              }`}
                            >
                              <div>
                                <div className="flex items-start justify-between gap-2">
                                  <span className="font-semibold text-slate-900">{service.name}</span>
                                  <span className="shrink-0 font-bold text-sky-700">
                                    {formatPrice(service.price)}
                                  </span>
                                </div>
                                {service.description && (
                                  <p className="mt-1 text-xs text-slate-500">{service.description}</p>
                                )}
                              </div>
                              <span className="mt-3 inline-block text-xs font-medium text-slate-500">
                                Duración estimada: {service.durationMin} min
                              </span>
                            </button>
                          )
                        })}
                      </div>
                    )}
                  </section>

                  <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="mb-4 text-base font-bold text-slate-900">
                      3. Especialista y fecha / hora
                    </h3>
                    <div className="grid gap-4 sm:grid-cols-2">
                      <div className="space-y-1.5">
                        <label htmlFor="portalEmployeeId" className="block text-sm font-medium text-slate-700">
                          Especialista
                        </label>
                        <select
                          id="portalEmployeeId"
                          value={employeeId}
                          onChange={(e) => setEmployeeId(e.target.value)}
                          className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                          required
                        >
                          <option value="">Selecciona un especialista...</option>
                          {employees.map((employee) => (
                            <option key={employee.id} value={employee.id}>
                              {employee.name} — {employee.role}
                            </option>
                          ))}
                        </select>
                      </div>

                      <Input
                        id="portalDateTime"
                        label="Fecha y hora deseada"
                        type="datetime-local"
                        value={dateTime}
                        onChange={(e) => setDateTime(e.target.value)}
                        required
                      />
                    </div>
                  </section>
                </div>

                <aside className="lg:col-span-1">
                  <div className="sticky top-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="text-base font-bold text-slate-900">Resumen de tu cita</h3>
                    <dl className="mt-4 space-y-3 text-sm">
                      <div className="flex justify-between gap-2 border-b border-slate-100 pb-2">
                        <dt className="text-slate-500">Vehículo</dt>
                        <dd className="text-right font-medium text-slate-900">
                          {selectedVehicle
                            ? `${selectedVehicle.plate} (${selectedVehicle.brand})`
                            : 'Por seleccionar'}
                        </dd>
                      </div>
                      <div className="flex justify-between gap-2 border-b border-slate-100 pb-2">
                        <dt className="text-slate-500">Servicio</dt>
                        <dd className="text-right font-medium text-slate-900">
                          {selectedService ? selectedService.name : 'Por seleccionar'}
                        </dd>
                      </div>
                      <div className="flex justify-between gap-2 border-b border-slate-100 pb-2">
                        <dt className="text-slate-500">Especialista</dt>
                        <dd className="text-right font-medium text-slate-900">
                          {selectedEmployee ? selectedEmployee.name : 'Por seleccionar'}
                        </dd>
                      </div>
                      <div className="flex justify-between gap-2 pt-1">
                        <dt className="font-semibold text-slate-700">Total estimado</dt>
                        <dd className="text-lg font-bold text-sky-700">
                          {selectedService ? formatPrice(selectedService.price) : '$0'}
                        </dd>
                      </div>
                    </dl>

                    <div className="mt-6 space-y-2.5">
                      <Button type="submit" loading={submitting} className="w-full">
                        Confirmar y agendar cita
                      </Button>
                      <Link
                        to="/portal/dashboard"
                        className="inline-flex w-full items-center justify-center rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 ring-1 ring-inset ring-slate-300 transition-colors hover:bg-slate-50"
                      >
                        Volver al inicio
                      </Link>
                    </div>
                  </div>
                </aside>
              </form>
            )}
          </>
        )}
      </div>
    </MainLayout>
  )
}

