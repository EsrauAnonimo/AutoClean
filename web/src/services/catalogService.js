import api from './api'

/**
 * Lista todos los tipos de servicio del catálogo.
 */
export async function getServiceTypes() {
  const { data } = await api.get('/service-types')
  return data
}

/**
 * Crea un nuevo tipo de servicio en el catálogo.
 */
export async function createServiceType(payload) {
  const { data } = await api.post('/service-types', payload)
  return data
}

/**
 * Actualiza un tipo de servicio existente.
 */
export async function updateServiceType(id, payload) {
  const { data } = await api.put(`/service-types/${id}`, payload)
  return data
}

/**
 * Elimina un tipo de servicio por su identificador.
 */
export async function deleteServiceType(id) {
  await api.delete(`/service-types/${id}`)
}

/**
 * Lista todos los empleados registrados.
 */
export async function getEmployees() {
  const { data } = await api.get('/employees')
  return data
}

/**
 * Crea un nuevo empleado operativo.
 */
export async function createEmployee(payload) {
  const { data } = await api.post('/employees', payload)
  return data
}

/**
 * Actualiza la información de un empleado existente.
 */
export async function updateEmployee(id, payload) {
  const { data } = await api.put(`/employees/${id}`, payload)
  return data
}

/**
 * Elimina un empleado por su identificador.
 */
export async function deleteEmployee(id) {
  await api.delete(`/employees/${id}`)
}
