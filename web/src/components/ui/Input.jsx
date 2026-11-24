const SIZE_CLASSES = {
  m: 'h-[44px] text-[15px]',
  l: 'h-[52px] text-[15px]',
}

/**
 * Campo de entrada del UI Kit AutoClean con validación en línea.
 */
export function Input({
  id,
  label,
  error,
  size = 'm',
  className = '',
  ...props
}) {
  const sizeClass = SIZE_CLASSES[size] || SIZE_CLASSES.m
  const borderClass = error
    ? 'border-[#B91C1C] bg-[#FEF2F2]/40 focus:border-[#B91C1C] focus:ring-[#EF4444]/20'
    : 'border-[#E4E6F2] bg-white hover:border-[#8B5CF6]/50 focus:border-[#4F46E5] focus:ring-[#4F46E5]/15'

  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={id} className="block text-[13px] font-semibold text-[#1E1B4B]">
          {label}
        </label>
      )}
      <input
        id={id}
        className={`block w-full rounded-xl border px-3.5 text-[#1E1B4B] placeholder-[#5B5F7A]/60 transition focus:ring-4 focus:outline-none disabled:cursor-not-allowed disabled:bg-[#F5F6FB] disabled:text-[#5B5F7A]/70 ${sizeClass} ${borderClass} ${className}`}
        {...props}
      />
      {error && <p className="text-[12px] font-medium text-[#B91C1C]">{error}</p>}
    </div>
  )
}
