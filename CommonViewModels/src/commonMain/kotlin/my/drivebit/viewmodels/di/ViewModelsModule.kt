package my.drivebit.viewmodels.di

import my.drivebit.utils.EmailInputValidator
import my.drivebit.utils.EmailValidator
import my.drivebit.utils.InputValidator
import my.drivebit.utils.PhoneInputValidator
import my.drivebit.utils.PhoneValidator
import my.drivebit.utils.Validator
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.ButterViewModelImpl
import my.drivebit.viewmodels.EditProfileViewModel
import my.drivebit.viewmodels.EditProfileViewModelImpl
import my.drivebit.viewmodels.EmailLoginViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.PhoneLoginViewModel
import my.drivebit.viewmodels.ProfileViewModel
import my.drivebit.viewmodels.ProfileViewModelImpl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commonViewModelsModule: Module =
    module {

        single<Validator>(named("phone")) {
            PhoneValidator()
        }

        single<Validator>(named("email")) {
            EmailValidator()
        }

        single<InputValidator>(named("phoneInput")) {
            PhoneInputValidator()
        }

        single<InputValidator>(named("emailInput")) {
            EmailInputValidator()
        }

        factory {
            FiltersViewModel(
                storage = get(),
            )
        }

        factory {
            IconUserViewModel(
                storage = get(),
            )
        }

        factory<AuthFormViewModel>(named("phone")) {
            PhoneLoginViewModel(
                auth = get(),
                phoneValidator = get(named("phone")),
                phoneInputValidator = get(named("phoneInput")),
            )
        }

        factory<AuthFormViewModel>(named("email")) {
            EmailLoginViewModel(
                auth = get(),
                emailValidator = get(named("email")),
                emailInputValidator = get(named("emailInput")),
            )
        }

        single<ButterViewModel> {
            ButterViewModelImpl(
                storage = get(),
            )
        }

        factory<ProfileViewModel> {
            ProfileViewModelImpl(
                userService = get(),
            )
        }

        factory<EditProfileViewModel> {
            EditProfileViewModelImpl(
                userService = get(),
                initialFirstName = "",
                initialLastName = "",
                initialMiddleName = "",
            )
        }
    }
