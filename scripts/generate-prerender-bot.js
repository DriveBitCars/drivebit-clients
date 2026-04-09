#!/usr/bin/env node

const API_BASE = process.env.API_BASE_URL || "https://drivebit.ru/api";
const CITY_ID = "158835";
const PAGE_SIZE = 100;
const OUTPUT = process.env.OUTPUT_PATH || "prerender-bot.html";
const SITEMAP_FILENAME = process.env.SITEMAP_FILENAME || "sitemap.xml";
const BOT_HTML_MIRROR = process.env.BOT_HTML_MIRROR || "";
const SITEMAP_MIRROR = process.env.SITEMAP_MIRROR || "";
const CAR_DIR = process.env.CAR_DIR || "car";
const BASE = process.env.SITE_BASE || "https://drivebit.ru";

function formatCarTitle(car) {
  const g = car.general || {};
  const parts = [];
  if (g.brandName) parts.push(g.brandName);
  if (g.modelName) parts.push(g.modelName);
  if (g.year && g.year > 0) parts.push(String(g.year));
  if (parts.length > 0) return parts.join(" ");
  const idPrefix = car.id?.length > 8 ? car.id.slice(0, 8) : (car.id || "");
  return idPrefix ? `Автомобиль #${idPrefix}` : "Автомобиль";
}

function sanitizePhotoUrl(url) {
  if (!url) return null;
  const idx = url.indexOf("/publicbct/");
  return idx >= 0 ? url.slice(idx) : url;
}

function getFirstPhotoUrl(car) {
  const fromGeneral = car.general?.photos || [];
  const fromTop = car.photos || [];
  const all = [...fromGeneral, ...fromTop];
  const first = all.find((p) => p?.url);
  return sanitizePhotoUrl(first?.url) || null;
}

async function fetchAllCars() {
  const cars = [];
  let page = 1;
  let totalPages = 1;

  while (page <= totalPages) {
    const url = `${API_BASE}/Car/list/filtered/${CITY_ID}?page=${page}&pageSize=${PAGE_SIZE}`;
    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`API error ${res.status}: ${res.statusText} for ${url}`);
    }
    const data = await res.json();
    const items = data.items ?? data.Items ?? [];
    const tp = data.totalPages ?? data.TotalPages ?? 1;
    totalPages = tp;
    cars.push(...items);
    if (items.length === 0) break;
    page++;
  }

  return cars;
}

async function fetchCarDetail(carId) {
  const url = `${API_BASE}/Car/${carId}`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`API error ${res.status} for Car/${carId}`);
  }
  return res.json();
}

function buildCarDetailHtml(car) {
  const name = formatCarTitle(car);
  const dailyRate = car.dailyRate ?? car.DailyRate ?? car.price ?? car.Price ?? 0;
  const description = car.general?.description ?? "";
  const addr = car.general?.address ?? car.address;
  const addrCity = (typeof addr === "object" ? addr?.city : null) ?? "";
  const addrRegion = (typeof addr === "object" ? addr?.region : null) ?? "";

  const photosFromTop = car.photos ?? [];
  const photosFromGeneral = car.general?.photos ?? [];
  const allPhotos = [...photosFromTop, ...photosFromGeneral].filter((p) => p?.url);
  const uniquePhotos = Array.from(new Map(allPhotos.map((p) => [p.id, p])).values());
  const firstPhotoUrl = uniquePhotos[0] ? sanitizePhotoUrl(uniquePhotos[0].url) : null;

  const chassis = car.chassis ?? {};
  const body = car.body ?? {};
  const specs = [];
  if (car.seatsCount ?? car.general?.seats) specs.push(`${car.seatsCount ?? car.general?.seats} мест`);
  if (chassis.engineTypeTranslate) specs.push(chassis.engineTypeTranslate);
  if (chassis.engineVolume) specs.push(`${chassis.engineVolume} л`);
  if (chassis.driveTypeTranslate) specs.push(chassis.driveTypeTranslate);
  if (chassis.transmissionTranslate) specs.push(chassis.transmissionTranslate);
  if (car.availableMileagePerDayKm) specs.push(`${car.availableMileagePerDayKm} км/день`);

  const photoUrlForMeta = firstPhotoUrl ? (firstPhotoUrl.startsWith("http") ? firstPhotoUrl : BASE + firstPhotoUrl) : `${BASE}/images/logos/logo.png`;
  const carUrl = `${BASE}/car/${car.id}.html`;
  const spaUrl = `${BASE}/car-detail?id=${car.id}`;

  const photosHtml = uniquePhotos
    .map((p) => {
      const url = sanitizePhotoUrl(p?.url);
      return url ? `<img src="${escapeHtml(url)}" alt="${escapeHtml(name)}" class="car-detail-photo">` : "";
    })
    .filter(Boolean)
    .join("\n");

  const productLd = {
    "@context": "https://schema.org",
    "@type": "Product",
    name: name,
    description: description || `Аренда ${name} от собственника`,
    ...(photoUrlForMeta && { image: photoUrlForMeta }),
    url: carUrl,
    ...(dailyRate > 0 && {
      offers: {
        "@type": "Offer",
        price: Math.round(dailyRate),
        priceCurrency: "RUB",
      },
    }),
  };

  return `<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Аренда ${escapeHtml(name)} | DriveBit</title>
    <meta name="description" content="Аренда ${escapeHtml(name)} от собственника. ${dailyRate > 0 ? Math.round(dailyRate) + "₽/сутки. " : ""}DriveBit - аренда авто дешевле проката на 40%.">
    <meta name="keywords" content="аренда ${escapeHtml(name)}, аренда авто, прокат автомобилей, DriveBit">
    <meta name="robots" content="index, follow">
    <meta property="og:type" content="product">
    <meta property="og:url" content="${carUrl}">
    <meta property="og:title" content="Аренда ${escapeHtml(name)} | DriveBit">
    <meta property="og:description" content="Аренда от собственника${dailyRate > 0 ? ". " + Math.round(dailyRate) + "₽/сутки" : ""}">
    <meta property="og:image" content="${photoUrlForMeta}">
    <link rel="canonical" href="${carUrl}">
    <link rel="icon" type="image/svg+xml" href="/images/logos/turo_logo.svg">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script type="application/ld+json">${JSON.stringify(productLd)}</script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Poppins', system-ui, sans-serif; }
        body { color: #1a1a1a; background: #fff; line-height: 1.6; }
        .container { max-width: 1000px; margin: 0 auto; padding: 0 20px; }
        header { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 12px 0; }
        .logo { text-decoration: none; color: #2563eb; font-size: 24px; font-weight: 700; }
        .car-detail-photo { width: 100%; max-height: 400px; object-fit: cover; border-radius: 12px; margin-bottom: 8px; }
        .car-detail-photos { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; margin: 24px 0; }
        .car-detail-title { font-size: 1.8rem; margin: 24px 0 8px; }
        .car-detail-price { font-size: 1.8rem; color: #2563eb; font-weight: 700; margin: 16px 0; }
        .car-detail-specs { display: flex; flex-wrap: wrap; gap: 12px; margin: 16px 0; color: #4b5563; }
        .car-detail-spec { background: #f3f4f6; padding: 8px 16px; border-radius: 8px; font-size: 14px; }
        .car-detail-desc { margin: 24px 0; color: #4b5563; }
        .car-detail-location { margin: 16px 0; color: #6b7280; font-size: 14px; }
        .book-btn { display: inline-block; padding: 16px 32px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; text-decoration: none; border-radius: 12px; font-weight: 600; margin: 24px 0; }
        .back-link { color: #2563eb; text-decoration: none; font-size: 14px; margin-top: 24px; display: inline-block; }
    </style>
</head>
<body>
    <header>
        <div class="container">
            <a href="/" class="logo">DriveBit</a>
        </div>
    </header>
    <main class="container">
        <a href="/" class="back-link">← Каталог автомобилей</a>
        <div class="car-detail-photos">${photosHtml || '<div class="car-detail-photo" style="background:#e5e7eb;height:300px;display:flex;align-items:center;justify-content:center;color:#6b7280;">Нет фото</div>'}</div>
        <h1 class="car-detail-title">${escapeHtml(name)}</h1>
        ${addrCity || addrRegion ? `<div class="car-detail-location">📍 ${escapeHtml(addrCity || addrRegion)}</div>` : ""}
        <div class="car-detail-price">${dailyRate > 0 ? Math.round(dailyRate) + "₽" : "Цена по запросу"} <span>/ сутки</span></div>
        ${specs.length > 0 ? `<div class="car-detail-specs">${specs.map((s) => `<span class="car-detail-spec">${escapeHtml(s)}</span>`).join("")}</div>` : ""}
        ${description ? `<div class="car-detail-desc">${escapeHtml(description)}</div>` : ""}
        <a href="${spaUrl}" class="book-btn">Забронировать</a>
    </main>
</body>
</html>`;
}

function escapeHtml(s) {
  if (s == null || s === "") return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function buildSitemap(cars) {
  const today = new Date().toISOString().slice(0, 10);
  const staticUrls = [
    { loc: `${BASE}/`, priority: "1.0", changefreq: "daily" },
    { loc: `${BASE}/list-your-car`, priority: "0.8", changefreq: "weekly" },
    { loc: `${BASE}/list-your-car.html`, priority: "0.8", changefreq: "weekly" },
  ];
  const carUrls = cars.map((car) => ({
    loc: `${BASE}/car/${car.id}.html`,
    priority: "0.7",
    changefreq: "weekly",
  }));
  const urls = [...staticUrls, ...carUrls];
  return `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9
        http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
${urls
  .map(
    (u) => `    <url>
        <loc>${u.loc}</loc>
        <lastmod>${today}</lastmod>
        <changefreq>${u.changefreq}</changefreq>
        <priority>${u.priority}</priority>
    </url>`
  )
  .join("\n")}
</urlset>
`;
}

function buildHtml(cars) {
  const listItems = cars.map((car, idx) => {
    const name = formatCarTitle(car);
    const photoUrl = getFirstPhotoUrl(car);
    const detailUrl = `/car/${car.id}.html`;
    return {
      "@type": "ListItem",
      position: idx + 1,
      item: {
        "@type": "Product",
        name: name,
        ...(photoUrl && { image: photoUrl.startsWith("http") ? photoUrl : BASE + photoUrl }),
        url: `${BASE}${detailUrl}`,
      },
    };
  });

  const itemListLd = {
    "@context": "https://schema.org",
    "@type": "ItemList",
    name: "Аренда автомобилей DriveBit - Каталог",
    description: "Автомобили для аренды от собственников в Москве",
    numberOfItems: cars.length,
    itemListElement: listItems,
  };

  const organizationLd = {
    "@context": "https://schema.org",
    "@type": "Organization",
    name: "DriveBit",
    url: BASE,
    logo: `${BASE}/images/logos/logo.png`,
    description:
      "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7.",
    contactPoint: {
      "@type": "ContactPoint",
      contactType: "Customer Service",
      availableLanguage: "Russian",
    },
  };

  const webSiteLd = {
    "@context": "https://schema.org",
    "@type": "WebSite",
    name: "DriveBit",
    url: BASE,
    potentialAction: {
      "@type": "SearchAction",
      target: `${BASE}/?q={search_term_string}`,
      "query-input": "required name=search_term_string",
    },
  };

  const serviceLd = {
    "@context": "https://schema.org",
    "@type": "Service",
    serviceType: "Car Rental",
    provider: { "@type": "Organization", name: "DriveBit" },
    areaServed: { "@type": "Country", name: "Russia" },
    description:
      "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7.",
  };

  const cardsHtml = cars
    .map((car) => {
      const name = escapeHtml(formatCarTitle(car));
      const price = car.price ?? car.Price ?? car.dailyRate ?? car.DailyRate ?? 0;
      const priceStr = Number(price)
        ? `${Math.round(Number(price))}₽`
        : "Цена по запросу";
      const photoUrl = getFirstPhotoUrl(car);
      const detailUrl = `/car/${car.id}.html`;
      const imgHtml = photoUrl
        ? `<img src="${escapeHtml(photoUrl)}" alt="${name}" class="car-image">`
        : '<div class="car-image car-no-photo">Нет фото</div>';
      return `
    <article class="car-card">
      <a href="${detailUrl}">
        ${imgHtml}
        <div class="car-details">
          <h3 class="car-title">${name}</h3>
          <div class="car-price">${escapeHtml(priceStr)} <span>/ сутки</span></div>
          <span class="book-btn">Смотреть</span>
        </div>
      </a>
    </article>`;
    })
    .join("");

  return `<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DriveBit - Аренда автомобилей от собственников | Каталог Москва</title>
    <meta name="description" content="Аренда автомобилей от собственников в Москве. Дешевле проката на 40%. ${cars.length} автомобилей в каталоге. Полная страховка, поддержка 24/7.">
    <meta name="keywords" content="аренда автомобилей, аренда авто Москва, прокат автомобилей, аренда авто от собственников, каршеринг, аренда машин, DriveBit, аренда авто СПб, аренда авто Калининград">
    <meta name="author" content="DriveBit">
    <meta name="robots" content="index, follow">
    <meta name="language" content="Russian">
    <meta name="revisit-after" content="1 days">
    <meta property="og:type" content="website">
    <meta property="og:url" content="${BASE}/">
    <meta property="og:title" content="DriveBit - Аренда автомобилей от собственников | Каталог">
    <meta property="og:description" content="Аренда автомобилей от собственников в Москве. ${cars.length} автомобилей. Дешевле проката на 40%. Полная страховка.">
    <meta property="og:image" content="${BASE}/images/logos/logo.png">
    <meta property="og:image:width" content="1200">
    <meta property="og:image:height" content="630">
    <meta property="og:locale" content="ru_RU">
    <meta property="og:site_name" content="DriveBit">
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:url" content="${BASE}/">
    <meta name="twitter:title" content="DriveBit - Аренда автомобилей от собственников">
    <meta name="twitter:description" content="Аренда автомобилей от собственников. Дешевле проката на 40%.">
    <meta name="twitter:image" content="${BASE}/images/logos/logo.png">
    <link rel="canonical" href="${BASE}/">
    <link rel="alternate" hreflang="ru" href="${BASE}/">
    <link rel="alternate" hreflang="x-default" href="${BASE}/">
    <link rel="icon" type="image/svg+xml" href="images/logos/turo_logo.svg">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script type="application/ld+json">
    ${JSON.stringify(organizationLd)}
    </script>
    <script type="application/ld+json">
    ${JSON.stringify(webSiteLd)}
    </script>
    <script type="application/ld+json">
    ${JSON.stringify(serviceLd)}
    </script>
    <script type="application/ld+json">
    ${JSON.stringify(itemListLd)}
    </script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Poppins', system-ui, sans-serif; }
        body { color: #1a1a1a; background: #fff; line-height: 1.6; }
        .container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
        header { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 12px 0; }
        .header-content { display: flex; justify-content: space-between; align-items: center; }
        .logo { text-decoration: none; color: #2563eb; font-size: 24px; font-weight: 700; }
        .section-title { text-align: center; margin: 48px 0 24px; }
        .section-title h1 { font-size: 2.5rem; color: #1f2937; }
        .section-title p { color: #6b7280; margin-top: 8px; }
        .cars-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 24px; padding: 24px 0 80px; }
        .car-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border: 1px solid #f1f5f9; }
        .car-card a { text-decoration: none; color: inherit; display: block; }
        .car-image { width: 100%; height: 200px; object-fit: cover; display: block; }
        .car-no-photo { background: #e5e7eb; color: #6b7280; display: flex; align-items: center; justify-content: center; font-size: 14px; }
        .car-details { padding: 16px; }
        .car-title { font-size: 1.1rem; font-weight: 600; color: #1f2937; margin-bottom: 8px; }
        .car-price { font-size: 1.5rem; color: #2563eb; font-weight: 700; }
        .book-btn { display: inline-block; margin-top: 12px; padding: 10px 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; border-radius: 8px; font-weight: 600; font-size: 14px; }
        .seo-text { margin: 40px 0 24px; padding: 24px 0; border-top: 1px solid #e5e7eb; color: #4b5563; font-size: 15px; line-height: 1.8; }
    </style>
</head>
<body>
    <header>
        <div class="container header-content">
            <a href="/" class="logo">DriveBit</a>
        </div>
    </header>
    <main class="container">
        <div class="section-title">
            <h1>Аренда автомобилей от собственников</h1>
            <p>${cars.length} автомобилей в каталоге. Дешевле проката на 40%.</p>
        </div>
        <section class="seo-text">
            <p><strong>DriveBit</strong> — сервис аренды автомобилей от собственников в Москве и других городах России. Аренда авто через DriveBit дешевле классического проката автомобилей на 40% за счёт работы без посредников. В каталоге представлены машины разных марок и классов: от экономичных седанов до внедорожников.</p>
            <p>Прокат автомобилей от собственников включает полную страховку и круглосуточную поддержку. Аренда авто Москва, Санкт-Петербург, Калининград — выбирайте город и бронируйте машину на нужные даты. Каршеринг от частников с прозрачными условиями и быстрым оформлением.</p>
        </section>
        <div class="cars-grid">
${cardsHtml}
        </div>
    </main>
</body>
</html>`;
}

async function main() {
  try {
    const fs = await import("fs");
    const path = await import("path");
    const outputDir = path.dirname(OUTPUT);
    const carOutputDir = path.join(outputDir, CAR_DIR);

    const cars = await fetchAllCars();
    fs.mkdirSync(carOutputDir, { recursive: true });

    for (let i = 0; i < cars.length; i++) {
      const car = cars[i];
      try {
        const detail = await fetchCarDetail(car.id);
        const carHtml = buildCarDetailHtml(detail);
        const carPath = path.join(carOutputDir, `${car.id}.html`);
        fs.writeFileSync(carPath, carHtml, "utf-8");
        console.log(`  [${i + 1}/${cars.length}] car/${car.id}.html`);
      } catch (e) {
        console.warn(`  [${i + 1}/${cars.length}] car/${car.id}.html failed: ${e.message}`);
      }
    }

    const html = buildHtml(cars);
    fs.writeFileSync(OUTPUT, html, "utf-8");
    console.log(`Generated ${path.basename(OUTPUT)} with ${cars.length} cars`);

    if (BOT_HTML_MIRROR) {
      const mirrorPath = path.join(outputDir, BOT_HTML_MIRROR);
      fs.writeFileSync(mirrorPath, html, "utf-8");
      console.log(`Generated ${BOT_HTML_MIRROR} (same catalog HTML as primary output)`);
    }

    const sitemap = buildSitemap(cars);
    const sitemapPath = path.join(outputDir, SITEMAP_FILENAME);
    fs.writeFileSync(sitemapPath, sitemap, "utf-8");
    console.log(`Generated ${SITEMAP_FILENAME} with ${3 + cars.length} URLs`);

    if (SITEMAP_MIRROR) {
      const mirrorSitemapPath = path.join(outputDir, SITEMAP_MIRROR);
      fs.writeFileSync(mirrorSitemapPath, sitemap, "utf-8");
      console.log(`Generated ${SITEMAP_MIRROR} (same URLs as ${SITEMAP_FILENAME})`);
    }
  } catch (err) {
    console.error("Error:", err.message);
    process.exit(1);
  }
}

main();
