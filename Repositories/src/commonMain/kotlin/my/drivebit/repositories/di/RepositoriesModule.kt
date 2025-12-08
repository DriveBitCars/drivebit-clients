package my.drivebit.repositories.di

import my.drivebit.repositories.ChangeEmailRepositoryImpl
import my.drivebit.repositories.ChangePhoneRepositoryImpl
import my.drivebit.repositories.CreateOtpRepository
import my.drivebit.repositories.CreateOtpRepositoryImpl
import my.drivebit.repositories.OtpResultRepository
import my.drivebit.repositories.VerifyOtpRepositoryImpl
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
    }
