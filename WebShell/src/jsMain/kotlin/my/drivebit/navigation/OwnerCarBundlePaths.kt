package my.drivebit.navigation

fun isOwnerCarBundlePath(path: String): Boolean =
    path.startsWith("/create-car") ||
        path.startsWith("/my-cars") ||
        path.startsWith("/car-edit") ||
        path.startsWith("/car-photos-upload") ||
        (path.startsWith("/car-photos") && !path.startsWith("/car-photos-gallery")) ||
        path.startsWith("/car-availability") ||
        path.startsWith("/city-selection") ||
        path.startsWith("/address-input") ||
        path.startsWith("/car-sts-upload") ||
        path.startsWith("/license-plate-input") ||
        path.startsWith("/car-brand-selection") ||
        path.startsWith("/car-model-selection") ||
        path.startsWith("/body-type-selection") ||
        path.startsWith("/drive-type-selection") ||
        path.startsWith("/engine-type-selection") ||
        path.startsWith("/engine-volume-input") ||
        path.startsWith("/production-year-input") ||
        path.startsWith("/seats-count-input") ||
        path.startsWith("/trunk-size-selection") ||
        path.startsWith("/daily-rate-input") ||
        path.startsWith("/description-input") ||
        path.startsWith("/passport-upload")

val ownerCarBundleShellRoutes: List<String> =
    listOf(
        "create-car",
        "my-cars",
        "car-edit",
        "car-photos-upload",
        "car-photos",
        "car-availability",
        "city-selection",
        "address-input",
        "car-sts-upload",
        "license-plate-input",
        "car-brand-selection",
        "car-model-selection",
        "body-type-selection",
        "drive-type-selection",
        "engine-type-selection",
        "engine-volume-input",
        "production-year-input",
        "seats-count-input",
        "trunk-size-selection",
        "daily-rate-input",
        "description-input",
        "passport-upload",
    )
