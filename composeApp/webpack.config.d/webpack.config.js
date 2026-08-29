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
        { from: /^\/chats\/?$/, to: '/chats/index.html' },
        { from: /^\/chat\/?$/, to: '/chat/index.html' },
        { from: /^\/verify-otp\/?$/, to: '/verify-otp/index.html' },
        { from: /^\/login-by-phone\/?$/, to: '/login-by-phone/index.html' },
        { from: /^\/login-by-mail\/?$/, to: '/login-by-mail/index.html' },
        { from: /^\/login-by-password\/?$/, to: '/login-by-password/index.html' },
        { from: /^\/my-city-selection\/?$/, to: '/my-city-selection/index.html' },
        { from: /^\/profile\/?$/, to: '/profile/index.html' },
        { from: /^\/my-bookings\/?$/, to: '/my-bookings/index.html' },
        { from: /^\/leave-review\/?$/, to: '/leave-review/index.html' },
        { from: /^\/my-deals\/?$/, to: '/my-deals/index.html' },
        { from: /^\/documents\/?$/, to: '/documents/index.html' },
        { from: /^\/download-booking-contract\/?$/, to: '/download-booking-contract/index.html' },
        { from: /^\/payment\/?$/, to: '/payment/index.html' },
        { from: /^\/edit-name\/?$/, to: '/edit-name/index.html' },
        { from: /^\/change-email\/?$/, to: '/change-email/index.html' },
        { from: /^\/change-phone\/?$/, to: '/change-phone/index.html' },
        { from: /^\/change-password\/?$/, to: '/change-password/index.html' },
        { from: /^\/inspection-act\/?$/, to: '/inspection-act/index.html' },
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

