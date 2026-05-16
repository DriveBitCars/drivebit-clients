config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
config.devServer.open = false;
config.devServer.port = process.env.PORT ? parseInt(process.env.PORT, 10) : 'auto';
config.devServer.hot = true;
config.devServer.liveReload = true;

// Proxy для аватаров и фотографий автомобилей - проксируем запросы на внешний сервер
config.devServer.proxy = [
    {
        context: ['/avatar'],
        target: 'http://157.22.252.70:9000',
        pathRewrite: {
            '^/avatar': '/publicbct/avatars',
        },
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
    },
    {
        context: ['/publicbct'],
        target: 'http://157.22.252.70:9000',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
    },
];

