# 🚗 Drivebit Clients

**Modern Kotlin Multiplatform mobile and web applications for Drivebit car rental platform.**

[![CI/CD](https://github.com/AntonButov/drivebit-clients/workflows/Fast%20Check/badge.svg)](https://github.com/AntonButov/drivebit-clients/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.0-orange.svg)](https://github.com/JetBrains/compose-multiplatform)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📱 Supported Platforms

- **Android** - Native Android app with Material Design 3
- **iOS** - Native iOS app with iOS Human Interface Guidelines
- **Web** - Modern web app with Compose for Web (WASM)

## 🏗️ Architecture

### Modular Structure
```
📦 drivebit-clients/
├── 🎯 composeApp/          # Main application module
├── 📱 Mobile/              # Mobile-specific UI components
├── 🎨 Filters/             # Search and filtering functionality
├── 💾 Storage/             # Cross-platform data storage
├── 🚀 Splash/              # Splash screen module
└── 🍎 iosApp/              # iOS app entry point
```

### Technology Stack
- **UI Framework:** Compose Multiplatform
- **Navigation:** Voyager Navigator
- **Dependency Injection:** Koin (Android/iOS), Manual DI (WASM)
- **Storage:** Multiplatform Settings
- **Serialization:** Kotlinx Serialization
- **Build System:** Gradle with Kotlin DSL

## 🚀 Quick Start

### Prerequisites
- **JDK 17** or higher
- **Android Studio** (for Android development)
- **Xcode** (for iOS development, macOS only)
- **Node.js 18** (for web development)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/AntonButov/drivebit-clients.git
   cd drivebit-clients
   ```

2. **Build all modules**
   ```bash
   ./gradlew build
   ```

## 📱 Platform-Specific Builds

### Android
```bash
# Debug build
./gradlew :composeApp:assembleDebug

# Release build
./gradlew :composeApp:assembleRelease

# Run on device/emulator
./gradlew :composeApp:installDebug
```

### iOS
```bash
# Build iOS framework
./gradlew :composeApp:linkDebugFrameworkIosArm64

# Open in Xcode
open iosApp/iosApp.xcodeproj
```

### Web (WASM)
```bash
# Development server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Production build
./gradlew :composeApp:wasmJsBrowserProductionWebpack
```

## 🧪 Testing & Quality

### Run Tests
```bash
# All tests
./gradlew test

# Specific module
./gradlew :Storage:test
```

### Code Quality
```bash
# Static analysis
./gradlew ktlintCheck detekt

# Format code
./gradlew ktlintFormat
```

## 🔧 Development Features

### Fast CI/CD
- **Quick checks:** 5-8 minutes (PR validation)
- **Full checks:** 25 minutes (main branch only)
- **Parallel builds:** Android, iOS, WASM simultaneously
- **Smart caching:** Gradle + Kotlin/Native dependencies

### Code Quality Tools
- **Ktlint** - Kotlin code style enforcement
- **Detekt** - Static code analysis
- **GitHub Dependabot** - Automated dependency updates
- **Multi-platform testing** - Shared test code across platforms

## 📚 Key Features

### 🎯 Cross-Platform UI
- **Shared UI code** across Android, iOS, and Web
- **Platform-specific adaptations** for native look and feel
- **Responsive design** for different screen sizes
- **Material Design 3** for Android, iOS HIG for iOS

### 🔍 Search & Filtering
- **Advanced search** with multiple filter options
- **Real-time filtering** by location, price, car type
- **Favorites system** for saved searches
- **Recent searches** history

### 💾 Data Management
- **Cross-platform storage** using Multiplatform Settings
- **Secure token management** for authentication
- **Offline support** for cached data
- **Data synchronization** across devices

### 🚀 Performance
- **Fast CI/CD** with optimized build pipeline
- **Parallel compilation** for faster builds
- **Smart caching** for dependencies
- **Incremental compilation** for development

## 🔍 Prerender для роботов

В деплое по-прежнему генерируются статические `car/{id}.html` и `prerender-bot.html` (плюс sitemap, SEO, ссылки). **Обычные пользователи** могут получать их с диска; **поисковые боты** в актуальном [`nginx-prerender.conf.example`](nginx-prerender.conf.example) должны получать **только HTML из Prerender в Docker** (`127.0.0.1:3000`), а не эти файлы напрямую.

### Googlebot и YandexBot

В `map $http_user_agent $is_bot` заданы в том числе **`~*yandex`**.

**Нельзя** оставлять старую схему `map $bot_home_file` + `try_files $bot_home_file` для `/` — из‑за неё бот видит статический «Каталог Москва» с диска. Нужен **`return 418`** для бота и **`@prerender_bot`** → proxy на Prerender; ответы кэшируются в [`nginx-prerender-cache-http.conf.example`](nginx-prerender-cache-http.conf.example).

Для путей **`/car/*.html`** и **`/prerender-bot.html`** в примере то же правило: **бот → 418 → Prerender**, человек → `try_files` к статике. В [`nginx-prerender.conf.example`](nginx-prerender.conf.example) в regex по-прежнему допускается устаревший URL `/prerender-bot-ru.html`, чтобы старые ссылки вели бота в Prerender.

**Быстрый ответ боту** — когда nginx отдаёт **HIT** из `proxy_cache` (после прогрева или повторного захода). Первый MISS идёт в Chromium и может занимать **десятки секунд**; в примере задано **`proxy_cache_lock_timeout 300s`**, чтобы при одном MISS остальные боты ждали один рендер, а не дублировали нагрузку. На сервере примените тот же таймаут, если ещё стоит `5s`. **Свежесть кэшированного HTML для ботов** в примере: **24 часа** (`proxy_cache_valid 200 1d` в `@prerender_bot`); на проде перенесите те же значения из [`nginx-prerender.conf.example`](nginx-prerender.conf.example) и [`nginx-prerender-cache-http.conf.example`](nginx-prerender-cache-http.conf.example).

**Ночной прогрев (раз в сутки):** workflow [`.github/workflows/prerender-nightly-warm.yml`](.github/workflows/prerender-nightly-warm.yml) (`cron`: `00:15 UTC` ≈ `03:15 MSK`; те же `SERVER_HOST` / `SERVER_USER` / `SERVER_SSH_KEY` / `SERVER_PORT`, что у деплоя). Расписание срабатывает с **default branch** репозитория. Ручной запуск: **Actions → Prerender nightly warm → Run workflow**. Скрипт: [`scripts/warm-prerender-nightly.sh`](scripts/warm-prerender-nightly.sh). Альтернатива — cron на сервере: [`scripts/cron-drivebit-prerender.example`](scripts/cron-drivebit-prerender.example) (после деплоя копии лежат в `/opt/drivebit-scripts/`). После деплоя: [`scripts/warm-prerender-after-deploy.sh`](scripts/warm-prerender-after-deploy.sh); при необходимости **`CLEAR_NGINX_PRERENDER_CACHE=0`**.

Prerender: [`scripts/prerender-docker/README.md`](scripts/prerender-docker/README.md).

### Проверка prerender через curl

```bash
# Бот: HTML от Prerender (после выкладки nginx + работающий Docker на :3000).
# Ожидайте заголовок X-Prerender-Nginx-Cache: MISS/HIT и title после рендера SPA (не обязательно «Каталог Москва»).
curl -sI -H "User-Agent: Mozilla/5.0 (compatible; Googlebot/2.1)" "https://drivebit.ru/" | grep -iE 'HTTP/|x-prerender'

# Человек — SPA из index.html
curl -s -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0" "https://drivebit.ru/" | grep -o '<title>[^<]*'

# Статический каталог в деплое (без бот-UA)
curl -s "https://drivebit.ru/prerender-bot.html" | head -5
```

## 🔎 Swagger Discovery (Retride)

Use the discovery CLI to probe and rank Swagger/OpenAPI endpoints for `retride.ru`:

```bash
python scripts/swagger_discovery/retride_discovery.py --domain retride.ru --out-dir .firecrawl/retride
```

Dry-run with deterministic timeout parameters:

```bash
python scripts/swagger_discovery/retride_discovery.py --domain retride.ru --out-dir .firecrawl/retride --timeout-connect 3 --timeout-read 7
```

More details: [`scripts/swagger_discovery/README.md`](scripts/swagger_discovery/README.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Add tests for new features
- Ensure all CI checks pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- **Дизайн (Figma):** [Untitled — главная / промо-блоки](https://www.figma.com/design/HKUnzXUJHqkwH7KS1RAB75/Untitled?node-id=0-1&t=0xkSUSnUmMeuvoQw-1)
- **Kotlin Multiplatform:** [Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- **Compose Multiplatform:** [GitHub](https://github.com/JetBrains/compose-multiplatform)
- **Kotlin/WASM:** [Documentation](https://kotl.in/wasm/)
- **Issues:** [GitHub Issues](https://github.com/AntonButov/drivebit-clients/issues)

---

**Built with ❤️ using Kotlin Multiplatform and Compose Multiplatform**# GitHub Pages Status: Tue Oct 14 19:59:42 MSK 2025
# Force GitHub Pages update Wed Oct 15 11:40:34 MSK 2025
