import { getVehicleStatusMeta } from '../utils/vehicleStatus'

/**
 * Badge con ícono minimalista para el estado operativo en vivo del vehículo.
 */
export function VehicleStatusBadge({ status, prefix = '' }) {
  const meta = getVehicleStatusMeta(status)

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ${meta.badgeClass}`}
    >
      <svg
        className="h-3.5 w-3.5 shrink-0"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d={meta.iconPath} />
      </svg>
      <span>
        {prefix}
        {meta.label}
      </span>
    </span>
  )
}
