config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
config.devServer.open = false;
config.devServer.port = 8080;
config.devServer.hot = true;
config.devServer.liveReload = true;

// Proxy для аватаров и фотографий автомобилей - проксируем запросы на внешний сервер
config.devServer.proxy = [
    {
        context: ['/avatar'],
        target: 'http://155.212.170.94:9000',
        pathRewrite: {
            '^/avatar': '/publicbct/avatars',
        },
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
    },
    {
        context: ['/publicbct'],
        target: 'http://155.212.170.94:9000',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
    },
];

