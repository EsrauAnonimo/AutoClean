import api from './api'

/**
 * Lista todos los clientes registrados.
 */
export async function getCustomers() {
  const { data } = await api.get('/customers')
  return data
}

/**
 * Crea un nuevo cliente.
 */
export async function createCustomer(payload) {
  const { data } = await api.post('/customers', payload)
  return data
}

/**
 * Actualiza los datos de un cliente existente.
 */
export async function updateCustomer(id, payload) {
  const { data } = await api.put(`/customers/${id}`, payload)
  return data
}

/**
 * Elimina un cliente por su identificador.
 */
export async function deleteCustomer(id) {
  await api.delete(`/customers/${id}`)
}
