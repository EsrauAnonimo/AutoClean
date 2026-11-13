import { useCallback, useEffect, useMemo, useState } from 'react'
import { getApiErrorMessage } from '../services/api'
import { getAppointments } from '../services/appointmentService'
import { getServiceTypes } from '../services/catalogService'
import { createCostEstimate, getCostEstimates } from '../services/costingService'
import { getSupplies } from '../services/supplyService'
import { showToast } from '../utils/toast'
import { Button } from './Button'
import { Input } from './Input'

function createEmptyLine() {
  return {
    key: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    concept: '',
    amount: '',
  }
}

function formatCop(value) {
  const numeric = Number(value)
  if (Number.isNaN(numeric)) return '—'
  return `$${numeric.toLocaleString('es-CO')}`
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
 * Módulo de motor de costeo: permite simular costos por servicio/cita,
 * aprovechar insumos de inventario como conceptos rápidos y consultar el historial de márgenes.
 */
export function CostingWorkspace({ onEstimateCreated }) {
  const [estimates, setEstimates] = useState([])
  const [serviceTypes, setServiceTypes] = useState([])
  const [appointments, setAppointments] = useState([])
  const [supplies, setSupplies] = useState([])

  const [serviceTypeId, setServiceTypeId] = useState('')
  const [appointmentId, setAppointmentId] = useState('')
  const [lines, setLines] = useState([createEmptyLine()])
  const [expandedEstimateId, setExpandedEstimateId] = useState(null)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const loadData = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [estimateList, serviceList, appointmentList, supplyList] = await Promise.all([
        getCostEstimates(),
        getServiceTypes(),
        getAppointments(),
        getSupplies(),
      ])
      const safeServices = Array.isArray(serviceList) ? serviceList : []
      setEstimates(Array.isArray(estimateList) ? estimateList : [])
      setServiceTypes(safeServices)
      setAppointments(Array.isArray(appointmentList) ? appointmentList : [])
      setSupplies(Array.isArray(supplyList) ? supplyList : [])
      setServiceTypeId((prev) => prev || safeServices[0]?.id || '')
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el motor de costeo.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const selectedService = useMemo(
    () => serviceTypes.find((item) => item.id === serviceTypeId) || null,
    [serviceTypes, serviceTypeId],
  )

  const availableAppointments = useMemo(
    () => appointments.filter((item) => item.status !== 'CANCELLED'),
    [appointments],
  )

  const appointmentMap = useMemo(() => {
    const map = new Map()
    for (const item of appointments) {
      map.set(item.id, item)
    }
    return map
  }, [appointments])

  const previewSummary = useMemo(() => {
    const price = Number(selectedService?.price) || 0
    const estimatedCost = lines.reduce((acc, line) => {
      const value = Number(line.amount)
      return acc + (Number.isFinite(value) && value >= 0 ? value : 0)
    }, 0)
    const difference = price - estimatedCost
    const marginPercent = price > 0 ? Math.round((difference / price) * 100) : 0
    return { price, estimatedCost, difference, marginPercent }
  }, [lines, selectedService])

  const handleAppointmentChange = (nextAppointmentId) => {
    setAppointmentId(nextAppointmentId)
    if (!nextAppointmentId) return
    const found = appointmentMap.get(nextAppointmentId)
    if (found?.serviceTypeId) {
      setServiceTypeId(found.serviceTypeId)
    }
  }

  const updateLine = (key, field, value) => {
    setLines((prev) => prev.map((line) => (line.key === key ? { ...line, [field]: value } : line)))
  }

  const removeLine = (key) => {
    setLines((prev) => (prev.length > 1 ? prev.filter((line) => line.key !== key) : [createEmptyLine()]))
  }

  const addQuickSupplyLine = (supply) => {
    const label = `Insumo: ${supply.name} (${supply.unit})`
    setLines((prev) => {
      const firstEmpty = prev.find((line) => !line.concept.trim() && !line.amount)
      if (firstEmpty) {
        return prev.map((line) => (line.key === firstEmpty.key ? { ...line, concept: label } : line))
      }
      return [...prev, { key: `${Date.now()}-${supply.id}`, concept: label, amount: '' }]
    })
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    if (!serviceTypeId) {
      setErrorMessage('Selecciona el tipo de servicio para calcular el costeo.')
      return
    }

    const normalizedLines = []
    for (const line of lines) {
      const concept = line.concept.trim()
      const amountText = String(line.amount ?? '').trim()
      if (!concept && !amountText) {
        continue
      }
      if (!concept) {
        setErrorMessage('Cada línea con monto debe incluir un concepto.')
        return
      }
      const parsedAmount = Number(amountText)
      if (!Number.isFinite(parsedAmount) || parsedAmount < 0) {
        setErrorMessage(`El monto de "${concept}" debe ser un número mayor o igual a 0.`)
        return
      }
      normalizedLines.push({ concept, amount: parsedAmount })
    }

    setSubmitting(true)
    try {
      const created = await createCostEstimate({
        serviceTypeId,
        appointmentId: appointmentId || null,
        lines: normalizedLines,
      })
      setEstimates((prev) => [created, ...prev])
      setExpandedEstimateId(created.id)
      setLines([createEmptyLine()])
      setAppointmentId('')
      showToast({
        type: 'success',
        title: 'Estimación guardada',
        description: `${created.serviceTypeName} · Margen ${formatCop(created.difference)}`,
      })
      onEstimateCreated?.()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar la estimación de costo.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      {errorMessage && (
        <div
          role="alert"
          className="rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
        >
          {errorMessage}
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-12">
        <section className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card xl:col-span-5">
          <div className="mb-4">
            <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Nueva estimación de costo</h2>
            <p className="mt-1 text-xs text-[#5B5F7A]">
              Suma líneas de insumos o mano de obra contra el precio del catálogo.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label htmlFor="costingServiceType" className="block text-sm font-medium text-[#1E1B4B]">
                Tipo de servicio
              </label>
              <select
                id="costingServiceType"
                value={serviceTypeId}
                onChange={(e) => setServiceTypeId(e.target.value)}
                disabled={loading || !serviceTypes.length}
                className="block h-11 w-full rounded-xl border border-[#E4E6F2] bg-white px-3.5 text-sm text-[#1E1B4B] focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5]/15 focus:outline-none"
                required
              >
                {!serviceTypes.length && <option value="">Sin servicios registrados</option>}
                {serviceTypes.map((service) => (
                  <option key={service.id} value={service.id}>
                    {service.name} ({formatCop(service.price)})
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label htmlFor="costingAppointment" className="block text-sm font-medium text-[#1E1B4B]">
                Cita asociada (opcional)
              </label>
              <select
                id="costingAppointment"
                value={appointmentId}
                onChange={(e) => handleAppointmentChange(e.target.value)}
                disabled={loading}
                className="block h-11 w-full rounded-xl border border-[#E4E6F2] bg-white px-3.5 text-sm text-[#1E1B4B] focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5]/15 focus:outline-none"
              >
                <option value="">Simulación general de catálogo (sin cita)</option>
                {availableAppointments.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.vehiclePlate} · {item.customerName} ({item.serviceTypeName})
                  </option>
                ))}
              </select>
            </div>

            {supplies.length > 0 && (
              <div className="space-y-2 rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 p-3">
                <p className="text-[11px] font-semibold tracking-wider text-[#5B5F7A] uppercase">
                  Agregar rápido desde inventario
                </p>
                <div className="flex flex-wrap gap-1.5">
                  {supplies.slice(0, 6).map((supply) => (
                    <button
                      key={supply.id}
                      type="button"
                      onClick={() => addQuickSupplyLine(supply)}
                      className="inline-flex items-center gap-1 rounded-full border border-[#E4E6F2] bg-white px-2.5 py-1 text-xs font-medium text-[#1E1B4B] transition hover:border-[#4F46E5] hover:text-[#4F46E5]"
                    >
                      <span>+ {supply.name}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="space-y-2.5">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-[#1E1B4B]">Líneas de costo directo</span>
                <button
                  type="button"
                  onClick={() => setLines((prev) => [...prev, createEmptyLine()])}
                  className="text-xs font-semibold text-[#4F46E5] hover:underline"
                >
                  + Agregar línea
                </button>
              </div>

              <div className="space-y-2">
                {lines.map((line, index) => (
                  <div key={line.key} className="grid grid-cols-12 items-start gap-2">
                    <div className="col-span-6">
                      <Input
                        id={`costConcept-${index}`}
                        aria-label={`Concepto ${index + 1}`}
                        placeholder="Ej: Shampoo pH neutro"
                        value={line.concept}
                        onChange={(e) => updateLine(line.key, 'concept', e.target.value)}
                      />
                    </div>
                    <div className="col-span-4">
                      <Input
                        id={`costAmount-${index}`}
                        aria-label={`Monto ${index + 1}`}
                        type="number"
                        min="0"
                        step="100"
                        placeholder="0"
                        value={line.amount}
                        onChange={(e) => updateLine(line.key, 'amount', e.target.value)}
                      />
                    </div>
                    <div className="col-span-2 flex justify-end pt-1">
                      <button
                        type="button"
                        onClick={() => removeLine(line.key)}
                        aria-label="Eliminar línea"
                        className="flex h-9 w-9 items-center justify-center rounded-xl border border-[#E4E6F2] text-sm font-semibold text-[#5B5F7A] transition hover:border-[#B91C1C]/30 hover:bg-[#FEE2E2] hover:text-[#B91C1C]"
                      >
                        ×
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-3 gap-2 rounded-xl border border-[#E4E6F2] bg-[#F5F6FB] p-3.5 text-xs">
              <div>
                <span className="block text-[#5B5F7A]">Precio servicio</span>
                <span className="mt-0.5 block text-sm font-bold tabular-nums text-[#1E1B4B]">
                  {formatCop(previewSummary.price)}
                </span>
              </div>
              <div>
                <span className="block text-[#5B5F7A]">Costo estimado</span>
                <span className="mt-0.5 block text-sm font-bold tabular-nums text-[#1E1B4B]">
                  {formatCop(previewSummary.estimatedCost)}
                </span>
              </div>
              <div>
                <span className="block text-[#5B5F7A]">Margen ({previewSummary.marginPercent}%)</span>
                <span
                  className={`mt-0.5 block text-sm font-bold tabular-nums ${
                    previewSummary.difference >= 0 ? 'text-[#15803D]' : 'text-[#B91C1C]'
                  }`}
                >
                  {formatCop(previewSummary.difference)}
                </span>
              </div>
            </div>

            <Button type="submit" size="l" loading={submitting} disabled={!serviceTypeId} className="w-full">
              Guardar estimación de costo
            </Button>
          </form>
        </section>

        <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card xl:col-span-7">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#E4E6F2] px-6 py-4">
            <div>
              <h2 className="text-[18px] font-semibold text-[#1E1B4B]">Historial de estimaciones</h2>
              <p className="mt-0.5 text-xs text-[#5B5F7A]">
                Registros guardados en el motor de costeo y disponibles en Ingresos vs Costos.
              </p>
            </div>
            <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold tabular-nums text-[#4F46E5]">
              {estimates.length} registros
            </span>
          </div>

          {loading ? (
            <div className="p-8 text-center text-sm text-[#5B5F7A]">Cargando estimaciones...</div>
          ) : !estimates.length ? (
            <div className="p-10 text-center">
              <p className="text-base font-semibold text-[#1E1B4B]">Aún no hay estimaciones registradas</p>
              <p className="mt-1 text-sm text-[#5B5F7A]">
                Crea la primera estimación en el panel izquierdo para calcular el margen real de tus servicios.
              </p>
            </div>
          ) : (
            <div className="divide-y divide-[#E4E6F2]">
              {estimates.map((estimate) => {
                const isExpanded = expandedEstimateId === estimate.id
                const linkedAppointment = estimate.appointmentId
                  ? appointmentMap.get(estimate.appointmentId)
                  : null
                const diff = Number(estimate.difference) || 0

                return (
                  <article key={estimate.id} className="px-6 py-4 transition hover:bg-[#F5F6FB]/50">
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3 className="text-sm font-semibold text-[#1E1B4B]">{estimate.serviceTypeName}</h3>
                          {linkedAppointment ? (
                            <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-[11px] font-semibold text-[#4F46E5]">
                              {linkedAppointment.vehiclePlate} · {linkedAppointment.customerName}
                            </span>
                          ) : estimate.appointmentId ? (
                            <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-[11px] font-semibold text-[#4F46E5]">
                              Con cita vinculada
                            </span>
                          ) : (
                            <span className="rounded-full bg-[#F5F6FB] px-2.5 py-0.5 text-[11px] font-medium text-[#5B5F7A]">
                              Catálogo
                            </span>
                          )}
                        </div>
                        <p className="mt-1 text-xs text-[#5B5F7A]">
                          {formatDateTime(estimate.createdAt)} · {estimate.lines?.length ?? 0} línea(s) de costo
                        </p>
                      </div>

                      <div className="flex items-center gap-4">
                        <div className="text-right text-xs">
                          <span className="block text-[#5B5F7A]">
                            Precio {formatCop(estimate.price)} · Costo {formatCop(estimate.estimatedCost)}
                          </span>
                          <span
                            className={`mt-0.5 block text-sm font-bold tabular-nums ${
                              diff >= 0 ? 'text-[#15803D]' : 'text-[#B91C1C]'
                            }`}
                          >
                            Margen {formatCop(diff)}
                          </span>
                        </div>

                        <button
                          type="button"
                          onClick={() => setExpandedEstimateId(isExpanded ? null : estimate.id)}
                          className="rounded-lg border border-[#E4E6F2] px-2.5 py-1.5 text-xs font-semibold text-[#1E1B4B] hover:border-[#4F46E5]"
                        >
                          {isExpanded ? 'Ocultar' : 'Detalle'}
                        </button>
                      </div>
                    </div>

                    {isExpanded && (
                      <div className="mt-3 rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 p-3">
                        {!estimate.lines?.length ? (
                          <p className="text-xs text-[#5B5F7A]">Sin líneas de costo detalladas ($0).</p>
                        ) : (
                          <ul className="space-y-1.5 text-xs">
                            {estimate.lines.map((line) => (
                              <li key={line.id} className="flex items-center justify-between gap-2">
                                <span className="font-medium text-[#1E1B4B]">{line.concept}</span>
                                <span className="font-semibold tabular-nums text-[#5B5F7A]">
                                  {formatCop(line.amount)}
                                </span>
                              </li>
                            ))}
                          </ul>
                        )}
                      </div>
                    )}
                  </article>
                )
              })}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

