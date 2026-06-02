config.devServer = config.devServer || {};
config.devServer.historyApiFallback = {
    rewrites: [
        { from: /^\/contacts\/?$/, to: '/contacts/index.html' },
        { from: /^\/cookies\/?$/, to: '/cookies/index.html' },
        { from: /^\/privacy\/?$/, to: '/privacy/index.html' },
        { from: /^\/offer\/?$/, to: '/offer/index.html' },
        { from: /^\/payment-success\/?$/, to: '/payment-success/index.html' },
        { from: /^\/payment-failure\/?$/, to: '/payment-failure/index.html' },
        { from: /^\/car-detail/, to: '/car-detail/index.html' },
        { from: /^\/car-photos-gallery/, to: '/car-photos-gallery/index.html' },
    ],
    index: '/index.html',
};
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
    {
        context: ['/privatebct'],
        target: 'http://157.22.252.70:9000',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
    },
];

