# Lista de Verificación de Seguridad

[English Version](SECURITY_CHECKLIST.md)

## Resumen

Este documento proporciona una lista de verificación de seguridad para el proyecto Personal Issue Tracker para asegurar que la información sensible no se exponga en el repositorio público de GitHub.

## ✅ Medidas de Seguridad Implementadas

### 1. Protección de Variables de Entorno

#### Archivos Protegidos por .gitignore
- ✅ Todos los archivos `.env` (excepto `.env.example` y `.env.*.template`)
- ✅ `backend/.env` - Contiene secretos JWT y credenciales de base de datos
- ✅ `frontend/.env` - Contiene configuración de API
- ✅ `frontend/.env.production` - URLs de API de producción
- ✅ `frontend/.env.staging` - Configuración de entorno de staging

#### Archivos de Plantilla (Seguros para Commit)
- ✅ `backend/.env.example` - Configuración de ejemplo sin secretos
- ✅ `backend/.env.prod.template` - Plantilla de producción con placeholders
- ✅ `frontend/.env.example` - Configuración de ejemplo del frontend

### 2. Archivos de Configuración

#### Configuración de Spring Boot
Todos los archivos de configuración de producción usan variables de entorno:
- ✅ `application-prod.yml` - Usa sintaxis `${VARIABLE}` para todos los datos sensibles
- ✅ Sin contraseñas, secretos o claves API hardcodeadas
- ✅ Secretos JWT cargados desde variables de entorno
- ✅ Credenciales de base de datos cargadas desde variables de entorno
- ✅ Contraseñas de Redis cargadas desde variables de entorno

#### Archivos de Configuración Seguros (Commiteados)
- ✅ `application.yml` - Configuración base sin secretos
- ✅ `application-dev.yml` - Valores por defecto de desarrollo (no sensibles)
- ✅ `application-test.yml` - Configuración de pruebas (no sensible)
- ✅ `.testcontainers.properties` - Configuración local de Docker (no sensible)

### 3. Docker e Infraestructura

#### Archivos Protegidos
- ✅ `docker-compose.override.yml` - Overrides locales con potenciales secretos
- ✅ `docker-compose.local.yml` - Desarrollo local con credenciales
- ✅ `infrastructure/k8s/secrets/` - Directorio de secretos de Kubernetes
- ✅ `infrastructure/terraform/*.tfvars` - Variables de Terraform con secretos

#### Archivos Seguros (Commiteados)
- ✅ `docker-compose.yml` - Configuración base de desarrollo
- ✅ `docker-compose.prod.yml` - Plantilla de producción usando variables de entorno

### 4. Certificados y Claves

Todos los archivos de certificados y claves están protegidos:
- ✅ `*.key` - Claves privadas
- ✅ `*.pem` - Certificados PEM
- ✅ `*.p12` - Keystores PKCS12
- ✅ `*.jks` - Keystores Java
- ✅ `certs/` - Directorios de certificados
- ✅ `ssl/` - Directorios SSL

### 5. Credenciales de Cloud

Todas las credenciales de proveedores cloud están protegidas:
- ✅ `.aws/` - Credenciales AWS
- ✅ `gcp-credentials.json` - Credenciales Google Cloud
- ✅ `.azure/` - Credenciales Azure
- ✅ `api-keys.txt` - Claves API
- ✅ `tokens.txt` - Tokens de autenticación

### 6. Archivos de Base de Datos

Archivos de base de datos protegidos:
- ✅ `*.sql.backup` - Backups de base de datos
- ✅ `*.dump` - Dumps de base de datos
- ✅ `backup/` y `backups/` - Directorios de backup
- ✅ Directorios de volúmenes de base de datos (postgres_data/, redis_data/, etc.)

### 7. Archivos de IDE y Sistema

Archivos innecesarios excluidos:
- ✅ `.DS_Store` - Archivos de sistema macOS
- ✅ `.idea/` - Configuración IntelliJ IDEA
- ✅ `.vscode/` - Configuración VS Code (excepto configuraciones compartidas)
- ✅ `Thumbs.db` - Caché de miniaturas Windows
- ✅ `*.swp`, `*.swo` - Archivos swap de Vim

### 8. Artefactos de Build y Dependencias

Outputs de build excluidos:
- ✅ `backend/target/` - Output de build Maven
- ✅ `frontend/node_modules/` - Dependencias NPM
- ✅ `frontend/dist/` - Output de build del frontend
- ✅ `*.class`, `*.jar` - Archivos Java compilados

### 9. Logs y Archivos Temporales

Archivos de log protegidos:
- ✅ `logs/` - Directorio de logs de aplicación
- ✅ `*.log` - Todos los archivos de log
- ✅ `*.tmp`, `*.temp` - Archivos temporales
- ✅ `cache/`, `.cache/` - Directorios de caché

### 10. Archivos Personales y de Desarrollo

Archivos personales excluidos:
- ✅ `TODO.md`, `NOTES.md` - Notas personales
- ✅ `personal-notes/` - Documentación personal
- ✅ `scratch/` - Archivos scratch
- ✅ `*.local`, `*.personal`, `*.private` - Configuraciones personales

## 🔒 Guías de Información Sensible

### Qué NUNCA Commitear

1. **Contraseñas y Secretos**
   - Contraseñas de base de datos
   - Secretos JWT
   - Claves API
   - Secretos de cliente OAuth
   - Claves de encriptación

2. **Credenciales**
   - Credenciales de proveedores cloud (AWS, GCP, Azure)
   - Claves de cuenta de servicio
   - Claves privadas SSH
   - Certificados SSL/TLS y claves privadas

3. **Información Personal**
   - Direcciones de email (excepto en documentación)
   - Números de teléfono
   - Tokens API personales
   - Datos de usuario o PII

4. **Configuración de Producción**
   - URLs de base de datos de producción con credenciales
   - Endpoints de API de producción con autenticación
   - URLs de servicios de producción
   - Información de red interna

### Qué es Seguro Commitear

1. **Archivos de Plantilla**
   - Archivos `.env.example` con valores placeholder
   - Archivos `.env.*.template` con nombres de variables
   - Plantillas de configuración con sintaxis `${VARIABLE}`

2. **Valores por Defecto de Desarrollo**
   - URLs de desarrollo local (localhost)
   - Puertos de desarrollo por defecto
   - Feature flags no sensibles
   - Endpoints de API públicos (sin autenticación)

3. **Documentación**
   - Instrucciones de setup
   - Diagramas de arquitectura
   - Documentación de API
   - Guías de desarrollo

## 🛡️ Mejores Prácticas de Seguridad

### Antes de Commitear

1. **Revisar Cambios**
   ```bash
   git diff
   git status
   ```

2. **Buscar Secretos**
   ```bash
   # Buscar potenciales secretos
   git diff | grep -i "password\|secret\|key\|token"
   ```

3. **Verificar .gitignore**
   ```bash
   # Verificar si el archivo está ignorado
   git check-ignore <filename>
   ```

### Variables de Entorno

1. **Usar Variables de Entorno**
   - Nunca hardcodear secretos en el código
   - Usar sintaxis `${VARIABLE}` en archivos de configuración
   - Cargar desde entorno o sistemas de gestión de secretos

2. **Documentar Variables Requeridas**
   - Listar todas las variables de entorno requeridas en `.env.example`
   - Proporcionar descripciones y valores de ejemplo
   - Documentar en README y guías de despliegue

3. **Separar Entornos**
   - Usar diferentes secretos para dev, staging y producción
   - Nunca usar secretos de producción en desarrollo
   - Rotar secretos regularmente

### Gestión de Secretos

1. **Desarrollo**
   - Usar archivos `.env` (gitignored)
   - Usar herramientas locales de gestión de secretos
   - Nunca compartir secretos por chat o email

2. **Producción**
   - Usar servicios de gestión de secretos (AWS Secrets Manager, HashiCorp Vault, etc.)
   - Usar variables de entorno en plataformas de despliegue
   - Implementar políticas de rotación de secretos

3. **CI/CD**
   - Usar gestión de secretos de la plataforma CI/CD
   - Nunca loguear secretos en output de CI/CD
   - Usar variables enmascaradas en logs de CI/CD

## 🔍 Comandos de Verificación

### Buscar Secretos Commiteados

```bash
# Verificar si archivos sensibles están rastreados
git ls-files | grep -E "\.env$|\.env\.|secret|password|key"

# Buscar potenciales secretos en archivos commiteados
git grep -i "password\|secret\|api_key\|token" -- '*.yml' '*.properties' '*.json'

# Verificar efectividad de .gitignore
git status --ignored
```

### Verificar Archivos de Entorno

```bash
# Listar todos los archivos .env
find . -name ".env*" -not -path "*/node_modules/*"

# Verificar cuáles están ignorados
find . -name ".env*" -not -path "*/node_modules/*" | xargs -I {} git check-ignore {}
```

### Auditar Archivos de Configuración

```bash
# Buscar valores hardcodeados en configs de Spring Boot
grep -r "password:" backend/src/main/resources/
grep -r "secret:" backend/src/main/resources/

# Solo debería encontrar referencias ${VARIABLE}, no valores reales
```

## 📋 Lista de Verificación Pre-Despliegue

Antes de desplegar a producción:

- [ ] Todos los secretos están almacenados en variables de entorno o gestión de secretos
- [ ] No hay archivos `.env` commiteados (excepto `.example` y `.template`)
- [ ] Todos los archivos de configuración usan sintaxis `${VARIABLE}` para secretos
- [ ] URLs y endpoints de producción no están hardcodeados
- [ ] Certificados SSL/TLS están debidamente asegurados
- [ ] Credenciales de base de datos no están en control de versiones
- [ ] Claves API se cargan desde entorno
- [ ] Secretos JWT son fuertes y únicos por entorno
- [ ] Credenciales cloud no están commiteadas
- [ ] Archivos de backup están gitignored
- [ ] Logs están gitignored
- [ ] Artefactos de build están gitignored

## 🚨 Si los Secretos se Commitean Accidentalmente

Si accidentalmente commiteas secretos:

1. **Rotar el Secreto Inmediatamente**
   - Cambiar la contraseña/clave/token inmediatamente
   - Actualizar en todos los entornos

2. **Eliminar del Historial de Git**
   ```bash
   # Usar git filter-branch o BFG Repo-Cleaner
   # ADVERTENCIA: Esto reescribe el historial
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch <archivo-con-secreto>" \
     --prune-empty --tag-name-filter cat -- --all
   ```

3. **Force Push (si es necesario)**
   ```bash
   git push origin --force --all
   ```

4. **Notificar al Equipo**
   - Informar a miembros del equipo sobre el incidente
   - Asegurar que todos actualicen sus repositorios locales
   - Documentar el incidente para referencia futura

## 📚 Recursos Adicionales

- [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [GitHub Security Best Practices](https://docs.github.com/en/code-security/getting-started/best-practices-for-preventing-data-leaks-in-your-organization)
- [12-Factor App: Config](https://12factor.net/config)

---

**Última Actualización**: 14 de Enero de 2026
**Frecuencia de Revisión**: Trimestral o después de cambios mayores
