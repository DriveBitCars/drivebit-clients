const { chromium } = require('playwright');
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 8080;
const USE_PRODUCTION_BUILD = process.env.USE_PRODUCTION_BUILD === 'true';

async function startServer() {
  return new Promise((resolve, reject) => {
    let server;
    
    if (USE_PRODUCTION_BUILD) {
      const distPath = path.join(process.cwd(), 'composeApp/build/dist/js/productionExecutable');
      server = http.createServer((req, res) => {
        let filePath = path.join(distPath, req.url === '/' ? 'index.html' : req.url);
        
        if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
          filePath = path.join(distPath, 'index.html');
        }
        
        const ext = path.extname(filePath);
        const contentType = {
          '.html': 'text/html',
          '.js': 'application/javascript',
          '.wasm': 'application/wasm',
          '.mjs': 'application/javascript',
          '.json': 'application/json',
          '.css': 'text/css',
          '.svg': 'image/svg+xml',
          '.jpg': 'image/jpeg',
          '.png': 'image/png',
        }[ext] || 'application/octet-stream';
        
        fs.readFile(filePath, (err, data) => {
          if (err) {
            console.error(`File not found: ${filePath}, error: ${err.message}`);
            res.writeHead(404);
            res.end('Not found');
          } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(data);
          }
        });
      });
    } else {
      console.log('⚠️  Development server mode - make sure to run: ./gradlew :composeApp:wasmJsBrowserDevelopmentRun');
    }
    
    server.listen(PORT, () => {
      console.log(`✅ Server started on http://localhost:${PORT}`);
      resolve(server);
    });
    
    server.on('error', reject);
  });
}

async function waitForServer() {
  const maxAttempts = 60;
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const response = await fetch(`http://localhost:${PORT}`);
      if (response.ok) {
        return true;
      }
    } catch (e) {
      // Server not ready yet
    }
    await new Promise(resolve => setTimeout(resolve, 1000));
  }
  return false;
}

async function testButterMenu() {
  let server;
  
  try {
    if (USE_PRODUCTION_BUILD) {
      const distPath = path.join(process.cwd(), 'composeApp/build/dist/js/productionExecutable');
      if (!fs.existsSync(distPath)) {
        throw new Error(`Distribution path does not exist: ${distPath}. Make sure to run: ./gradlew :composeApp:jsBrowserDistribution`);
      }
      if (!fs.existsSync(path.join(distPath, 'index.html'))) {
        throw new Error(`index.html not found in ${distPath}`);
      }
      console.log(`✅ Distribution path exists: ${distPath}`);
      server = await startServer();
      await waitForServer();
    } else {
      console.log('⏳ Waiting for development server to be ready...');
      const isReady = await waitForServer();
      if (!isReady) {
        throw new Error('Development server did not start in time');
      }
    }
    
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({
      viewport: { width: 1280, height: 720 },
    });
    const page = await context.newPage();
    
    console.log(`🌐 Opening http://localhost:${PORT}...`);
    await page.goto(`http://localhost:${PORT}`, { waitUntil: 'networkidle', timeout: 30000 });
    
    console.log('⏳ Waiting for page to load...');
    await page.waitForTimeout(3000);
    
    console.log('📸 Taking initial screenshot...');
    await page.screenshot({ path: 'test-results/before-menu-click.png', fullPage: true });
    
    console.log('🔍 Looking for menu button...');
    let menuButton = null;
    
    // Wait for page to be fully loaded
    await page.waitForLoadState('networkidle');
    
    // Try multiple selectors to find the menu button
    const selectors = [
      'div.universal-button img[alt="Menu"]',
      'div[class*="universal-button"] img[alt="Menu"]',
      'img[alt="Menu"]',
      'img[src*="burger.svg"]',
      '.universal-button',
      'div[class*="universal-button"]'
    ];
    
    for (const selector of selectors) {
      try {
        const elements = await page.$$(selector);
        console.log(`Selector "${selector}" found ${elements.length} elements`);
        
        for (const element of elements) {
          const tagName = await element.evaluate(el => el.tagName.toLowerCase()).catch(() => '');
          const alt = await element.getAttribute('alt').catch(() => '');
          const src = await element.getAttribute('src').catch(() => '');
          const innerHTML = await element.innerHTML().catch(() => '');
          const parentHTML = await element.evaluate(el => el.parentElement?.outerHTML || '').catch(() => '');
          
          console.log(`  Element: tag=${tagName}, alt=${alt}, src=${src?.substring(0, 50)}`);
          
          if (alt === 'Menu' || src?.includes('burger.svg') || innerHTML.includes('burger.svg') || parentHTML.includes('burger.svg')) {
            // If it's an img, get its parent (the button)
            if (tagName === 'img') {
              menuButton = await element.evaluateHandle(el => el.closest('.universal-button, div[class*="universal-button"]') || el.parentElement);
            } else {
              menuButton = element;
            }
            console.log(`✅ Found menu button using selector "${selector}"`);
            break;
          }
        }
        
        if (menuButton) break;
      } catch (e) {
        console.log(`Selector "${selector}" failed: ${e.message}`);
      }
    }
    
    // Fallback: find any div with universal-button class that contains images
    if (!menuButton) {
      const allButtons = await page.$$('div.universal-button, div[class*="universal-button"]');
      console.log(`Found ${allButtons.length} universal-button divs`);
      
      for (let i = 0; i < allButtons.length; i++) {
        const innerHTML = await allButtons[i].innerHTML().catch(() => '');
        const hasBurger = innerHTML.includes('burger.svg') || innerHTML.includes('menu');
        console.log(`Button ${i}: has burger=${hasBurger}`);
        
        if (hasBurger) {
          menuButton = allButtons[i];
          console.log(`✅ Found menu button at index ${i}`);
          break;
        }
      }
    }
    
    if (!menuButton) {
      // Last resort: get page HTML for debugging
      const html = await page.content();
      console.log('Page HTML (first 2000 chars):', html.substring(0, 2000));
      throw new Error('Menu button not found');
    }
    
    page.on('console', msg => {
      const type = msg.type();
      const text = msg.text();
      if (type === 'error' || text.includes('error') || text.includes('Error')) {
        console.log(`🔴 Browser console [${type}]: ${text}`);
      } else if (text.includes('Butter') || text.includes('menu')) {
        console.log(`📝 Browser console [${type}]: ${text}`);
      }
    });
    
    page.on('pageerror', error => {
      console.log(`❌ Page error: ${error.message}`);
    });
    
    console.log('🖱️  Clicking menu button...');
    
    const beforeClickHTML = await page.content();
    const beforeHasMenu = beforeClickHTML.includes('Логин') || beforeClickHTML.includes('Регистрация');
    console.log(`📝 Before click - HTML contains menu: ${beforeHasMenu}`);
    
    await page.evaluate(() => {
      window.testButtonClicked = false;
      const buttons = document.querySelectorAll('.universal-button, button, [role="button"]');
      buttons.forEach(btn => {
        const originalOnClick = btn.onclick;
        btn.onclick = function(e) {
          window.testButtonClicked = true;
          console.log('✅ Button clicked!', btn);
          if (originalOnClick) originalOnClick.call(this, e);
        };
      });
    });
    
    await menuButton.click();
    
    const clicked = await page.evaluate(() => window.testButtonClicked);
    console.log(`🔍 Button click registered: ${clicked}`);
    
    console.log('⏳ Waiting for menu to appear...');
    await page.waitForTimeout(1000);
    
    const htmlAfterClick = await page.content();
    const hasMenuText = htmlAfterClick.includes('Логин') || htmlAfterClick.includes('Регистрация') || htmlAfterClick.includes('Сдать авто');
    console.log(`🔍 After click (1s) - HTML contains menu text: ${hasMenuText}`);
    
    await page.waitForTimeout(2000);
    
    const htmlAfterClick2 = await page.content();
    const hasMenuText2 = htmlAfterClick2.includes('Логин') || htmlAfterClick2.includes('Регистрация') || htmlAfterClick2.includes('Сдать авто');
    console.log(`🔍 After click (3s) - HTML contains menu text: ${hasMenuText2}`);
    
    const allFixedDivs = await page.$$eval('div[style*="position: fixed"], div[style*="position:fixed"]', divs => {
      return divs.map(div => {
        const text = div.textContent || '';
        return {
          text: text.substring(0, 100),
          style: div.getAttribute('style') || '',
          visible: div.offsetParent !== null,
        };
      });
    }).catch(() => []);
    
    console.log(`\n🔍 Found ${allFixedDivs.length} fixed position divs:`);
    allFixedDivs.forEach((div, i) => {
      const text = div.text || '';
      console.log(`  Div ${i}: visible=${div.visible}, text="${text.substring(0, 50)}"`);
    });
    
    const allDivs = await page.$$eval('div', divs => {
      return divs.map((div, i) => {
        const text = div.textContent || '';
        return {
          index: i,
          text: text.substring(0, 50),
          style: div.getAttribute('style') || '',
          classes: div.className || '',
          visible: div.offsetParent !== null,
          position: window.getComputedStyle(div).position,
          zIndex: window.getComputedStyle(div).zIndex,
        };
      }).filter(d => d.text && (d.text.includes('Логин') || d.text.includes('Регистрация') || d.text.includes('Сдать авто')));
    }).catch(() => []);
    
    console.log(`\n🔍 Found ${allDivs.length} divs with menu text:`);
    allDivs.forEach(div => {
      console.log(`  - Text: "${div.text}", Position: ${div.position}, Z-index: ${div.zIndex}, Visible: ${div.visible}`);
    });
    
    console.log('📸 Taking screenshot after menu click...');
    await page.screenshot({ path: 'test-results/after-menu-click.png', fullPage: true });
    
    console.log('🔍 Checking if menu items are visible...');
    const allText = await page.textContent('body').catch(() => '');
    
    console.log('\n📝 Page text content (first 500 chars):');
    console.log(allText.substring(0, 500));
    
    const loginVisible = allText.includes('Логин');
    const registrVisible = allText.includes('Регистрация');
    const beCameAHostVisible = allText.includes('Сдать авто');
    
    const menuDivs = await page.$$('div').catch(() => []);
    console.log(`\n🔍 Found ${menuDivs.length} divs on page`);
    
    let menuFound = false;
    for (let i = 0; i < Math.min(menuDivs.length, 20); i++) {
      const text = await menuDivs[i].textContent().catch(() => '') || '';
      const style = await menuDivs[i].getAttribute('style').catch(() => '') || '';
      if (text && (text.includes('Логин') || text.includes('Регистрация') || text.includes('Сдать авто'))) {
        console.log(`✅ Found menu div ${i}: text="${(text || '').substring(0, 50)}", style="${(style || '').substring(0, 100)}"`);
        menuFound = true;
      }
    }
    
    console.log('\n📊 Menu visibility check:');
    console.log(`  Логин: ${loginVisible ? '✅' : '❌'}`);
    console.log(`  Регистрация: ${registrVisible ? '✅' : '❌'}`);
    console.log(`  Сдать авто: ${beCameAHostVisible ? '✅' : '❌'}`);
    console.log(`  Menu div found: ${menuFound ? '✅' : '❌'}`);
    
    const allVisible = (loginVisible || menuFound) && (registrVisible || menuFound) && (beCameAHostVisible || menuFound);
    
    if (allVisible) {
      console.log('\n✅ SUCCESS: ButterMenu opened correctly!');
    } else {
      console.log('\n❌ FAILED: Some menu items are not visible');
    }
    
    await browser.close();
    
    if (server) {
      server.close();
    }
    
    process.exit(allVisible ? 0 : 1);
  } catch (error) {
    console.error('❌ Error:', error.message);
    if (server) {
      server.close();
    }
    process.exit(1);
  }
}

if (!fs.existsSync('test-results')) {
  fs.mkdirSync('test-results');
}

testButterMenu();

