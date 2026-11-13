import { useId } from 'react'

/**
 * Logotipo vectorial de AutoClean: emblema aerodinámico automotriz con onda de lavado
 * y destellos de detailing, adaptable al tamaño del header o sidebar.
 */
export function AutoCleanLogo({
  subtitle = '',
  size = 'md',
  tone = 'dark',
  className = '',
}) {
  const uid = useId()
  const bgGradientId = `ac-bg-${uid}`
  const shineGradientId = `ac-shine-${uid}`
  const waveGradientId = `ac-wave-${uid}`

  const iconSizes = {
    sm: 'h-9 w-9',
    md: 'h-10 w-10',
    lg: 'h-12 w-12',
  }

  const titleSizes = {
    sm: 'text-[15px]',
    md: 'text-[17px]',
    lg: 'text-[20px]',
  }

  const isLightSurface = tone === 'light'
  const isSolidWhite = tone === 'white'

  return (
    <div className={`inline-flex items-center gap-3 select-none ${className}`}>
      <svg
        viewBox="0 0 48 48"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className={`${iconSizes[size] || iconSizes.md} shrink-0 ${isSolidWhite ? '' : 'drop-shadow-sm'}`}
        aria-hidden="true"
      >
        <defs>
          <linearGradient id={bgGradientId} x1="4" y1="2" x2="44" y2="46" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#4F46E5" />
            <stop offset="55%" stopColor="#6D28D9" />
            <stop offset="100%" stopColor="#8B5CF6" />
          </linearGradient>
          <linearGradient id={shineGradientId} x1="24" y1="2" x2="24" y2="26" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#FFFFFF" stopOpacity="0.28" />
            <stop offset="100%" stopColor="#FFFFFF" stopOpacity="0" />
          </linearGradient>
          <linearGradient id={waveGradientId} x1="8" y1="30" x2="40" y2="38" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#67E8F9" />
            <stop offset="100%" stopColor="#E0E7FF" />
          </linearGradient>
        </defs>

        <rect x="2" y="2" width="44" height="44" rx="13" fill={`url(#${bgGradientId})`} />
        <rect
          x="2.75"
          y="2.75"
          width="42.5"
          height="42.5"
          rx="12.25"
          stroke="rgba(255,255,255,0.28)"
          strokeWidth="1.5"
        />
        <path
          d="M15 3.5H33C39 3.5 43.5 7.5 44.3 13.5C35 16.5 16 17.5 3.7 13.5C4.5 7.5 9 3.5 15 3.5Z"
          fill={`url(#${shineGradientId})`}
        />

        {/* Destellos de brillo detailing */}
        <path
          d="M35.5 9.5L36.5 12.2L39.2 13.2L36.5 14.2L35.5 16.9L34.5 14.2L31.8 13.2L34.5 12.2L35.5 9.5Z"
          fill="#67E8F9"
        />
        <path
          d="M13 12.5L13.6 14.1L15.2 14.7L13.6 15.3L13 16.9L12.4 15.3L10.8 14.7L12.4 14.1L13 12.5Z"
          fill="#FFFFFF"
          fillOpacity="0.85"
        />

        {/* Silueta coupé aerodinámica */}
        <path
          d="M9.5 28.5L13.2 21.8C14.3 19.8 16.4 18.6 18.7 18.6H27.8C29.9 18.6 31.9 19.6 33.2 21.3L36.5 25.4L39.2 26.2C40.3 26.5 41 27.5 41 28.6V30.5C41 31.3 40.3 32 39.5 32H37.2"
          stroke="#FFFFFF"
          strokeWidth="2.3"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M16.5 23.8H30.8"
          stroke="#E0E7FF"
          strokeWidth="1.7"
          strokeLinecap="round"
          strokeOpacity="0.9"
        />
        <path
          d="M9.5 28.5C8.6 28.5 8 29.2 8 30.1V30.8C8 31.5 8.5 32 9.2 32H11.5"
          stroke="#FFFFFF"
          strokeWidth="2.3"
          strokeLinecap="round"
        />
        <path d="M18.2 32H30.4" stroke="#FFFFFF" strokeWidth="2.3" strokeLinecap="round" />

        {/* Ruedas */}
        <circle cx="14.8" cy="32" r="2.7" fill="#1E1B4B" stroke="#FFFFFF" strokeWidth="2" />
        <circle cx="33.8" cy="32" r="2.7" fill="#1E1B4B" stroke="#FFFFFF" strokeWidth="2" />

        {/* Onda de agua y acabado brillante */}
        <path
          d="M8.5 37.8C14.5 35.3 22.8 39.2 31.5 37.6C35.2 36.9 38.2 35.6 40.2 34.4"
          stroke={`url(#${waveGradientId})`}
          strokeWidth="2.2"
          strokeLinecap="round"
        />
      </svg>

      <div className="leading-tight">
        <p
          className={`${titleSizes[size] || titleSizes.md} font-semibold tracking-tight ${
            isLightSurface ? 'text-[#1E1B4B]' : 'text-white'
          }`}
        >
          Auto
          <span
            className={
              isSolidWhite
                ? 'text-white'
                : isLightSurface
                  ? 'text-[#4F46E5]'
                  : 'text-[#A5F3FC]'
            }
          >
            Clean
          </span>
        </p>
        {subtitle && (
          <p
            className={`text-[11px] font-normal ${
              isLightSurface ? 'text-[#5B5F7A]' : 'text-indigo-200/90'
            }`}
          >
            {subtitle}
          </p>
        )}
      </div>
    </div>
  )
}

