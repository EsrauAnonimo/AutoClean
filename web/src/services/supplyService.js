import api from './api'

/**
 * Lista todos los insumos del inventario.
 */
export async function getSupplies() {
  const { data } = await api.get('/supplies')
  return data
}

/**
 * Registra un nuevo insumo en el inventario.
 */
export async function createSupply(payload) {
  const { data } = await api.post('/supplies', payload)
  return data
}

/**
 * Actualiza un insumo existente.
 */
export async function updateSupply(id, payload) {
  const { data } = await api.put(`/supplies/${id}`, payload)
  return data
}

/**
 * Elimina un insumo por su identificador.
 */
export async function deleteSupply(id) {
  await api.delete(`/supplies/${id}`)
}
