import { AutoCleanLogo } from './AutoCleanLogo'

const TRACKING_STEPS = [
  { label: 'Recibido', active: true },
  { label: 'Lavando', active: true },
  { label: 'Detallando', active: true },
  { label: 'Listo', active: false },
  { label: 'Entregado', active: false },
]

/**
 * Panel visual 50/50 para Login y Registro (oculto en móvil).
 * Una sola columna centrada verticalmente con eslogan protagonista y una tarjeta única de seguimiento.
 */
export function AuthHeroPanel() {
  return (
    <div className="hidden bg-gradient-to-br from-[#1E1B4B] to-[#312E81] lg:flex lg:flex-col lg:p-16">
      <div>
        <AutoCleanLogo size="lg" tone="white" />
      </div>

      <div className="my-auto flex flex-col items-start space-y-10">
        <div>
          <h2 className="text-[44px] leading-[1.15] font-semibold tracking-tight text-white">
            Brillo que se nota.
          </h2>
          <p className="mt-3 text-[16px] leading-relaxed font-normal text-[#C7D2FE]">
            Lavado y detailing profesional.
          </p>
        </div>

        <div className="w-full max-w-[420px] rounded-[16px] border border-white/[0.12] bg-white/[0.08] p-5">
          <div className="flex items-center justify-between gap-3">
            <span className="inline-flex items-center rounded-md border border-[#1E1B4B]/20 bg-[#FACC15] px-2.5 py-1 font-mono text-xs font-semibold tracking-wider text-[#1E1B4B]">
              KLT 842
            </span>
            <span className="rounded-full bg-[#8B5CF6] px-3 py-1 text-xs font-semibold text-white">
              Detallando
            </span>
          </div>

          <p className="mt-4 text-[15px] font-semibold text-white">
            Full Detailing Cerámico · Mazda CX-30
          </p>

          <div className="mt-4 grid grid-cols-5 gap-2">
            {TRACKING_STEPS.map((step) => (
              <div key={step.label} className="space-y-1.5">
                <div
                  className={[
                    'h-1.5 rounded-full',
                    step.active ? 'bg-[#8B5CF6]' : 'bg-white/20',
                  ].join(' ')}
                />
                <p
                  className={[
                    'truncate text-[11px] font-normal',
                    step.active ? 'text-white' : 'text-white/55',
                  ].join(' ')}
                >
                  {step.label}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
