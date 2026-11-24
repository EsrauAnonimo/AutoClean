import { useState } from 'react'

/**
 * Pagina en memoria una lista: devuelve la página actual, el total y los elementos visibles.
 */
export function usePagination(items, pageSize = 8) {
  const [page, setPage] = useState(1)
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize))
  const safePage = Math.min(page, totalPages)
  const start = (safePage - 1) * pageSize
  const pageItems = items.slice(start, start + pageSize)

  return { page: safePage, setPage, totalPages, pageItems }
}
