# QA Backend Checklist

Marcar cada caso con evidencia en `EVIDENCIAS_BACKEND.md`.

## Seguridad

- [ ] Login correcto devuelve token y usuario sin password.
- [ ] Login incorrecto devuelve 401.
- [ ] Token invalido o expirado devuelve 401 controlado.
- [ ] Ruta protegida sin token devuelve 401/403.
- [ ] `/api/usuarios` con rol no admin devuelve 403.
- [ ] `/api/auth/register` rechaza rol distinto de `CLIENTE`.
- [ ] H2 console no esta disponible en `prod`.
- [ ] CORS responde para origen configurado.

## Clientes

- [ ] Crear cliente con DNI valido.
- [ ] Crear cliente con DNI invalido devuelve 400.
- [ ] Crear cliente con RUC invalido devuelve 400.
- [ ] Editar cliente.
- [ ] Correo duplicado devuelve 409.
- [ ] `mis-datos` devuelve el cliente asociado.

## Vehiculos

- [ ] Crear vehiculo con cliente existente.
- [ ] Crear vehiculo con cliente inexistente devuelve 404.
- [ ] Error si cuota inicial >= precio.
- [ ] Editar vehiculo.
- [ ] Eliminar vehiculo.

## Creditos

- [ ] Crear credito TEA.
- [ ] Crear credito TNA con capitalizacion mensual.
- [ ] Error TNA sin capitalizacion.
- [ ] Error gracia >= plazo.
- [ ] Error SIN_GRACIA con mesesGracia > 0.
- [ ] Error balloon mayor al monto financiado.
- [ ] Error moneda credito distinta a moneda vehiculo.
- [ ] Eliminar credito.

## Simulaciones e indicadores

- [ ] Generar cronograma sin gracia.
- [ ] Generar cronograma con gracia total.
- [ ] Generar cronograma con gracia parcial.
- [ ] Generar cronograma con balloon.
- [ ] Ultimo saldo final es 0.
- [ ] Gracia total muestra `cuotaBase=0` e `interesCapitalizado`.
- [ ] VAN no devuelve NaN.
- [ ] TIR no devuelve NaN.
- [ ] TCEA no devuelve NaN.
- [ ] Caso no convergente devuelve null controlado.

## Historial y comparador

- [ ] Guardar simulacion.
- [ ] Guardar otra vez el mismo credito actualiza historial.
- [ ] Listar historial.
- [ ] Duplicar simulacion.
- [ ] Comparar escenarios misma moneda.
- [ ] Bloquear comparacion moneda distinta con `MONEDAS_NO_COMPARABLES`.
- [ ] Eliminar simulacion guardada.

## Alertas y tipo de cambio

- [ ] Alerta cuota/ingreso alto.
- [ ] Alerta balloon alto.
- [ ] Alerta credito USD.
- [ ] Alerta gracia total.
- [ ] Alerta TCEA alta.
- [ ] Escenario sano sin WARN.
- [ ] Tipo de cambio fallback USD/PEN.
- [ ] Tipo de cambio par no soportado devuelve 400.

## Build

- [ ] `./mvnw clean test`
- [ ] `./mvnw clean package`
- [ ] Revisar que no existan secretos ni H2 versionado.
