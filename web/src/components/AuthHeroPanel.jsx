import { AutoCleanLogo } from './AutoCleanLogo'

/**
 * Panel izquierdo 50/50 para Login y Registro (oculto en móvil).
 * Jerarquía limpia: fondo índigo sutil, logo arriba a la izquierda, eslogan protagonista
 * y auto vectorial compacto monocromático de apoyo anclado abajo a la derecha.
 */
export function AuthHeroPanel() {
  return (
    <div className="relative hidden overflow-hidden bg-gradient-to-br from-[#1E1B4B] to-[#312E81] lg:flex lg:flex-col lg:justify-between lg:p-16">
      <div className="relative z-10">
        <AutoCleanLogo size="lg" />
      </div>

      {/* Bloque protagonista ubicado en el tercio medio-inferior con >=48px de aire sobre la ilustración */}
      <div className="relative z-10 my-auto max-w-[360px] pb-20 pt-10">
        <h2 className="text-[44px] leading-[1.15] font-semibold tracking-tight text-white">
          Brillo que se nota.
        </h2>
        <div className="my-5 h-[2px] w-12 rounded-full bg-[#8B5CF6]" aria-hidden="true" />
        <p className="text-[16px] leading-relaxed font-normal text-[#C7D2FE]">
          Lavado y detailing profesional.
        </p>
      </div>

      <div aria-hidden="true" className="h-4" />

      {/* Ilustración vectorial plana de apoyo, recortada parcialmente abajo a la derecha */}
      <svg
        viewBox="0 0 360 155"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
        className="pointer-events-none absolute -right-10 bottom-0 w-[44%] max-w-[320px] opacity-75"
      >
        {/* Sombra sutil de apoyo */}
        <ellipse cx="195" cy="139" rx="165" ry="7" fill="#1E1B4B" fillOpacity="0.6" />

        {/* Gotas pequeñas (3) */}
        <path
          d="M66 44C66 44 61 52 61 55.5C61 58.3 63.2 60.5 66 60.5C68.8 60.5 71 58.3 71 55.5C71 52 66 44 66 44Z"
          fill="#6366F1"
        />
        <path
          d="M116 20C116 20 112.5 25.5 112.5 28C112.5 29.9 114.1 31.5 116 31.5C117.9 31.5 119.5 29.9 119.5 28C119.5 25.5 116 20 116 20Z"
          fill="#6366F1"
        />
        <path
          d="M274 28C274 28 270 34.5 270 37.5C270 39.7 271.8 41.5 274 41.5C276.2 41.5 278 39.7 278 37.5C278 34.5 274 28 274 28Z"
          fill="#6366F1"
        />

        {/* Silueta compacta en vista lateral (tonos índigo monocromáticos) */}
        <path
          d="M36 116C26 115 20 109 20 99C20 90 25 84 35 81L74 71L113 40C122 33 134 29 147 29H236C255 29 273 38 286 52L308 72L338 78C349 80 356 88 356 99V110C356 116 351 120 344 120H324H52C44 120 39 118 36 116Z"
          fill="#4338CA"
        />

        {/* Volumen inferior / faldón lateral */}
        <path
          d="M22 103H354V112C354 116.5 349.5 120 344 120H34C27 120 22 115 22 108V103Z"
          fill="#3730A3"
        />

        {/* Ventanas laterales */}
        <path
          d="M89 72L119 46C125 41 133 38 142 38H184V72H89Z"
          fill="#6366F1"
        />
        <path
          d="M196 38H233C247 38 260 44 270 54L287 72H196V38Z"
          fill="#6366F1"
        />

        {/* Un solo reflejo claro sobre la carrocería */}
        <path
          d="M52 84C104 79 182 78 268 82"
          stroke="#C7D2FE"
          strokeOpacity="0.65"
          strokeWidth="3"
          strokeLinecap="round"
        />

        {/* Pasos de rueda y llantas */}
        <circle cx="96" cy="118" r="24" fill="#1E1B4B" />
        <circle cx="96" cy="118" r="18" fill="#4338CA" />
        <circle cx="96" cy="118" r="9" fill="#6366F1" />

        <circle cx="272" cy="118" r="24" fill="#1E1B4B" />
        <circle cx="272" cy="118" r="18" fill="#4338CA" />
        <circle cx="272" cy="118" r="9" fill="#6366F1" />
      </svg>
    </div>
  )
}
