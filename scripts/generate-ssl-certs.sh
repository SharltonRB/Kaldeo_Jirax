#!/bin/bash

# SSL Certificate Generation Script for Personal Issue Tracker
# This script generates self-signed certificates for development/testing
# For production, use certificates from a trusted CA like Let's Encrypt

set -e

# Configuration
DOMAIN="${SSL_DOMAIN:-localhost}"
KEYSTORE_PASSWORD="${SSL_KEYSTORE_PASSWORD:-changeit}"
CERT_DIR="infrastructure/docker/ssl"
KEYSTORE_FILE="$CERT_DIR/keystore.p12"
CERT_FILE="$CERT_DIR/cert.pem"
KEY_FILE="$CERT_DIR/key.pem"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔐 SSL Certificate Generation Script${NC}"
echo "=================================="

# Create SSL directory if it doesn't exist
mkdir -p "$CERT_DIR"

# Check if certificates already exist
if [[ -f "$KEYSTORE_FILE" && -f "$CERT_FILE" && -f "$KEY_FILE" ]]; then
    echo -e "${YELLOW}⚠️  SSL certificates already exist.${NC}"
    read -p "Do you want to regenerate them? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Keeping existing certificates."
        exit 0
    fi
    echo "Regenerating certificates..."
fi

echo -e "${GREEN}📋 Certificate Configuration:${NC}"
echo "Domain: $DOMAIN"
echo "Keystore: $KEYSTORE_FILE"
echo "Certificate: $CERT_FILE"
echo "Private Key: $KEY_FILE"
echo

# Generate private key
echo -e "${GREEN}🔑 Generating private key...${NC}"
openssl genrsa -out "$KEY_FILE" 2048

# Generate certificate signing request
echo -e "${GREEN}📝 Generating certificate signing request...${NC}"
openssl req -new -key "$KEY_FILE" -out "$CERT_DIR/cert.csr" \
    -subj "/C=US/ST=State/L=City/O=Organization/OU=IT Department/CN=$DOMAIN"

# Generate self-signed certificate
echo -e "${GREEN}📜 Generating self-signed certificate...${NC}"
openssl x509 -req -days 365 -in "$CERT_DIR/cert.csr" -signkey "$KEY_FILE" -out "$CERT_FILE" \
    -extensions v3_req -extfile <(cat <<EOF
[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = $DOMAIN
DNS.2 = localhost
DNS.3 = *.localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF
)

# Generate PKCS12 keystore for Spring Boot
echo -e "${GREEN}🏪 Generating PKCS12 keystore...${NC}"
openssl pkcs12 -export -in "$CERT_FILE" -inkey "$KEY_FILE" \
    -out "$KEYSTORE_FILE" -name "issuetracker" \
    -passout pass:"$KEYSTORE_PASSWORD"

# Clean up CSR file
rm -f "$CERT_DIR/cert.csr"

# Set appropriate permissions
chmod 600 "$KEY_FILE" "$KEYSTORE_FILE"
chmod 644 "$CERT_FILE"

echo -e "${GREEN}✅ SSL certificates generated successfully!${NC}"
echo
echo -e "${YELLOW}📋 Next Steps:${NC}"
echo "1. Update your .env.prod file with:"
echo "   SSL_ENABLED=true"
echo "   SSL_KEY_STORE=/app/ssl/keystore.p12"
echo "   SSL_KEY_STORE_PASSWORD=$KEYSTORE_PASSWORD"
echo
echo "2. For production, replace self-signed certificates with CA-signed certificates"
echo "3. Consider using Let's Encrypt for free SSL certificates"
echo
echo -e "${YELLOW}⚠️  Security Notes:${NC}"
echo "- Self-signed certificates will show security warnings in browsers"
echo "- For production, use certificates from a trusted Certificate Authority"
echo "- Keep private keys secure and never commit them to version control"
echo
echo -e "${GREEN}🔍 Certificate Information:${NC}"
openssl x509 -in "$CERT_FILE" -text -noout | grep -A 1 "Subject:"
openssl x509 -in "$CERT_FILE" -text -noout | grep -A 3 "Subject Alternative Name:"