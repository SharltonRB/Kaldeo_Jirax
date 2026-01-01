# Testcontainers Troubleshooting

## Problema Actual

Los tests de Testcontainers fallan en macOS con Docker Desktop debido a un problema de conectividad:

```
Could not find a valid Docker environment. Please check configuration.
BadRequestException (Status 400)
```

## Causa

Este es un problema conocido con la combinación de:
- macOS
- Docker Desktop 
- Testcontainers 1.19.3
- Java 21

El problema ocurre porque Testcontainers no puede conectarse correctamente a la API de Docker Desktop, incluso cuando el socket está disponible.

## Soluciones Intentadas

1. ✅ **Configuración de socket**: Configuramos `DOCKER_HOST` y `TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE`
2. ✅ **Variables de entorno**: Probamos diferentes ubicaciones de socket
3. ✅ **Deshabilitación de Ryuk**: Agregamos `TESTCONTAINERS_RYUK_DISABLED=true`
4. ❌ **Resultado**: El problema persiste

## Estado Actual

- ✅ **Tests con H2**: Funcionan perfectamente (rápidos y confiables)
- ❌ **Tests con Testcontainers**: Fallan por problema de conectividad
- ✅ **Docker Compose**: Funciona para desarrollo manual

## Estrategia Recomendada

### Para Desarrollo Local
```bash
# Tests rápidos con H2
mvn test

# PostgreSQL real para pruebas manuales
docker-compose up -d
```

### Para CI/CD
En pipelines de CI/CD (Linux), Testcontainers debería funcionar sin problemas:

```yaml
# GitHub Actions / Jenkins
- name: Run Testcontainers Tests
  run: mvn test -Dspring.profiles.active=testcontainers -Dtestcontainers.enabled=true
```

## Posibles Soluciones Futuras

1. **Actualizar Testcontainers**: Probar versión más reciente
2. **Usar Colima**: Alternativa a Docker Desktop más compatible
3. **Docker en Linux VM**: Usar una VM Linux para tests
4. **Testcontainers Cloud**: Usar servicio cloud de Testcontainers

## Conclusión

El proyecto está bien configurado para ambos enfoques. Los tests con H2 proporcionan feedback rápido para desarrollo, mientras que Docker Compose permite validación manual con PostgreSQL real cuando sea necesario.

Los Testcontainers se pueden arreglar más adelante, pero no bloquean el desarrollo actual.