import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { createAppointment } from '../services/appointmentService'
import { getEmployees, getServiceTypes } from '../services/catalogService'
import { getVehicleByPlate } from '../services/vehicleService'

const INITIAL_FORM = {
  plate: '',
  serviceTypeId: '',
  employeeId: '',
  dateTime: '',
}

const MORNING_START_MINUTES = 7 * 60
const MORNING_END_MINUTES = 12 * 60
const AFTERNOON_START_MINUTES = 14 * 60 + 30
const AFTERNOON_END_MINUTES = 21 * 60

function getLocalDateTimeMin() {
  const now = new Date()
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 16)
}

function validateAppointmentDateTime(value) {
  if (!value) {
    return 'Selecciona la fecha y hora de tu cita.'
  }

  const parsedDate = new Date(value)
  if (Number.isNaN(parsedDate.getTime())) {
    return 'La fecha y hora seleccionadas no son válidas.'
  }

  if (parsedDate.getTime() < Date.now()) {
    return 'No puedes agendar una cita en una fecha u hora pasada.'
  }

  const totalMinutes = parsedDate.getHours() * 60 + parsedDate.getMinutes()
  const inMorningShift =
    totalMinutes >= MORNING_START_MINUTES && totalMinutes <= MORNING_END_MINUTES
  const inAfternoonShift =
    totalMinutes >= AFTERNOON_START_MINUTES && totalMinutes <= AFTERNOON_END_MINUTES

  if (!inMorningShift && !inAfternoonShift) {
    return 'Horario fuera de atención. Solo atendemos de 7:00 a. m. a 12:00 p. m. y de 2:30 p. m. a 9:00 p. m.'
  }

  return ''
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
 * Busca el vehículo por placa, aplica franjas horarias estrictas e informa pago en efectivo.
 */
export function BookAppointmentPage() {
  const { user } = useAuth()
  const customerId = user?.customerId ?? null

  const [serviceTypes, setServiceTypes] = useState([])
  const [employees, setEmployees] = useState([])

  const [form, setForm] = useState(INITIAL_FORM)
  const [resolvedVehicle, setResolvedVehicle] = useState(null)
  const [searchingVehicle, setSearchingVehicle] = useState(false)
  const [plateError, setPlateError] = useState('')
  const [timeError, setTimeError] = useState('')

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

    Promise.all([getServiceTypes(), getEmployees()])
      .then(([serviceList, employeeList]) => {
        if (!active) return
        setServiceTypes(Array.isArray(serviceList) ? serviceList : [])
        setEmployees(Array.isArray(employeeList) ? employeeList : [])
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(
            getApiErrorMessage(error, 'No fue posible cargar el catálogo para agendar tu cita.'),
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

  const handlePlateChange = (event) => {
    const nextPlate = event.target.value.toUpperCase()
    setForm((prev) => ({ ...prev, plate: nextPlate }))
    setPlateError('')
    setErrorMessage('')
    if (resolvedVehicle && resolvedVehicle.plate !== nextPlate.trim()) {
      setResolvedVehicle(null)
    }
  }

  const handleDateTimeChange = (event) => {
    const nextValue = event.target.value
    setForm((prev) => ({ ...prev, dateTime: nextValue }))
    setErrorMessage('')
    setTimeError(nextValue ? validateAppointmentDateTime(nextValue) : '')
  }

  const handleFieldChange = (field) => (event) => {
    const value = event.target.value
    setForm((prev) => ({ ...prev, [field]: value }))
    if (errorMessage) {
      setErrorMessage('')
    }
  }

  const lookupVehicleByPlate = async (rawPlate) => {
    const normalizedPlate = rawPlate.trim().toUpperCase()
    if (!normalizedPlate) {
      setPlateError('Ingresa la placa del vehículo.')
      return null
    }

    setSearchingVehicle(true)
    setPlateError('')
    try {
      const vehicle = await getVehicleByPlate(normalizedPlate)
      setResolvedVehicle(vehicle)
      return vehicle
    } catch (error) {
      setResolvedVehicle(null)
      setPlateError(
        getApiErrorMessage(
          error,
          `No encontramos un vehículo registrado con la placa ${normalizedPlate}.`,
        ),
      )
      return null
    } finally {
      setSearchingVehicle(false)
    }
  }

  const handleSearchPlateClick = async () => {
    await lookupVehicleByPlate(form.plate)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!customerId) {
      return
    }

    setErrorMessage('')
    setCreatedAppointment(null)

    const normalizedPlate = form.plate.trim().toUpperCase()
    if (!normalizedPlate) {
      setPlateError('Ingresa la placa de tu vehículo.')
      return
    }

    const scheduleValidationError = validateAppointmentDateTime(form.dateTime)
    if (scheduleValidationError) {
      setTimeError(scheduleValidationError)
      setErrorMessage(scheduleValidationError)
      return
    }

    if (!form.serviceTypeId || !form.employeeId) {
      setErrorMessage('Selecciona el tipo de servicio y el especialista para confirmar la cita.')
      return
    }

    const parsedDate = new Date(form.dateTime)
    setSubmitting(true)
    try {
      let vehicle = resolvedVehicle
      if (!vehicle || vehicle.plate?.toUpperCase() !== normalizedPlate) {
        vehicle = await lookupVehicleByPlate(normalizedPlate)
        if (!vehicle?.id) {
          return
        }
      }

      const response = await createAppointment({
        customerId,
        vehicleId: vehicle.id,
        employeeId: form.employeeId,
        serviceTypeId: form.serviceTypeId,
        dateTime: parsedDate.toISOString(),
      })

      setCreatedAppointment(response)
      setForm(INITIAL_FORM)
      setResolvedVehicle(null)
      setTimeError('')
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
            Ingresa la placa de tu vehículo, elige el servicio de detailing, tu especialista y un horario dentro de nuestras franjas de atención.
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
            Cargando catálogo de servicios y especialistas...
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
                    <p className="mt-3 text-xs font-semibold text-emerald-800">
                      Modalidad: Pago en efectivo al finalizar el servicio.
                    </p>
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
                <div className="space-y-2">
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
                    <div className="flex-1">
                      <Input
                        id="appointment-plate"
                        label="Placa del vehículo"
                        type="text"
                        placeholder="Ej: ABC123"
                        value={form.plate}
                        onChange={handlePlateChange}
                        error={plateError}
                        className="uppercase"
                        required
                      />
                    </div>
                    <Button
                      type="button"
                      variant="secondary"
                      loading={searchingVehicle}
                      onClick={handleSearchPlateClick}
                      className="sm:mb-0.5"
                    >
                      Buscar placa
                    </Button>
                  </div>

                  {resolvedVehicle ? (
                    <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-3.5 py-2.5 text-xs text-emerald-900">
                      Vehículo verificado:{' '}
                      <span className="font-bold">{resolvedVehicle.plate}</span> ·{' '}
                      {resolvedVehicle.brand} {resolvedVehicle.model} ({resolvedVehicle.year})
                    </div>
                  ) : (
                    <p className="text-xs text-slate-500">
                      Ingresa la placa registrada de tu vehículo.{' '}
                      <Link to="/portal/mis-vehiculos" className="font-semibold text-sky-600 hover:text-sky-700">
                        ¿Aún no lo registras? Hazlo en Mis Vehículos
                      </Link>
                      .
                    </p>
                  )}
                </div>

                <div className="grid gap-5 sm:grid-cols-2">
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

                  <div className="space-y-1.5 sm:col-span-2">
                    <label htmlFor="appointment-datetime" className="block text-sm font-medium text-slate-700">
                      Fecha y hora
                    </label>
                    <input
                      id="appointment-datetime"
                      type="datetime-local"
                      min={minDateTime}
                      value={form.dateTime}
                      onChange={handleDateTimeChange}
                      className="block w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-500/20 focus:outline-none sm:max-w-sm"
                      required
                    />
                    <p className="text-xs text-slate-500">
                      Horarios permitidos: de 7:00 a. m. a 12:00 p. m. y de 2:30 p. m. a 9:00 p. m.
                    </p>
                    {timeError && <p className="text-xs font-medium text-rose-600">{timeError}</p>}
                  </div>
                </div>

                <div className="rounded-xl border border-sky-100 bg-sky-50/70 p-4 space-y-3">
                  {selectedService ? (
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
                  ) : (
                    <p className="text-xs text-slate-600">
                      Selecciona un servicio para ver el resumen de tarifa y duración estimada.
                    </p>
                  )}

                  <div className="border-t border-sky-200/70 pt-2.5 text-sm font-semibold text-slate-800">
                    Pago en efectivo al finalizar el servicio
                  </div>
                </div>

                <div className="flex justify-end pt-2">
                  <Button
                    type="submit"
                    loading={submitting}
                    disabled={serviceTypes.length === 0 || employees.length === 0 || Boolean(timeError)}
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
