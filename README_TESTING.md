# Guía de Testing Optimizada

## Problema Identificado
Los tests estaban tardando demasiado debido a:
- Property tests ejecutando 100 casos por defecto
- Tests de integración con Spring Boot que tardan en inicializar
- Falta de paralelización y optimización

## Solución Implementada

### 1. Perfiles de Maven Optimizados

#### Desarrollo Rápido (Recomendado para desarrollo diario)
```bash
./test-scripts.sh fast
# o
mvn clean test -Pfast-tests
```
- Excluye property tests
- Paralelización habilitada
- ~8 segundos vs ~5+ minutos

#### Property Tests Rápidos
```bash
./test-scripts.sh quick-property
# o
mvn test -Pquick-property-tests
```
- Solo 10 casos por property test
- Para verificación rápida de lógica

#### Tests Completos (Para CI)
```bash
./test-scripts.sh ci
# o
mvn clean test -Pci-tests
```
- 100 casos por property test
- Paralelización completa

### 2. Comandos Útiles

```bash
# Solo compilar sin tests
./test-scripts.sh compile

# Install rápido para desarrollo
./test-scripts.sh install-fast

# Install sin tests
./test-scripts.sh install-skip

# Tests unitarios únicamente
./test-scripts.sh unit
```

### 3. Configuración de QuickTheories

La configuración ahora respeta variables del sistema:
- `quicktheories.examples`: Número de casos (default: 25)
- `quicktheories.shrinks`: Ciclos de reducción (default: 50)

### 4. Optimizaciones Aplicadas

1. **Paralelización**: Tests ejecutan en paralelo
2. **Reutilización de JVM**: `reuseForks=true`
3. **Configuración dinámica**: Property tests configurables
4. **Perfiles específicos**: Diferentes niveles de testing
5. **Warnings eliminados**: Java agent warnings resueltos

### 5. Recomendaciones de Uso

**Durante desarrollo:**
```bash
./test-scripts.sh fast
```

**Antes de commit:**
```bash
./test-scripts.sh quick-property
```

**En CI/CD:**
```bash
./test-scripts.sh ci
```

**Para build rápido:**
```bash
./test-scripts.sh install-fast
```

### 6. Tiempos Esperados

- **fast**: ~8 segundos
- **quick-property**: ~30 segundos  
- **unit**: ~5 segundos
- **ci**: ~2-3 minutos

Esta configuración mantiene la calidad de testing mientras proporciona feedback rápido durante el desarrollo.