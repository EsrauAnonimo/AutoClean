import { useCallback, useEffect, useState } from 'react'
import { Button } from '../components/Button'
import { MainLayout } from '../components/MainLayout'
import { VehicleForm } from '../components/VehicleForm'
import { useAuth } from '../context/AuthContext'
import { getApiErrorMessage } from '../services/api'
import {
  createVehicle,
  deleteVehicle,
  getVehiclesByCustomer,
  updateVehicle,
} from '../services/vehicleService'

/**
 * Pantalla "Mis Vehículos" del Portal de Clientes para listar, registrar, editar y eliminar
 * exclusivamente los vehículos del usuario autenticado.
 */
export function MyVehiclesPage() {
  const { user } = useAuth()
  const customerId = user?.customerId ?? null

  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(Boolean(customerId))
  const [errorMessage, setErrorMessage] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [editingVehicle, setEditingVehicle] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [formFieldErrors, setFormFieldErrors] = useState({})
  const [busyId, setBusyId] = useState(null)

  const loadVehicles = useCallback(async () => {
    if (!customerId) {
      setLoading(false)
      return
    }
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getVehiclesByCustomer(customerId)
      setVehicles(Array.isArray(data) ? data : [])
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar tus vehículos.'))
    } finally {
      setLoading(false)
    }
  }, [customerId])

  useEffect(() => {
    if (!customerId) {
      return
    }
    let active = true

    getVehiclesByCustomer(customerId)
      .then((data) => {
        if (active) {
          setVehicles(Array.isArray(data) ? data : [])
        }
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar tus vehículos.'))
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

  const openCreateForm = () => {
    setEditingVehicle(null)
    setFormError('')
    setFormFieldErrors({})
    setShowForm(true)
  }

  const openEditForm = (vehicle) => {
    setEditingVehicle(vehicle)
    setFormError('')
    setFormFieldErrors({})
    setShowForm(true)
  }

  const closeForm = () => {
    setEditingVehicle(null)
    setFormError('')
    setFormFieldErrors({})
    setShowForm(false)
  }

  const handleSaveVehicle = async (formPayload) => {
    if (!customerId) {
      return
    }
    setSubmitting(true)
    setFormError('')
    setFormFieldErrors({})

    const payload = {
      customerId,
      ...formPayload,
    }

    try {
      if (editingVehicle?.id) {
        await updateVehicle(editingVehicle.id, payload)
      } else {
        await createVehicle(payload)
      }
      closeForm()
      await loadVehicles()
    } catch (error) {
      const validationErrors = error.response?.data?.validationErrors
      if (validationErrors && typeof validationErrors === 'object') {
        setFormFieldErrors(validationErrors)
      }
      setFormError(getApiErrorMessage(error, 'No fue posible guardar el vehículo.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeleteVehicle = async (vehicle) => {
    const confirmed = window.confirm(`¿Seguro que deseas eliminar el vehículo ${vehicle.plate}?`)
    if (!confirmed) {
      return
    }

    setBusyId(vehicle.id)
    setErrorMessage('')
    try {
      await deleteVehicle(vehicle.id)
      if (editingVehicle?.id === vehicle.id) {
        closeForm()
      }
      await loadVehicles()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el vehículo.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h2 className="text-xl font-bold text-slate-900">Mis Vehículos</h2>
            <p className="mt-1 text-sm text-slate-600">
              Administra los vehículos asociados a tu cuenta para agendar servicios rápidamente.
            </p>
          </div>
          {customerId && !showForm && (
            <Button onClick={openCreateForm}>+ Agregar vehículo</Button>
          )}
        </section>

        {!customerId ? (
          <div
            role="alert"
            className="rounded-2xl border border-amber-200 bg-amber-50 p-6 text-sm text-amber-800 shadow-xs"
          >
            <p className="font-semibold">Perfil de cliente no vinculado</p>
            <p className="mt-1">
              Tu cuenta aún no tiene un identificador de cliente asignado (<code>customerId</code>).
              Para registrar vehículos, crea una cuenta ingresando tu nombre y teléfono o comunícate con AutoClean.
            </p>
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

            {showForm && (
              <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-4">
                  <h3 className="text-base font-bold text-slate-900">
                    {editingVehicle ? `Editar vehículo ${editingVehicle.plate}` : 'Registrar nuevo vehículo'}
                  </h3>
                  <p className="text-xs text-slate-500">
                    Ingresa placa, marca, modelo y año (entre 1900 y 2100).
                  </p>
                </div>

                <VehicleForm
                  key={editingVehicle?.id ?? 'new'}
                  initialVehicle={editingVehicle}
                  onSubmit={handleSaveVehicle}
                  onCancel={closeForm}
                  submitting={submitting}
                  serverError={formError}
                  serverFieldErrors={formFieldErrors}
                />
              </section>
            )}

            {loading ? (
              <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-xs">
                Cargando tus vehículos...
              </div>
            ) : vehicles.length === 0 ? (
              <section className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center shadow-xs">
                <h3 className="text-base font-semibold text-slate-900">
                  Aún no tienes vehículos registrados
                </h3>
                <p className="mt-1 text-sm text-slate-500">
                  Registra tu primer vehículo para poder agendar citas de lavado o detailing.
                </p>
                {!showForm && (
                  <div className="mt-5">
                    <Button onClick={openCreateForm}>Agregar mi primer vehículo</Button>
                  </div>
                )}
              </section>
            ) : (
              <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {vehicles.map((vehicle) => (
                  <article
                    key={vehicle.id}
                    className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-xs transition hover:border-sky-300"
                  >
                    <div>
                      <div className="flex items-center justify-between gap-2">
                        <span className="inline-flex items-center rounded-lg bg-slate-900 px-2.5 py-1 text-xs font-bold tracking-wider text-white">
                          {vehicle.plate}
                        </span>
                        <span className="rounded-full bg-sky-50 px-2.5 py-0.5 text-xs font-semibold text-sky-700">
                          {vehicle.year}
                        </span>
                      </div>

                      <h3 className="mt-3 text-base font-bold text-slate-900">
                        {vehicle.brand} {vehicle.model}
                      </h3>
                    </div>

                    <div className="mt-5 flex items-center gap-2 border-t border-slate-100 pt-4">
                      <Button
                        variant="secondary"
                        className="flex-1 py-2 text-xs"
                        onClick={() => openEditForm(vehicle)}
                        disabled={busyId === vehicle.id}
                      >
                        Editar
                      </Button>
                      <button
                        type="button"
                        onClick={() => handleDeleteVehicle(vehicle)}
                        disabled={busyId === vehicle.id}
                        className="flex-1 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {busyId === vehicle.id ? 'Eliminando...' : 'Eliminar'}
                      </button>
                    </div>
                  </article>
                ))}
              </section>
            )}
          </>
        )}
      </div>
    </MainLayout>
  )
}
