import { useCallback, useEffect, useMemo, useState } from 'react'
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
import { showToast } from '../utils/toast'

const INITIAL_FORM = {
  name: '',
  phone: '',
  email: '',
}

const PHONE_REGEX = /^[+]?[0-9\s\-()]{7,20}$/
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PAGE_SIZE = 8

/**
 * Gestión de clientes (Admin): buscador, paginación, tabla moderna y modal crear/editar.
 */
export function CustomersPage() {
  const [customers, setCustomers] = useState([])
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

  const loadCustomers = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getCustomers()
      setCustomers(Array.isArray(data) ? data : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar la lista de clientes.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadCustomers()
  }, [loadCustomers])

  const filteredCustomers = useMemo(() => {
    const q = searchQuery.trim().toLowerCase()
    if (!q) return customers
    return customers.filter(
      (customer) =>
        customer.name?.toLowerCase().includes(q) ||
        customer.phone?.toLowerCase().includes(q) ||
        customer.email?.toLowerCase().includes(q),
    )
  }, [customers, searchQuery])

  const totalPages = Math.max(1, Math.ceil(filteredCustomers.length / PAGE_SIZE))
  const safePage = Math.min(currentPage, totalPages)
  const paginatedCustomers = useMemo(() => {
    const start = (safePage - 1) * PAGE_SIZE
    return filteredCustomers.slice(start, start + PAGE_SIZE)
  }, [filteredCustomers, safePage])

  const openCreateModal = () => {
    setEditingId(null)
    setForm(INITIAL_FORM)
    setFieldErrors({})
    setShowModal(true)
  }

  const openEditModal = (customer) => {
    setEditingId(customer.id)
    setForm({
      name: customer.name || '',
      phone: customer.phone || '',
      email: customer.email || '',
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

  const handleFieldChange = (field) => (event) => {
    const value = event.target.value
    setForm((prev) => ({ ...prev, [field]: value }))
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
    const phone = form.phone.trim()
    const email = form.email.trim()
    const errors = {}

    if (!name) {
      errors.name = 'El nombre completo es obligatorio.'
    }
    if (!phone || !PHONE_REGEX.test(phone)) {
      errors.phone = 'Ingresa un teléfono válido (7 a 20 caracteres).'
    }
    if (!email || !EMAIL_REGEX.test(email)) {
      errors.email = 'Ingresa un correo electrónico válido.'
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setSubmitting(true)
    try {
      if (editingId) {
        const updated = await updateCustomer(editingId, { name, phone, email })
        setCustomers((prev) => prev.map((item) => (item.id === editingId ? updated : item)))
        showToast({ type: 'success', title: 'Cliente actualizado', description: updated.name })
      } else {
        const created = await createCustomer({ name, phone, email })
        setCustomers((prev) => [...prev, created])
        showToast({ type: 'success', title: 'Cliente creado', description: created.name })
      }
      closeModal()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el cliente.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (customer) => {
    if (!window.confirm(`¿Deseas eliminar al cliente ${customer.name}?`)) return
    setErrorMessage('')
    setBusyId(customer.id)
    try {
      await deleteCustomer(customer.id)
      setCustomers((prev) => prev.filter((item) => item.id !== customer.id))
      if (editingId === customer.id) closeModal()
      showToast({ type: 'info', title: 'Cliente eliminado', description: customer.name })
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el cliente.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] font-bold tracking-tight text-[#1E1B4B]">Clientes</h1>
            <p className="mt-1 text-sm text-[#5B5F7A]">
              Directorio y datos de contacto de los propietarios registrados.
            </p>
          </div>
          <Button onClick={openCreateModal}>+ Nuevo cliente</Button>
        </div>

        {errorMessage && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
          >
            <span>{errorMessage}</span>
            <button
              type="button"
              onClick={loadCustomers}
              className="rounded-lg bg-white px-3 py-1 text-xs font-semibold text-[#B91C1C]"
            >
              Reintentar
            </button>
          </div>
        )}

        <section className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#E4E6F2] bg-white p-4 shadow-card">
          <div className="relative flex-1 min-w-[240px]">
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
              placeholder="Buscar por nombre, teléfono o correo..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value)
                setCurrentPage(1)
              }}
              className="h-11 w-full rounded-xl border border-[#E4E6F2] bg-[#F5F6FB]/70 pr-3.5 pl-10 text-sm text-[#1E1B4B] placeholder:text-[#5B5F7A]/70 focus:border-[#4F46E5] focus:bg-white focus:ring-2 focus:ring-[#4F46E5]/15 focus:outline-none"
            />
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs font-medium text-[#5B5F7A] tabular-nums">
              {filteredCustomers.length} clientes
            </span>
            <Button variant="secondary" onClick={loadCustomers} disabled={loading}>
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
          ) : filteredCustomers.length === 0 ? (
            <div className="p-12 text-center">
              <p className="text-base font-semibold text-[#1E1B4B]">
                {searchQuery ? 'Sin coincidencias para tu búsqueda' : 'Aún no hay clientes registrados'}
              </p>
              <p className="mt-1 text-sm text-[#5B5F7A]">
                {searchQuery
                  ? 'Prueba con otro nombre, teléfono o correo.'
                  : 'Registra el primer cliente para asociarle vehículos y citas.'}
              </p>
              {!searchQuery && (
                <div className="mt-5">
                  <Button onClick={openCreateModal}>+ Nuevo cliente</Button>
                </div>
              )}
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                  <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                    <tr>
                      <th className="px-5 py-3.5">Nombre</th>
                      <th className="px-5 py-3.5">Teléfono</th>
                      <th className="px-5 py-3.5">Correo electrónico</th>
                      <th className="px-5 py-3.5 text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#E4E6F2] bg-white">
                    {paginatedCustomers.map((customer) => (
                      <tr key={customer.id} className="transition hover:bg-[#F5F6FB]/60">
                        <td className="px-5 py-3.5 font-semibold text-[#1E1B4B]">{customer.name}</td>
                        <td className="whitespace-nowrap px-5 py-3.5 tabular-nums text-[#5B5F7A]">
                          {customer.phone}
                        </td>
                        <td className="px-5 py-3.5 text-[#5B5F7A]">{customer.email}</td>
                        <td className="whitespace-nowrap px-5 py-3.5 text-right">
                          <div className="inline-flex items-center gap-2">
                            <Button
                              variant="ghost"
                              className="h-9 px-3 text-xs text-[#4F46E5]"
                              disabled={busyId === customer.id}
                              onClick={() => openEditModal(customer)}
                            >
                              Editar
                            </Button>
                            <Button
                              variant="danger"
                              className="h-9 px-3 text-xs"
                              disabled={busyId === customer.id}
                              onClick={() => handleDelete(customer)}
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
          <div className="w-full max-w-md rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-float">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-[#1E1B4B]">
                {editingId ? 'Editar cliente' : 'Nuevo cliente'}
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
                id="customerName"
                label="Nombre completo"
                placeholder="Ana Gómez"
                value={form.name}
                onChange={handleFieldChange('name')}
                error={fieldErrors.name}
                required
              />
              <Input
                id="customerPhone"
                label="Teléfono"
                placeholder="+57 300 123 4567"
                value={form.phone}
                onChange={handleFieldChange('phone')}
                error={fieldErrors.phone}
                required
              />
              <Input
                id="customerEmail"
                label="Correo electrónico"
                type="email"
                placeholder="ana@correo.com"
                value={form.email}
                onChange={handleFieldChange('email')}
                error={fieldErrors.email}
                required
              />

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
