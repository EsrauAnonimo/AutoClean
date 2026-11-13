import { useEffect, useState } from 'react'
import { TOAST_EVENT } from '../utils/toast'

const TOAST_STYLES = {
  success: {
    border: 'border-l-[#15803D]',
    iconBg: 'bg-[#DCFCE7] text-[#15803D]',
    symbol: '✓',
  },
  error: {
    border: 'border-l-[#B91C1C]',
    iconBg: 'bg-[#FEE2E2] text-[#B91C1C]',
    symbol: '!',
  },
  info: {
    border: 'border-l-[#4F46E5]',
    iconBg: 'bg-[#EDE9FE] text-[#4F46E5]',
    symbol: 'i',
  },
}

/**
 * Contenedor global de notificaciones toast (éxito, error e información).
 */
export function ToastViewport() {
  const [toasts, setToasts] = useState([])

  useEffect(() => {
    const handleToast = (event) => {
      const detail = event.detail || {}
      const id = `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
      setToasts((prev) => [...prev.slice(-2), { id, ...detail }])
      window.setTimeout(() => {
        setToasts((prev) => prev.filter((item) => item.id !== id))
      }, 4500)
    }

    window.addEventListener(TOAST_EVENT, handleToast)
    return () => window.removeEventListener(TOAST_EVENT, handleToast)
  }, [])

  if (toasts.length === 0) return null

  return (
    <div
      aria-live="polite"
      className="pointer-events-none fixed inset-x-4 top-4 z-50 flex flex-col items-end gap-2.5 sm:inset-x-auto sm:right-5 sm:top-5 sm:w-full sm:max-w-sm"
    >
      {toasts.map((toast) => {
        const style = TOAST_STYLES[toast.type] || TOAST_STYLES.info
        return (
          <div
            key={toast.id}
            className={`pointer-events-auto flex w-full items-start gap-3 rounded-xl border border-[#E4E6F2] border-l-4 ${style.border} bg-white p-4 shadow-card transition-all`}
          >
            <span
              className={`mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ${style.iconBg}`}
            >
              {style.symbol}
            </span>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-[#1E1B4B]">{toast.title}</p>
              {toast.description && (
                <p className="mt-0.5 text-[13px] text-[#5B5F7A]">{toast.description}</p>
              )}
            </div>
            <button
              type="button"
              onClick={() => setToasts((prev) => prev.filter((item) => item.id !== toast.id))}
              className="text-xs font-semibold text-[#5B5F7A] hover:text-[#1E1B4B]"
              aria-label="Cerrar notificación"
            >
              ✕
            </button>
          </div>
        )
      })}
    </div>
  )
}

