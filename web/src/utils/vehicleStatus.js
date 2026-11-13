const VEHICLE_STATUS_META = {
  RECEIVED: {
    label: 'Recibido',
    badgeClass: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
  },
  WASHING: {
    label: 'Lavando',
    badgeClass: 'bg-amber-50 text-amber-800 ring-1 ring-amber-200',
  },
  DETAILING: {
    label: 'Detailing',
    badgeClass: 'bg-indigo-50 text-indigo-700 ring-1 ring-indigo-200',
  },
  READY: {
    label: 'Listo para entregar',
    badgeClass: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
  },
  DELIVERED: {
    label: 'Entregado',
    badgeClass: 'bg-teal-50 text-teal-700 ring-1 ring-teal-200',
  },
}

/**
 * Retorna etiqueta legible y clases visuales para un estado operativo del vehículo.
 *
 * @param {string | null | undefined} status estado del vehículo (VehicleStatus)
 * @returns {{ label: string, badgeClass: string }}
 */
export function getVehicleStatusMeta(status) {
  const key = status || 'RECEIVED'
  return (
    VEHICLE_STATUS_META[key] || {
      label: key,
      badgeClass: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
    }
  )
}
