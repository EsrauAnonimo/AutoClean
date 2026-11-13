import { useCallback, useEffect, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { getApiErrorMessage } from '../services/api'
import { createSupply, deleteSupply, getSupplies, updateSupply } from '../services/supplyService'

const INITIAL_SUPPLY_FORM = {
  name: '',
  stock: '',
  unit: '',
}

/**
 * Vista de gestión CRUD del inventario de insumos.
 */
export function SuppliesPage() {
  const [supplies, setSupplies] = useState([])
  const [form, setForm] = useState(INITIAL_SUPPLY_FORM)
  const [editingId, setEditingId] = useState(null)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadSupplies = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getSupplies()
      setSupplies(data)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el inventario de insumos.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let active = true
    getSupplies()
      .then((data) => {
        if (active) setSupplies(data)
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el inventario de insumos.'))
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
    setForm(INITIAL_SUPPLY_FORM)
    setEditingId(null)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    const parsedStock = Number.parseInt(form.stock, 10)
    if (!form.name.trim() || !form.unit.trim() || Number.isNaN(parsedStock) || parsedStock < 0) {
      setErrorMessage('Ingresa nombre, unidad de medida y un stock entero mayor o igual a 0.')
      return
    }

    const payload = {
      name: form.name.trim(),
      stock: parsedStock,
      unit: form.unit.trim(),
    }

    setSubmitting(true)
    try {
      if (editingId) {
        const updated = await updateSupply(editingId, payload)
        setSupplies((prev) => prev.map((item) => (item.id === editingId ? updated : item)))
      } else {
        const created = await createSupply(payload)
        setSupplies((prev) => [...prev, created])
      }
      resetForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el insumo.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleEdit = (supply) => {
    setEditingId(supply.id)
    setForm({
      name: supply.name || '',
      stock: String(supply.stock ?? ''),
      unit: supply.unit || '',
    })
  }

  const handleDelete = async (supplyId) => {
    if (!window.confirm('¿Deseas eliminar este insumo del inventario?')) return
    setErrorMessage('')
    setBusyId(supplyId)
    try {
      await deleteSupply(supplyId)
      setSupplies((prev) => prev.filter((item) => item.id !== supplyId))
      if (editingId === supplyId) resetForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el insumo.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Insumos</h2>
          <p className="mt-1 text-sm text-slate-500">
            Controla existencias y unidades de los productos utilizados en cada servicio.
          </p>
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
              {editingId ? 'Editar insumo' : 'Nuevo insumo'}
            </h3>
            {editingId && (
              <Button variant="secondary" onClick={resetForm}>
                Cancelar edición
              </Button>
            )}
          </div>

          <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-4">
            <Input
              id="supplyName"
              label="Nombre del insumo"
              placeholder="Shampoo pH Neutro"
              value={form.name}
              onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
              required
            />
            <Input
              id="supplyStock"
              label="Stock"
              type="number"
              min="0"
              step="1"
              placeholder="15"
              value={form.stock}
              onChange={(e) => setForm((prev) => ({ ...prev, stock: e.target.value }))}
              required
            />
            <Input
              id="supplyUnit"
              label="Unidad"
              placeholder="Litros"
              value={form.unit}
              onChange={(e) => setForm((prev) => ({ ...prev, unit: e.target.value }))}
              required
            />
            <div className="flex items-end">
              <Button type="submit" loading={submitting} className="w-full">
                {editingId ? 'Guardar cambios' : 'Agregar insumo'}
              </Button>
            </div>
          </form>
        </section>

        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
            <h3 className="text-base font-semibold text-slate-900">Inventario actual</h3>
            <Button variant="secondary" onClick={loadSupplies} disabled={loading}>
              Actualizar
            </Button>
          </div>

          {loading ? (
            <div className="p-8 text-center text-sm text-slate-500">Cargando insumos...</div>
          ) : supplies.length === 0 ? (
            <div className="p-8 text-center text-sm text-slate-500">No hay insumos registrados.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                <thead className="bg-slate-50 text-xs font-semibold tracking-wider text-slate-600 uppercase">
                  <tr>
                    <th className="px-4 py-3">Nombre</th>
                    <th className="px-4 py-3">Stock</th>
                    <th className="px-4 py-3">Unidad</th>
                    <th className="px-4 py-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {supplies.map((supply) => (
                    <tr key={supply.id} className="hover:bg-slate-50/80">
                      <td className="px-4 py-3 font-medium text-slate-900">{supply.name}</td>
                      <td className="whitespace-nowrap px-4 py-3">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            supply.stock === 0
                              ? 'bg-rose-50 text-rose-700 ring-1 ring-rose-200'
                              : 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200'
                          }`}
                        >
                          {supply.stock}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-slate-700">{supply.unit}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-right">
                        <div className="inline-flex gap-2">
                          <button
                            type="button"
                            disabled={busyId === supply.id}
                            onClick={() => handleEdit(supply)}
                            className="rounded-md px-2.5 py-1 text-xs font-semibold text-sky-700 hover:bg-sky-50"
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            disabled={busyId === supply.id}
                            onClick={() => handleDelete(supply.id)}
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
