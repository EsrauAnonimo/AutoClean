import { useCallback, useEffect, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { getApiErrorMessage } from '../services/api'
import {
  createCustomer,
  deleteCustomer,
  getCustomers,
  updateCustomer,
} from '../services/customerService'

const PHONE_REGEX = /^[+]?[0-9\s\-()]{7,20}$/
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const INITIAL_CUSTOMER_FORM = {
  name: '',
  phone: '',
  email: '',
}

/**
 * Vista de gestión CRUD de clientes del lavadero.
 */
export function CustomersPage() {
  const [customers, setCustomers] = useState([])
  const [form, setForm] = useState(INITIAL_CUSTOMER_FORM)
  const [editingId, setEditingId] = useState(null)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadCustomers = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getCustomers()
      setCustomers(data)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar los clientes.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let active = true
    getCustomers()
      .then((data) => {
        if (active) setCustomers(data)
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar los clientes.'))
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
    setForm(INITIAL_CUSTOMER_FORM)
    setEditingId(null)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    const name = form.name.trim()
    const phone = form.phone.trim()
    const email = form.email.trim()

    if (!name || !PHONE_REGEX.test(phone) || !EMAIL_REGEX.test(email)) {
      setErrorMessage('Ingresa un nombre válido, teléfono (7-20 caracteres) y correo electrónico válido.')
      return
    }

    setSubmitting(true)
    try {
      if (editingId) {
        const updated = await updateCustomer(editingId, { name, phone, email })
        setCustomers((prev) => prev.map((item) => (item.id === editingId ? updated : item)))
      } else {
        const created = await createCustomer({ name, phone, email })
        setCustomers((prev) => [...prev, created])
      }
      resetForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el cliente.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleEdit = (customer) => {
    setEditingId(customer.id)
    setForm({
      name: customer.name || '',
      phone: customer.phone || '',
      email: customer.email || '',
    })
  }

  const handleDelete = async (customerId) => {
    if (!window.confirm('¿Deseas eliminar este cliente?')) return
    setErrorMessage('')
    setBusyId(customerId)
    try {
      await deleteCustomer(customerId)
      setCustomers((prev) => prev.filter((item) => item.id !== customerId))
      if (editingId === customerId) resetForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el cliente.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Clientes</h2>
          <p className="mt-1 text-sm text-slate-500">
            Administra los datos de contacto de los clientes registrados en AutoClean.
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
              {editingId ? 'Editar cliente' : 'Nuevo cliente'}
            </h3>
            {editingId && (
              <Button variant="secondary" onClick={resetForm}>
                Cancelar edición
              </Button>
            )}
          </div>

          <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Input
              id="customerName"
              label="Nombre completo"
              placeholder="Ana Gómez"
              value={form.name}
              onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
              required
            />
            <Input
              id="customerPhone"
              label="Teléfono"
              placeholder="+57 300 123 4567"
              value={form.phone}
              onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
              required
            />
            <Input
              id="customerEmail"
              label="Correo electrónico"
              type="email"
              placeholder="ana@correo.com"
              value={form.email}
              onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
              required
            />
            <div className="flex items-end">
              <Button type="submit" loading={submitting} className="w-full">
                {editingId ? 'Guardar cambios' : 'Agregar cliente'}
              </Button>
            </div>
          </form>
        </section>

        <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
            <h3 className="text-base font-semibold text-slate-900">Listado de clientes</h3>
            <Button variant="secondary" onClick={loadCustomers} disabled={loading}>
              Actualizar
            </Button>
          </div>

          {loading ? (
            <div className="p-8 text-center text-sm text-slate-500">Cargando clientes...</div>
          ) : customers.length === 0 ? (
            <div className="p-8 text-center text-sm text-slate-500">No hay clientes registrados.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                <thead className="bg-slate-50 text-xs font-semibold tracking-wider text-slate-600 uppercase">
                  <tr>
                    <th className="px-4 py-3">Nombre</th>
                    <th className="px-4 py-3">Teléfono</th>
                    <th className="px-4 py-3">Correo electrónico</th>
                    <th className="px-4 py-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {customers.map((customer) => (
                    <tr key={customer.id} className="hover:bg-slate-50/80">
                      <td className="px-4 py-3 font-medium text-slate-900">{customer.name}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-700">{customer.phone}</td>
                      <td className="px-4 py-3 text-slate-700">{customer.email}</td>
                      <td className="whitespace-nowrap px-4 py-3 text-right">
                        <div className="inline-flex gap-2">
                          <button
                            type="button"
                            disabled={busyId === customer.id}
                            onClick={() => handleEdit(customer)}
                            className="rounded-md px-2.5 py-1 text-xs font-semibold text-sky-700 hover:bg-sky-50"
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            disabled={busyId === customer.id}
                            onClick={() => handleDelete(customer.id)}
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
