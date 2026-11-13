import { useCallback, useEffect, useState } from 'react'
import { Button } from '../components/Button'
import { Input } from '../components/Input'
import { MainLayout } from '../components/MainLayout'
import { getApiErrorMessage } from '../services/api'
import {
  createEmployee,
  createServiceType,
  deleteEmployee,
  deleteServiceType,
  getEmployees,
  getServiceTypes,
  updateEmployee,
  updateServiceType,
} from '../services/catalogService'

const INITIAL_SERVICE_FORM = {
  name: '',
  description: '',
  price: '',
  durationMin: '',
}

const INITIAL_EMPLOYEE_FORM = {
  name: '',
  role: '',
}

/**
 * Vista de administración del catálogo de servicios y del personal operativo.
 */
export function CatalogPage({ initialTab = 'services' }) {
  const [activeTab, setActiveTab] = useState(initialTab)

  const [services, setServices] = useState([])
  const [employees, setEmployees] = useState([])

  const [serviceForm, setServiceForm] = useState(INITIAL_SERVICE_FORM)
  const [editingServiceId, setEditingServiceId] = useState(null)

  const [employeeForm, setEmployeeForm] = useState(INITIAL_EMPLOYEE_FORM)
  const [editingEmployeeId, setEditingEmployeeId] = useState(null)

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  const loadCatalog = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [serviceList, employeeList] = await Promise.all([getServiceTypes(), getEmployees()])
      setServices(serviceList)
      setEmployees(employeeList)
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el catálogo y los empleados.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let active = true
    Promise.all([getServiceTypes(), getEmployees()])
      .then(([serviceList, employeeList]) => {
        if (!active) return
        setServices(serviceList)
        setEmployees(employeeList)
      })
      .catch((error) => {
        if (active) {
          setErrorMessage(getApiErrorMessage(error, 'No fue posible cargar el catálogo y los empleados.'))
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  const resetServiceForm = () => {
    setServiceForm(INITIAL_SERVICE_FORM)
    setEditingServiceId(null)
  }

  const resetEmployeeForm = () => {
    setEmployeeForm(INITIAL_EMPLOYEE_FORM)
    setEditingEmployeeId(null)
  }

  const handleSaveService = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    const numericPrice = Number(serviceForm.price)
    const numericDuration = Number.parseInt(serviceForm.durationMin, 10)
    if (!serviceForm.name.trim() || Number.isNaN(numericPrice) || numericPrice < 0 || Number.isNaN(numericDuration) || numericDuration < 1) {
      setErrorMessage('Ingresa nombre, precio no negativo y duración mayor a 0 minutos.')
      return
    }

    const payload = {
      name: serviceForm.name.trim(),
      description: serviceForm.description.trim(),
      price: numericPrice,
      durationMin: numericDuration,
    }

    setSubmitting(true)
    try {
      if (editingServiceId) {
        const updated = await updateServiceType(editingServiceId, payload)
        setServices((prev) => prev.map((item) => (item.id === editingServiceId ? updated : item)))
      } else {
        const created = await createServiceType(payload)
        setServices((prev) => [...prev, created])
      }
      resetServiceForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el tipo de servicio.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleEditService = (service) => {
    setActiveTab('services')
    setEditingServiceId(service.id)
    setServiceForm({
      name: service.name || '',
      description: service.description || '',
      price: String(service.price ?? ''),
      durationMin: String(service.durationMin ?? ''),
    })
  }

  const handleDeleteService = async (serviceId) => {
    if (!window.confirm('¿Deseas eliminar este tipo de servicio?')) return
    setErrorMessage('')
    setBusyId(serviceId)
    try {
      await deleteServiceType(serviceId)
      setServices((prev) => prev.filter((item) => item.id !== serviceId))
      if (editingServiceId === serviceId) resetServiceForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el tipo de servicio.'))
    } finally {
      setBusyId(null)
    }
  }

  const handleSaveEmployee = async (event) => {
    event.preventDefault()
    setErrorMessage('')

    if (!employeeForm.name.trim() || !employeeForm.role.trim()) {
      setErrorMessage('El nombre y el cargo del empleado son obligatorios.')
      return
    }

    const payload = {
      name: employeeForm.name.trim(),
      role: employeeForm.role.trim(),
    }

    setSubmitting(true)
    try {
      if (editingEmployeeId) {
        const updated = await updateEmployee(editingEmployeeId, payload)
        setEmployees((prev) => prev.map((item) => (item.id === editingEmployeeId ? updated : item)))
      } else {
        const created = await createEmployee(payload)
        setEmployees((prev) => [...prev, created])
      }
      resetEmployeeForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible guardar el empleado.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleEditEmployee = (employee) => {
    setActiveTab('employees')
    setEditingEmployeeId(employee.id)
    setEmployeeForm({
      name: employee.name || '',
      role: employee.role || '',
    })
  }

  const handleDeleteEmployee = async (employeeId) => {
    if (!window.confirm('¿Deseas eliminar este empleado?')) return
    setErrorMessage('')
    setBusyId(employeeId)
    try {
      await deleteEmployee(employeeId)
      setEmployees((prev) => prev.filter((item) => item.id !== employeeId))
      if (editingEmployeeId === employeeId) resetEmployeeForm()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'No fue posible eliminar el empleado.'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-[28px] font-bold tracking-tight text-[#1E1B4B]">Catálogo y Empleados</h1>
            <p className="mt-1 text-sm text-[#5B5F7A]">
              Administra los servicios de lavado/detailing y el equipo operativo.
            </p>
          </div>

          <div className="inline-flex rounded-xl border border-[#E4E6F2] bg-white p-1 shadow-card">
            <button
              type="button"
              onClick={() => setActiveTab('services')}
              className={`rounded-lg px-4 py-2 text-sm font-semibold transition ${
                activeTab === 'services' ? 'bg-[#4F46E5] text-white' : 'text-[#5B5F7A] hover:bg-[#F5F6FB]'
              }`}
            >
              Tipos de servicio ({services.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('employees')}
              className={`rounded-lg px-4 py-2 text-sm font-semibold transition ${
                activeTab === 'employees' ? 'bg-[#4F46E5] text-white' : 'text-[#5B5F7A] hover:bg-[#F5F6FB]'
              }`}
            >
              Empleados ({employees.length})
            </button>
          </div>
        </div>

        {errorMessage && (
          <div
            role="alert"
            className="rounded-xl border border-[#B91C1C]/20 bg-[#FEE2E2] px-4 py-3 text-sm font-medium text-[#B91C1C]"
          >
            {errorMessage}
          </div>
        )}

        {activeTab === 'services' ? (
          <>
            <section className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card">
              <div className="mb-4 flex items-center justify-between">
                <h2 className="text-[18px] font-semibold text-[#1E1B4B]">
                  {editingServiceId ? 'Editar tipo de servicio' : 'Nuevo tipo de servicio'}
                </h2>
                {editingServiceId && (
                  <Button variant="secondary" onClick={resetServiceForm}>
                    Cancelar edición
                  </Button>
                )}
              </div>

              <form onSubmit={handleSaveService} className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <Input
                  id="serviceName"
                  label="Nombre"
                  placeholder="Lavado Full Detailing"
                  value={serviceForm.name}
                  onChange={(e) => setServiceForm((prev) => ({ ...prev, name: e.target.value }))}
                  required
                />
                <Input
                  id="serviceDescription"
                  label="Descripción"
                  placeholder="Interior, exterior y encerado"
                  value={serviceForm.description}
                  onChange={(e) => setServiceForm((prev) => ({ ...prev, description: e.target.value }))}
                />
                <Input
                  id="servicePrice"
                  label="Precio"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="45000"
                  value={serviceForm.price}
                  onChange={(e) => setServiceForm((prev) => ({ ...prev, price: e.target.value }))}
                  required
                />
                <Input
                  id="serviceDuration"
                  label="Duración (min)"
                  type="number"
                  min="1"
                  step="1"
                  placeholder="60"
                  value={serviceForm.durationMin}
                  onChange={(e) => setServiceForm((prev) => ({ ...prev, durationMin: e.target.value }))}
                  required
                />
                <div className="sm:col-span-2 lg:col-span-4 flex justify-end">
                  <Button type="submit" loading={submitting}>
                    {editingServiceId ? 'Guardar cambios' : 'Agregar servicio'}
                  </Button>
                </div>
              </form>
            </section>

            <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
              <div className="flex items-center justify-between border-b border-[#E4E6F2] px-6 py-4">
                <h3 className="text-base font-semibold text-[#1E1B4B]">Catálogo de servicios</h3>
                <Button variant="secondary" onClick={loadCatalog} disabled={loading}>
                  Actualizar
                </Button>
              </div>

              {loading ? (
                <div className="p-8 text-center text-sm text-[#5B5F7A]">Cargando tipos de servicio...</div>
              ) : services.length === 0 ? (
                <div className="p-8 text-center text-sm text-[#5B5F7A]">No hay servicios registrados.</div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                    <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                      <tr>
                        <th className="px-4 py-3">Nombre</th>
                        <th className="px-4 py-3">Descripción</th>
                        <th className="px-4 py-3">Precio</th>
                        <th className="px-4 py-3">Duración</th>
                        <th className="px-4 py-3 text-right">Acciones</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#E4E6F2] bg-white">
                      {services.map((service) => (
                        <tr key={service.id} className="hover:bg-[#F5F6FB]/60">
                          <td className="px-4 py-3 font-medium text-[#1E1B4B]">{service.name}</td>
                          <td className="px-4 py-3 text-[#5B5F7A]">{service.description || '—'}</td>
                          <td className="whitespace-nowrap px-4 py-3 font-semibold tabular-nums text-[#1E1B4B]">
                            ${Number(service.price).toLocaleString('es-CO')}
                          </td>
                          <td className="whitespace-nowrap px-4 py-3 tabular-nums text-[#5B5F7A]">{service.durationMin} min</td>
                          <td className="whitespace-nowrap px-4 py-3 text-right">
                            <div className="inline-flex gap-2">
                              <button
                                type="button"
                                disabled={busyId === service.id}
                                onClick={() => handleEditService(service)}
                                className="rounded-md px-2.5 py-1 text-xs font-semibold text-[#4F46E5] hover:bg-indigo-50"
                              >
                                Editar
                              </button>
                              <button
                                type="button"
                                disabled={busyId === service.id}
                                onClick={() => handleDeleteService(service.id)}
                                className="rounded-md px-2.5 py-1 text-xs font-semibold text-[#B91C1C] hover:bg-[#FEE2E2]"
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
          </>
        ) : (
          <>
            <section className="rounded-2xl border border-[#E4E6F2] bg-white p-6 shadow-card">
              <div className="mb-4 flex items-center justify-between">
                <h2 className="text-[18px] font-semibold text-[#1E1B4B]">
                  {editingEmployeeId ? 'Editar empleado' : 'Nuevo empleado'}
                </h2>
                {editingEmployeeId && (
                  <Button variant="secondary" onClick={resetEmployeeForm}>
                    Cancelar edición
                  </Button>
                )}
              </div>

              <form onSubmit={handleSaveEmployee} className="grid gap-4 sm:grid-cols-3">
                <Input
                  id="employeeName"
                  label="Nombre completo"
                  placeholder="Carlos Mendoza"
                  value={employeeForm.name}
                  onChange={(e) => setEmployeeForm((prev) => ({ ...prev, name: e.target.value }))}
                  required
                />
                <Input
                  id="employeeRole"
                  label="Cargo / Rol"
                  placeholder="Detailer Senior"
                  value={employeeForm.role}
                  onChange={(e) => setEmployeeForm((prev) => ({ ...prev, role: e.target.value }))}
                  required
                />
                <div className="flex items-end">
                  <Button type="submit" loading={submitting} className="w-full">
                    {editingEmployeeId ? 'Guardar cambios' : 'Agregar empleado'}
                  </Button>
                </div>
              </form>
            </section>

            <section className="overflow-hidden rounded-2xl border border-[#E4E6F2] bg-white shadow-card">
              <div className="flex items-center justify-between border-b border-[#E4E6F2] px-6 py-4">
                <h3 className="text-base font-semibold text-[#1E1B4B]">Listado de empleados</h3>
                <Button variant="secondary" onClick={loadCatalog} disabled={loading}>
                  Actualizar
                </Button>
              </div>

              {loading ? (
                <div className="p-8 text-center text-sm text-[#5B5F7A]">Cargando empleados...</div>
              ) : employees.length === 0 ? (
                <div className="p-8 text-center text-sm text-[#5B5F7A]">No hay empleados registrados.</div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-[#E4E6F2] text-left text-sm">
                    <thead className="bg-[#F5F6FB] text-xs font-semibold tracking-wider text-[#5B5F7A] uppercase">
                      <tr>
                        <th className="px-4 py-3">Nombre</th>
                        <th className="px-4 py-3">Cargo / Rol</th>
                        <th className="px-4 py-3 text-right">Acciones</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#E4E6F2] bg-white">
                      {employees.map((employee) => (
                        <tr key={employee.id} className="hover:bg-[#F5F6FB]/60">
                          <td className="px-4 py-3 font-medium text-[#1E1B4B]">{employee.name}</td>
                          <td className="px-4 py-3">
                            <span className="inline-flex rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold text-[#4F46E5]">
                              {employee.role}
                            </span>
                          </td>
                          <td className="whitespace-nowrap px-4 py-3 text-right">
                            <div className="inline-flex gap-2">
                              <button
                                type="button"
                                disabled={busyId === employee.id}
                                onClick={() => handleEditEmployee(employee)}
                                className="rounded-md px-2.5 py-1 text-xs font-semibold text-[#4F46E5] hover:bg-indigo-50"
                              >
                                Editar
                              </button>
                              <button
                                type="button"
                                disabled={busyId === employee.id}
                                onClick={() => handleDeleteEmployee(employee.id)}
                                className="rounded-md px-2.5 py-1 text-xs font-semibold text-[#B91C1C] hover:bg-[#FEE2E2]"
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
          </>
        )}
      </div>
    </MainLayout>
  )
}
