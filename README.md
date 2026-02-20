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

Боты получают статический HTML с каталогом машин. Для каждой машины генерируется отдельная страница в `car/{id}.html`. Пользователи получают SPA.

### Проверка prerender через curl

```bash
# Бот получает prerender (title: «Каталог Москва»)
curl -s -H "User-Agent: Mozilla/5.0 (compatible; Googlebot/2.1)" "https://drivebit.my/" | grep -o '<title>[^<]*'
curl -s -H "User-Agent: Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)" "https://drivebit.my/" | grep -o '<title>[^<]*'

# Человек получает SPA (title: «Дешевле проката на 40%»)
curl -s -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0" "https://drivebit.my/" | grep -o '<title>[^<]*'

# Явно запросить prerender-страницу
curl -s "https://drivebit.my/prerender-bot.html" | head -20
curl -s "https://drivebit.ru/prerender-bot-ru.html" | head -20

# Страницы машин
curl -s "https://drivebit.my/car/dacc0a4f-7644-4373-acaa-dbbe41648ab4.html" | grep -o '<title>[^<]*'
```

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

- **Kotlin Multiplatform:** [Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- **Compose Multiplatform:** [GitHub](https://github.com/JetBrains/compose-multiplatform)
- **Kotlin/WASM:** [Documentation](https://kotl.in/wasm/)
- **Issues:** [GitHub Issues](https://github.com/AntonButov/drivebit-clients/issues)

---

**Built with ❤️ using Kotlin Multiplatform and Compose Multiplatform**# GitHub Pages Status: Tue Oct 14 19:59:42 MSK 2025
# Force GitHub Pages update Wed Oct 15 11:40:34 MSK 2025
