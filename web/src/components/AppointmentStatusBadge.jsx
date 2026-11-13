const STATUS_STYLES = {
  PENDING: {
    label: 'Pendiente',
    className: 'bg-amber-50 text-amber-800 ring-1 ring-amber-200',
  },
  IN_PROGRESS: {
    label: 'En proceso',
    className: 'bg-sky-50 text-sky-800 ring-1 ring-sky-200',
  },
  FINISHED: {
    label: 'Finalizada',
    className: 'bg-emerald-50 text-emerald-800 ring-1 ring-emerald-200',
  },
  CANCELLED: {
    label: 'Cancelada',
    className: 'bg-rose-50 text-rose-800 ring-1 ring-rose-200',
  },
}

/**
 * Etiqueta visual semántica para representar el estado de una cita en su máquina de estados.
 */
export function AppointmentStatusBadge({ status }) {
  const config = STATUS_STYLES[status] || {
    label: status || 'Desconocido',
    className: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${config.className}`}
    >
      {config.label}
    </span>
  )
}
