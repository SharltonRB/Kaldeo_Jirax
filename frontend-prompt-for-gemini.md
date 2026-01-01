# Prompt para Gemini: Desarrollo Frontend Personal Issue Tracker

## Contexto del Proyecto

Necesito que desarrolles un frontend completo para un sistema de seguimiento de issues personales (Personal Issue Tracker) que se conectará a un backend REST API ya existente desarrollado en Spring Boot. El frontend debe ser desarrollado en React con TypeScript, utilizando Vite como bundler y Tailwind CSS para estilos.

## Stack Tecnológico Requerido

- **Framework**: React 18+ con TypeScript
- **Build Tool**: Vite
- **Estilos**: Tailwind CSS
- **Tema Visual**: Liquid Glass de Apple (minimalista, translúcido, moderno)
- **Funcionalidades**: Soporte para tema claro y oscuro con toggle
- **Estado**: Context API de React o Zustand para manejo de estado global
- **HTTP Client**: Axios para comunicación con el backend
- **Routing**: React Router DOM

## Diseño Visual y Tema

El diseño debe seguir el estilo **Liquid Glass de Apple** con estas características:

### Estilo Visual General
- Elementos con efecto de vidrio translúcido (backdrop-blur)
- Bordes suaves y redondeados
- Sombras sutiles y difusas
- Espaciado generoso y limpio
- Tipografía moderna y legible (Inter o SF Pro Display)
- Colores suaves y gradientes sutiles
- Animaciones fluidas y transiciones suaves

### Sistema de Colores
**Tema Claro:**
- Fondo principal: Blanco translúcido con blur
- Elementos de vidrio: rgba(255, 255, 255, 0.7) con backdrop-blur-md
- Texto primario: Gris oscuro (#1f2937)
- Texto secundario: Gris medio (#6b7280)
- Acentos: Azul Apple (#007AFF) y variaciones
- Bordes: rgba(0, 0, 0, 0.1)

**Tema Oscuro:**
- Fondo principal: Negro/gris muy oscuro translúcido
- Elementos de vidrio: rgba(0, 0, 0, 0.3) con backdrop-blur-md
- Texto primario: Blanco (#ffffff)
- Texto secundario: Gris claro (#d1d5db)
- Acentos: Azul más brillante (#0A84FF)
- Bordes: rgba(255, 255, 255, 0.1)

### Toggle de Tema
- Botón toggle ubicado en la esquina superior derecha de la navegación
- Animación suave entre temas
- Persistencia de preferencia en localStorage
- Detección automática de preferencia del sistema

## Arquitectura del Backend (Para Referencia)

El backend expone las siguientes entidades principales:

### Entidades y Relaciones

**User (Usuario)**
- Campos: id, email, password_hash, name, created_at, updated_at
- Relaciones: Tiene muchos Projects, Sprints, Issues, Labels

**Project (Proyecto)**
- Campos: id, user_id, name, project_key, description, created_at, updated_at
- Relaciones: Pertenece a User, tiene muchos Issues e IssueTypes personalizados
- Función: Contenedor organizacional para agrupar trabajo relacionado

**Sprint**
- Campos: id, user_id, name, start_date, end_date, status, created_at, updated_at
- Estados: PLANNED, ACTIVE, COMPLETED
- Relaciones: Pertenece a User, tiene muchos Issues
- Restricción: Solo un sprint ACTIVE por usuario
- Función: Organización temporal del trabajo, independiente de proyectos

**IssueType (Tipo de Issue)**
- Campos: id, project_id, name, description, is_global, created_at
- Tipos Globales: BUG, STORY, TASK, EPIC (disponibles para todos)
- Tipos Personalizados: Específicos por proyecto
- Relaciones: Puede pertenecer a Project (si es personalizado), tiene muchos Issues

**Issue (Issue/Tarea)**
- Campos: id, user_id, project_id, sprint_id, issue_type_id, parent_issue_id, title, description, status, priority, story_points, created_at, updated_at
- Estados: BACKLOG, SELECTED_FOR_DEVELOPMENT, IN_PROGRESS, IN_REVIEW, DONE
- Prioridades: LOW, MEDIUM, HIGH, CRITICAL
- Relaciones: Pertenece a User, Project, IssueType; opcionalmente a Sprint y parent Issue
- Jerarquía: EPICs no tienen padre, otros issues deben tener un EPIC como padre

**Label (Etiqueta)**
- Campos: id, user_id, name, color, created_at
- Relaciones: Pertenece a User, relación many-to-many con Issues

**Comment (Comentario)**
- Campos: id, user_id, issue_id, content, created_at, updated_at
- Relaciones: Pertenece a User e Issue

## Páginas y Ventanas Requeridas

### 1. Página de Autenticación (/auth)

**Propósito**: Manejo de login y registro de usuarios

**Componentes Necesarios:**
- Formulario de Login con campos email y password
- Formulario de Registro con campos name, email, password, confirm password
- Toggle entre Login y Registro
- Validación en tiempo real de formularios
- Manejo de errores de autenticación
- Loading states durante las peticiones

**Funcionalidades:**
- Validación de email formato válido
- Validación de contraseña (mínimo 8 caracteres)
- Confirmación de contraseña en registro
- Envío de credenciales al endpoint POST /api/auth/login
- Envío de datos de registro al endpoint POST /api/auth/register
- Almacenamiento seguro del JWT token
- Redirección automática al dashboard tras autenticación exitosa
- Manejo de errores 401, 400, 500

**Conexión Backend:**
- POST /api/auth/login - Envía {email, password}
- POST /api/auth/register - Envía {name, email, password}
- Recibe JWT token para autenticación posterior

### 2. Dashboard Principal (/)

**Propósito**: Vista general del estado actual del trabajo del usuario

**Secciones Principales:**
- Header con navegación, nombre de usuario y toggle de tema
- Resumen de métricas (issues por estado, sprint activo, proyectos)
- Lista de issues recientes o en progreso
- Accesos rápidos a crear nuevo issue, proyecto o sprint
- Calendario o timeline del sprint activo

**Componentes Necesarios:**
- Cards de métricas con números y gráficos simples
- Lista compacta de issues con estado, prioridad y proyecto
- Botones de acción rápida
- Indicador de sprint activo con progreso
- Navegación lateral o superior

**Funcionalidades:**
- Visualización de estadísticas generales
- Acceso rápido a todas las secciones
- Filtros básicos por estado o prioridad
- Actualización en tiempo real de métricas

**Conexión Backend:**
- GET /api/dashboard/metrics - Obtiene estadísticas generales
- GET /api/issues/recent - Issues recientes del usuario
- GET /api/sprints/active - Sprint activo actual

### 3. Gestión de Proyectos (/projects)

**Propósito**: CRUD completo de proyectos y visualización de issues por proyecto

**Vista Lista de Proyectos:**
- Grid o lista de todos los proyectos del usuario
- Información: nombre, key, descripción, número de issues, fecha creación
- Botones para crear, editar, eliminar proyectos
- Búsqueda y filtrado de proyectos

**Vista Detalle de Proyecto (/projects/:id):**
- Información completa del proyecto
- Lista de todos los issues del proyecto organizados por épicas
- Estadísticas del proyecto (issues por estado, por tipo)
- Gestión de tipos de issues personalizados del proyecto
- Botón para crear nuevo issue en el proyecto

**Modal/Formulario de Proyecto:**
- Campos: name, key, description
- Validación de key único
- Opciones para crear tipos de issues personalizados

**Funcionalidades:**
- Crear proyecto con validación de key único por usuario
- Editar información de proyecto
- Eliminar proyecto (con confirmación)
- Ver todos los issues del proyecto
- Crear tipos de issues personalizados
- Filtrar issues por tipo, estado, prioridad
- Búsqueda de issues dentro del proyecto

**Conexión Backend:**
- GET /api/projects - Lista todos los proyectos del usuario
- POST /api/projects - Crea nuevo proyecto
- GET /api/projects/:id - Obtiene proyecto específico
- PUT /api/projects/:id - Actualiza proyecto
- DELETE /api/projects/:id - Elimina proyecto
- GET /api/projects/:id/issues - Issues del proyecto
- POST /api/projects/:id/issue-types - Crea tipo personalizado

### 4. Gestión de Sprints (/sprints)

**Propósito**: CRUD de sprints y planificación temporal del trabajo

**Vista Lista de Sprints:**
- Lista de todos los sprints (activos, planificados, completados)
- Información: nombre, fechas, estado, número de issues
- Indicador visual del sprint activo
- Botones para crear, editar, activar, completar sprints

**Vista Detalle de Sprint (/sprints/:id):**
- Información completa del sprint
- Board estilo Kanban con columnas por estado de issues
- Drag & drop para mover issues entre estados
- Burndown chart o gráfico de progreso
- Lista de issues asignados al sprint
- Capacidad para agregar/quitar issues del sprint

**Modal/Formulario de Sprint:**
- Campos: name, start_date, end_date
- Validación de fechas (end_date > start_date)
- Selección de issues para incluir en el sprint

**Funcionalidades:**
- Crear sprint con fechas válidas
- Editar sprint (solo si no está completado)
- Activar sprint (solo uno activo por vez)
- Completar sprint
- Agregar/quitar issues del sprint
- Mover issues entre estados via drag & drop
- Ver progreso del sprint
- Filtrar issues por proyecto, tipo, prioridad

**Conexión Backend:**
- GET /api/sprints - Lista todos los sprints del usuario
- POST /api/sprints - Crea nuevo sprint
- GET /api/sprints/:id - Obtiene sprint específico
- PUT /api/sprints/:id - Actualiza sprint
- DELETE /api/sprints/:id - Elimina sprint
- POST /api/sprints/:id/activate - Activa sprint
- POST /api/sprints/:id/complete - Completa sprint
- PUT /api/sprints/:id/issues - Actualiza issues del sprint

### 5. Gestión de Issues (/issues)

**Propósito**: CRUD completo de issues con jerarquía de épicas

**Vista Lista de Issues:**
- Lista/tabla de todos los issues del usuario
- Columnas: título, proyecto, tipo, estado, prioridad, sprint, épica padre
- Filtros avanzados: por proyecto, sprint, estado, tipo, prioridad
- Búsqueda por título o descripción
- Ordenamiento por múltiples criterios
- Vista jerárquica mostrando épicas y sus hijos

**Vista Detalle de Issue (/issues/:id):**
- Información completa del issue
- Campos editables inline: título, descripción, estado, prioridad, story points
- Gestión de labels (agregar/quitar)
- Sección de comentarios con CRUD
- Historial de cambios (audit log)
- Issues hijos (si es épica)
- Botones para cambiar estado, asignar a sprint

**Modal/Formulario de Issue:**
- Campos obligatorios: title, project, issue_type, priority
- Campos opcionales: description, story_points, labels, parent_issue (si no es EPIC)
- Validación: EPICs no pueden tener padre, otros tipos deben tener épica padre
- Selector de proyecto con tipos de issues disponibles
- Selector de épica padre (solo épicas del mismo proyecto)

**Funcionalidades:**
- Crear issue con validación de jerarquía
- Editar todos los campos del issue
- Cambiar estado con transiciones válidas
- Asignar/desasignar de sprint
- Gestionar labels (many-to-many)
- Agregar/editar/eliminar comentarios
- Ver historial de cambios
- Filtrado y búsqueda avanzada
- Vista jerárquica épica-hijos

**Conexión Backend:**
- GET /api/issues - Lista issues con filtros
- POST /api/issues - Crea nuevo issue
- GET /api/issues/:id - Obtiene issue específico
- PUT /api/issues/:id - Actualiza issue
- DELETE /api/issues/:id - Elimina issue
- GET /api/issues/:id/comments - Comentarios del issue
- POST /api/issues/:id/comments - Crea comentario
- GET /api/issues/:id/audit-logs - Historial de cambios

### 6. Gestión de Labels (/labels)

**Propósito**: CRUD de etiquetas para categorización adicional

**Vista Lista de Labels:**
- Grid de labels con color y nombre
- Contador de issues que usan cada label
- Botones para crear, editar, eliminar labels

**Modal/Formulario de Label:**
- Campos: name, color (color picker)
- Validación de nombre único por usuario

**Funcionalidades:**
- Crear label con color personalizado
- Editar nombre y color
- Eliminar label (con advertencia si está en uso)
- Ver issues que usan cada label

**Conexión Backend:**
- GET /api/labels - Lista todos los labels del usuario
- POST /api/labels - Crea nuevo label
- PUT /api/labels/:id - Actualiza label
- DELETE /api/labels/:id - Elimina label

### 7. Configuración/Perfil (/profile)

**Propósito**: Gestión de cuenta de usuario y configuraciones

**Secciones:**
- Información personal (nombre, email)
- Cambio de contraseña
- Preferencias de la aplicación
- Gestión de tipos de issues globales (solo lectura)
- Estadísticas de uso

**Funcionalidades:**
- Actualizar información personal
- Cambiar contraseña con validación
- Configurar preferencias de tema
- Ver estadísticas de productividad

**Conexión Backend:**
- GET /api/user/profile - Información del usuario
- PUT /api/user/profile - Actualiza perfil
- PUT /api/user/password - Cambia contraseña

## Componentes Reutilizables Necesarios

### Componentes de UI Base
- Button (con variantes: primary, secondary, danger, ghost)
- Input (text, email, password, textarea)
- Select/Dropdown
- Modal/Dialog
- Card (con efecto glass)
- Badge (para estados, prioridades, tipos)
- Avatar
- Loading Spinner
- Toast/Notification
- Tooltip
- DatePicker
- ColorPicker

### Componentes de Negocio
- IssueCard (vista compacta de issue)
- IssueForm (formulario completo de issue)
- ProjectCard
- SprintCard
- LabelBadge
- StatusBadge
- PriorityBadge
- CommentItem
- AuditLogItem
- KanbanBoard
- KanbanColumn
- DragDropIssue

### Componentes de Layout
- Header/Navbar
- Sidebar
- PageContainer
- ContentArea
- ThemeToggle

## Manejo de Estado Global

### Context/Store Structure
```typescript
interface AppState {
  user: User | null;
  theme: 'light' | 'dark';
  projects: Project[];
  sprints: Sprint[];
  issues: Issue[];
  labels: Label[];
  issueTypes: IssueType[];
  loading: boolean;
  error: string | null;
}
```

### Actions Necesarias
- Authentication (login, logout, register)
- Theme toggle
- CRUD operations for all entities
- Filtering and searching
- Sprint management (activate, complete)
- Issue state transitions

## Routing Structure

```
/ - Dashboard
/auth - Authentication
/projects - Projects list
/projects/:id - Project detail
/sprints - Sprints list  
/sprints/:id - Sprint detail
/issues - Issues list
/issues/:id - Issue detail
/labels - Labels management
/profile - User profile
```

## Funcionalidades Especiales

### Drag & Drop
- Mover issues entre estados en sprint board
- Reordenar prioridades en listas
- Asignar issues a sprints

### Real-time Updates
- Notificaciones de cambios en issues
- Actualización automática de métricas
- Sincronización entre pestañas

### Responsive Design
- Mobile-first approach
- Adaptación de layouts para tablet y desktop
- Navegación móvil optimizada

### Accessibility
- Soporte completo de teclado
- ARIA labels apropiados
- Contraste adecuado en ambos temas
- Screen reader compatibility

## Consideraciones de Performance

- Lazy loading de componentes
- Paginación en listas largas
- Debounce en búsquedas
- Caching de datos frecuentes
- Optimistic updates para mejor UX

## Manejo de Errores

- Error boundaries para componentes
- Manejo de errores de red
- Validación de formularios
- Mensajes de error user-friendly
- Retry mechanisms para requests fallidos

Este prompt debe generar un frontend completo, moderno y funcional que se integre perfectamente con el backend existente, siguiendo las mejores prácticas de React y TypeScript, con un diseño visual atractivo y una experiencia de usuario fluida.