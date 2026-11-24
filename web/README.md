# AutoClean — Frontend web

Panel admin y portal de clientes en React + Vite + Tailwind.

```bash
cp .env.example .env   # define VITE_API_URL
npm install
npm run dev            # http://localhost:5173
npm run build
npm run lint
```

Estructura de `src/`:

- `pages/auth/` — login, registro y retorno de OAuth2.
- `pages/admin/` — vistas del panel administrativo.
- `pages/client/` — vistas del portal de clientes.
- `components/ui/` — piezas básicas (botón, input, badges, logo).
- `components/layout/` — layout principal, rutas protegidas y toasts.
- `components/auth/`, `components/vehicles/`, `components/costing/` — componentes por funcionalidad.
- `context/` — `AuthContext` (sesión JWT).
- `services/` — llamadas HTTP al backend (`api.js` configura Axios).
- `utils/` — helpers de formato, fechas, validación y toasts.
