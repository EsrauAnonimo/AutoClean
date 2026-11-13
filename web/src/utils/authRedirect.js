/**
 * Determina la ruta inicial del usuario autenticado según su rol en AutoClean.
 *
 * @param {string | null | undefined} role rol asignado en la sesión JWT
 * @returns {string} ruta de inicio correspondiente
 */
export function getHomeRouteByRole(role) {
  if (role === 'CLIENTE') {
    return '/portal/dashboard'
  }
  return '/dashboard'
}

