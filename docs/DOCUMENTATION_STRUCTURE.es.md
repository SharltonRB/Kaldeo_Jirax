# Estructura de Documentación y Convenciones de Nomenclatura

[English Version](DOCUMENTATION_STRUCTURE.md)

## Resumen

Este documento describe la estructura de documentación y las convenciones de nomenclatura utilizadas en todo el proyecto Personal Issue Tracker.

## Convenciones de Nomenclatura

### Versiones de Idioma

Todos los archivos de documentación siguen un patrón de nomenclatura consistente:

- **Inglés (Principal)**: `filename.md` o `filename.en.md`
- **Español**: `filename.es.md`

### Ejemplos

```
README.md           # Versión en inglés (principal)
README.en.md        # Versión en inglés (explícita)
README.es.md        # Versión en español

SECURITY.md         # Versión en inglés (principal)
SECURITY.es.md      # Versión en español
```

## Estructura de Directorios

```
docs/
├── README.md                           # Índice de documentación (Inglés)
├── README.es.md                        # Índice de documentación (Español)
├── INDEX.md                            # Índice completo de documentación
├── DEVELOPMENT.md                      # Guía de desarrollo
├── SECURITY.md                         # Guías de seguridad
├── PRODUCTION_DEPLOYMENT.md            # Guía de despliegue en producción
├── DOCUMENTATION_STRUCTURE.md          # Este archivo
├── DOCUMENTATION_STRUCTURE.es.md       # Este archivo (Español)
│
├── architecture/                       # Arquitectura del sistema
│   ├── README.md                       # Resumen de arquitectura (Inglés)
│   └── README.es.md                    # Resumen de arquitectura (Español)
│
├── development/                        # Guías de desarrollo
│   ├── FRONTEND_BACKEND_INTEGRATION_ANALYSIS.md
│   ├── FRONTEND_ERRORS_FIXED.md
│   ├── gitignore-guide.md
│   ├── gitignore-summary.md
│   ├── INTEGRATION_SUMMARY.md
│   └── internationalization-summary.md
│
├── fixes/                              # Documentación de correcciones
│   ├── modal_layout_fixes.md
│   └── sprint_completion_fixes.md
│
├── improvements/                       # Mejoras de características
│   ├── error_handling_improvements.md
│   ├── project_notifications_implementation.md
│   ├── project_notifications_implementation.es.md
│   ├── sprint_activation_modal_improvements.md
│   ├── sprint_activation_validation.md
│   ├── sprint_completion_backlog_logic.md
│   ├── sprint_notifications_system.md
│   ├── sprint_notifications_system.es.md
│   ├── test_login_improvements.md
│   └── tooltip_ui_improvement.md
│
├── security/                           # Documentación de seguridad
│   ├── security-guidelines.md
│   ├── SECURITY.md                     # Guía de seguridad (Inglés)
│   └── SECURITY.es.md                  # Guía de seguridad (Español)
│
└── testing/                            # Documentación de testing
    ├── README_TESTING.md
    ├── test_sprint_activation.sh
    ├── test_sprint_calendar.sh
    ├── TESTCONTAINERS_TROUBLESHOOTING.md
    └── TESTING_STRATEGY.md
```

## Categorías de Documentación

### 1. Architecture (`architecture/`)
- Diseño del sistema y diagramas de arquitectura
- Patrones de diseño y decisiones arquitectónicas
- Relaciones entre componentes y flujo de datos

### 2. Development (`development/`)
- Configuración y setup de desarrollo
- Guías de integración
- Integración frontend-backend
- Guías de internacionalización

### 3. Fixes (`fixes/`)
- Correcciones de errores y resolución de problemas
- Descripciones de problemas y soluciones
- Guías de troubleshooting

### 4. Improvements (`improvements/`)
- Mejoras de características
- Mejoras de UI/UX
- Optimizaciones de rendimiento
- Documentación de nueva funcionalidad

### 5. Security (`security/`)
- Mejores prácticas de seguridad
- Autenticación y autorización
- Guías de protección de datos
- Resultados de auditorías de seguridad

### 6. Testing (`testing/`)
- Estrategias y enfoques de testing
- Scripts de prueba y automatización
- Troubleshooting de testing
- Reportes de cobertura de pruebas

## Reglas de Organización de Archivos

### 1. Ubicación
- Documentación de nivel raíz (README, guías principales) → Raíz del proyecto o `docs/`
- Documentación específica de categoría → Subdirectorio apropiado en `docs/`
- Documentación específica de módulo → Directorio del módulo (ej., `backend/`, `frontend/`)

### 2. Nomenclatura
- Usar nombres descriptivos en minúsculas con guiones bajos o guiones
- Incluir sufijo de idioma para versiones no inglesas (`.es.md`)
- Usar nomenclatura consistente en archivos relacionados

### 3. Contenido
- Siempre proporcionar versiones en inglés y español para documentos importantes
- Enlazar entre versiones de idioma en la parte superior de cada archivo
- Mantener la documentación actualizada con los cambios de código

## Reorganización Reciente (Enero 2026)

### Archivos Movidos

1. **NOTIFICACIONES_IMPLEMENTADAS.md** → `docs/improvements/project_notifications_implementation.md`
   - Traducido al inglés
   - Creada versión en español: `project_notifications_implementation.es.md`

2. **.env.prod.template** → `backend/.env.prod.template`
   - Movido al directorio backend para mejor organización

### Archivos Renombrados

1. Archivos README raíz:
   - `README.md` (Español) → `README.es.md`
   - Creado nuevo `README.md` (Inglés, principal)
   - Creado `README.en.md` (Inglés, explícito)

2. Archivos README de docs:
   - Actualizados para seguir convención de nomenclatura consistente
   - Agregados enlaces de versiones de idioma

## Guías de Contribución

Al agregar nueva documentación:

1. **Elegir la ubicación correcta**: Colocar archivos en el directorio de categoría apropiado
2. **Seguir convenciones de nomenclatura**: Usar nomenclatura consistente con sufijos de idioma
3. **Proporcionar traducciones**: Crear versiones en inglés y español para docs importantes
4. **Enlazar versiones**: Agregar enlaces de versiones de idioma en la parte superior de cada archivo
5. **Actualizar índices**: Actualizar `INDEX.md` y archivos README de categoría
6. **Mantener organizado**: No dejar archivos de documentación en la raíz a menos que sean guías principales

## Enlaces de Versiones de Idioma

Agregar estos enlaces en la parte superior de cada archivo de documentación:

```markdown
# Título del Documento

[🇪🇸 Versión en Español](filename.es.md) | [🇬🇧 English Version](filename.en.md)
```

## Mantenimiento

- Revisar la estructura de documentación trimestralmente
- Eliminar documentación obsoleta
- Consolidar información duplicada
- Asegurar que todos los docs importantes tengan traducciones
- Actualizar esta guía cuando la estructura cambie
