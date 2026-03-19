#!/bin/bash

set -e

echo "🛑 Остановка существующих процессов..."

# Убиваем процессы jsBrowserDevelopmentRun
if pgrep -f "jsBrowserDevelopmentRun" > /dev/null; then
    echo "   Убиваем процессы jsBrowserDevelopmentRun..."
    pkill -f "jsBrowserDevelopmentRun" 2>/dev/null || true
    sleep 1
fi

# Убиваем процессы gradle, связанные с веб-сервером
if pgrep -f "gradle.*jsBrowser" > /dev/null; then
    echo "   Убиваем процессы gradle jsBrowser..."
    pkill -f "gradle.*jsBrowser" 2>/dev/null || true
    sleep 1
fi

echo "✅ Старые процессы остановлены"
echo ""
echo "🚀 Запуск веб-сервера (порт подберётся автоматически)..."
echo "🌐 URL смотрите в выводе webpack-dev-server ниже"
echo ""

# Без PORT webpack использует port: 'auto' и занимает свободный порт
unset PORT
./gradlew :composeApp:jsBrowserDevelopmentRun

