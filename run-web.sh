#!/bin/bash

set -e

PORT=8084

echo "🛑 Остановка существующих процессов..."

# Убиваем процессы на порту 8084
if lsof -ti:$PORT > /dev/null 2>&1; then
    echo "   Убиваем процесс на порту $PORT..."
    lsof -ti:$PORT | xargs kill -9 2>/dev/null || true
    sleep 1
fi

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
echo "🚀 Запуск веб-сервера на порту $PORT..."
echo "🌐 После запуска откройте: http://localhost:$PORT"
echo ""

# Запускаем сервер с указанным портом через переменную окружения
# yarn lock теперь обновляется автоматически в build.gradle.kts
# Порт настраивается в build.gradle.kts через переменную PORT
export PORT=$PORT
./gradlew :composeApp:jsBrowserDevelopmentRun --no-daemon

