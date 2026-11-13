const VARIANT_CLASSES = {
  primary:
    'bg-[#4F46E5] text-white hover:bg-[#4338CA] focus-visible:ring-4 focus-visible:ring-[#4F46E5]/25 disabled:bg-[#4F46E5]/50',
  secondary:
    'bg-white text-[#1E1B4B] border border-[#E4E6F2] hover:bg-[#F5F6FB] hover:border-[#4F46E5]/40 focus-visible:ring-4 focus-visible:ring-[#4F46E5]/15 disabled:opacity-50',
  ghost:
    'bg-transparent text-[#5B5F7A] hover:bg-[#EDE9FE]/60 hover:text-[#4F46E5] focus-visible:ring-2 focus-visible:ring-[#4F46E5]/25 disabled:opacity-40',
  danger:
    'bg-[#FEE2E2] text-[#B91C1C] border border-[#FECACA] hover:bg-[#B91C1C] hover:text-white focus-visible:ring-4 focus-visible:ring-[#EF4444]/25 disabled:opacity-45',
}

const SIZE_CLASSES = {
  m: 'h-[44px] px-4 text-sm',
  l: 'h-[52px] px-5 text-[15px]',
}

/**
 * Botón del UI Kit AutoClean con soporte de variantes y tamaños M (44px) / L (52px).
 */
export function Button({
  children,
  type = 'button',
  variant = 'primary',
  size = 'm',
  loading = false,
  disabled = false,
  className = '',
  ...props
}) {
  const variantClass = VARIANT_CLASSES[variant] || VARIANT_CLASSES.primary
  const sizeClass = SIZE_CLASSES[size] || SIZE_CLASSES.m

  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition-all focus-visible:outline-none disabled:cursor-not-allowed ${sizeClass} ${variantClass} ${className}`}
      {...props}
    >
      {loading ? (
        <>
          <span
            aria-hidden="true"
            className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
          />
          <span>Procesando...</span>
        </>
      ) : (
        children
      )}
    </button>
  )
}
