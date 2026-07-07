# Backend / Frontend Contract

Base URL local: `http://localhost:8080`

Auth header para rutas protegidas: `Authorization: Bearer {{token}}`

Errores: `ApiResponse` con `success=false`, `message`, `data`.

## Publicos

| Metodo | Ruta | Estado | Request | Response | Observaciones |
|---|---|---|---|---|---|
| GET | `/api/health` | Implementado | - | `{status, service, timestamp}` | Publico |
| GET | `/api/tipo-cambio/latest?base=USD&target=PEN` | Implementado | query | `{base,target,rate,source,updatedAt,message}` | Publico, fallback configurable |
| POST | `/api/auth/login` | Implementado | `{correo,password}` | `{token, usuario}` | `usuario` no incluye password |
| POST | `/api/auth/register` | Implementado | `RegisterRequest` | `UsuarioResponseDTO` | Publico solo para rol `CLIENTE` |

## Clientes

Todas requieren JWT.

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| GET | `/api/clientes` | Implementado | JWT | Asesor lista sus clientes; admin ve todos |
| GET | `/api/clientes/{id}` | Implementado | JWT | Pendiente endurecer ownership para CLIENTE |
| POST | `/api/clientes` | Implementado | JWT | Valida DNI/RUC, telefono, correo/documento unico |
| PUT | `/api/clientes/{id}` | Implementado | JWT | Valida duplicados excluyendo id |
| DELETE | `/api/clientes/{id}` | Implementado | JWT | Elimina vehiculo, credito, cronograma y comparador asociados |
| GET | `/api/clientes/mis-datos` | Implementado | JWT | Busca cliente por correo del usuario |
| PUT | `/api/clientes/mis-datos` | Implementado | JWT | Actualiza cliente asociado |
| GET | `/api/clientes/verificar-correo?correo=&excluirId=` | Implementado | JWT | Devuelve `{duplicado}` |

## Vehiculos

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| GET | `/api/vehiculos` | Implementado | JWT | Filtro `buscar` opcional |
| GET | `/api/vehiculos/{id}` | Implementado | JWT | - |
| GET | `/api/vehiculos/por-cliente/{clienteId}` | Implementado | JWT | Devuelve `null` si no existe |
| POST | `/api/vehiculos` | Implementado | JWT | Valida cliente, anio y cuota inicial |
| PUT | `/api/vehiculos/{id}` | Implementado | JWT | - |
| DELETE | `/api/vehiculos/{id}` | Implementado | JWT | Desvincula del cliente |

## Creditos

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| GET | `/api/creditos` | Implementado | JWT | Lista creditos |
| GET | `/api/creditos/{id}` | Implementado | JWT | - |
| GET | `/api/creditos/por-cliente/{clienteId}` | Implementado | JWT | Devuelve `null` si no existe |
| POST | `/api/creditos` | Implementado | JWT | Valida monto, TNA, gracia, balloon, moneda |
| PUT | `/api/creditos/{id}` | Implementado | JWT | Recalcula monto a financiar y TEM |
| DELETE | `/api/creditos/{id}` | Implementado | JWT | Elimina cronograma e historial asociado |

## Simulaciones

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| GET | `/api/simulaciones/generar/{creditoId}` | Implementado | JWT | Genera cronograma sin guardar |
| POST | `/api/simulaciones` | Implementado | JWT | Guarda o actualiza por `creditoId` |
| GET | `/api/simulaciones/por-credito/{creditoId}` | Implementado | JWT | Devuelve `null` si no existe |
| GET | `/api/simulaciones/{creditoId}/indicadores?tasaDescuentoVan=` | Implementado | JWT | VAN/TIR/TCEA desde deudor |

## Historial y comparador

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| GET | `/api/simulaciones-guardadas` | Implementado | JWT | Filtro `buscar` opcional |
| GET | `/api/simulaciones-guardadas/{id}` | Implementado | JWT | - |
| GET | `/api/simulaciones-guardadas/comparar?idA=&idB=` | Implementado | JWT | No elige mejor opcion si monedas difieren |
| POST | `/api/simulaciones-guardadas/{id}/duplicar` | Implementado | JWT | Crea copia |
| DELETE | `/api/simulaciones-guardadas/{id}` | Implementado | JWT | Elimina simulacion guardada |

## Alertas

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| POST | `/api/alertas/evaluar` | Implementado | JWT | Evalua cuota/ingreso, balloon, USD, gracia, TCEA, costos |

## Usuarios

Todas requieren JWT con rol `ADMINISTRADOR`.

| Metodo | Ruta | Estado | Auth | Observaciones |
|---|---|---|---|---|
| GET | `/api/usuarios` | Implementado | ADMIN | Devuelve `UsuarioResponseDTO` |
| POST | `/api/usuarios` | Implementado | ADMIN | Password temporal explicita obligatoria |
| PUT | `/api/usuarios/{id}` | Implementado | ADMIN | Password temporal opcional para reset |
| PATCH | `/api/usuarios/{id}/estado` | Implementado | ADMIN | Activa/desactiva |
| DELETE | `/api/usuarios/{id}` | Implementado | ADMIN | - |
| GET | `/api/usuarios/estadisticas` | Implementado | ADMIN | - |
| GET | `/api/usuarios/verificar-correo?correo=&excluirId=` | Implementado | ADMIN | Devuelve `{duplicado}` |

## Pendientes funcionales

- Endurecer ownership por `CLIENTE` para acceso a datos propios en endpoints por id.
- Normalizar comparacion PEN/USD si se define tipo de cambio aplicable al comparador.
- Sustituir fallback de tipo de cambio por proveedor real desde backend.
- Definir migraciones versionadas antes de produccion estricta.
