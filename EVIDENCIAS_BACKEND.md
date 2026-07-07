# Evidencias Backend

Capturar evidencias antes de subir a GitHub o desplegar.

## Build y tests

- Consola de `./mvnw clean test`.
- Consola de `./mvnw clean package`.
- Version de Java (`java -version`).

## Seguridad

- Login exitoso en Postman/Thunder.
- Token recibido, ocultando parte central si se comparte captura.
- Login incorrecto con 401.
- Request protegido sin token con 401/403.
- Request protegido con token valido.
- Respuesta de login sin `password`.
- `/api/usuarios` rechazado para rol no admin.

## Flujo funcional

- Creacion de cliente.
- Creacion de vehiculo.
- Creacion de credito TEA.
- Creacion de credito TNA con capitalizacion.
- Error TNA sin capitalizacion.
- Cronograma sin gracia.
- Cronograma gracia total.
- Cronograma gracia parcial.
- Cronograma con balloon y saldo final 0.
- Indicadores VAN/TIR/TCEA.
- Guardado de simulacion.
- Historial listado.
- Comparacion misma moneda.
- Comparacion PEN/USD bloqueada.
- Alertas financieras.
- Tipo de cambio fallback.
- Health check.

## Base de datos y despliegue futuro

- H2 dev conectado o PostgreSQL local conectado.
- Variables de entorno usadas, sin mostrar secretos reales.
- Confirmacion de que `.env`, `data/`, `*.mv.db`, logs y secretos no estan en Git.
- Confirmacion de que no se desplego nada en esta iteracion.
