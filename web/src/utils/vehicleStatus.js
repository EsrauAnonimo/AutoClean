const VEHICLE_STATUS_META = {
  RECEIVED: {
    label: 'Recibido',
    badgeClass: 'bg-slate-100 text-slate-700 ring-1 ring-slate-300/70',
    dotClass: 'bg-slate-500',
    iconPath: 'M9 11l3 3L22 4M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
    step: 1,
  },
  WASHING: {
    label: 'Lavando',
    badgeClass: 'bg-[#FEF3C7] text-[#B45309] ring-1 ring-[#F59E0B]/30',
    dotClass: 'bg-[#F59E0B]',
    iconPath: 'M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z',
    step: 2,
  },
  DETAILING: {
    label: 'Detallando',
    badgeClass: 'bg-[#EDE9FE] text-[#6D28D9] ring-1 ring-[#8B5CF6]/30',
    dotClass: 'bg-[#8B5CF6]',
    iconPath: 'M12 3l1.8 4.2L18 9l-4.2 1.8L12 15l-1.8-4.2L6 9l4.2-1.8L12 3z',
    step: 3,
  },
  READY: {
    label: 'Listo',
    badgeClass: 'bg-[#DCFCE7] text-[#15803D] ring-1 ring-[#22C55E]/30',
    dotClass: 'bg-[#15803D]',
    iconPath: 'M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4 12 14.01l-3-3',
    step: 4,
  },
  DELIVERED: {
    label: 'Entregado',
    badgeClass: 'bg-teal-50 text-teal-700 ring-1 ring-teal-300/70',
    dotClass: 'bg-teal-600',
    iconPath: 'M7.5 19a3.5 3.5 0 1 1 0-7 3.5 3.5 0 0 1 0 7Zm2.6-6.1L21 2m-5.5 5.5 3 3L22 7l-3-3',
    step: 5,
  },
}

export const VEHICLE_STATUS_ORDER = ['RECEIVED', 'WASHING', 'DETAILING', 'READY', 'DELIVERED']

/**
 * Retorna etiqueta, clases Tailwind, paso e ícono Lucide para un estado de vehículo.
 */
export function getVehicleStatusMeta(status) {
  const key = status || 'RECEIVED'
  return VEHICLE_STATUS_META[key] || VEHICLE_STATUS_META.RECEIVED
}
