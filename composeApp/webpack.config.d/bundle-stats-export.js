(function () {
    if (process.env.DRIVEBIT_EXPORT_WEBPACK_STATS !== "1") {
        return;
    }

    const fs = require("fs");
    const path = require("path");

    const outDir =
        process.env.DRIVEBIT_BUNDLE_STATS_DIR ||
        path.resolve(__dirname, "../../../.perf/bundle-analysis");

    config.plugins = config.plugins || [];
    config.plugins.push({
        apply: (compiler) => {
            compiler.hooks.done.tap("DrivebitExportBundleStats", (stats) => {
                if (stats.hasErrors()) {
                    return;
                }
                let base = "composeApp";
                const fileName = config.output && config.output.filename;
                if (typeof fileName === "string") {
                    base = fileName.replace(/\.js$/i, "");
                } else if (typeof fileName === "function") {
                    try {
                        const resolved = fileName({ chunk: { name: "main" } });
                        if (typeof resolved === "string") {
                            base = resolved.replace(/\.js$/i, "");
                        }
                    } catch (_) {
                        /* keep default */
                    }
                }
                fs.mkdirSync(outDir, { recursive: true });
                const json = stats.toJson({
                    assets: true,
                    modules: true,
                    chunks: true,
                    nestedModules: false,
                    reasons: false,
                    source: false,
                });
                fs.writeFileSync(
                    path.join(outDir, `${base}-webpack-stats.json`),
                    JSON.stringify(json),
                );
            });
        },
    });
})();
