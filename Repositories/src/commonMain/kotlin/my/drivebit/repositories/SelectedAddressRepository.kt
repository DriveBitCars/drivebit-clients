package my.drivebit.repositories

import com.russhwolf.settings.Settings
import my.drivebit.network.services.AddressData

interface SelectedAddressRepository {
    fun saveAddress(address: String)

    fun getAddress(): String?

    fun saveAddressData(addressData: AddressData?)

    fun getAddressData(): AddressData?

    fun clearAddress()
}

internal class SelectedAddressRepositoryImpl(
    private val settings: Settings,
) : SelectedAddressRepository {
    companion object {
        private const val ADDRESS_KEY = "selected_address"
        private const val ADDRESS_DATA_KEY = "selected_address_data"
    }

    private val addressDataCache =
        SettingsJsonCache(
            key = ADDRESS_DATA_KEY,
            serializer = AddressData.serializer(),
            settings = settings,
        )

    override fun saveAddress(address: String) {
        settings.putString(ADDRESS_KEY, address)
    }

    override fun getAddress(): String? = settings.getStringOrNull(ADDRESS_KEY)

    override fun saveAddressData(addressData: AddressData?) {
        if (addressData == null) {
            addressDataCache.clear()
            return
        }
        addressDataCache.save(addressData)
    }

    override fun getAddressData(): AddressData? = addressDataCache.loadOrNull()

    override fun clearAddress() {
        settings.remove(ADDRESS_KEY)
        addressDataCache.clear()
    }
}
