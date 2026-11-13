import api from './api'

/**
 * Lista los vehículos registrados, con filtro opcional por cliente propietario.
 */
export async function getVehicles(customerId) {
  const params = customerId ? { customerId } : undefined
  const { data } = await api.get('/vehicles', { params })
  return data
}

/**
 * Lista los vehículos asociados exclusivamente a un cliente específico.
 */
export async function getVehiclesByCustomer(customerId) {
  if (!customerId) {
    throw new Error('El identificador del cliente es obligatorio para consultar sus vehículos.')
  }
  const { data } = await api.get(`/vehicles/customer/${customerId}`)
  return data
}

/**
 * Busca un vehículo registrado por su número de placa.
 */
export async function getVehicleByPlate(plate) {
  const normalizedPlate = String(plate || '').trim().toUpperCase()
  if (!normalizedPlate) {
    throw new Error('La placa del vehículo es obligatoria.')
  }
  const { data } = await api.get(`/vehicles/plate/${encodeURIComponent(normalizedPlate)}`)
  return data
}

/**
 * Registra un nuevo vehículo asociado a un cliente.
 */
export async function createVehicle(payload) {
  const { data } = await api.post('/vehicles', payload)
  return data
}

/**
 * Actualiza un vehículo existente.
 */
export async function updateVehicle(id, payload) {
  const { data } = await api.put(`/vehicles/${id}`, payload)
  return data
}

/**
 * Elimina un vehículo por su identificador.
 */
export async function deleteVehicle(id) {
  await api.delete(`/vehicles/${id}`)
}
