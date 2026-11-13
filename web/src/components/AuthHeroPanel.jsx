import { AutoCleanLogo } from './AutoCleanLogo'

/**
 * Panel visual 50/50 para Login y Registro (solo escritorio; oculto en móvil).
 * Mantiene únicamente fotografía, overlay índigo al 65%, logo arriba y frase corta abajo.
 */
export function AuthHeroPanel({ headline = 'Tu auto, siempre impecable.' }) {
  return (
    <div className="relative hidden overflow-hidden lg:flex lg:flex-col lg:justify-between lg:p-12">
      <img
        src="/images/auth-car.jpg"
        alt="Vehículo recién detallado en estudio AutoClean"
        className="absolute inset-0 h-full w-full object-cover object-center"
      />
      <div aria-hidden="true" className="absolute inset-0 bg-[#1E1B4B]/65" />

      <div className="relative z-10">
        <AutoCleanLogo size="lg" />
      </div>

      <p className="relative z-10 max-w-md text-[38px] leading-[1.2] font-semibold tracking-tight text-white">
        {headline}
      </p>
    </div>
  )
}
