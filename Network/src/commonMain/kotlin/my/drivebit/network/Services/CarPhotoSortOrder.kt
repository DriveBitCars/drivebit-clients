package my.drivebit.network.services

fun List<CarPhotoResponse>.sortedResponsesBySortOrder(): List<CarPhotoResponse> = sortedBy { it.sortOrder }

fun List<CarPhotoItem>.sortedItemsBySortOrder(): List<CarPhotoItem> = sortedBy { it.sortOrder }
