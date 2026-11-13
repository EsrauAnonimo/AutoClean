import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { getApiErrorMessage } from '../services/api'
import {
  createSupply,
  deleteSupply,
  getSupplies,
  updateSupply,
} from '../services/supplyService'
import { showToast } from '../utils/toast'

const INITIAL_FORM = {
  name: '',
  stock: '',
  unit: '',
}

const LOW_STOCK_THRESHOLD = 5

/**
 * Inventario de insumos (Admin): alertas automáticas de bajo stock y modal crear/editar.
 */
export function SuppliesPage() {
  const [supplies, setSupplies] = useState([])
  const [searchQuery, setSearchQuery] = useState('')
  const [onlyLowStock, setOnlyLowStock] = useState(false)

  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(INITIAL_FORM)
  const [fieldErrors, setFieldErrors] = useState({})

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadSupplies = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getSupplies()
      setSupplies(Array.isArray(data) ? data : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el inventario de insumos.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadSupplies()
  }, [loadSupplies])

  const lowStockCount = useMemo(
    () => supplies.filter((item) => Number(item.stock) <= LOW_STOCK_THRESHOLD).length,
    [supplies],
  )

  const filteredSupplies = useMemo(() => {
    const q = searchQuery.trim().toLowerCase()
    return supplies.filter((item) => {
      if (onlyLowStock && Number(item.stock) > LOW_STOCK_THRESHOLD) {
        return false
      }
      if (!q) return true
      return (
        item.name?.toLowerCase().includes(q) ||
        item.unit?.toLowerCase().includes(q)
      )
    })
  }, [supplies, searchQuery, onlyLowStock])

  const openCreateModal = () => {
    setEditingId(null)
    setForm(INITIAL_FORM)
    setFieldErrors({})
    setShowModal(true)
  }

  const openEditModal = (supply) => {
    setEditingId(supply.id)
    setForm({
      name: supply.name || '',
      stock: String(supply.stock ?? ''),
      unit: supply.unit || '',
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

    const name = form.name.trim()
    const unit = form.unit.trim()
    const parsedStock = Number.parseInt(form.stock, 10)
    const errors = {}

    if (!name) errors.name = 'El nombre del insumo es obligatorio.'
    if (!unit) errors.unit = 'La unidad es obligatoria.'
    if (Number.isNaN(parsedStock) || parsedStock < 0) {
      errors.stock = 'El stock debe ser un número entero mayor o igual a 0.'
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setSubmitting(true)
    try {
      const payload = { name, stock: parsedStock, unit }
      if (editingId) {
        const updated = await updateSupply(editingId, payload)
        setSupplies((prev) => prev.map((item) => (item.id === editingId ? updated : item)))
        showToast({ type: 'success', title: 'Insumo actualizado', description: updated.name })
      } else {
        const created = await createSupply(payload)
        setSupplies((prev) => [...prev, created])
        showToast({ type: 'success', title: 'Insumo agregado', description: created.name })
      }
      closeModal()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el insumo.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (supply) => {
    if (!window.confirm(`¿Deseas eliminar "${supply.name}" del inventario?`)) return
    setErrorMessage('')
    setBusyId(supply.id)
    try {
      await deleteSupply(supply.id)
      setSupplies((prev) => prev.filter((item) => item.id !== supply.id))
      if (editingId === supply.id) closeModal()
      showToast({ type: 'info', title: 'Insumo eliminado', description: supply.name })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el insumo.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] font-bold tracking-tight text-[#1E1B4B]">Insumos</h1>
            <p className="mt-1 text-sm text-[#5B5F7A]">
              Control de existencias de productos y alertas tempranas de reposición.
            </p>
          </div>
          <Button onClick={openCreateModal}>+ Nuevo insumo</Button>
        </div>

        {lowStockCount > 0 && (
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#B45309]/25 bg-[#FEF3C7] px-5 py-3.5 text-sm text-[#B45309]">
            <div className="flex items-center gap-2.5">
              <span className="h-2.5 w-2.5 rounded-full bg-[#B45309]" />
              <span className="font-semibold">
                {lowStockCount} {lowStockCount === 1 ? 'insumo con' : 'insumos con'} bajo inventario (≤{' '}
                {LOW_STOCK_THRESHOLD} unidades)
              </span>
            </div>
            <button
              type="button"
              onClick={() => setOnlyLowStock((prev) => !prev)}
              className="rounded-lg bg-white px-3 py-1.5 text-xs font-semibold text-[#B45309] shadow-xs"
            >
              {onlyLowStock ? 'Ver todos' : 'Filtrar bajo stock'}
            </button>
          </div>
        )}

        {errorMessage && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
          >
            <span>{errorMessage}</span>
            <button
              type="button"
              onClick={loadSupplies}
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
              placeholder="Buscar insumo o unidad..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="h-11 w-full rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 pr-3.5 pl-10 text-sm text-[#1E1B4B] placeholder:text-[#5B5F7A]/70 focus:border-[#4F46E5] focus:bg-white focus:ring-2 focus:ring-[#4F46E5]/15 focus:outline-none"
            />
          </div>

          <Button variant="secondary" onClick={loadSupplies} disabled={loading}>
            Actualizar
          </Button>
        </section>

        <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
          {loading ? (
            <div className="space-y-3 p-6">
              {[1, 2, 3].map((row) => (
                <div key={row} className="h-12 animate-pulse rounded-xl bg-[#F5F6FB]" />
              ))}
            </div>
          ) : filteredSupplies.length === 0 ? (
            <div className="p-12 text-center">
              <p className="text-base font-semibold text-[#1E1B4B]">No hay insumos en esta vista</p>
              <p className="mt-1 text-sm text-[#5B5F7A]">
                Registra shampoo, ceras, microfibras o limpia-llantas para controlar su stock.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                  <tr>
                    <th className="px-5 py-3.5">Producto</th>
                    <th className="px-5 py-3.5">Nivel de stock</th>
                    <th className="px-5 py-3.5">Estado</th>
                    <th className="px-5 py-3.5">Unidad</th>
                    <th className="px-5 py-3.5 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#E4E6F2] bg-white">
                  {filteredSupplies.map((supply) => {
                    const stockNum = Number(supply.stock) || 0
                    const isLow = stockNum <= LOW_STOCK_THRESHOLD
                    const barPercent = Math.min(100, Math.round((stockNum / 20) * 100))

                    return (
                      <tr key={supply.id} className="transition hover:bg-[#F5F6FB]/60">
                        <td className="px-5 py-3.5 font-semibold text-[#1E1B4B]">{supply.name}</td>
                        <td className="px-5 py-3.5">
                          <div className="flex items-center gap-3">
                            <div className="h-2 w-24 overflow-hidden rounded-full bg-[#F5F6FB]">
                              <div
                                className={[
                                  'h-full rounded-full transition-all',
                                  stockNum === 0
                                    ? 'bg-[#B91C1C]'
                                    : isLow
                                      ? 'bg-[#B45309]'
                                      : 'bg-[#15803D]',
                                ].join(' ')}
                                style={{ width: `${Math.max(stockNum > 0 ? 10 : 0, barPercent)}%` }}
                              />
                            </div>
                            <span className="text-sm font-bold tabular-nums text-[#1E1B4B]">
                              {stockNum}
                            </span>
                          </div>
                        </td>
                        <td className="whitespace-nowrap px-5 py-3.5">
                          {stockNum === 0 ? (
                            <span className="inline-flex rounded-full bg-[#FEE2E2] px-2.5 py-1 text-xs font-semibold text-[#B91C1C]">
                              Agotado
                            </span>
                          ) : isLow ? (
                            <span className="inline-flex rounded-full bg-[#FEE2E2] px-2.5 py-1 text-xs font-semibold text-[#B91C1C]">
                              Bajo inventario
                            </span>
                          ) : (
                            <span className="inline-flex rounded-full bg-[#DCFCE7] px-2.5 py-1 text-xs font-semibold text-[#15803D]">
                              Disponible
                            </span>
                          )}
                        </td>
                        <td className="px-5 py-3.5 text-[#5B5F7A]">{supply.unit}</td>
                        <td className="whitespace-nowrap px-5 py-3.5 text-right">
                          <div className="inline-flex items-center gap-2">
                            <Button
                              variant="ghost"
                              className="h-9 px-3 text-xs text-[#4F46E5]"
                              disabled={busyId === supply.id}
                              onClick={() => openEditModal(supply)}
                            >
                              Editar
                            </Button>
                            <Button
                              variant="danger"
                              className="h-9 px-3 text-xs"
                              disabled={busyId === supply.id}
                              onClick={() => handleDelete(supply)}
                            >
                              Eliminar
                            </Button>
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

      {showModal && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#1E1B4B]/45 p-4 backdrop-blur-xs"
        >
          <div className="w-full max-w-md rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-float">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-[#1E1B4B]">
                {editingId ? 'Editar insumo' : 'Nuevo insumo'}
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
              <Input
                id="supplyName"
                label="Nombre del producto"
                placeholder="Shampoo pH Neutro"
                value={form.name}
                onChange={(e) => {
                  setForm((prev) => ({ ...prev, name: e.target.value }))
                  clearFieldError('name')
                }}
                error={fieldErrors.name}
                required
              />
              <div className="grid gap-4 sm:grid-cols-2">
                <Input
                  id="supplyStock"
                  label="Stock"
                  type="number"
                  min="0"
                  step="1"
                  placeholder="15"
                  value={form.stock}
                  onChange={(e) => {
                    setForm((prev) => ({ ...prev, stock: e.target.value }))
                    clearFieldError('stock')
                  }}
                  error={fieldErrors.stock}
                  required
                />
                <Input
                  id="supplyUnit"
                  label="Unidad"
                  placeholder="Litros"
                  value={form.unit}
                  onChange={(e) => {
                    setForm((prev) => ({ ...prev, unit: e.target.value }))
                    clearFieldError('unit')
                  }}
                  error={fieldErrors.unit}
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
