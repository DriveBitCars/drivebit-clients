const { chromium } = require('playwright');

async function checkWebServer(port = 8080) {
  const url = `http://localhost:${port}`;
  console.log(`🌐 Проверка веб-сервера на ${url}...`);
  
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 3000 });
    
    const title = await page.title();
    const urlFinal = page.url();
    
    console.log(`✅ Сервер доступен!`);
    console.log(`   URL: ${urlFinal}`);
    console.log(`   Title: ${title}`);
    
    const hasDriveBit = await page.textContent('body').then(text => 
      text.includes('DriveBit') || text.includes('Drivebit')
    );
    
    if (hasDriveBit) {
      console.log(`✅ Контент DriveBit найден на странице`);
    }
    
    const bodyText = await page.textContent('body');
    console.log(`\n📄 Первые 200 символов содержимого:`);
    console.log(bodyText.substring(0, 200));
    
    // Делаем скриншот
    const screenshotPath = 'screenshot.png';
    await page.screenshot({ path: screenshotPath, fullPage: true });
    console.log(`\n📸 Скриншот сохранен: ${screenshotPath}`);
    
    await browser.close();
    return true;
  } catch (error) {
    console.error(`❌ Ошибка при проверке сервера:`, error.message);
    await browser.close();
    return false;
  }
}

const port = process.env.PORT || 8080;
console.log(`Проверяю порт: ${port}`);
checkWebServer(parseInt(port))
  .then(success => process.exit(success ? 0 : 1))
  .catch(error => {
    console.error('Критическая ошибка:', error);
    process.exit(1);
  });

