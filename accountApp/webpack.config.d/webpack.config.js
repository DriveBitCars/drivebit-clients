config.devServer = config.devServer || {};
config.devServer.open = false;
config.devServer.port = process.env.PORT ? parseInt(process.env.PORT, 10) : 'auto';
config.devServer.hot = true;
config.devServer.liveReload = true;
config.devServer.historyApiFallback = {
    rewrites: [
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
    ],
};

config.devServer.proxy = [
    {
        context: ['/avatar'],
        target: 'http://157.22.252.70:9000',
        pathRewrite: {
            '^/avatar': '/publicbct/avatars',
        },
        changeOrigin: true,
        secure: false,
    },
    {
        context: ['/publicbct'],
        target: 'http://157.22.252.70:9000',
        changeOrigin: true,
        secure: false,
    },
    {
        context: ['/privatebct'],
        target: 'http://157.22.252.70:9000',
        changeOrigin: true,
        secure: false,
    },
];
