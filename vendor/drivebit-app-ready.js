(function () {
    document.documentElement.classList.add("drivebit-js");

    function markReady() {
        document.body.classList.add("drivebit-app-ready");
    }

    function watchRoot(root) {
        if (root.childElementCount > 0) {
            markReady();
            return;
        }
        new MutationObserver(function () {
            if (root.childElementCount > 0) {
                markReady();
            }
        }).observe(root, { childList: true });
    }

    function init() {
        var root = document.getElementById("root");
        if (!root) {
            return;
        }
        watchRoot(root);
    }

    if (document.body) {
        init();
    } else {
        document.addEventListener("DOMContentLoaded", init);
    }
})();
