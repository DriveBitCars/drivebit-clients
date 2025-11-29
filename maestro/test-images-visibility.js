const { chromium } = require('playwright');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 8080;
const SERVER_URL = `http://localhost:${PORT}`;
const MAX_WAIT_TIME = 60000; // 60 секунд
const CHECK_INTERVAL = 2000; // проверка каждые 2 секунды

let serverProcess = null;

function waitForServer(url, maxWait, checkInterval) {
  return new Promise((resolve, reject) => {
    const startTime = Date.now();
    
    const checkServer = async () => {
      try {
        const response = await fetch(url, { method: 'HEAD' });
        if (response.ok) {
          resolve();
          return;
        }
      } catch (error) {
        // Сервер еще не запущен
      }
      
      if (Date.now() - startTime > maxWait) {
        reject(new Error(`Сервер не запустился за ${maxWait}ms`));
        return;
      }
      
      setTimeout(checkServer, checkInterval);
    };
    
    checkServer();
  });
}

function startServer() {
  return new Promise((resolve, reject) => {
    const useProductionBuild = process.env.USE_PRODUCTION_BUILD === 'true';
    
    if (useProductionBuild) {
      console.log('🚀 Запуск HTTP сервера для production build...');
      const http = require('http');
      const path = require('path');
      const fs = require('fs');
      
      const distPath = path.join(process.cwd(), 'composeApp/build/dist/js/productionExecutable');
      
      if (!fs.existsSync(distPath)) {
        reject(new Error(`Production build не найден: ${distPath}`));
        return;
      }
      
      const server = http.createServer((req, res) => {
        let filePath = path.join(distPath, req.url === '/' ? 'index.html' : req.url);
        
        // Проверяем существование файла
        if (!fs.existsSync(filePath) || !filePath.startsWith(distPath)) {
          filePath = path.join(distPath, 'index.html');
        }
        
        const ext = path.extname(filePath);
        const contentType = {
          '.html': 'text/html',
          '.js': 'application/javascript',
          '.css': 'text/css',
          '.png': 'image/png',
          '.jpg': 'image/jpeg',
          '.jpeg': 'image/jpeg',
          '.svg': 'image/svg+xml',
          '.wasm': 'application/wasm',
          '.mjs': 'application/javascript'
        }[ext] || 'application/octet-stream';
        
        fs.readFile(filePath, (err, data) => {
          if (err) {
            res.writeHead(404);
            res.end('Not found');
          } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(data);
          }
        });
      });
      
      server.listen(PORT, () => {
        console.log(`✅ HTTP сервер запущен на порту ${PORT}`);
        serverProcess = { kill: () => server.close() };
        resolve();
      });
      
      server.on('error', (error) => {
        reject(error);
      });
    } else {
      console.log('🚀 Запуск веб-сервера (dev mode)...');
      serverProcess = spawn('./gradlew', [':composeApp:jsBrowserDevelopmentRun', '--no-daemon'], {
        cwd: process.cwd(),
        stdio: 'pipe',
        shell: true
      });
      
      serverProcess.stdout.on('data', (data) => {
        const output = data.toString();
        if (output.includes('Server started') || output.includes('Compiled successfully')) {
          console.log('✅ Сервер запущен');
        }
      });
      
      serverProcess.stderr.on('data', (data) => {
        console.error(`Server error: ${data}`);
      });
      
      serverProcess.on('error', (error) => {
        reject(error);
      });
      
      resolve();
    }
  });
}

function stopServer() {
  if (serverProcess) {
    console.log('🛑 Остановка сервера...');
    serverProcess.kill();
    serverProcess = null;
  }
  // Также убиваем процессы gradle
  const { execSync } = require('child_process');
  try {
    execSync('pkill -f "jsBrowserDevelopmentRun" || true', { stdio: 'ignore' });
  } catch (e) {
    // Игнорируем ошибки
  }
}

async function testImagesVisibility() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    console.log(`🌐 Открытие ${SERVER_URL}...`);
    await page.goto(SERVER_URL, { waitUntil: 'networkidle', timeout: 30000 });
    
    console.log('✅ Страница загружена');
    
    // Проверяем наличие изображений на странице
    const images = await page.$$eval('img', (imgs) => {
      return imgs.map(img => ({
        src: img.src,
        alt: img.alt,
        naturalWidth: img.naturalWidth,
        naturalHeight: img.naturalHeight,
        complete: img.complete
      }));
    });
    
    console.log(`\n📸 Найдено изображений: ${images.length}`);
    
    if (images.length === 0) {
      throw new Error('❌ На странице не найдено ни одного изображения');
    }
    
    // Проверяем каждое изображение
    const failedImages = [];
    for (const img of images) {
      const isVisible = img.naturalWidth > 0 && img.naturalHeight > 0 && img.complete;
      const status = isVisible ? '✅' : '❌';
      console.log(`${status} ${img.src || img.alt || 'unnamed'}: ${img.naturalWidth}x${img.naturalHeight}`);
      
      if (!isVisible) {
        failedImages.push(img);
      }
    }
    
    if (failedImages.length > 0) {
      throw new Error(`❌ ${failedImages.length} изображений не загружены или не видны`);
    }
    
    // Проверяем конкретные изображения, которые должны быть на главной странице
    const expectedImages = [
      'images/logos/turo_logo.svg',
      'images/filter-main/airplane.svg',
      'images/menu/burger.svg'
    ];
    
    console.log('\n🔍 Проверка ожидаемых изображений:');
    for (const expectedPath of expectedImages) {
      const found = images.some(img => img.src.includes(expectedPath));
      if (found) {
        console.log(`✅ Найдено: ${expectedPath}`);
      } else {
        console.log(`⚠️  Не найдено: ${expectedPath}`);
      }
    }
    
    // Делаем скриншот
    const screenshotsDir = 'test-results';
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true });
    }
    const screenshotPath = path.join(screenshotsDir, 'images-visibility-test.png');
    await page.screenshot({ path: screenshotPath, fullPage: true });
    console.log(`\n📸 Скриншот сохранен: ${screenshotPath}`);
    
    console.log('\n✅ Все изображения видны и загружены!');
    await browser.close();
    return true;
    
  } catch (error) {
    console.error(`\n❌ Ошибка при тестировании:`, error.message);
    
    // Делаем скриншот при ошибке
    try {
      const screenshotsDir = 'test-results';
      if (!fs.existsSync(screenshotsDir)) {
        fs.mkdirSync(screenshotsDir, { recursive: true });
      }
      const screenshotPath = path.join(screenshotsDir, 'images-visibility-test-error.png');
      await page.screenshot({ path: screenshotPath, fullPage: true });
      console.log(`📸 Скриншот ошибки сохранен: ${screenshotPath}`);
    } catch (e) {
      // Игнорируем ошибки скриншота
    }
    
    await browser.close();
    return false;
  }
}

async function main() {
  const shouldStartServer = process.env.SKIP_SERVER_START !== 'true';
  let serverStarted = false;
  
  try {
    if (shouldStartServer) {
      await startServer();
      console.log(`⏳ Ожидание запуска сервера на ${SERVER_URL}...`);
      await waitForServer(SERVER_URL, MAX_WAIT_TIME, CHECK_INTERVAL);
      console.log('✅ Сервер готов');
      serverStarted = true;
    } else {
      console.log('⏳ Ожидание готовности сервера...');
      await waitForServer(SERVER_URL, MAX_WAIT_TIME, CHECK_INTERVAL);
      console.log('✅ Сервер готов');
    }
    
    const success = await testImagesVisibility();
    process.exit(success ? 0 : 1);
    
  } catch (error) {
    console.error('❌ Критическая ошибка:', error.message);
    process.exit(1);
  } finally {
    if (shouldStartServer && serverStarted) {
      stopServer();
    }
  }
}

main();

