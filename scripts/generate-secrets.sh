#!/bin/bash

# Script para generar secrets seguros para producción
# Uso: ./scripts/generate-secrets.sh

echo "🔐 Generando secrets seguros para producción..."
echo ""
echo "================================================"
echo "JWT SECRET (copia esto para Railway/Vercel):"
echo "================================================"
openssl rand -base64 64
echo ""
echo "================================================"
echo "DATABASE PASSWORD (opcional, Railway lo genera):"
echo "================================================"
openssl rand -base64 32
echo ""
echo "================================================"
echo "REDIS PASSWORD (opcional, Railway lo genera):"
echo "================================================"
openssl rand -base64 24
echo ""
echo "✅ Secrets generados exitosamente!"
echo ""
echo "📝 Instrucciones:"
echo "1. Copia el JWT SECRET y úsalo en Railway como JWT_SECRET"
echo "2. Railway generará automáticamente las passwords de DB y Redis"
echo "3. Guarda estos valores en un lugar seguro (NO los subas a Git)"
echo ""
