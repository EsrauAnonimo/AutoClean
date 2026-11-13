import api from './api'

/**
 * Lista todas las citas registradas, con filtros opcionales por cliente o estado.
 */
export async function getAppointments(params = {}) {
  const cleanParams = Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
  const { data } = await api.get('/appointments', { params: cleanParams })
  return data
}

/**
 * Crea una nueva cita programada (estado por defecto PENDING).
 */
export async function createAppointment(payload) {
  const { data } = await api.post('/appointments', payload)
  return data
}

/**
 * Actualiza los datos principales de una cita existente.
 */
export async function updateAppointment(id, payload) {
  const { data } = await api.put(`/appointments/${id}`, payload)
  return data
}

/**
 * Cambia el estado de una cita aplicando la máquina de estados del backend.
 */
export async function updateAppointmentStatus(id, status) {
  const { data } = await api.patch(`/appointments/${id}/status`, { status })
  return data
}

/**
 * Elimina una cita por su identificador.
 */
export async function deleteAppointment(id) {
  await api.delete(`/appointments/${id}`)
}

/**
 * Consulta clientes disponibles para poblar selectores de creación de citas.
 */
export async function getCustomers() {
  const { data } = await api.get('/customers')
  return data
}

/**
 * Consulta vehículos registrados, opcionalmente filtrados por cliente.
 */
export async function getVehicles(customerId) {
  const params = customerId ? { customerId } : undefined
  const { data } = await api.get('/vehicles', { params })
  return data
}
