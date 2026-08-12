package my.drivebit.viewmodels

sealed interface ContractSignEffect {
    data class Ok(
        val bookingId: String,
    ) : ContractSignEffect

    data class Fail(
        val bookingId: String,
        val message: String,
    ) : ContractSignEffect
}
