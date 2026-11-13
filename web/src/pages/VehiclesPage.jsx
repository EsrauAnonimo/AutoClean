import { useCallback, useEffect, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { getApiErrorMessage } from '../services/api'
import { getCustomers } from '../services/customerService'
import {
  createVehicle,
  deleteVehicle,
  getVehicles,
  updateVehicle,
} from '../services/vehicleService'

const INITIAL_VEHICLE_FORM = {
  customerId: '',
  plate: '',
  brand: '',
  model: '',
  year: '',
}

/**
 * Vista de gestión CRUD de vehículos vinculados a su cliente propietario.
 */
export function VehiclesPage() {
  const [vehicles, setVehicles] = useState([])
  const [customers, setCustomers] = useState([])
  const [customerFilter, setCustomerFilter] = useState('')
  const [form, setForm] = useState(INITIAL_VEHICLE_FORM)
  const [editingId, setEditingId] = useState(null)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadData = useCallback(async (selectedCustomerId = customerFilter) => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [vehicleList, customerList] = await Promise.all([
        getVehicles(selectedCustomerId || undefined),
        getCustomers(),
      ])
      setVehicles(vehicleList)
      setCustomers(customerList)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar los vehículos.'))
    } finally {
      setLoading(false)
    }
  }, [customerFilter])

  useEffect(() => {
    let active = true
    Promise.all([getVehicles(), getCustomers()])
      .then(([vehicleList, customerList]) => {
        if (!active) return
        setVehicles(vehicleList)
        setCustomers(customerList)
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar los vehículos.'))
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  const resetForm = () => {
    setForm(INITIAL_VEHICLE_FORM)
    setEditingId(null)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    const plate = form.plate.trim().toUpperCase()
    const brand = form.brand.trim()
    const model = form.model.trim()
    const parsedYear = Number.parseInt(form.year, 10)

    if (!form.customerId || !plate || !brand || !model || Number.isNaN(parsedYear) || parsedYear < 1900 || parsedYear > 2100) {
      setErrorMessage('Completa cliente, placa, marca, modelo y un año válido entre 1900 y 2100.')
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
      } else {
        await createVehicle(payload)
      }
      resetForm()
      await loadData(customerFilter)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el vehículo.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleEdit = (vehicle) => {
    setEditingId(vehicle.id)
    setForm({
      customerId: vehicle.customerId || '',
      plate: vehicle.plate || '',
      brand: vehicle.brand || '',
      model: vehicle.model || '',
      year: String(vehicle.year ?? ''),
    })
  }

  const handleDelete = async (vehicleId) => {
    if (!window.confirm('¿Deseas eliminar este vehículo?')) return
    setErrorMessage('')
    setBusyId(vehicleId)
    try {
      await deleteVehicle(vehicleId)
      setVehicles((prev) => prev.filter((item) => item.id !== vehicleId))
      if (editingId === vehicleId) resetForm()
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
            <h2 className="text-2xl font-bold tracking-tight text-slate-900">Vehículos</h2>
            <p className="mt-1 text-sm text-slate-500">
              Registra los vehículos asociados a cada cliente para agendar citas.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="vehicleCustomerFilter" className="text-xs font-medium text-slate-600">
              Filtrar por cliente:
            </label>
            <select
              id="vehicleCustomerFilter"
              value={customerFilter}
              onChange={(e) => {
                const nextCustomerId = e.target.value
                setCustomerFilter(nextCustomerId)
                loadData(nextCustomerId)
              }}
              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-sky-500 focus:outline-none"
            >
              <option value="">Todos los clientes</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.name}
                </option>
              ))}
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
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-base font-semibold text-slate-900">
              {editingId ? 'Editar vehículo' : 'Nuevo vehículo'}
            </h3>
            {editingId && (
              <Button variant="secondary" onClick={resetForm}>
                Cancelar edición
              </Button>
            )}
          </div>

          <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <div className="space-y-1.5">
              <label htmlFor="vehicleCustomerId" className="block text-sm font-medium text-slate-700">
                Cliente propietario
              </label>
              <select
                id="vehicleCustomerId"
                value={form.customerId}
                onChange={(e) => setForm((prev) => ({ ...prev, customerId: e.target.value }))}
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

            <Input
              id="vehiclePlate"
              label="Placa"
              placeholder="ABC-123"
              value={form.plate}
              onChange={(e) => setForm((prev) => ({ ...prev, plate: e.target.value }))}
              required
            />
            <Input
              id="vehicleBrand"
              label="Marca"
              placeholder="Mazda"
              value={form.brand}
              onChange={(e) => setForm((prev) => ({ ...prev, brand: e.target.value }))}
              required
            />
            <Input
              id="vehicleModel"
              label="Modelo"
              placeholder="CX-30"
              value={form.model}
              onChange={(e) => setForm((prev) => ({ ...prev, model: e.target.value }))}
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
              onChange={(e) => setForm((prev) => ({ ...prev, year: e.target.value }))}
              required
            />

            <div className="flex items-end">
              <Button type="submit" loading={submitting} className="w-full">
                {editingId ? 'Guardar cambios' : 'Agregar vehículo'}
              </Button>
            </div>
          </form>
        </section>

        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
            <h3 className="text-base font-semibold text-slate-900">Listado de vehículos</h3>
            <Button variant="secondary" onClick={() => loadData(customerFilter)} disabled={loading}>
              Actualizar
            </Button>
          </div>

          {loading ? (
            <div className="p-8 text-center text-sm text-slate-500">Cargando vehículos...</div>
          ) : vehicles.length === 0 ? (
            <div className="p-8 text-center text-sm text-slate-500">No hay vehículos registrados.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                <thead className="bg-slate-50 text-xs font-semibold tracking-wider text-slate-600 uppercase">
                  <tr>
                    <th className="px-4 py-3">Placa</th>
                    <th className="px-4 py-3">Marca</th>
                    <th className="px-4 py-3">Modelo</th>
                    <th className="px-4 py-3">Año</th>
                    <th className="px-4 py-3">Propietario</th>
                    <th className="px-4 py-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {vehicles.map((vehicle) => (
                    <tr key={vehicle.id} className="hover:bg-slate-50/80">
                      <td className="whitespace-nowrap px-4 py-3 font-mono text-xs font-semibold text-slate-900">
                        {vehicle.plate}
                      </td>
                      <td className="px-4 py-3 text-slate-700">{vehicle.brand}</td>
                      <td className="px-4 py-3 text-slate-700">{vehicle.model}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-700">{vehicle.year}</td>
                      <td className="px-4 py-3 text-slate-700">{vehicle.customerName || '—'}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-right">
                        <div className="inline-flex gap-2">
                          <button
                            type="button"
                            disabled={busyId === vehicle.id}
                            onClick={() => handleEdit(vehicle)}
                            className="rounded-md px-2.5 py-1 text-xs font-semibold text-sky-700 hover:bg-sky-50"
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            disabled={busyId === vehicle.id}
                            onClick={() => handleDelete(vehicle.id)}
                            className="rounded-md px-2.5 py-1 text-xs font-semibold text-rose-600 hover:bg-rose-50"
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
      </div>
    </MainLayout>
  )
}
