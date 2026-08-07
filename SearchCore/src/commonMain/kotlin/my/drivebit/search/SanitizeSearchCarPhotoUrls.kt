package my.drivebit.search

import my.drivebit.network.services.CarItem
import my.drivebit.network.services.sortedBySortOrder

/** Rewrite direct MinIO host:9000 URLs to same-origin `/publicbct/...` paths for HTTPS pages. */
fun sanitizeSearchCarPhotoUrls(items: List<CarItem>): List<CarItem> =
    items.map { car ->
        val photosFromGeneral = car.general.photos
        val photosFromTopLevel = car.photos
        val allPhotos = (photosFromGeneral + photosFromTopLevel).distinctBy { it.id }

        car.copy(
            photos = allPhotos.map { it.withSanitizedUrls() }.sortedBySortOrder(),
            general =
                car.general.copy(
                    photos = photosFromGeneral.map { it.withSanitizedUrls() }.sortedBySortOrder(),
                ),
        )
    }
