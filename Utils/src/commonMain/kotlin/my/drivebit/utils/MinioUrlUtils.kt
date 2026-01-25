package my.drivebit.utils

fun extractPathFromApiUrl(apiUrl: String): String = apiUrl.split("155.212.170.94:9000").last()
