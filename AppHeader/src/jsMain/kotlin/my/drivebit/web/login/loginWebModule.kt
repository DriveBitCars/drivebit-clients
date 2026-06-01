package my.drivebit.web.login

import my.drivebit.viewmodels.login.LoginDraftStorage
import my.drivebit.viewmodels.login.LoginMviViewModel
import my.drivebit.viewmodels.login.LoginMviViewModelImpl
import my.drivebit.viewmodels.login.LoginScreenKind
import org.koin.core.qualifier.named
import org.koin.dsl.module

val loginWebModule =
    module {
        single<LoginDraftStorage> { JsLoginDraftStorage() }

        factory<LoginMviViewModel>(named("phoneLoginMvi")) {
            LoginMviViewModelImpl(
                kind = LoginScreenKind.Phone,
                createOtpRepository = get(),
                phoneValidator = get(named("phone")),
                phoneInputValidator = get(named("phoneInput")),
                emailValidator = get(named("email")),
                emailInputValidator = get(named("emailInput")),
                loginDraftStorage = get(),
            )
        }

        factory<LoginMviViewModel>(named("emailLoginMvi")) {
            LoginMviViewModelImpl(
                kind = LoginScreenKind.Email,
                createOtpRepository = get(),
                phoneValidator = get(named("phone")),
                phoneInputValidator = get(named("phoneInput")),
                emailValidator = get(named("email")),
                emailInputValidator = get(named("emailInput")),
                loginDraftStorage = get(),
            )
        }
    }
