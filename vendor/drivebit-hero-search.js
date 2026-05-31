(function () {
    function pad(n) {
        return String(n).padStart(2, "0");
    }

    function formatDate(d) {
        return d.getFullYear() + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate());
    }

    function initForm(form) {
        var start = form.querySelector('input[name="startDate"]');
        var end = form.querySelector('input[name="endDate"]');
        if (!start || !end) return;

        var today = new Date();
        var tomorrow = new Date(today.getTime());
        tomorrow.setDate(tomorrow.getDate() + 1);

        if (!start.value) start.value = formatDate(today);
        if (!end.value) end.value = formatDate(tomorrow);

        form.addEventListener("submit", function (e) {
            if (start.value && end.value && start.value > end.value) {
                e.preventDefault();
                end.value = start.value;
            }
        });
    }

    document.querySelectorAll(".drivebit-hero-search").forEach(initForm);
})();
