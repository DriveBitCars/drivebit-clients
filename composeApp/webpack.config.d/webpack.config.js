config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
config.devServer.open = false;
config.devServer.port = 8080;

// Proxy для аватаров - проксируем запросы к /publicbct/avatars/ на внешний сервер
config.devServer.proxy = [
    {
        context: ['/publicbct/avatars'],
        target: 'http://155.212.170.94:9000',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
    },
];

