package my.drivebit.network

inline fun <T> Result<T>.handleServiceError(
    serviceName: String,
    methodName: String,
): T =
    getOrElse { e ->
        println("❌ [$serviceName] $methodName failed")
        println("   - Error: ${e.message}")
        e.printStackTrace()
        throw e
    }
