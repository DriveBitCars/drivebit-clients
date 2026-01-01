package my.drivebit.repositories

import kotlinx.serialization.json.Json

internal val repositoriesJson: Json =
    Json {
        ignoreUnknownKeys = true
    }
