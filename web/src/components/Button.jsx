const VARIANT_CLASSES = {
  primary:
    'bg-sky-600 text-white hover:bg-sky-700 focus-visible:outline-sky-600 disabled:bg-sky-400',
  secondary:
    'bg-white text-slate-700 ring-1 ring-inset ring-slate-300 hover:bg-slate-50 focus-visible:outline-slate-600',
}

/**
 * Botón reutilizable con variantes visuales de Tailwind CSS.
 */
export function Button({
  children,
  type = 'button',
  variant = 'primary',
  loading = false,
  disabled = false,
  className = '',
  ...props
}) {
  const variantClass = VARIANT_CLASSES[variant] || VARIANT_CLASSES.primary

  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center rounded-lg px-4 py-2.5 text-sm font-semibold shadow-sm transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed ${variantClass} ${className}`}
      {...props}
    >
      {loading ? 'Procesando...' : children}
    </button>
  )
}
