import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { createAppointment } from '../services/appointmentService'
import { getEmployees, getServiceTypes } from '../services/catalogService'
import { getVehiclesByCustomer } from '../services/vehicleService'

const INITIAL_FORM = {
  vehicleId: '',
  serviceTypeId: '',
  employeeId: '',
  dateTime: '',
}

function getLocalDateTimeMin() {
  const now = new Date()
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 16)
}

function formatPrice(value) {
  const numeric = Number(value)
  if (Number.isNaN(numeric)) return '$0'
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(numeric)
}

function formatAppointmentDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

/**
 * Pantalla "Agendar Cita" del Portal de Clientes (/portal/agendar).
 * Aplica la regla Opción A: cada cita vincula 1 vehículo, 1 tipo de servicio, 1 empleado y fecha/hora.
 */
export function BookAppointmentPage() {
  const { user } = useAuth()
  const customerId = user?.customerId ?? null

  const [serviceTypes, setServiceTypes] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [employees, setEmployees] = useState([])

  const [form, setForm] = useState(INITIAL_FORM)
  const [minDateTime] = useState(() => getLocalDateTimeMin())
  const [loading, setLoading] = useState(Boolean(customerId))
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [createdAppointment, setCreatedAppointment] = useState(null)

  useEffect(() => {
    if (!customerId) {
      return
    }
    let active = true

    Promise.all([
      getServiceTypes(),
      getVehiclesByCustomer(customerId),
      getEmployees(),
    ])
      .then(([serviceList, vehicleList, employeeList]) => {
        if (!active) return
        setServiceTypes(Array.isArray(serviceList) ? serviceList : [])
        setVehicles(Array.isArray(vehicleList) ? vehicleList : [])
        setEmployees(Array.isArray(employeeList) ? employeeList : [])
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(
            getApiErrorMessage(error, 'No fue posible cargar la información para agendar tu cita.'),
          )
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [customerId])

  const selectedService = useMemo(
    () => serviceTypes.find((service) => service.id === form.serviceTypeId) || null,
    [serviceTypes, form.serviceTypeId],
  )

  const handleFieldChange = (field) => (event) => {
    const value = event.target.value
    setForm((prev) => ({ ...prev, [field]: value }))
    if (errorMessage) {
      setErrorMessage('')
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!customerId) {
      return
    }

    setErrorMessage('')
    setCreatedAppointment(null)

    if (!form.vehicleId || !form.serviceTypeId || !form.employeeId || !form.dateTime) {
      setErrorMessage('Selecciona vehículo, servicio, especialista y fecha/hora para confirmar la cita.')
      return
    }

    const parsedDate = new Date(form.dateTime)
    if (Number.isNaN(parsedDate.getTime())) {
      setErrorMessage('La fecha y hora seleccionadas no son válidas.')
      return
    }

    setSubmitting(true)
    try {
      const response = await createAppointment({
        customerId,
        vehicleId: form.vehicleId,
        employeeId: form.employeeId,
        serviceTypeId: form.serviceTypeId,
        dateTime: parsedDate.toISOString(),
      })
      setCreatedAppointment(response)
      setForm(INITIAL_FORM)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible agendar tu cita.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-xl font-bold text-slate-900">Agendar Cita</h2>
          <p className="mt-1 text-sm text-slate-600">
            Elige tu vehículo, el servicio de detailing, el especialista disponible y el horario que prefieras.
          </p>
        </section>

        {!customerId ? (
          <div
            role="alert"
            className="rounded-2xl border border-amber-200 bg-amber-50 p-6 text-sm text-amber-800 shadow-xs"
          >
            <p className="font-semibold">Perfil de cliente no vinculado</p>
            <p className="mt-1">
              Tu sesión no tiene un identificador de cliente asignado (<code>customerId</code>).
              Regístrate con nombre y teléfono o comunícate con AutoClean para habilitar tus reservas.
            </p>
          </div>
        ) : loading ? (
          <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-xs">
            Cargando catálogo de servicios, especialistas y tus vehículos...
          </div>
        ) : vehicles.length === 0 ? (
          <section className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center shadow-xs">
            <h3 className="text-base font-semibold text-slate-900">
              Primero registra un vehículo
            </h3>
            <p className="mt-1 text-sm text-slate-500">
              Para agendar una cita debes tener al menos un vehículo asociado a tu cuenta.
            </p>
            <div className="mt-5">
              <Link
                to="/portal/mis-vehiculos"
                className="inline-flex items-center justify-center rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-sky-700"
              >
                Ir a Mis Vehículos
              </Link>
            </div>
          </section>
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

            {createdAppointment && (
              <section
                aria-live="polite"
                className="rounded-2xl border border-emerald-200 bg-emerald-50 p-6 shadow-xs"
              >
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <span className="inline-flex rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-800">
                      Cita confirmada ({createdAppointment.status || 'PENDING'})
                    </span>
                    <h3 className="mt-2 text-lg font-bold text-emerald-950">
                      Tu turno fue agendado exitosamente
                    </h3>
                    <dl className="mt-3 grid gap-2 text-sm text-emerald-900 sm:grid-cols-2">
                      <div>
                        <dt className="text-xs font-medium text-emerald-700">Servicio</dt>
                        <dd className="font-semibold">{createdAppointment.serviceTypeName}</dd>
                      </div>
                      <div>
                        <dt className="text-xs font-medium text-emerald-700">Vehículo</dt>
                        <dd className="font-semibold">{createdAppointment.vehiclePlate}</dd>
                      </div>
                      <div>
                        <dt className="text-xs font-medium text-emerald-700">Especialista</dt>
                        <dd className="font-semibold">{createdAppointment.employeeName}</dd>
                      </div>
                      <div>
                        <dt className="text-xs font-medium text-emerald-700">Fecha y hora</dt>
                        <dd className="font-semibold">
                          {formatAppointmentDateTime(createdAppointment.dateTime)}
                        </dd>
                      </div>
                    </dl>
                  </div>

                  <div className="flex flex-wrap gap-3">
                    <Link
                      to="/portal/historial"
                      className="inline-flex items-center justify-center rounded-lg bg-emerald-700 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-emerald-800"
                    >
                      Ver historial de citas
                    </Link>
                  </div>
                </div>
              </section>
            )}

            {(serviceTypes.length === 0 || employees.length === 0) && (
              <div
                role="alert"
                className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
              >
                {serviceTypes.length === 0
                  ? 'Actualmente no hay tipos de servicio disponibles en el catálogo.'
                  : 'Actualmente no hay especialistas disponibles para asignar la cita.'}
              </div>
            )}

            <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <form onSubmit={handleSubmit} className="space-y-5" noValidate>
                <div className="grid gap-5 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <label htmlFor="appointment-vehicle" className="block text-sm font-medium text-slate-700">
                      Vehículo
                    </label>
                    <select
                      id="appointment-vehicle"
                      value={form.vehicleId}
                      onChange={handleFieldChange('vehicleId')}
                      className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                      required
                    >
                      <option value="">Selecciona un vehículo...</option>
                      {vehicles.map((vehicle) => (
                        <option key={vehicle.id} value={vehicle.id}>
                          {vehicle.plate} - {vehicle.brand} {vehicle.model} ({vehicle.year})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-1.5">
                    <label htmlFor="appointment-service" className="block text-sm font-medium text-slate-700">
                      Tipo de servicio
                    </label>
                    <select
                      id="appointment-service"
                      value={form.serviceTypeId}
                      onChange={handleFieldChange('serviceTypeId')}
                      className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                      required
                    >
                      <option value="">Selecciona un servicio...</option>
                      {serviceTypes.map((service) => (
                        <option key={service.id} value={service.id}>
                          {service.name} ({formatPrice(service.price)} · {service.durationMin} min)
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-1.5">
                    <label htmlFor="appointment-employee" className="block text-sm font-medium text-slate-700">
                      Especialista
                    </label>
                    <select
                      id="appointment-employee"
                      value={form.employeeId}
                      onChange={handleFieldChange('employeeId')}
                      className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                      required
                    >
                      <option value="">Selecciona un especialista...</option>
                      {employees.map((employee) => (
                        <option key={employee.id} value={employee.id}>
                          {employee.name} ({employee.role})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-1.5">
                    <label htmlFor="appointment-datetime" className="block text-sm font-medium text-slate-700">
                      Fecha y hora
                    </label>
                    <input
                      id="appointment-datetime"
                      type="datetime-local"
                      min={minDateTime}
                      value={form.dateTime}
                      onChange={handleFieldChange('dateTime')}
                      className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none"
                      required
                    />
                  </div>
                </div>

                {selectedService && (
                  <div className="rounded-xl border border-sky-100 bg-sky-50/70 p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <p className="text-xs font-semibold tracking-wide text-sky-700 uppercase">
                          Resumen del servicio seleccionado
                        </p>
                        <h3 className="mt-0.5 text-base font-bold text-slate-900">
                          {selectedService.name}
                        </h3>
                        {selectedService.description && (
                          <p className="mt-1 text-sm text-slate-600">{selectedService.description}</p>
                        )}
                      </div>
                      <div className="text-right">
                        <p className="text-lg font-bold text-sky-700">
                          {formatPrice(selectedService.price)}
                        </p>
                        <p className="text-xs font-medium text-slate-600">
                          Duración estimada: {selectedService.durationMin} min
                        </p>
                      </div>
                    </div>
                  </div>
                )}

                <div className="flex justify-end pt-2">
                  <Button
                    type="submit"
                    loading={submitting}
                    disabled={serviceTypes.length === 0 || employees.length === 0}
                  >
                    Confirmar cita
                  </Button>
                </div>
              </form>
            </section>
          </>
        )}
      </div>
    </MainLayout>
  )
}
