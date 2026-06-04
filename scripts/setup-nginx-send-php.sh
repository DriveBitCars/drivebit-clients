#!/bin/bash
# PHP handler for /send.php (lead form on list-your-car.html).
# Run on front server: sudo bash scripts/setup-nginx-send-php.sh

set -e

echo "🔧 Configuring nginx location = /send.php ..."

NGINX_CONFIG=""
if [ -f "/etc/nginx/sites-available/drivebit.ru" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.ru"
elif [ -f "/etc/nginx/sites-available/drivebit.my" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit.my"
elif [ -f "/etc/nginx/sites-available/drivebit-clients" ]; then
    NGINX_CONFIG="/etc/nginx/sites-available/drivebit-clients"
elif [ -f "/etc/nginx/conf.d/drivebit.conf" ]; then
    NGINX_CONFIG="/etc/nginx/conf.d/drivebit.conf"
else
    echo "⚠️  Nginx config not found"
    exit 1
fi

PHP_SOCK=""
for s in /run/php/php8.3-fpm.sock /run/php/php8.2-fpm.sock /run/php/php8.1-fpm.sock /var/run/php/php-fpm.sock; do
    if [ -S "$s" ]; then
        PHP_SOCK="$s"
        break
    fi
done

if [ -z "$PHP_SOCK" ]; then
    echo "⚠️  PHP-FPM socket not found. Install: apt-get install -y php-fpm php-mbstring"
    exit 1
fi

echo "✅ Nginx config: $NGINX_CONFIG"
echo "✅ PHP socket: $PHP_SOCK"

if grep -q 'location = /send.php' "$NGINX_CONFIG"; then
    echo "✅ location = /send.php already configured"
    exit 0
fi

BACKUP="${NGINX_CONFIG}.backup.send-php.$(date +%Y%m%d_%H%M%S)"
cp "$NGINX_CONFIG" "$BACKUP"
echo "💾 Backup: $BACKUP"

TEMP_CONFIG=$(mktemp)
cat > "$TEMP_CONFIG" <<EOF
    # Lead form handler (list-your-car.html)
    location = /send.php {
        include fastcgi_params;
        fastcgi_param SCRIPT_FILENAME \$document_root/send.php;
        fastcgi_pass unix:${PHP_SOCK};
    }

EOF

if grep -q "^[[:space:]]*location /[[:space:]]*{" "$NGINX_CONFIG"; then
    sed -i "/^[[:space:]]*location \/[[:space:]]*{/r $TEMP_CONFIG" "$NGINX_CONFIG"
else
    echo "⚠️  location / block not found — add location = /send.php manually"
    rm -f "$TEMP_CONFIG"
    exit 1
fi
rm -f "$TEMP_CONFIG"

DEPLOY_ROOT="/var/www/drivebit-clients"
if [ -d "$DEPLOY_ROOT" ]; then
    touch "$DEPLOY_ROOT/leads.log"
    chown www-data:www-data "$DEPLOY_ROOT/leads.log" 2>/dev/null || chown nginx:nginx "$DEPLOY_ROOT/leads.log" 2>/dev/null || true
    chmod 664 "$DEPLOY_ROOT/leads.log"
    echo "✅ leads.log ready at $DEPLOY_ROOT/leads.log"
fi

echo "🔍 nginx -t ..."
nginx -t

echo "🔄 Reloading nginx..."
systemctl reload nginx

echo "✅ Done. Test: curl -s -X POST https://drivebit.ru/send.php"
