# 🚗 Drivebit Clients

**Modern Kotlin Multiplatform mobile and web applications for Drivebit car rental platform.**

[CI/CD](https://github.com/AntonButov/drivebit-clients/actions)
[Kotlin](https://kotlinlang.org/)
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
[License](LICENSE)

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

## 🔎 Swagger Discovery (Retride)

Use the discovery CLI to probe and rank Swagger/OpenAPI endpoints for `retride.ru`:

```bash
python scripts/swagger_discovery/retride_discovery.py --domain retride.ru --out-dir .firecrawl/retride
```

Dry-run with deterministic timeout parameters:

```bash
python scripts/swagger_discovery/retride_discovery.py --domain retride.ru --out-dir .firecrawl/retride --timeout-connect 3 --timeout-read 7
```

More details: `[scripts/swagger_discovery/README.md](scripts/swagger_discovery/README.md)`

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

## Серверы

- **Фронт (nginx):** `80.249.146.3` — конфиги переносятся с предыдущего фронта (`/etc/nginx`, `/etc/letsencrypt`). Скрипт: `scripts/nginx-migrate-from-old-server.sh` (по умолчанию `OLD_HOST=root@45.131.42.106`, `NEW_HOST=root@80.249.146.3`; переопределяются переменными окружения).
- **Прежний фронт:** `45.131.42.106`. Upstream API/MinIO: `api.drivebit.ru` (DNS).

---

**Built with ❤️ using Kotlin Multiplatform and Compose Multiplatform**

# GitHub Pages Status: Tue Oct 14 19:59:42 MSK 2025

# Force GitHub Pages update Wed Oct 15 11:40:34 MSK 2025

