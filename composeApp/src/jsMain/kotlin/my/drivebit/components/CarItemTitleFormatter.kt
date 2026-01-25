package my.drivebit.components

import my.drivebit.network.services.CarItem

fun formatCarTitle(car: CarItem): String {
    val parts = mutableListOf<String>()
    car.general
        ?.brandName
        ?.takeIf { it.isNotBlank() }
        ?.let { parts.add(it) }
    car.general
        ?.modelName
        ?.takeIf { it.isNotBlank() }
        ?.let { parts.add(it) }
    car.general
        ?.year
        ?.takeIf { it > 0 }
        ?.let { parts.add(it.toString()) }

    return if (parts.isNotEmpty()) {
        parts.joinToString(" ")
    } else {
        val idPrefix = if (car.id.length > 8) car.id.substring(0, 8) else car.id
        "Автомобиль #$idPrefix"
    }
}
