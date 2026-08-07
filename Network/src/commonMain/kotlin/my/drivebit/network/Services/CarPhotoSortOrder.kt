package my.drivebit.network.services

fun List<CarPhotoResponse>.sortedBySortOrder(): List<CarPhotoResponse> = sortedBy { it.sortOrder }

fun List<CarPhotoItem>.sortedBySortOrder(): List<CarPhotoItem> = sortedBy { it.sortOrder }
