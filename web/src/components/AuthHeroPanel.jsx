import { AutoCleanLogo } from './AutoCleanLogo'

const LIVE_STAGES = [
  { label: 'Recibido', done: true },
  { label: 'Lavando', done: true },
  { label: 'Detallando', active: true },
  { label: 'Listo' },
  { label: 'Entregado' },
]

const BENEFITS = [
  { title: 'Por placa', desc: 'Agenda en 4 pasos' },
  { title: 'En vivo', desc: 'Avance etapa a etapa' },
  { title: 'Efectivo', desc: 'Pagas al finalizar' },
]

/**
 * Panel lateral visual compartido para Login y Registro con estética SaaS premium.
 */
export function AuthHeroPanel({ variant = 'login' }) {
  const isRegister = variant === 'register'

  return (
    <div className="relative flex flex-col justify-between overflow-hidden bg-gradient-to-br from-[#131036] via-[#262268] to-[#4338CA] px-6 py-8 text-white lg:col-span-5 lg:p-11">
      {/* Glows atmosféricos y malla sutil */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-24 -right-24 h-72 w-72 rounded-full bg-[#8B5CF6]/35 blur-3xl"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-28 -left-20 h-72 w-72 rounded-full bg-[#06B6D4]/25 blur-3xl"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 opacity-[0.07]"
        style={{
          backgroundImage: 'radial-gradient(#ffffff 1px, transparent 1px)',
          backgroundSize: '22px 22px',
        }}
      />

      <div className="relative z-10">
        <AutoCleanLogo
          size="lg"
          subtitle={isRegister ? 'Portal de Clientes' : 'Detailing & Car Care'}
        />
      </div>

      <div className="relative z-10 my-7 space-y-6 lg:my-0">
        <div className="space-y-3.5">
          <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3.5 py-1 text-xs font-semibold text-indigo-100 backdrop-blur-sm">
            <span className="h-2 w-2 animate-pulse rounded-full bg-cyan-300" />
            {isRegister ? 'Registro en segundos' : 'Lavado y Detailing Premium'}
          </span>

          <h1 className="text-[28px] leading-[1.15] font-extrabold tracking-tight sm:text-[32px] lg:text-[36px]">
            {isRegister ? (
              <>
                Tu garage digital,{' '}
                <span className="bg-gradient-to-r from-[#A5F3FC] via-[#C4B5FD] to-white bg-clip-text text-transparent">
                  siempre bajo control.
                </span>
              </>
            ) : (
              <>
                Tu vehículo impecable, con{' '}
                <span className="bg-gradient-to-r from-[#A5F3FC] via-[#C4B5FD] to-white bg-clip-text text-transparent">
                  seguimiento en tiempo real.
                </span>
              </>
            )}
          </h1>

          <p className="max-w-md text-[15px] leading-relaxed text-indigo-100/85">
            {isRegister
              ? 'Guarda tus placas una sola vez, elige servicio y franja horaria, y recibe aviso cuando tu auto esté listo.'
              : 'Agenda en menos de un minuto con tu placa, observa cada etapa del lavado en vivo y paga en efectivo al recibirlo.'}
          </p>
        </div>

        {/* Vista previa tipo tarjeta en vivo */}
        <div className="hidden rounded-2xl border border-white/15 bg-white/[0.08] p-4 shadow-2xl backdrop-blur-md lg:block">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2.5">
              <span className="rounded-md border border-amber-300/60 bg-amber-300 px-2 py-0.5 font-mono text-[11px] font-extrabold tracking-widest text-[#1E1B4B]">
                KLT842
              </span>
              <div>
                <p className="text-xs font-semibold text-white">Full Detailing Cerámico</p>
                <p className="text-[11px] text-indigo-200/80">Mazda CX-30 · Especialista Carlos</p>
              </div>
            </div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-cyan-400/20 px-2.5 py-1 text-[11px] font-semibold text-cyan-200">
              <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-cyan-300" />
              Detallando
            </span>
          </div>

          <div className="mt-4 grid grid-cols-5 gap-1.5">
            {LIVE_STAGES.map((stage) => (
              <div key={stage.label} className="space-y-1">
                <div
                  className={[
                    'h-1.5 rounded-full transition-all',
                    stage.done
                      ? 'bg-emerald-300'
                      : stage.active
                        ? 'bg-cyan-300 shadow-[0_0_8px_rgba(103,232,249,0.8)]'
                        : 'bg-white/20',
                  ].join(' ')}
                />
                <p
                  className={[
                    'truncate text-[10px] font-medium',
                    stage.done || stage.active ? 'text-white' : 'text-indigo-200/60',
                  ].join(' ')}
                >
                  {stage.label}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Puntos clave compactos */}
        <div className="hidden grid-cols-3 gap-2.5 sm:grid">
          {BENEFITS.map((item) => (
            <div
              key={item.title}
              className="rounded-xl border border-white/10 bg-white/[0.06] px-3 py-2.5 backdrop-blur-xs"
            >
              <p className="text-xs font-bold text-white">{item.title}</p>
              <p className="mt-0.5 text-[11px] text-indigo-200/80">{item.desc}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="relative z-10 hidden items-center justify-between gap-2 border-t border-white/15 pt-4 text-[12px] text-indigo-100/85 lg:flex">
        <span className="inline-flex items-center gap-2">
          <svg className="h-4 w-4 text-cyan-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="9" />
            <path d="M12 7v5l3 2" />
          </svg>
          Horario de atención
        </span>
        <span className="font-semibold tabular-nums text-white">
          7:00 a. m.–12:00 p. m. · 2:30 p. m.–9:00 p. m.
        </span>
      </div>
    </div>
  )
}
