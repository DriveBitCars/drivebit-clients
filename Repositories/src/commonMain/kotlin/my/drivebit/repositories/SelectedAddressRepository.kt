package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        private val json = Json { ignoreUnknownKeys = true }
    }

    override fun saveAddress(address: String) {
        settings.putString(ADDRESS_KEY, address)
    }

    override fun getAddress(): String? = settings.getStringOrNull(ADDRESS_KEY)

    override fun saveAddressData(addressData: AddressData?) {
        if (addressData != null) {
            val jsonString = json.encodeToString(addressData)
            settings.putString(ADDRESS_DATA_KEY, jsonString)
        } else {
            settings.remove(ADDRESS_DATA_KEY)
        }
    }

    override fun getAddressData(): AddressData? {
        val jsonString = settings.getString(ADDRESS_DATA_KEY, "")
        return if (jsonString.isEmpty()) {
            null
        } else {
            try {
                json.decodeFromString<AddressData>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun clearAddress() {
        settings.remove(ADDRESS_KEY)
        settings.remove(ADDRESS_DATA_KEY)
    }
}
