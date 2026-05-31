config.devServer = config.devServer || {};
config.devServer.open = false;
config.devServer.port = process.env.PORT ? parseInt(process.env.PORT, 10) : 'auto';
config.devServer.hot = true;
config.devServer.liveReload = true;
config.devServer.historyApiFallback = {
    rewrites: [
        { from: /^\/[^/]+\/poblizosti\/?$/, to: '/moskva/poblizosti/index.html' },
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
