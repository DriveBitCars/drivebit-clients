package my.drivebit.repositories

interface SelectedAddressRepository {
    fun saveAddress(address: String)
    fun getAddress(): String?
    fun clearAddress()
}

internal class SelectedAddressRepositoryImpl : SelectedAddressRepository {
    private var address: String? = null

    override fun saveAddress(address: String) {
        this.address = address
    }

    override fun getAddress(): String? = address

    override fun clearAddress() {
        address = null
    }
}

