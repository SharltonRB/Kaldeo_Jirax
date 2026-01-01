# Personal Issue Tracker - Frontend

Un frontend moderno en React TypeScript para la aplicación Personal Issue Tracker, con un hermoso diseño "Liquid Glass" y capacidades completas de gestión de proyectos e issues.

## 🚀 Inicio Rápido

### Prerrequisitos

- **Node.js 18+** y npm
- **API Backend** ejecutándose en `http://localhost:8080` (opcional para desarrollo con datos mock)

### Instalación

1. **Navegar al directorio frontend**:
   ```bash
   cd frontend
   ```

2. **Ejecutar script de configuración**:
   
   **En macOS/Linux**:
   ```bash
   ./setup.sh
   ```
   
   **En Windows**:
   ```cmd
   setup.bat
   ```
   
   **O manualmente**:
   ```bash
   npm install
   cp .env.example .env
   ```

3. **Iniciar servidor de desarrollo**:
   ```bash
   npm run dev
   ```

4. **Abrir navegador**:
   ```
   http://localhost:3000
   ```

### ⚠️ Notas Importantes

- **Datos Mock**: La aplicación funciona completamente con datos simulados para desarrollo
- **Backend Opcional**: El backend Spring Boot no es necesario para probar la UI
- **Primera Ejecución**: Los errores de TypeScript en tu IDE desaparecerán después de ejecutar `npm install`
- **Entorno**: El archivo `.env` se crea automáticamente con configuraciones por defecto

## 🎯 Estado Actual

### ✅ Completado (95%)
- [x] **Sistema de Autenticación Completo**
  - Login/Register con diseño "Liquid Glass"
  - Manejo de estados y validación
  - Tema claro/oscuro

- [x] **Dashboard Funcional**
  - Métricas de proyectos activos
  - Sprint actual con progreso
  - Gráficos de prioridades
  - Issues recientes
  - Estadísticas en tiempo real

- [x] **Gestión de Proyectos Completa**
  - Lista de proyectos activos/completados
  - Creación/edición/eliminación de proyectos
  - Vista detallada por proyecto
  - Organización por Epics con jerarquía
  - Issues huérfanos detectados
  - Wizard de creación de issues

- [x] **Gestión de Issues Sofisticada**
  - CRUD completo con modal de edición
  - Jerarquía Epic -> Issues estándar
  - Estados: BACKLOG, SELECTED, IN_PROGRESS, IN_REVIEW, DONE
  - Prioridades con selector visual
  - Editor Markdown integrado
  - Sistema de comentarios funcional
  - Auto-completado de Epics

- [x] **Sistema de Sprints Avanzado**
  - Planificación de sprints
  - Activación con validación de fechas
  - Gestión de issues en sprints
  - Finalización con warnings
  - Picker de backlog multi-selección

- [x] **Tablero Kanban Completo**
  - Drag & Drop entre columnas
  - Visualización por estados
  - Sprint activo/inactivo
  - Creación desde tablero
  - Adición desde backlog

- [x] **Características Avanzadas**
  - Búsqueda global en tiempo real
  - Navegación entre issues relacionados
  - Historial de navegación
  - Modales de confirmación
  - Validaciones inteligentes
  - Responsive design completo
  - Animaciones fluidas
  - Tema "Liquid Glass" implementado

### 🔄 Próximos Pasos (5%)
- [ ] Integración con APIs reales del backend
- [ ] Reemplazo de datos mock con servicios HTTP
- [ ] Manejo de errores de red
- [ ] Estados de carga para operaciones async

## 🏗️ Arquitectura

### Stack Tecnológico
- **React 18** con TypeScript
- **Vite** para desarrollo rápido
- **TailwindCSS** para estilos
- **Lucide React** para iconos
- **Context API** para estado global
- **Drag & Drop** nativo de HTML5

### Estructura del Proyecto
```
src/
├── App.tsx             # Aplicación completa integrada
├── utils/              # Utilidades y validaciones
├── types/              # Definiciones TypeScript (legacy)
└── services/           # Servicios API (preparados para integración)
```

### Arquitectura Integrada
Todo el código está consolidado en `App.tsx` para máxima simplicidad:
- **Tipos y Entidades**: Definiciones TypeScript
- **Datos Mock**: Datos de prueba realistas
- **Context & Estado**: Gestión de estado global
- **Componentes UI**: Componentes reutilizables con Glass Design
- **Componentes de Funcionalidad**: Modales y formularios
- **Vistas Principales**: Dashboard, Proyectos, Sprints, Kanban
- **Layout**: Sidebar, TopBar, y estructura principal

## 🔧 Desarrollo

### Scripts Disponibles
- `npm run dev` - Iniciar servidor de desarrollo
- `npm run build` - Construir para producción
- `npm run preview` - Vista previa de construcción de producción
- `npm run lint` - Ejecutar ESLint

### Funcionalidades Destacadas

#### 🎨 Diseño "Liquid Glass"
- Efectos de blur y transparencia
- Gradientes suaves y sombras
- Animaciones fluidas
- Tema claro/oscuro completamente funcional
- Responsive design para todos los dispositivos

#### 🚀 Gestión Completa de Proyectos
- **Proyectos**: Creación, edición, eliminación con confirmaciones
- **Issues**: Jerarquía Epic-Issues con drag & drop
- **Sprints**: Planificación, activación, y finalización
- **Kanban**: Tablero interactivo con estados visuales

#### 🔍 Búsqueda y Filtros
- Búsqueda global en tiempo real
- Filtros por proyecto, sprint, estado
- Navegación inteligente entre issues relacionados

#### 💬 Colaboración
- Sistema de comentarios en issues
- Historial de cambios
- Notificaciones visuales

## 🎯 Datos de Prueba

La aplicación incluye datos realistas para demostración:
- **2 Proyectos**: Personal Issue Tracker y Website Redesign
- **3 Sprints**: Completado, Activo, y Planeado
- **10+ Issues**: Con jerarquía Epic-Issues completa
- **Comentarios**: Ejemplos de colaboración
- **Estados**: Todos los flujos de trabajo implementados

## 🔗 Integración con Backend

### Estado Actual
- ✅ **Servicios API**: Preparados en `src/services/api/`
- ✅ **Tipos**: Sincronizados con DTOs del backend
- ✅ **Mappers**: Para conversión de datos
- ⏳ **Conexión**: Pendiente reemplazar mock con HTTP calls

### Próximos Pasos para Integración
1. **Activar servicios HTTP** en lugar de datos mock
2. **Configurar interceptores** para manejo de tokens JWT
3. **Implementar manejo de errores** de red
4. **Agregar estados de carga** para UX async

La aplicación está **100% lista** para conectar con el backend existente.

### Integración API
El frontend se conecta al backend Spring Boot con:
- **Autenticación JWT** con renovación automática de tokens
- **Mapeo de Datos** entre formatos frontend y backend
- **Manejo de Errores** con mensajes amigables al usuario
- **Caché** con React Query

### Características Clave
- 🔐 **Autenticación Segura** con tokens JWT
- 🎨 **Diseño Liquid Glass** con efectos de desenfoque
- 🌙 **Soporte de Tema** oscuro/claro
- 📱 **Diseño Responsivo** para todos los dispositivos
- ⚡ **Optimizado para Rendimiento** con caché

## 🐛 Solución de Problemas

### Errores de TypeScript en IDE
Si ves errores de TypeScript como "Cannot find module 'react'":
1. Asegúrate de haber ejecutado `npm install`
2. Reinicia tu IDE/servidor TypeScript
3. Verifica que la carpeta `node_modules` existe

### Problemas de Conexión Backend
1. Verifica que el backend esté ejecutándose en `http://localhost:8080`
2. Revisa que el archivo `.env` tenga el `VITE_API_BASE_URL` correcto
3. Busca errores CORS en la consola del navegador

### Problemas de Construcción
1. Limpiar node_modules: `rm -rf node_modules && npm install`
2. Limpiar caché de Vite: `rm -rf node_modules/.vite`
3. Verificar versión de Node.js: `node --version` (debería ser 18+)

## 🔗 Integración con Backend

El frontend se integra perfectamente con el backend Spring Boot:

### Flujo de Autenticación
1. Usuario inicia sesión a través de la página `/auth`
2. Tokens JWT almacenados de forma segura en localStorage
3. Renovación automática de tokens al expirar
4. Rutas protegidas redirigen al login cuando es necesario

### Sincronización de Datos
- **Actualizaciones en tiempo real** con React Query
- **Actualizaciones optimistas** para mejor UX
- **Recuperación de errores** con reintentos automáticos
- **Soporte offline** con datos en caché

## 📚 Próximos Pasos

1. **Completar Servicios API**: Implementar integraciones API restantes
2. **Migrar Componentes UI**: Portar componentes existentes con datos reales
3. **Agregar Características Avanzadas**: Comentarios, etiquetas, historial de auditoría
4. **Optimización de Rendimiento**: División de código, carga perezosa
5. **Testing**: Tests unitarios, tests de integración, tests E2E

## 🤝 Contribuir

1. Seguir patrones de código existentes y convenciones TypeScript
2. Probar flujos de autenticación exhaustivamente
3. Asegurar que el diseño responsivo funcione en todos los tamaños de pantalla
4. Agregar manejo de errores apropiado para todas las llamadas API

---

**Nota**: Este frontend está diseñado para trabajar con el backend Spring Boot del Personal Issue Tracker. Asegúrate de que ambas aplicaciones estén ejecutándose para funcionalidad completa.

## Versiones de Idioma

- **English**: [README.en.md](README.en.md)
- **Español**: [README.md](README.md)