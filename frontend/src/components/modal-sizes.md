# Modal Size System

Este documento describe el sistema estandarizado de tamaños para modales en la aplicación.

## Tamaños Disponibles

### `MODAL_SIZES.sm` - Small (448px)
- **Uso**: Confirmaciones muy simples, alertas básicas
- **Ejemplo**: Alertas de confirmación mínimas

### `MODAL_SIZES.md` - Medium (512px) 
- **Uso**: Confirmaciones estándar, formularios básicos
- **Ejemplo**: Confirmaciones con texto explicativo

### `MODAL_SIZES.lg` - Large (576px)
- **Uso**: Confirmaciones con más contenido, formularios simples
- **Ejemplo**: Modales de eliminación con advertencias

### `MODAL_SIZES.xl` - Extra Large (672px)
- **Uso**: Formularios medianos, contenido con más espacio
- **Ejemplo**: Selección de opciones, formularios con múltiples campos

### `MODAL_SIZES['2xl']` - 2X Large (768px)
- **Uso**: Formularios amplios, contenido de dos columnas
- **Ejemplo**: Crear proyecto, formularios con secciones

### `MODAL_SIZES['3xl']` - 3X Large (896px)
- **Uso**: Contenido muy ancho, formularios complejos
- **Ejemplo**: Crear issue, formularios con múltiples secciones

### `MODAL_SIZES['4xl']` - 4X Large (1024px)
- **Uso**: Contenido extra ancho, formularios muy complejos
- **Ejemplo**: Configuraciones avanzadas, dashboards

### `MODAL_SIZES['5xl']` - 5X Large (1152px)
- **Uso**: Contenido ultra ancho
- **Ejemplo**: Tablas amplias, vistas de datos

### `MODAL_SIZES['6xl']` - 6X Large (1280px)
- **Uso**: Ancho máximo para modales
- **Ejemplo**: Vistas completas, interfaces complejas

### Tamaños Especiales

#### `MODAL_SIZES.responsive` - Responsivo Agresivo
- **Mobile**: `max-w-md` (448px)
- **Small**: `max-w-lg` (512px) 
- **Medium**: `max-w-xl` (576px)
- **Large**: `max-w-2xl` (672px)
- **XL**: `max-w-3xl` (768px)
- **2XL**: `max-w-4xl` (896px)

#### `MODAL_SIZES.form` - Óptimo para Formularios (672px)
- **Uso**: Formularios estándar que necesitan espacio adecuado
- **Ejemplo**: Crear proyecto, editar configuraciones

#### `MODAL_SIZES.wide` - Ancho (896px)
- **Uso**: Modales que necesitan mucho espacio horizontal
- **Ejemplo**: Crear issue, formularios complejos

#### `MODAL_SIZES.ultrawide` - Ultra Ancho (1152px)
- **Uso**: Modales que necesitan el máximo espacio disponible
- **Ejemplo**: Detalles de issue, vistas completas

## Asignación Actual de Modales

- **Create Project**: `form` (672px) - Espacio adecuado para formulario
- **Create New Issue**: `wide` (896px) - Más espacio para campos múltiples
- **Delete Confirmations**: `lg` (576px) - Espacio para advertencias
- **Sprint Completion**: `xl` (672px) - Espacio para detalles y advertencias
- **Issue Type Selection**: `xl` (672px) - Espacio para botones grandes
- **Epic Parent Selection**: `xl` (672px) - Espacio para lista de opciones
- **Issue Detail**: `ultrawide` (1152px) - Máximo espacio para contenido complejo
- **Backlog Picker**: `6xl` (1280px) - Máximo ancho para tablas
- **Sprint Activation**: `4xl` (896px) - Espacio para calendario y formularios

## Beneficios del Sistema Actualizado

1. **Mejor Aprovechamiento del Espacio**: Los modales ahora usan más espacio en pantallas anchas
2. **Consistencia Visual**: Tamaños estandarizados en toda la aplicación
3. **Responsividad Mejorada**: El tamaño `responsive` se adapta agresivamente
4. **UX Optimizada**: Los modales ya no se ven angostos en pantallas grandes
5. **Flexibilidad**: Múltiples opciones de tamaño para diferentes necesidades