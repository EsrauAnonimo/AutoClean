import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { CompleteCustomerProfileCard } from '../components/CompleteCustomerProfileCard'
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
import { showToast } from '../utils/toast'

/**
 * Pantalla Mis Vehículos del Portal del Cliente (mobile-first).
 * Muestra tarjetas visuales con placa colombiana, ícono de vehículo y FAB móvil.
 */
export function MyVehiclesPage() {
  const { user } = useAuth()
  const customerId = user?.customerId

  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(Boolean(customerId))
  const [errorMessage, setErrorMessage] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editingVehicle, setEditingVehicle] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [formFieldErrors, setFormFieldErrors] = useState({})
  const [deleteCandidate, setDeleteCandidate] = useState(null)
  const [busyId, setBusyId] = useState(null)

  const loadVehicles = useCallback(async () => {
    if (!customerId) {
      setVehicles([])
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
    loadVehicles()
  }, [loadVehicles])

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
    setShowForm(false)
    setEditingVehicle(null)
    setFormError('')
    setFormFieldErrors({})
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
        showToast({
          type: 'success',
          title: 'Vehículo actualizado',
          description: `La placa ${formPayload.plate} quedó actualizada.`,
        })
      } else {
        await createVehicle(payload)
        showToast({
          type: 'success',
          title: 'Vehículo agregado',
          description: `Placa ${formPayload.plate} lista para agendar.`,
        })
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

  const handleConfirmDelete = async () => {
    if (!deleteCandidate) {
      return
    }
    const target = deleteCandidate
    setBusyId(target.id)
    setErrorMessage('')
    try {
      await deleteVehicle(target.id)
      if (editingVehicle?.id === target.id) {
        closeForm()
      }
      setDeleteCandidate(null)
      showToast({
        type: 'info',
        title: 'Vehículo eliminado',
        description: `Se retiró la placa ${target.plate} de tu garage.`,
      })
      await loadVehicles()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el vehículo.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-[13px] font-medium text-[#5B5F7A]">Tu garage</p>
            <h1 className="text-[26px] font-bold tracking-tight text-[#1E1B4B]">Mis Vehículos</h1>
          </div>

          {customerId && (
            <div className="hidden items-center gap-2.5 sm:flex">
              <Link
                to="/portal/agendar"
                className="inline-flex h-11 items-center justify-center rounded-xl border border-[#E4E6F2] bg-white px-4 text-sm font-semibold text-[#1E1B4B] transition hover:bg-[#F5F6FB]"
              >
                Agendar cita
              </Link>
              <Button onClick={openCreateForm}>+ Agregar vehículo</Button>
            </div>
          )}
        </div>

        {!customerId ? (
          <CompleteCustomerProfileCard />
        ) : (
          <>
            {errorMessage && (
              <div
                role="alert"
                className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
              >
                <span>{errorMessage}</span>
                <button
                  type="button"
                  onClick={loadVehicles}
                  className="rounded-lg bg-white px-3 py-1 text-xs font-semibold text-[#B91C1C]"
                >
                  Reintentar
                </button>
              </div>
            )}

            {loading ? (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {[1, 2, 3].map((item) => (
                  <div
                    key={item}
                    className="h-48 animate-pulse rounded-2xl border border-[#E4E6F2] bg-white p-5"
                  >
                    <div className="h-8 w-28 rounded-lg bg-[#E4E6F2]" />
                    <div className="mt-4 h-5 w-40 rounded bg-[#E4E6F2]" />
                    <div className="mt-2 h-4 w-24 rounded bg-[#E4E6F2]" />
                  </div>
                ))}
              </div>
            ) : vehicles.length === 0 ? (
              <section className="rounded-2xl border border-dashed border-[#E4E6F2] bg-white p-10 text-center shadow-card">
                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-[#4F46E5]">
                  <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9">
                    <path d="M5 17h14v-5l-2-5H7l-2 5v5z" />
                    <circle cx="7.5" cy="17" r="1.5" />
                    <circle cx="16.5" cy="17" r="1.5" />
                  </svg>
                </div>
                <h2 className="mt-4 text-lg font-semibold text-[#1E1B4B]">
                  Aún no tienes vehículos guardados
                </h2>
                <p className="mt-1 text-sm text-[#5B5F7A]">
                  Registra tu primer vehículo para consultar y agendar en segundos por placa.
                </p>
                <div className="mt-5">
                  <Button size="l" onClick={openCreateForm}>
                    + Agregar mi primer vehículo
                  </Button>
                </div>
              </section>
            ) : (
              <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {vehicles.map((vehicle) => (
                  <article
                    key={vehicle.id}
                    className="flex flex-col justify-between rounded-2xl border border-[#E4E6F2] bg-white p-5 shadow-card transition hover:border-[#4F46E5]/40"
                  >
                    <div>
                      <div className="flex items-start justify-between gap-3">
                        <div className="overflow-hidden rounded-lg border-2 border-[#1E1B4B] bg-amber-300 shadow-xs">
                          <div className="bg-[#1E1B4B] px-2.5 py-0.5 text-center text-[8px] font-bold tracking-[0.2em] text-amber-300 uppercase">
                            Colombia
                          </div>
                          <div className="px-3 py-1 font-mono text-base font-extrabold tracking-widest text-[#1E1B4B]">
                            {vehicle.plate}
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          <span className="rounded-full bg-[#F5F6FB] px-2.5 py-1 text-xs font-semibold tabular-nums text-[#5B5F7A]">
                            {vehicle.year}
                          </span>
                          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-50 text-[#4F46E5]">
                            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                              <path d="M5 17h14v-5l-2-5H7l-2 5v5z" />
                              <circle cx="7.5" cy="17" r="1.5" />
                              <circle cx="16.5" cy="17" r="1.5" />
                            </svg>
                          </span>
                        </div>
                      </div>

                      <h2 className="mt-4 text-lg font-semibold text-[#1E1B4B]">
                        {vehicle.brand} {vehicle.model}
                      </h2>
                      <p className="mt-0.5 text-xs text-[#5B5F7A]">Listo para agendamiento rápido</p>
                    </div>

                    <div className="mt-5 space-y-2 border-t border-[#E4E6F2] pt-4">
                      <Link
                        to={`/portal/agendar?plate=${encodeURIComponent(vehicle.plate || '')}`}
                        className="flex h-11 w-full items-center justify-center rounded-xl bg-[#4F46E5] px-4 text-sm font-semibold text-white transition hover:bg-[#4338CA]"
                      >
                        Agendar con esta placa
                      </Link>
                      <div className="grid grid-cols-2 gap-2">
                        <Button
                          variant="secondary"
                          size="m"
                          onClick={() => openEditForm(vehicle)}
                          disabled={busyId === vehicle.id}
                        >
                          Editar
                        </Button>
                        <Button
                          variant="danger"
                          size="m"
                          onClick={() => setDeleteCandidate(vehicle)}
                          disabled={busyId === vehicle.id}
                        >
                          Eliminar
                        </Button>
                      </div>
                    </div>
                  </article>
                ))}
              </section>
            )}

            <button
              type="button"
              onClick={openCreateForm}
              className="fixed right-4 bottom-24 z-30 inline-flex h-13 items-center gap-2 rounded-full bg-[#4F46E5] px-5 text-sm font-semibold text-white shadow-float transition hover:bg-[#4338CA] sm:hidden"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Agregar vehículo
            </button>
          </>
        )}
      </div>

      {showForm && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#1E1B4B]/45 p-4 backdrop-blur-xs"
        >
          <div className="w-full max-w-lg rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-float">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-[#1E1B4B]">
                  {editingVehicle ? `Editar vehículo ${editingVehicle.plate}` : 'Agregar vehículo'}
                </h2>
                <p className="text-xs text-[#5B5F7A]">
                  Ingresa la placa en formato ABC123, marca, modelo y año.
                </p>
              </div>
              <button
                type="button"
                onClick={closeForm}
                aria-label="Cerrar modal"
                className="flex h-8 w-8 items-center justify-center rounded-lg text-[#5B5F7A] hover:bg-[#F5F6FB]"
              >
                ✕
              </button>
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
          </div>
        </div>
      )}

      {deleteCandidate && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#1E1B4B]/45 p-4 backdrop-blur-xs"
        >
          <div className="w-full max-w-md rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-float">
            <h2 className="text-lg font-semibold text-[#1E1B4B]">¿Eliminar vehículo?</h2>
            <p className="mt-2 text-sm text-[#5B5F7A]">
              Vas a retirar el vehículo{' '}
              <strong className="font-semibold text-[#1E1B4B]">
                {deleteCandidate.brand} {deleteCandidate.model} ({deleteCandidate.plate})
              </strong>
              .
            </p>
            <div className="mt-6 flex justify-end gap-3">
              <Button
                variant="secondary"
                onClick={() => setDeleteCandidate(null)}
                disabled={busyId === deleteCandidate.id}
              >
                Cancelar
              </Button>
              <Button
                variant="danger"
                loading={busyId === deleteCandidate.id}
                onClick={handleConfirmDelete}
              >
                Eliminar
              </Button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  )
}
