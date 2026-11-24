const STATUS_STYLES = {
  PENDING: {
    label: 'Pendiente',
    className: 'bg-[#FEF3C7] text-[#B45309] ring-1 ring-[#F59E0B]/30',
  },
  IN_PROGRESS: {
    label: 'En proceso',
    className: 'bg-[#EDE9FE] text-[#6D28D9] ring-1 ring-[#8B5CF6]/30',
  },
  FINISHED: {
    label: 'Finalizada',
    className: 'bg-[#DCFCE7] text-[#15803D] ring-1 ring-[#22C55E]/30',
  },
  CANCELLED: {
    label: 'Cancelada',
    className: 'bg-[#FEE2E2] text-[#B91C1C] ring-1 ring-[#EF4444]/30',
  },
}

/**
 * Badge semántico para el estado de una cita en el sistema de diseño AutoClean.
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
