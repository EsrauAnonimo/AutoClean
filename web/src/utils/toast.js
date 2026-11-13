const TOAST_EVENT = 'autoclean:toast'

/**
 * Emite una notificación toast flotante en toda la aplicación.
 *
 * @param {{ type?: 'success' | 'error' | 'info', title: string, description?: string }} payload
 */
export function showToast(payload) {
  window.dispatchEvent(new CustomEvent(TOAST_EVENT, { detail: payload }))
}

export { TOAST_EVENT }
