package my.drivebit.maps.di

import my.drivebit.maps.LocationManager
import my.drivebit.maps.createLocationManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val mapsModule: Module =
    module {
        single<LocationManager> {
            createLocationManager()
        }
    }
