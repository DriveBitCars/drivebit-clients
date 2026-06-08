#!/bin/bash
# Static assets under /images/ must not fall through to SPA index.html (duplicate SEO titles).
# Serve files when present; 404 otherwise; always X-Robots-Tag: noindex.
# Run on front server: sudo bash scripts/setup-nginx-images-noindex.sh

set -e

NGINX_CONFIG=""
for p in /etc/nginx/sites-available/drivebit.ru /etc/nginx/sites-available/drivebit.my \
    /etc/nginx/sites-available/drivebit-clients /etc/nginx/conf.d/drivebit.conf; do
    if [ -f "$p" ]; then
        NGINX_CONFIG="$p"
        break
    fi
done

if [ -z "$NGINX_CONFIG" ]; then
    echo "nginx config not found"
    exit 1
fi

if grep -q 'location \^~ /images/' "$NGINX_CONFIG"; then
    echo "location ^~ /images/ already configured in $NGINX_CONFIG"
    exit 0
fi

BACKUP="${NGINX_CONFIG}.backup.images-noindex.$(date +%Y%m%d_%H%M%S)"
cp "$NGINX_CONFIG" "$BACKUP"
echo "Backup: $BACKUP"

TEMP_CONFIG=$(mktemp)
cat > "$TEMP_CONFIG" <<'CONF'
    # SEO: do not index static images; never SPA-fallback to index.html
    location ^~ /images/ {
        add_header X-Robots-Tag "noindex, nofollow, noarchive, nosnippet" always;
        try_files $uri =404;
    }
CONF

if grep -q "^[[:space:]]*location /[[:space:]]*{" "$NGINX_CONFIG"; then
    sed -i "/^[[:space:]]*location \/[[:space:]]*{/r $TEMP_CONFIG" "$NGINX_CONFIG"
else
    echo "location / block not found"
    rm -f "$TEMP_CONFIG"
    exit 1
fi
rm -f "$TEMP_CONFIG"

nginx -t
systemctl reload nginx
echo "Configured ^~ /images/ in $NGINX_CONFIG"
