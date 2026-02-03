package my.drivebit.repositories.di

import kotlinx.serialization.builtins.serializer
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.AddressSuggestRepositoryImpl
import my.drivebit.repositories.AvatarRepository
import my.drivebit.repositories.AvatarRepositoryImpl
import my.drivebit.repositories.CachedRepositoryImpl
import my.drivebit.repositories.CarBrandRepository
import my.drivebit.repositories.CarBrandRepositoryImpl
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.CarDataRepositoryImpl
import my.drivebit.repositories.CarEnumsHelper
import my.drivebit.repositories.CarEnumsRepository
import my.drivebit.repositories.CarEnumsRepositoryImpl
import my.drivebit.repositories.CarModelRepository
import my.drivebit.repositories.CarModelRepositoryImpl
import my.drivebit.repositories.CarSearchRepository
import my.drivebit.repositories.CarSearchRepositoryImpl
import my.drivebit.repositories.ChangeEmailRepositoryImpl
import my.drivebit.repositories.ChangePhoneRepositoryImpl
import my.drivebit.repositories.CityRepository
import my.drivebit.repositories.CityRepositoryImpl
import my.drivebit.repositories.CreateCarRepository
import my.drivebit.repositories.CreateCarRepositoryImpl
import my.drivebit.repositories.CreateOtpRepository
import my.drivebit.repositories.CreateOtpRepositoryImpl
import my.drivebit.repositories.CurrentTaskRepository
import my.drivebit.repositories.CurrentTaskRepositoryImpl
import my.drivebit.repositories.HasPassportRepo
import my.drivebit.repositories.HasPassportRepoImpl
import my.drivebit.repositories.LicensePlateRepository
import my.drivebit.repositories.LicensePlateRepositoryImpl
import my.drivebit.repositories.MyCarRepository
import my.drivebit.repositories.MyCarRepositoryImpl
import my.drivebit.repositories.MyCityRepository
import my.drivebit.repositories.MyCityRepositoryImpl
import my.drivebit.repositories.OtpResultRepository
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.repositories.SelectedAddressRepositoryImpl
import my.drivebit.repositories.SelectedBodyTypeRepository
import my.drivebit.repositories.SelectedBodyTypeRepositoryImpl
import my.drivebit.repositories.SelectedCarBrandRepository
import my.drivebit.repositories.SelectedCarBrandRepositoryImpl
import my.drivebit.repositories.SelectedCarModelRepository
import my.drivebit.repositories.SelectedCarModelRepositoryImpl
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.repositories.SelectedCityRepositoryImpl
import my.drivebit.repositories.SelectedDriveTypeRepository
import my.drivebit.repositories.SelectedDriveTypeRepositoryImpl
import my.drivebit.repositories.SelectedEngineTypeRepository
import my.drivebit.repositories.SelectedEngineTypeRepositoryImpl
import my.drivebit.repositories.SelectedTrunkSizeRepository
import my.drivebit.repositories.SelectedTrunkSizeRepositoryImpl
import my.drivebit.repositories.VerifyOtpRepositoryImpl
import my.drivebit.repositories.WinCodeRepository
import my.drivebit.repositories.WinCodeRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoriesModule: Module =
    module {
        single<CreateOtpRepository> {
            CreateOtpRepositoryImpl(
                auth = get(),
            )
        }

        single<OtpResultRepository>(named("VerifyOtp")) {
            VerifyOtpRepositoryImpl(
                auth = get(),
                storage = get(),
                avatarRepository = get<AvatarRepository>(),
            )
        }

        single<OtpResultRepository>(named("ChangeEmail")) {
            ChangeEmailRepositoryImpl(
                user = get(),
            )
        }

        single<OtpResultRepository>(named("ChangePhone")) {
            ChangePhoneRepositoryImpl(
                user = get(),
            )
        }

        single<AvatarRepository> {
            val photo = get<my.drivebit.network.services.Photo>()
            val storage = get<my.drivebit.shared.storage.Storage>()
            val settings = get<com.russhwolf.settings.Settings>()

            var avatarRepositoryImpl: AvatarRepositoryImpl? = null
            val cachedRepository =
                CachedRepositoryImpl(
                    key = "cached_avatar_url",
                    serializer = String.serializer(),
                    service = {
                        avatarRepositoryImpl?.calculateAvatarUrl()
                            ?: throw IllegalStateException("AvatarRepository not initialized")
                    },
                    settings = settings,
                )

            avatarRepositoryImpl =
                AvatarRepositoryImpl(
                    photo = photo,
                    storage = storage,
                    cachedRepository = cachedRepository,
                )
            avatarRepositoryImpl
        }

        single<AddressSuggestRepository> {
            AddressSuggestRepositoryImpl(
                dadata = get(),
            )
        }

        single<SelectedAddressRepository> {
            SelectedAddressRepositoryImpl(
                settings = get(),
            )
        }

        single<WinCodeRepository> {
            WinCodeRepositoryImpl(
                settings = get(),
            )
        }

        single<LicensePlateRepository> {
            LicensePlateRepositoryImpl(
                settings = get(),
            )
        }

        single<CarBrandRepository> {
            CarBrandRepositoryImpl(
                dictionary = get(),
            )
        }

        single<SelectedCarBrandRepository> {
            SelectedCarBrandRepositoryImpl(
                settings = get(),
            )
        }

        single<CarModelRepository> {
            CarModelRepositoryImpl(
                dictionary = get(),
            )
        }

        single<SelectedCarModelRepository> {
            SelectedCarModelRepositoryImpl(
                settings = get(),
            )
        }

        single<SelectedBodyTypeRepository> {
            SelectedBodyTypeRepositoryImpl(
                settings = get(),
            )
        }

        single<SelectedDriveTypeRepository> {
            SelectedDriveTypeRepositoryImpl(
                settings = get(),
            )
        }

        single<SelectedEngineTypeRepository> {
            SelectedEngineTypeRepositoryImpl(
                settings = get(),
            )
        }

        single<SelectedTrunkSizeRepository> {
            SelectedTrunkSizeRepositoryImpl(
                settings = get(),
            )
        }

        single<CarDataRepository> {
            CarDataRepositoryImpl(
                settings = get(),
            )
        }

        single<CityRepository> {
            CityRepositoryImpl(
                dictionary = get(),
            )
        }

        single<SelectedCityRepository> {
            SelectedCityRepositoryImpl(
                settings = get(),
            )
        }

        single<CurrentTaskRepository> {
            CurrentTaskRepositoryImpl(
                settings = get(),
            )
        }

        single<CarEnumsRepository> {
            CarEnumsRepositoryImpl(
                dictionary = get(),
            )
        }

        factory<CarEnumsHelper> {
            CarEnumsHelper(get<CarEnumsRepository>())
        }

        single<MyCarRepository> {
            MyCarRepositoryImpl(
                carService = get(),
                settings = get(),
            )
        }

        single<MyCityRepository> {
            MyCityRepositoryImpl(
                dictionary = get(),
                storage = get(),
            )
        }

        single<CarSearchRepository> {
            CarSearchRepositoryImpl(
                carService = get(),
                myCityRepository = get(),
            )
        }

        single<HasPassportRepo> {
            HasPassportRepoImpl(
                documents = get(),
                carEnumsRepository = get(),
            )
        }

        single<CreateCarRepository> {
            CreateCarRepositoryImpl(
                carDataRepository = get(),
                selectedCarBrandRepository = get(),
                selectedCarModelRepository = get(),
                selectedBodyTypeRepository = get(),
                selectedDriveTypeRepository = get(),
                selectedEngineTypeRepository = get(),
                selectedTrunkSizeRepository = get(),
                licensePlateRepository = get(),
                selectedAddressRepository = get(),
                selectedCityRepository = get(),
                myCarRepository = get(),
                carService = get(),
            )
        }
    }
