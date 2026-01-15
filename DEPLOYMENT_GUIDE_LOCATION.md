# 📍 Ubicación de las Guías de Deployment

## Para Desarrolladores del Proyecto

Si clonaste este repositorio y quieres hacer deployment, las guías detalladas están en tu carpeta local:

```
.deployment-guides/
```

Esta carpeta **NO está en Git** (está en .gitignore) para mantener el repositorio limpio.

## ¿Qué Contiene?

**Guía Principal**:
- **GUIA_DEPLOYMENT_COMPLETA.md** - ⭐ EMPIEZA AQUÍ (paso a paso en español)

**Guías Adicionales**:
- **QUICK_START_DEPLOYMENT.md** - Guía rápida de 30 minutos
- **RAILWAY_DEPLOYMENT.es.md** - Guía completa alternativa
- **DEPLOYMENT_CHECKLIST.md** - Checklist paso a paso
- **DEPLOYMENT_FAQ.md** - Preguntas frecuentes
- Y más...

## Si No Tienes la Carpeta

Si clonaste el repo y no tienes `.deployment-guides/`, es normal. Usa la documentación oficial:

📖 **[docs/PRODUCTION_DEPLOYMENT.md](docs/PRODUCTION_DEPLOYMENT.md)**

## Scripts Disponibles

```bash
# Generar secrets seguros
./scripts/generate-secrets.sh

# Herramientas de deployment
./scripts/deployment-helpers.sh help
```

---

**Nota**: La carpeta `.deployment-guides/` es para uso personal durante el desarrollo. La documentación oficial del proyecto está en `docs/`.
