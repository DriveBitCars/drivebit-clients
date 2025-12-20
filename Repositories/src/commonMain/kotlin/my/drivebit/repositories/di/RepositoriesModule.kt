package my.drivebit.repositories.di

import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.AddressSuggestRepositoryImpl
import my.drivebit.repositories.AvatarRepository
import my.drivebit.repositories.AvatarRepositoryImpl
import my.drivebit.repositories.CarBrandRepository
import my.drivebit.repositories.CarBrandRepositoryImpl
import my.drivebit.repositories.ChangeEmailRepositoryImpl
import my.drivebit.repositories.ChangePhoneRepositoryImpl
import my.drivebit.repositories.CityRepository
import my.drivebit.repositories.CityRepositoryImpl
import my.drivebit.repositories.CreateOtpRepository
import my.drivebit.repositories.CreateOtpRepositoryImpl
import my.drivebit.repositories.OtpResultRepository
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.repositories.SelectedAddressRepositoryImpl
import my.drivebit.repositories.SelectedCarBrandRepository
import my.drivebit.repositories.SelectedCarBrandRepositoryImpl
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.repositories.SelectedCityRepositoryImpl
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
            AvatarRepositoryImpl(
                photo = get(),
                storage = get(),
            )
        }

        single<AddressSuggestRepository> {
            AddressSuggestRepositoryImpl(
                dadata = get(),
            )
        }

        single<SelectedAddressRepository> {
            SelectedAddressRepositoryImpl()
        }

        single<WinCodeRepository> {
            WinCodeRepositoryImpl()
        }

        single<CarBrandRepository> {
            CarBrandRepositoryImpl(
                dictionary = get(),
            )
        }

        single<SelectedCarBrandRepository> {
            SelectedCarBrandRepositoryImpl()
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
    }
