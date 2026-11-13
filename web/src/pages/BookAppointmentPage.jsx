import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button } from '../components/Button'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
import { MainLayout } from '../components/MainLayout'
import { PlateInput } from '../components/PlateInput'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import { createAppointment, getOccupiedAppointmentSlots } from '../services/appointmentService'
import { getEmployees, getServiceTypes } from '../services/catalogService'
import { getVehicleByPlate, getVehiclesByCustomer } from '../services/vehicleService'
import { showToast } from '../utils/toast'

const STEPS = [
  { id: 1, label: 'Placa' },
  { id: 2, label: 'Servicio' },
  { id: 3, label: 'Fecha y hora' },
  { id: 4, label: 'Confirmar' },
]

const TIME_SLOTS = [
  { time: '06:00', label: '6:00 a. m.', period: 'Cerrado', available: false, reason: 'Fuera de horario' },
  { time: '07:00', label: '7:00 a. m.', period: 'Mañana', available: true },
  { time: '08:00', label: '8:00 a. m.', period: 'Mañana', available: true },
  { time: '09:00', label: '9:00 a. m.', period: 'Mañana', available: true },
  { time: '10:00', label: '10:00 a. m.', period: 'Mañana', available: true },
  { time: '11:00', label: '11:00 a. m.', period: 'Mañana', available: true },
  { time: '12:00', label: '12:00 p. m.', period: 'Mañana', available: true },
  { time: '12:30', label: '12:30 p. m.', period: 'Receso', available: false, reason: 'Horario cerrado' },
  { time: '13:30', label: '1:30 p. m.', period: 'Receso', available: false, reason: 'Horario cerrado' },
  { time: '14:30', label: '2:30 p. m.', period: 'Tarde', available: true },
  { time: '15:30', label: '3:30 p. m.', period: 'Tarde', available: true },
  { time: '16:30', label: '4:30 p. m.', period: 'Tarde', available: true },
  { time: '17:30', label: '5:30 p. m.', period: 'Tarde', available: true },
  { time: '18:30', label: '6:30 p. m.', period: 'Tarde', available: true },
  { time: '19:30', label: '7:30 p. m.', period: 'Noche', available: true },
  { time: '20:30', label: '8:30 p. m.', period: 'Noche', available: true },
  { time: '21:00', label: '9:00 p. m.', period: 'Noche', available: true },
  { time: '21:30', label: '9:30 p. m.', period: 'Cerrado', available: false, reason: 'Fuera de horario' },
]

function getTodayIsoDate() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function isWithinOperatingHours(dateObj) {
  const mins = dateObj.getHours() * 60 + dateObj.getMinutes()
  return (mins >= 420 && mins <= 720) || (mins >= 870 && mins <= 1260)
}

function formatCop(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0)
}

function formatReadableDateTime(dateStr, timeStr) {
  if (!dateStr || !timeStr) {
    return '—'
  }
  const dateObj = new Date(`${dateStr}T${timeStr}:00`)
  if (Number.isNaN(dateObj.getTime())) {
    return `${dateStr} ${timeStr}`
  }
  return new Intl.DateTimeFormat('es-CO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  }).format(dateObj)
}

/**
 * Portal del Cliente: Agendar cita en 4 pasos guiados (Placa -> Servicio -> Fecha/Hora -> Confirmar).
 */
export function BookAppointmentPage() {
  const { user } = useAuth()
  const customerId = user?.customerId
  const [searchParams] = useSearchParams()
  const initialPlate = (searchParams.get('plate') || '').toUpperCase()
  const todayIso = useMemo(() => getTodayIsoDate(), [])
  const [renderTimestamp] = useState(() => Date.now())

  const [step, setStep] = useState(1)
  const [plate, setPlate] = useState(initialPlate)
  const [resolvedVehicle, setResolvedVehicle] = useState(null)
  const [plateError, setPlateError] = useState('')
  const [searchingVehicle, setSearchingVehicle] = useState(false)

  const [serviceTypes, setServiceTypes] = useState([])
  const [employees, setEmployees] = useState([])
  const [myVehicles, setMyVehicles] = useState([])
  const [catalogLoading, setCatalogLoading] = useState(Boolean(customerId))

  const [selectedServiceId, setSelectedServiceId] = useState('')
  const [selectedDate, setSelectedDate] = useState(todayIso)
  const [selectedTime, setSelectedTime] = useState('')
  const [selectedEmployeeId, setSelectedEmployeeId] = useState('')
  const [occupiedSlots, setOccupiedSlots] = useState([])
  const [loadingAvailability, setLoadingAvailability] = useState(false)

  const [stepError, setStepError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [createdAppointment, setCreatedAppointment] = useState(null)

  const lookupVehicleByPlate = useCallback(
    async (rawPlate) => {
      const cleaned = rawPlate.trim().toUpperCase()
      if (!cleaned) {
        setPlateError('Ingresa la placa de tu vehículo para continuar.')
        return null
      }
      setSearchingVehicle(true)
      setPlateError('')
      try {
        const found = await getVehicleByPlate(cleaned)
        if (found?.customerId && customerId && found.customerId !== customerId) {
          setResolvedVehicle(null)
          setPlateError('Esta placa pertenece a otro cliente. Registra tu vehículo en Mis Vehículos.')
          return null
        }
        setResolvedVehicle(found)
        return found
      } catch (error) {
        setResolvedVehicle(null)
        setPlateError(
          getApiErrorMessage(
            error,
            `No encontramos un vehículo registrado con la placa ${cleaned}.`,
          ),
        )
        return null
      } finally {
        setSearchingVehicle(false)
      }
    },
    [customerId],
  )

  const loadCatalogData = useCallback(async () => {
    if (!customerId) {
      setCatalogLoading(false)
      return
    }
    setCatalogLoading(true)
    setStepError('')
    try {
      const [serviceList, employeeList, vehicleList] = await Promise.all([
        getServiceTypes(),
        getEmployees(),
        getVehiclesByCustomer(customerId).catch(() => []),
      ])
      setServiceTypes(Array.isArray(serviceList) ? serviceList : [])
      setEmployees(Array.isArray(employeeList) ? employeeList : [])
      setMyVehicles(Array.isArray(vehicleList) ? vehicleList : [])
    } catch (error) {
      setStepError(getApiErrorMessage(error, 'No fue posible cargar servicios ni especialistas.'))
    } finally {
      setCatalogLoading(false)
    }
  }, [customerId])

  useEffect(() => {
    loadCatalogData()
    if (customerId && initialPlate) {
      lookupVehicleByPlate(initialPlate)
    }
  }, [customerId, initialPlate, loadCatalogData, lookupVehicleByPlate])

  const selectedService = useMemo(
    () => serviceTypes.find((item) => item.id === selectedServiceId) || null,
    [serviceTypes, selectedServiceId],
  )

  const selectedEmployee = useMemo(
    () => employees.find((item) => item.id === selectedEmployeeId) || null,
    [employees, selectedEmployeeId],
  )

  useEffect(() => {
    if (!customerId || !selectedDate) {
      setOccupiedSlots([])
      return undefined
    }
    let active = true
    setLoadingAvailability(true)
    getOccupiedAppointmentSlots(selectedDate, selectedEmployeeId || undefined)
      .then((list) => {
        if (active) {
          setOccupiedSlots(Array.isArray(list) ? list : [])
        }
      })
      .catch(() => {
        if (active) {
          setOccupiedSlots([])
        }
      })
      .finally(() => {
        if (active) setLoadingAvailability(false)
      })
    return () => {
      active = false
    }
  }, [customerId, selectedDate, selectedEmployeeId])

  const isSlotOccupied = useCallback(
    (slotTime) => {
      if (!selectedDate || !occupiedSlots.length) return false
      const candidateStart = new Date(`${selectedDate}T${slotTime}:00`).getTime()
      if (Number.isNaN(candidateStart)) return false
      const durationMs = Math.max(Number(selectedService?.durationMin) || 60, 1) * 60_000
      const candidateEnd = candidateStart + durationMs

      const matchingSlots = occupiedSlots.filter((item) => {
        const start = new Date(item.startTime).getTime()
        const end = new Date(item.endTime).getTime()
        return !Number.isNaN(start) && !Number.isNaN(end) && candidateStart < end && start < candidateEnd
      })

      if (!matchingSlots.length) return false
      if (selectedEmployeeId) {
        return matchingSlots.some((item) => item.employeeId === selectedEmployeeId)
      }
      if (employees.length > 0) {
        const busyEmployeeIds = new Set(matchingSlots.map((item) => item.employeeId).filter(Boolean))
        return employees.every((emp) => busyEmployeeIds.has(emp.id))
      }
      return false
    },
    [employees, occupiedSlots, selectedDate, selectedEmployeeId, selectedService?.durationMin],
  )

  const getSlotStatus = (slot) => {
    if (!slot.available) {
      return { disabled: true, subtitle: slot.reason || slot.period }
    }
    if (!selectedDate) {
      return { disabled: false, subtitle: slot.period }
    }
    const candidate = new Date(`${selectedDate}T${slot.time}:00`)
    if (Number.isNaN(candidate.getTime()) || candidate.getTime() <= renderTimestamp) {
      return { disabled: true, subtitle: 'No disponible' }
    }
    if (!isWithinOperatingHours(candidate)) {
      return { disabled: true, subtitle: 'Fuera de horario' }
    }
    if (isSlotOccupied(slot.time)) {
      return { disabled: true, subtitle: 'Ocupado' }
    }
    return { disabled: false, subtitle: slot.period }
  }

  const handleNextStep = async () => {
    setStepError('')

    if (step === 1) {
      const cleaned = plate.trim().toUpperCase()
      if (resolvedVehicle && resolvedVehicle.plate?.toUpperCase() === cleaned) {
        setStep(2)
        return
      }
      const found = await lookupVehicleByPlate(cleaned)
      if (found?.id) {
        setStep(2)
      }
      return
    }

    if (step === 2) {
      if (!selectedServiceId) {
        setStepError('Selecciona el servicio de lavado o detailing que deseas agendar.')
        return
      }
      setStep(3)
      return
    }

    if (step === 3) {
      if (!selectedDate || !selectedTime) {
        setStepError('Elige una fecha y una franja horaria disponible.')
        return
      }
      const parsedDateTime = new Date(`${selectedDate}T${selectedTime}:00`)
      if (Number.isNaN(parsedDateTime.getTime()) || parsedDateTime <= new Date()) {
        setStepError('Selecciona una fecha y hora futura.')
        return
      }
      if (!isWithinOperatingHours(parsedDateTime)) {
        setStepError('Horario de atención: 7am-12pm y 2:30pm-9pm')
        return
      }
      if (!selectedEmployeeId) {
        setStepError('Elige al especialista que atenderá tu vehículo.')
        return
      }
      setStep(4)
    }
  }

  const handleConfirmBooking = async () => {
    if (!customerId || !resolvedVehicle?.id || !selectedServiceId || !selectedEmployeeId) {
      return
    }

    const parsedDateTime = new Date(`${selectedDate}T${selectedTime}:00`)
    if (!isWithinOperatingHours(parsedDateTime) || parsedDateTime <= new Date()) {
      setStepError('Horario de atención: 7am-12pm y 2:30pm-9pm')
      setStep(3)
      return
    }

    setSubmitting(true)
    setStepError('')
    try {
      const created = await createAppointment({
        customerId,
        vehicleId: resolvedVehicle.id,
        employeeId: selectedEmployeeId,
        serviceTypeId: selectedServiceId,
        dateTime: parsedDateTime.toISOString(),
        status: 'PENDING',
      })
      setCreatedAppointment(created)
      showToast({
        type: 'success',
        title: 'Cita confirmada',
        description: `Placa ${resolvedVehicle.plate} agendada correctamente.`,
      })
    } catch (error) {
      setStepError(getApiErrorMessage(error, 'No fue posible confirmar tu cita.'))
    } finally {
      setSubmitting(false)
    }
  }

  const resetWizard = () => {
    setCreatedAppointment(null)
    setStep(1)
    setPlate('')
    setResolvedVehicle(null)
    setPlateError('')
    setSelectedServiceId('')
    setSelectedDate(todayIso)
    setSelectedTime('')
    setSelectedEmployeeId('')
    setStepError('')
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-3xl space-y-5">
        <div>
          <p className="text-[13px] font-medium text-[#5B5F7A]">Reserva en 4 pasos</p>
          <h1 className="text-[26px] font-bold tracking-tight text-[#1E1B4B]">Agendar Cita</h1>
        </div>

        {!customerId ? (
          <CompleteCustomerProfileCard />
        ) : createdAppointment ? (
          <section className="rounded-2xl border border-[#15803D]/25 bg-white p-6 text-center shadow-card sm:p-8">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-[#DCFCE7] text-[#15803D]">
              <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M20 6L9 17l-5-5" />
              </svg>
            </div>
            <h2 className="mt-4 text-xl font-bold text-[#1E1B4B]">¡Cita reservada con éxito!</h2>
            <p className="mt-1 text-sm text-[#5B5F7A]">
              Tu vehículo{' '}
              <strong className="font-semibold text-[#1E1B4B]">
                {createdAppointment.vehiclePlate || resolvedVehicle?.plate}
              </strong>{' '}
              quedó agendado para{' '}
              <strong className="font-semibold text-[#1E1B4B]">
                {createdAppointment.serviceName || selectedService?.name}
              </strong>
              .
            </p>

            <div className="mt-5 rounded-xl border border-[#15803D]/20 bg-[#DCFCE7]/50 px-4 py-3 text-xs font-semibold text-[#15803D]">
              Pago en efectivo al finalizar
            </div>

            <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
              <Link
                to="/portal/historial"
                className="inline-flex h-[52px] items-center justify-center rounded-xl bg-[#4F46E5] px-6 text-sm font-semibold text-white hover:bg-[#4338CA]"
              >
                Seguir estado en vivo
              </Link>
              <Button variant="secondary" size="l" onClick={resetWizard}>
                Agendar otra cita
              </Button>
            </div>
          </section>
        ) : (
          <>
            <nav aria-label="Progreso de reserva" className="rounded-2xl border border-[#E4E6F2] bg-white p-4 shadow-card">
              <ol className="grid grid-cols-4 gap-2">
                {STEPS.map((item) => {
                  const isDone = step > item.id
                  const isCurrent = step === item.id
                  return (
                    <li key={item.id} className="flex flex-col items-center text-center">
                      <div
                        className={[
                          'flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold transition',
                          isDone
                            ? 'bg-[#15803D] text-white'
                            : isCurrent
                              ? 'bg-[#4F46E5] text-white ring-4 ring-[#4F46E5]/15'
                              : 'bg-[#F5F6FB] text-[#5B5F7A]',
                        ].join(' ')}
                      >
                        {isDone ? '✓' : item.id}
                      </div>
                      <span
                        className={[
                          'mt-1.5 text-[11px] font-semibold',
                          isCurrent ? 'text-[#1E1B4B]' : 'text-[#5B5F7A]',
                        ].join(' ')}
                      >
                        {item.label}
                      </span>
                    </li>
                  )
                })}
              </ol>
            </nav>

            {stepError && (
              <div
                role="alert"
                className="rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
              >
                {stepError}
              </div>
            )}

            <section className="rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card sm:p-6">
              {step === 1 && (
                <div className="space-y-5">
                  <div>
                    <h2 className="text-lg font-semibold text-[#1E1B4B]">
                      1. Escribe la placa de tu vehículo
                    </h2>
                    <p className="mt-1 text-sm text-[#5B5F7A]">
                      Verificamos tu auto directamente por placa antes de elegir el servicio.
                    </p>
                  </div>

                  <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
                    <div className="flex-1">
                      <PlateInput
                        id="booking-plate"
                        label="Placa colombiana"
                        value={plate}
                        onChange={(cleaned) => {
                          setPlate(cleaned)
                          setPlateError('')
                          if (resolvedVehicle && resolvedVehicle.plate?.toUpperCase() !== cleaned) {
                            setResolvedVehicle(null)
                          }
                        }}
                        error={plateError}
                        required
                      />
                    </div>
                    <Button
                      type="button"
                      variant="secondary"
                      size="l"
                      loading={searchingVehicle}
                      onClick={() => lookupVehicleByPlate(plate)}
                    >
                      Verificar placa
                    </Button>
                  </div>

                  {myVehicles.length > 0 && (
                    <div className="space-y-2 rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 p-3.5">
                      <p className="text-[11px] font-semibold tracking-wider text-[#5B5F7A] uppercase">
                        Tus vehículos registrados
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {myVehicles.map((vehicle) => {
                          const isCurrent = resolvedVehicle?.id === vehicle.id
                          return (
                            <button
                              key={vehicle.id}
                              type="button"
                              onClick={() => {
                                setPlate(vehicle.plate)
                                setResolvedVehicle(vehicle)
                                setPlateError('')
                              }}
                              className={`inline-flex items-center gap-2 rounded-xl border px-3 py-1.5 text-xs font-semibold transition ${
                                isCurrent
                                  ? 'border-[#4F46E5] bg-[#4F46E5] text-white'
                                  : 'border-[#E4E6F2] bg-white text-[#1E1B4B] hover:border-[#4F46E5]'
                              }`}
                            >
                              <span className="font-mono font-bold">{vehicle.plate}</span>
                              <span className={isCurrent ? 'text-indigo-100' : 'text-[#5B5F7A]'}>
                                {vehicle.brand} {vehicle.model}
                              </span>
                            </button>
                          )
                        })}
                      </div>
                    </div>
                  )}

                  {resolvedVehicle && (
                    <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#15803D]/25 bg-[#DCFCE7]/50 p-4">
                      <div>
                        <span className="text-xs font-semibold tracking-wider text-[#15803D] uppercase">
                          Vehículo verificado
                        </span>
                        <p className="mt-0.5 text-base font-bold text-[#1E1B4B]">
                          {resolvedVehicle.brand} {resolvedVehicle.model} · {resolvedVehicle.year}
                        </p>
                      </div>
                      <span className="rounded-md border border-[#1E1B4B] bg-amber-300 px-3 py-1 font-mono text-sm font-extrabold tracking-widest text-[#1E1B4B]">
                        {resolvedVehicle.plate}
                      </span>
                    </div>
                  )}

                  <p className="text-xs text-[#5B5F7A]">
                    ¿Tu placa aún no aparece registrada?{' '}
                    <Link to="/portal/mis-vehiculos" className="font-semibold text-[#4F46E5] hover:underline">
                      Regístrala primero en Mis Vehículos →
                    </Link>
                  </p>
                </div>
              )}

              {step === 2 && (
                <div className="space-y-4">
                  <div>
                    <h2 className="text-lg font-semibold text-[#1E1B4B]">2. Selecciona el servicio</h2>
                    <p className="mt-1 text-sm text-[#5B5F7A]">
                      Elige el tratamiento para tu vehículo{' '}
                      <strong className="font-semibold text-[#1E1B4B]">{resolvedVehicle?.plate}</strong>.
                    </p>
                  </div>

                  {catalogLoading ? (
                    <div className="grid gap-3 sm:grid-cols-2">
                      {[1, 2, 3, 4].map((skeleton) => (
                        <div key={skeleton} className="h-28 animate-pulse rounded-xl bg-[#F5F6FB]" />
                      ))}
                    </div>
                  ) : serviceTypes.length === 0 ? (
                    <div className="rounded-xl border border-dashed border-[#E4E6F2] bg-[#F5F6FB]/70 p-6 text-center">
                      <p className="text-sm font-semibold text-[#1E1B4B]">
                        No hay servicios disponibles en el catálogo todavía
                      </p>
                      <p className="mt-1 text-xs text-[#5B5F7A]">
                        Un administrador debe registrar los tipos de servicio en el panel o aplicar las migraciones iniciales.
                      </p>
                      <div className="mt-4">
                        <Button type="button" variant="secondary" onClick={loadCatalogData}>
                          Reintentar carga
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <div className="grid gap-3 sm:grid-cols-2">
                      {serviceTypes.map((service) => {
                        const selected = selectedServiceId === service.id
                        return (
                          <button
                            key={service.id}
                            type="button"
                            onClick={() => {
                              setSelectedServiceId(service.id)
                              setStepError('')
                            }}
                            className={[
                              'flex flex-col justify-between rounded-xl border p-4 text-left transition',
                              selected
                                ? 'border-[#4F46E5] bg-indigo-50/60 ring-2 ring-[#4F46E5]/20'
                                : 'border-[#E4E6F2] bg-white hover:border-[#4F46E5]/40',
                            ].join(' ')}
                          >
                            <div>
                              <div className="flex items-start justify-between gap-2">
                                <span className="text-sm font-bold text-[#1E1B4B]">
                                  {service.name}
                                </span>
                                <span className="shrink-0 text-sm font-bold tabular-nums text-[#4F46E5]">
                                  {formatCop(service.price)}
                                </span>
                              </div>
                              <p className="mt-1 text-xs text-[#5B5F7A]">
                                {service.description || 'Cuidado completo para tu vehículo.'}
                              </p>
                            </div>
                            <span className="mt-3 inline-flex w-fit rounded-full bg-[#F5F6FB] px-2.5 py-0.5 text-[11px] font-semibold text-[#5B5F7A]">
                              Duración: {service.durationMin} min
                            </span>
                          </button>
                        )
                      })}
                    </div>
                  )}
                </div>
              )}

              {step === 3 && (
                <div className="space-y-5">
                  <div>
                    <h2 className="text-lg font-semibold text-[#1E1B4B]">
                      3. Fecha, hora y especialista
                    </h2>
                    <p className="mt-1 text-sm text-[#5B5F7A]">
                      Horario de atención: 7:00 a. m.–12:00 p. m. y 2:30 p. m.–9:00 p. m.
                    </p>
                  </div>

                  <div className="space-y-1.5">
                    <label htmlFor="bookingDate" className="block text-sm font-medium text-[#1E1B4B]">
                      Fecha de la cita
                    </label>
                    <input
                      id="bookingDate"
                      type="date"
                      min={todayIso}
                      value={selectedDate}
                      onChange={(e) => {
                        setSelectedDate(e.target.value)
                        setSelectedTime('')
                        setStepError('')
                      }}
                      className="block h-11 w-full rounded-xl border border-[#E4E6F2] bg-white px-3.5 text-sm text-[#1E1B4B] focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5]/15 focus:outline-none sm:max-w-xs"
                    />
                  </div>

                  <div className="space-y-2">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <p className="text-sm font-medium text-[#1E1B4B]">Franja horaria</p>
                      {loadingAvailability && (
                        <span className="text-xs text-[#5B5F7A]">Verificando disponibilidad...</span>
                      )}
                    </div>
                    <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                      {TIME_SLOTS.map((slot) => {
                        const { disabled, subtitle } = getSlotStatus(slot)
                        const active = selectedTime === slot.time && !disabled
                        return (
                          <button
                            key={slot.time}
                            type="button"
                            disabled={disabled}
                            onClick={() => {
                              setSelectedTime(slot.time)
                              setStepError('')
                            }}
                            className={[
                              'rounded-xl border px-3 py-2.5 text-left transition',
                              disabled
                                ? 'cursor-not-allowed border-[#E4E6F2] bg-[#F5F6FB] text-[#5B5F7A]/60'
                                : active
                                  ? 'border-[#4F46E5] bg-[#4F46E5] text-white shadow-xs'
                                  : 'border-[#E4E6F2] bg-white text-[#1E1B4B] hover:border-[#4F46E5]',
                            ].join(' ')}
                          >
                            <span className="block text-xs font-bold tabular-nums">{slot.label}</span>
                            <span
                              className={[
                                'block text-[10px]',
                                active ? 'text-indigo-100' : subtitle === 'Ocupado' ? 'font-semibold text-[#B91C1C]' : 'text-[#5B5F7A]',
                              ].join(' ')}
                            >
                              {subtitle}
                            </span>
                          </button>
                        )
                      })}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <p className="text-sm font-medium text-[#1E1B4B]">Especialista</p>
                    {employees.length === 0 ? (
                      <div className="rounded-xl border border-dashed border-[#E4E6F2] bg-[#F5F6FB]/70 p-5 text-center">
                        <p className="text-sm font-semibold text-[#1E1B4B]">
                          No hay especialistas registrados todavía
                        </p>
                        <p className="mt-1 text-xs text-[#5B5F7A]">
                          Pide al administrador registrar al menos un empleado operativo para poder asignar tu cita.
                        </p>
                        <div className="mt-3">
                          <Button type="button" variant="secondary" onClick={loadCatalogData}>
                            Reintentar carga
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="grid gap-2 sm:grid-cols-2">
                      {employees.map((employee) => {
                        const selected = selectedEmployeeId === employee.id
                        return (
                          <button
                            key={employee.id}
                            type="button"
                            onClick={() => {
                              setSelectedEmployeeId(employee.id)
                              setStepError('')
                              if (selectedTime) {
                                const candidateStart = new Date(`${selectedDate}T${selectedTime}:00`).getTime()
                                const durationMs = Math.max(Number(selectedService?.durationMin) || 60, 1) * 60_000
                                const candidateEnd = candidateStart + durationMs
                                const hasConflict = occupiedSlots.some((item) => {
                                  if (item.employeeId !== employee.id) return false
                                  const start = new Date(item.startTime).getTime()
                                  const end = new Date(item.endTime).getTime()
                                  return candidateStart < end && start < candidateEnd
                                })
                                if (hasConflict) {
                                  setSelectedTime('')
                                  setStepError(
                                    `${employee.name} ya tiene ocupado ese horario. Elige otra franja disponible.`,
                                  )
                                }
                              }
                            }}
                            className={[
                              'flex items-center gap-3 rounded-xl border p-3 text-left transition',
                              selected
                                ? 'border-[#4F46E5] bg-indigo-50/60 ring-2 ring-[#4F46E5]/20'
                                : 'border-[#E4E6F2] bg-white hover:border-[#4F46E5]/40',
                            ].join(' ')}
                          >
                            <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#1E1B4B] text-xs font-bold text-white">
                              {employee.name?.slice(0, 2).toUpperCase() || 'AC'}
                            </span>
                            <div className="min-w-0">
                              <p className="truncate text-sm font-semibold text-[#1E1B4B]">
                                {employee.name}
                              </p>
                              <p className="truncate text-xs text-[#5B5F7A]">
                                {employee.role || 'Especialista AutoClean'}
                              </p>
                            </div>
                          </button>
                        )
                      })}
                      </div>
                    )}
                  </div>
                </div>
              )}

              {step === 4 && (
                <div className="space-y-5">
                  <div>
                    <h2 className="text-lg font-semibold text-[#1E1B4B]">4. Resumen y confirmar</h2>
                    <p className="mt-1 text-sm text-[#5B5F7A]">
                      Revisa los detalles de tu cita antes de confirmar.
                    </p>
                  </div>

                  <dl className="divide-y divide-[#E4E6F2] rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/50">
                    <div className="flex items-center justify-between gap-4 px-4 py-3 text-sm">
                      <dt className="text-[#5B5F7A]">Vehículo</dt>
                      <dd className="text-right font-semibold text-[#1E1B4B]">
                        <span className="mr-2 rounded bg-amber-300 px-2 py-0.5 font-mono text-xs font-extrabold text-[#1E1B4B]">
                          {resolvedVehicle?.plate}
                        </span>
                        {resolvedVehicle?.brand} {resolvedVehicle?.model}
                      </dd>
                    </div>
                    <div className="flex items-center justify-between gap-4 px-4 py-3 text-sm">
                      <dt className="text-[#5B5F7A]">Servicio</dt>
                      <dd className="text-right font-semibold text-[#1E1B4B]">
                        {selectedService?.name} · {selectedService?.durationMin} min
                      </dd>
                    </div>
                    <div className="flex items-center justify-between gap-4 px-4 py-3 text-sm">
                      <dt className="text-[#5B5F7A]">Fecha y hora</dt>
                      <dd className="text-right font-semibold capitalize text-[#1E1B4B]">
                        {formatReadableDateTime(selectedDate, selectedTime)}
                      </dd>
                    </div>
                    <div className="flex items-center justify-between gap-4 px-4 py-3 text-sm">
                      <dt className="text-[#5B5F7A]">Especialista</dt>
                      <dd className="text-right font-semibold text-[#1E1B4B]">
                        {selectedEmployee?.name}
                      </dd>
                    </div>
                    <div className="flex items-center justify-between gap-4 px-4 py-3.5 text-base">
                      <dt className="font-semibold text-[#1E1B4B]">Total estimado</dt>
                      <dd className="font-bold tabular-nums text-[#4F46E5]">
                        {formatCop(selectedService?.price)}
                      </dd>
                    </div>
                  </dl>

                  <div className="flex items-start gap-3 rounded-xl border border-[#15803D]/25 bg-[#DCFCE7]/60 p-4 text-[#15803D]">
                    <svg className="mt-0.5 h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="6" width="20" height="12" rx="2" />
                      <circle cx="12" cy="12" r="2.5" />
                    </svg>
                    <div>
                      <p className="text-sm font-bold">Pago en efectivo al finalizar</p>
                      <p className="text-xs">
                        No realizamos cobros anticipados en línea. Pagas directamente en recepción al recibir tu vehículo listo.
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </section>

            <div className="flex items-center gap-3 pt-1">
              {step > 1 && (
                <Button
                  type="button"
                  variant="secondary"
                  size="l"
                  onClick={() => {
                    setStep((prev) => prev - 1)
                    setStepError('')
                  }}
                  disabled={submitting}
                >
                  Anterior
                </Button>
              )}

              {step < 4 ? (
                <Button
                  type="button"
                  size="l"
                  loading={searchingVehicle}
                  onClick={handleNextStep}
                  className="flex-1"
                >
                  Continuar
                </Button>
              ) : (
                <Button
                  type="button"
                  size="l"
                  loading={submitting}
                  onClick={handleConfirmBooking}
                  className="flex-1"
                >
                  Confirmar cita
                </Button>
              )}
            </div>
          </>
        )}
      </div>
    </MainLayout>
  )
}
