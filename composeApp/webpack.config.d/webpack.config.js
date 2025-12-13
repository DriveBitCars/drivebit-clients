config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
config.devServer.open = false;
config.devServer.port = 8080;

// Proxy для аватаров - проксируем запросы к /publicbct/avatars/ на внешний сервер
// Используем setupMiddlewares для настройки proxy
const setupMiddlewares = config.devServer.setupMiddlewares || ((middlewares, devServer) => middlewares);
config.devServer.setupMiddlewares = (middlewares, devServer) => {
    // Добавляем proxy middleware для аватаров
    try {
        const httpProxy = require('http-proxy-middleware');
        devServer.app.use(
            '/publicbct/avatars',
            httpProxy.createProxyMiddleware({
                target: 'http://213.171.27.185:9000',
                changeOrigin: true,
                secure: false,
                logLevel: 'debug',
            })
        );
    } catch (e) {
        console.warn('http-proxy-middleware not found, proxy disabled');
    }
    return setupMiddlewares(middlewares, devServer);
};

