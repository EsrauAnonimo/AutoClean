import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { PlateInput } from '../components/PlateInput'
import { getApiErrorMessage } from '../services/api'
import { getCustomers } from '../services/customerService'
import {
  createVehicle,
  deleteVehicle,
  getVehicles,
  updateVehicle,
} from '../services/vehicleService'
import { showToast } from '../utils/toast'

const INITIAL_FORM = {
  customerId: '',
  plate: '',
  brand: '',
  model: '',
  year: '',
}

const PAGE_SIZE = 8

/**
 * Gestión de vehículos (Admin): buscador, filtro por cliente, paginación y modal crear/editar.
 */
export function VehiclesPage() {
  const [vehicles, setVehicles] = useState([])
  const [customers, setCustomers] = useState([])
  const [customerFilter, setCustomerFilter] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [currentPage, setCurrentPage] = useState(1)

  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(INITIAL_FORM)
  const [fieldErrors, setFieldErrors] = useState({})

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadData = useCallback(async (selectedCustomerId = '') => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [vehicleList, customerList] = await Promise.all([
        getVehicles(selectedCustomerId || undefined),
        getCustomers(),
      ])
      setVehicles(Array.isArray(vehicleList) ? vehicleList : [])
      setCustomers(Array.isArray(customerList) ? customerList : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar los vehículos.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData('')
  }, [loadData])

  const filteredVehicles = useMemo(() => {
    const q = searchQuery.trim().toLowerCase()
    if (!q) return vehicles
    return vehicles.filter(
      (vehicle) =>
        vehicle.plate?.toLowerCase().includes(q) ||
        vehicle.brand?.toLowerCase().includes(q) ||
        vehicle.model?.toLowerCase().includes(q) ||
        vehicle.customerName?.toLowerCase().includes(q),
    )
  }, [vehicles, searchQuery])

  const totalPages = Math.max(1, Math.ceil(filteredVehicles.length / PAGE_SIZE))
  const safePage = Math.min(currentPage, totalPages)
  const paginatedVehicles = useMemo(() => {
    const start = (safePage - 1) * PAGE_SIZE
    return filteredVehicles.slice(start, start + PAGE_SIZE)
  }, [filteredVehicles, safePage])

  const openCreateModal = () => {
    setEditingId(null)
    setForm({ ...INITIAL_FORM, customerId: customerFilter || '' })
    setFieldErrors({})
    setShowModal(true)
  }

  const openEditModal = (vehicle) => {
    setEditingId(vehicle.id)
    setForm({
      customerId: vehicle.customerId || '',
      plate: vehicle.plate || '',
      brand: vehicle.brand || '',
      model: vehicle.model || '',
      year: String(vehicle.year ?? ''),
    })
    setFieldErrors({})
    setShowModal(true)
  }

  const closeModal = () => {
    setShowModal(false)
    setEditingId(null)
    setForm(INITIAL_FORM)
    setFieldErrors({})
  }

  const clearFieldError = (field) => {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const next = { ...prev }
        delete next[field]
        return next
      })
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    const plate = form.plate.trim().toUpperCase()
    const brand = form.brand.trim()
    const model = form.model.trim()
    const parsedYear = Number.parseInt(form.year, 10)
    const errors = {}

    if (!form.customerId) errors.customerId = 'Selecciona el propietario del vehículo.'
    if (!plate) errors.plate = 'La placa es obligatoria.'
    if (!brand) errors.brand = 'La marca es obligatoria.'
    if (!model) errors.model = 'El modelo es obligatorio.'
    if (Number.isNaN(parsedYear) || parsedYear < 1900 || parsedYear > 2100) {
      errors.year = 'Ingresa un año válido entre 1900 y 2100.'
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    const payload = {
      customerId: form.customerId,
      plate,
      brand,
      model,
      year: parsedYear,
    }

    setSubmitting(true)
    try {
      if (editingId) {
        await updateVehicle(editingId, payload)
        showToast({ type: 'success', title: 'Vehículo actualizado', description: plate })
      } else {
        await createVehicle(payload)
        showToast({ type: 'success', title: 'Vehículo registrado', description: plate })
      }
      closeModal()
      await loadData(customerFilter)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el vehículo.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (vehicle) => {
    if (!window.confirm(`¿Deseas eliminar el vehículo ${vehicle.plate}?`)) return
    setErrorMessage('')
    setBusyId(vehicle.id)
    try {
      await deleteVehicle(vehicle.id)
      setVehicles((prev) => prev.filter((item) => item.id !== vehicle.id))
      if (editingId === vehicle.id) closeModal()
      showToast({ type: 'info', title: 'Vehículo eliminado', description: vehicle.plate })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el vehículo.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] font-bold tracking-tight text-[#1E1B4B]">Vehículos</h1>
            <p className="mt-1 text-sm text-[#5B5F7A]">
              Flota de vehículos registrados por placa y propietario.
            </p>
          </div>
          <Button onClick={openCreateModal}>+ Nuevo vehículo</Button>
        </div>

        {errorMessage && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
          >
            <span>{errorMessage}</span>
            <button
              type="button"
              onClick={() => loadData(customerFilter)}
              className="rounded-lg bg-white px-3 py-1 text-xs font-semibold text-[#B91C1C]"
            >
              Reintentar
            </button>
          </div>
        )}

        <section className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#E4E6F2] bg-white p-4 shadow-card">
          <div className="relative flex-1 min-w-[220px]">
            <svg
              className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#5B5F7A]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-3.5-3.5" />
            </svg>
            <input
              type="search"
              placeholder="Buscar por placa, marca, modelo o cliente..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value)
                setCurrentPage(1)
              }}
              className="h-11 w-full rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 pr-3.5 pl-10 text-sm text-[#1E1B4B] placeholder:text-[#5B5F7A]/70 focus:border-[#4F46E5] focus:bg-white focus:ring-2 focus:ring-[#4F46E5]/15 focus:outline-none"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <select
              aria-label="Filtrar por cliente"
              value={customerFilter}
              onChange={(e) => {
                const nextCustomerId = e.target.value
                setCustomerFilter(nextCustomerId)
                setCurrentPage(1)
                loadData(nextCustomerId)
              }}
              className="h-11 rounded-xl border border-[#E4E6F2] bg-white px-3.5 text-sm text-[#1E1B4B] focus:border-[#4F46E5] focus:outline-none"
            >
              <option value="">Todos los clientes</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.name}
                </option>
              ))}
            </select>

            <Button variant="secondary" onClick={() => loadData(customerFilter)} disabled={loading}>
              Actualizar
            </Button>
          </div>
        </section>

        <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
          {loading ? (
            <div className="space-y-3 p-6">
              {[1, 2, 3, 4].map((row) => (
                <div key={row} className="h-12 animate-pulse rounded-xl bg-[#F5F6FB]" />
              ))}
            </div>
          ) : filteredVehicles.length === 0 ? (
            <div className="p-12 text-center">
              <p className="text-base font-semibold text-[#1E1B4B]">
                No hay vehículos para mostrar
              </p>
              <p className="mt-1 text-sm text-[#5B5F7A]">
                Registra un vehículo nuevo o ajusta los filtros de búsqueda.
              </p>
              <div className="mt-5">
                <Button onClick={openCreateModal}>+ Nuevo vehículo</Button>
              </div>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                  <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                    <tr>
                      <th className="px-5 py-3.5">Placa</th>
                      <th className="px-5 py-3.5">Vehículo</th>
                      <th className="px-5 py-3.5">Año</th>
                      <th className="px-5 py-3.5">Propietario</th>
                      <th className="px-5 py-3.5 text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#E4E6F2] bg-white">
                    {paginatedVehicles.map((vehicle) => (
                      <tr key={vehicle.id} className="transition hover:bg-[#F5F6FB]/60">
                        <td className="whitespace-nowrap px-5 py-3.5">
                          <span className="inline-flex rounded-md border border-[#1E1B4B] bg-amber-300 px-2.5 py-0.5 font-mono text-xs font-extrabold tracking-widest text-[#1E1B4B]">
                            {vehicle.plate}
                          </span>
                        </td>
                        <td className="px-5 py-3.5 font-semibold text-[#1E1B4B]">
                          {vehicle.brand} {vehicle.model}
                        </td>
                        <td className="whitespace-nowrap px-5 py-3.5 tabular-nums text-[#5B5F7A]">
                          {vehicle.year}
                        </td>
                        <td className="px-5 py-3.5 text-[#5B5F7A]">
                          {vehicle.customerName || '—'}
                        </td>
                        <td className="whitespace-nowrap px-5 py-3.5 text-right">
                          <div className="inline-flex items-center gap-2">
                            <Button
                              variant="ghost"
                              className="h-9 px-3 text-xs text-[#4F46E5]"
                              disabled={busyId === vehicle.id}
                              onClick={() => openEditModal(vehicle)}
                            >
                              Editar
                            </Button>
                            <Button
                              variant="danger"
                              className="h-9 px-3 text-xs"
                              disabled={busyId === vehicle.id}
                              onClick={() => handleDelete(vehicle)}
                            >
                              Eliminar
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {totalPages > 1 && (
                <div className="flex items-center justify-between border-t border-[#E4E6F2] px-5 py-3 text-xs text-[#5B5F7A]">
                  <span className="tabular-nums">
                    Página {safePage} de {totalPages}
                  </span>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="secondary"
                      className="h-9 px-3 text-xs"
                      disabled={safePage <= 1}
                      onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                    >
                      Anterior
                    </Button>
                    <Button
                      variant="secondary"
                      className="h-9 px-3 text-xs"
                      disabled={safePage >= totalPages}
                      onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                    >
                      Siguiente
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </section>
      </div>

      {showModal && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#1E1B4B]/45 p-4 backdrop-blur-xs"
        >
          <div className="w-full max-w-lg rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-float">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-[#1E1B4B]">
                {editingId ? 'Editar vehículo' : 'Nuevo vehículo'}
              </h2>
              <button
                type="button"
                onClick={closeModal}
                aria-label="Cerrar modal"
                className="flex h-8 w-8 items-center justify-center rounded-lg text-[#5B5F7A] hover:bg-[#F5F6FB]"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="space-y-1.5">
                <label htmlFor="vehicleCustomerId" className="block text-sm font-medium text-[#1E1B4B]">
                  Propietario
                </label>
                <select
                  id="vehicleCustomerId"
                  value={form.customerId}
                  onChange={(e) => {
                    setForm((prev) => ({ ...prev, customerId: e.target.value }))
                    clearFieldError('customerId')
                  }}
                  className={[
                    'block h-11 w-full rounded-xl border bg-white px-3.5 text-sm text-[#1E1B4B] focus:outline-none',
                    fieldErrors.customerId
                      ? 'border-[#B91C1C] focus:ring-2 focus:ring-[#B91C1C]/20'
                      : 'border-[#E4E6F2] focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5]/15',
                  ].join(' ')}
                  required
                >
                  <option value="">Selecciona un cliente...</option>
                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name} ({customer.phone})
                    </option>
                  ))}
                </select>
                {fieldErrors.customerId && (
                  <p className="text-xs font-medium text-[#B91C1C]">{fieldErrors.customerId}</p>
                )}
              </div>

              <PlateInput
                id="vehiclePlate"
                label="Placa"
                value={form.plate}
                onChange={(cleaned) => {
                  setForm((prev) => ({ ...prev, plate: cleaned }))
                  clearFieldError('plate')
                }}
                error={fieldErrors.plate}
                compact
                required
              />

              <div className="grid gap-4 sm:grid-cols-3">
                <Input
                  id="vehicleBrand"
                  label="Marca"
                  placeholder="Mazda"
                  value={form.brand}
                  onChange={(e) => {
                    setForm((prev) => ({ ...prev, brand: e.target.value }))
                    clearFieldError('brand')
                  }}
                  error={fieldErrors.brand}
                  required
                />
                <Input
                  id="vehicleModel"
                  label="Modelo"
                  placeholder="CX-30"
                  value={form.model}
                  onChange={(e) => {
                    setForm((prev) => ({ ...prev, model: e.target.value }))
                    clearFieldError('model')
                  }}
                  error={fieldErrors.model}
                  required
                />
                <Input
                  id="vehicleYear"
                  label="Año"
                  type="number"
                  min="1900"
                  max="2100"
                  step="1"
                  placeholder="2024"
                  value={form.year}
                  onChange={(e) => {
                    setForm((prev) => ({ ...prev, year: e.target.value }))
                    clearFieldError('year')
                  }}
                  error={fieldErrors.year}
                  required
                />
              </div>

              <div className="flex justify-end gap-3 border-t border-[#E4E6F2] pt-4">
                <Button type="button" variant="secondary" onClick={closeModal} disabled={submitting}>
                  Cancelar
                </Button>
                <Button type="submit" loading={submitting}>
                  {editingId ? 'Guardar cambios' : 'Guardar'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </MainLayout>
  )
}
