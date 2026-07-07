# AutoCredit / AutoFinanZ Backend

Backend Spring Boot para simulacion de creditos vehiculares en Peru bajo modalidad Compra Inteligente. Expone una API REST para el frontend Angular, con autenticacion JWT, roles, clientes, vehiculos, creditos, cronograma frances, indicadores financieros, alertas, historial y comparador.

## Tecnologia

- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- H2 para dev/test
- PostgreSQL preparado para prod
- Maven Wrapper

## Ejecutar en desarrollo

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Perfil por defecto: `dev`.

H2 dev:

- URL JDBC: `jdbc:h2:file:./data/autocreditdb`
- Consola: `http://localhost:8080/h2-console`
- Usuario: `sa`
- Password: vacio

Credenciales demo solo en `dev` si `app.data-seeder.enabled=true`:

- `admin@autocredit.pe` / `Admin123`
- `asesor@autocredit.pe` / `Asesor123`
- `analista@autocredit.pe` / `Analista123`
- `cliente@autocredit.pe` / `Cliente123`

No se crean usuarios demo en `prod`.

## Tests y build

```bash
./mvnw clean test
./mvnw clean package
```

## Profiles

- `dev`: H2 archivo, consola H2 habilitada, seeder opcional.
- `test`: H2 en memoria, `ddl-auto=create-drop`, seeder deshabilitado.
- `prod`: PostgreSQL, H2 deshabilitado, `show-sql=false`, JWT y CORS desde variables.

## Variables de entorno

Ver `.env.example`.

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `CORS_ALLOWED_ORIGINS`
- `EXCHANGE_FALLBACK_USD_PEN`
- `JPA_DDL_AUTO`

## Seguridad

- `POST /api/auth/login` es publico.
- `POST /api/auth/register` es publico, pero solo acepta rol `CLIENTE`.
- `/api/usuarios/**` requiere rol `ADMINISTRADOR`.
- `/api/health` y `/api/tipo-cambio/latest` son publicos.
- El resto de endpoints requiere JWT.
- `Usuario.password` es write-only y las respuestas usan `UsuarioResponseDTO`.
- JWT secret en `prod` debe venir de `JWT_SECRET` y tener al menos 32 caracteres.
- CORS se configura con `CORS_ALLOWED_ORIGINS`; no usar `*` con credenciales.

## Integracion con frontend

Base URL local:

```text
http://localhost:8080
```

Header para rutas protegidas:

```text
Authorization: Bearer {{token}}
```

Las respuestas exitosas de negocio se mantienen directas. Los errores usan:

```json
{
  "success": false,
  "message": "Mensaje",
  "data": null
}
```

## Endpoints principales

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/clientes`
- `POST /api/clientes`
- `GET /api/vehiculos`
- `POST /api/vehiculos`
- `GET /api/creditos`
- `POST /api/creditos`
- `GET /api/simulaciones/generar/{creditoId}`
- `GET /api/simulaciones/{creditoId}/indicadores?tasaDescuentoVan=10`
- `GET /api/simulaciones-guardadas`
- `GET /api/simulaciones-guardadas/comparar?idA=&idB=`
- `POST /api/alertas/evaluar`
- `GET /api/tipo-cambio/latest?base=USD&target=PEN`
- `GET /api/usuarios`
- `GET /api/health`

Ver `BACKEND_FRONTEND_CONTRACT.md` para detalle.

## Antes de subir a GitHub

1. Ejecutar `./mvnw clean test`.
2. Ejecutar `./mvnw clean package`.
3. Confirmar que no exista `.env`, `data/`, `*.mv.db`, logs ni secretos reales.
4. Revisar `.env.example`.
5. Revisar `BACKEND_FRONTEND_CONTRACT.md`.

## Despliegue futuro

No desplegar sin:

- PostgreSQL creado.
- `JWT_SECRET` real y robusto.
- `CORS_ALLOWED_ORIGINS` con URL real del frontend.
- Migraciones o estrategia clara de `ddl-auto`.
- Evidencias de build, tests y smoke API.
