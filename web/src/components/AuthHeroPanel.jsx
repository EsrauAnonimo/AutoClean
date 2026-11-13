import { AutoCleanLogo } from './AutoCleanLogo'

const TRACKING_STEPS = [
  { label: 'Recibido', active: true },
  { label: 'Lavando', active: true },
  { label: 'Detallando', active: true },
  { label: 'Listo', active: false },
  { label: 'Entregado', active: false },
]

/**
 * Panel izquierdo 50/50 para Login y Registro (oculto en móvil).
 * Estructurado en 3 zonas equilibradas: cabecera con logo/badge, centro con eslogan
 * + tarjetas apiladas de seguimiento y pie con 3 puntos de confianza.
 */
export function AuthHeroPanel() {
  return (
    <div className="relative hidden overflow-hidden bg-gradient-to-br from-[#1E1B4B] to-[#3730A3] lg:flex lg:flex-col lg:justify-between lg:p-16">
      {/* Dos círculos concéntricos de profundidad detrás de las tarjetas */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-36 -right-32 h-[540px] w-[540px] rounded-full bg-[#8B5CF6]/[0.08]"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-16 -right-12 h-[360px] w-[360px] rounded-full bg-[#8B5CF6]/[0.12]"
      />

      {/* Zona 1: Arriba */}
      <div className="relative z-10 flex items-center justify-between gap-4">
        <AutoCleanLogo size="lg" tone="white" />
        <span className="rounded-full border border-white/15 bg-white/10 px-3.5 py-1.5 text-xs font-medium text-[#E0E7FF]">
          Lavado y detailing profesional
        </span>
      </div>

      {/* Zona 2: Centro (~60 % del alto) */}
      <div className="relative z-10 my-auto flex w-full flex-col items-start justify-center py-8">
        <div>
          <h2 className="text-[52px] leading-[1.1] font-semibold tracking-tight text-white">
            Brillo que se nota.
          </h2>
          <p className="mt-3 text-[18px] leading-relaxed font-normal text-[#C7D2FE]">
            Agenda con tu placa y sigue tu auto en vivo.
          </p>
        </div>

        {/* Composición de dos tarjetas apiladas */}
        <div className="relative mt-9 w-full max-w-[544px] pb-6 pr-6">
          {/* Tarjeta principal de seguimiento (hasta 520px) */}
          <div className="w-full rounded-[16px] border border-white/[0.14] bg-white/[0.10] p-6 shadow-[0_24px_48px_-16px_rgba(15,12,48,0.48)] backdrop-blur-md">
            <div className="flex items-center justify-between gap-3">
              <span className="inline-flex items-center rounded-md bg-[#FACC15] px-2.5 py-1 font-mono text-xs font-semibold tracking-wider text-[#1E1B4B]">
                KLT 842
              </span>
              <span className="rounded-full bg-[#8B5CF6] px-3 py-1 text-xs font-semibold text-white">
                Detallando
              </span>
            </div>

            <div className="mt-4">
              <p className="text-[16px] font-semibold text-white">
                Full Detailing Cerámico · Mazda CX-30
              </p>
              <p className="mt-1 text-[14px] font-normal text-[#C7D2FE]">
                Especialista: Carlos
              </p>
            </div>

            <div className="mt-5 grid grid-cols-5 gap-2">
              {TRACKING_STEPS.map((step) => (
                <div key={step.label} className="space-y-1.5">
                  <div
                    className={`h-1.5 rounded-full ${
                      step.active ? 'bg-[#8B5CF6]' : 'bg-white/20'
                    }`}
                  />
                  <p
                    className={`truncate text-[11px] font-medium ${
                      step.active ? 'text-white' : 'text-white/55'
                    }`}
                  >
                    {step.label}
                  </p>
                </div>
              ))}
            </div>

            <p className="mt-4 text-[13px] font-normal text-[#E0E7FF]">
              Hora estimada de entrega: <span className="font-semibold text-white">4:30 p. m.</span>
            </p>
          </div>

          {/* Segunda tarjeta superpuesta abajo a la derecha (+24px X, +24px Y) */}
          <div className="absolute bottom-0 right-0 flex w-[320px] items-center gap-3.5 rounded-[16px] border border-white/[0.14] bg-white/[0.10] p-4 shadow-[0_24px_48px_-16px_rgba(15,12,48,0.55)] backdrop-blur-md">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[#8B5CF6]/30 text-white">
              <svg
                className="h-5 w-5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9Zm-4.27 13a2 2 0 0 1-3.46 0" />
              </svg>
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-[13px] font-semibold text-white">
                Tu auto está listo para entrega
              </p>
              <p className="mt-0.5 text-[12px] font-normal text-[#C7D2FE]">Hace 2 min</p>
            </div>
          </div>
        </div>
      </div>

      {/* Zona 3: Abajo (3 puntos de confianza) */}
      <div className="relative z-10 grid grid-cols-3 gap-4 border-t border-white/15 pt-5 text-[#E0E7FF]">
        <div className="flex items-start gap-2.5">
          <svg
            className="mt-0.5 h-5 w-5 shrink-0 text-[#C7D2FE]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <circle cx="12" cy="12" r="9" />
            <path d="M12 7v5l3 2" />
          </svg>
          <div>
            <p className="text-[14px] leading-snug font-medium text-[#E0E7FF]">
              Lun a sáb · 7:00 a. m. – 9:00 p. m.
            </p>
            <p className="mt-0.5 text-[12px] font-normal text-[#C7D2FE]/80">
              Pausa de 12:00 a 2:30 p. m.
            </p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <svg
            className="mt-0.5 h-5 w-5 shrink-0 text-[#C7D2FE]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M12 3l7 3v6c0 4.5-3 8.2-7 9-4-.8-7-4.5-7-9V6l7-3Z" />
            <path d="m9.5 12 1.8 1.8 3.5-3.8" />
          </svg>
          <p className="text-[14px] leading-snug font-medium text-[#E0E7FF]">
            Pago en efectivo al finalizar
          </p>
        </div>

        <div className="flex items-start gap-2.5">
          <svg
            className="mt-0.5 h-5 w-5 shrink-0 text-[#C7D2FE]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M12 21s-6-5.3-6-10.5a6 6 0 1 1 12 0C18 15.7 12 21 12 21Z" />
            <circle cx="12" cy="10.5" r="2" />
          </svg>
          <p className="text-[14px] leading-snug font-medium text-[#E0E7FF]">
            Seguimiento paso a paso
          </p>
        </div>
      </div>
    </div>
  )
}
