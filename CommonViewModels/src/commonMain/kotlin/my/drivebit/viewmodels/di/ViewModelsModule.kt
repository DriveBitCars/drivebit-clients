package my.drivebit.viewmodels.di

import my.drivebit.utils.EmailInputValidator
import my.drivebit.utils.EmailValidator
import my.drivebit.utils.InputValidator
import my.drivebit.utils.PhoneInputValidator
import my.drivebit.utils.PhoneValidator
import my.drivebit.utils.Validator
import my.drivebit.utils.WinCodeValidator
import my.drivebit.viewmodels.AddressSuggestViewModel
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.AvatarUploadViewModel
import my.drivebit.viewmodels.AvatarUploadViewModelImpl
import my.drivebit.viewmodels.BodyTypeViewModel
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.ButterViewModelImpl
import my.drivebit.viewmodels.CarBrandViewModel
import my.drivebit.viewmodels.CarDetailViewModel
import my.drivebit.viewmodels.CarDetailViewModelImpl
import my.drivebit.viewmodels.CarEditMviViewModel
import my.drivebit.viewmodels.CarEditMviViewModelImpl
import my.drivebit.viewmodels.CarEditViewModel
import my.drivebit.viewmodels.CarEditViewModelImpl
import my.drivebit.viewmodels.CarMenuViewModel
import my.drivebit.viewmodels.CarMenuViewModelImpl
import my.drivebit.viewmodels.CarModelViewModel
import my.drivebit.viewmodels.CarPhotosViewModel
import my.drivebit.viewmodels.CarPhotosViewModelImpl
import my.drivebit.viewmodels.CarSearchViewModel
import my.drivebit.viewmodels.CarSearchViewModelImpl
import my.drivebit.viewmodels.CityViewModel
import my.drivebit.viewmodels.CreateCarFromDailyRateViewModel
import my.drivebit.viewmodels.CreateCarFromDailyRateViewModelImpl
import my.drivebit.viewmodels.DescriptionInputViewModel
import my.drivebit.viewmodels.DescriptionInputViewModelImpl
import my.drivebit.viewmodels.DocumentsViewModel
import my.drivebit.viewmodels.DocumentsViewModelImpl
import my.drivebit.viewmodels.DriveTypeViewModel
import my.drivebit.viewmodels.EditProfileViewModel
import my.drivebit.viewmodels.EditProfileViewModelImpl
import my.drivebit.viewmodels.EmailLoginViewModel
import my.drivebit.viewmodels.EngineTypeViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MainContentViewModelImpl
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.MyCarsViewModel
import my.drivebit.viewmodels.MyCarsViewModelImpl
import my.drivebit.viewmodels.MyCitySelectionViewModel
import my.drivebit.viewmodels.MyCitySelectionViewModelImpl
import my.drivebit.viewmodels.MyCityViewModel
import my.drivebit.viewmodels.MyCityViewModelImpl
import my.drivebit.viewmodels.PassportUploadViewModel
import my.drivebit.viewmodels.PassportUploadViewModelImpl
import my.drivebit.viewmodels.PhoneLoginViewModel
import my.drivebit.viewmodels.ProfileViewModel
import my.drivebit.viewmodels.ProfileViewModelImpl
import my.drivebit.viewmodels.SearchViewModel
import my.drivebit.viewmodels.SearchViewModelImpl
import my.drivebit.viewmodels.TrunkSizeViewModel
import my.drivebit.viewmodels.ValidatorViewModel
import my.drivebit.viewmodels.WinCodeInputViewModel
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

        single<InputValidator>(named("winCode")) {
            WinCodeValidator()
        }

        factory {
            FiltersViewModel(
                storage = get(),
            )
        }

        factory {
            MapViewModel(
                locationManager = get(),
            )
        }

        single {
            IconUserViewModel(
                avatarRepository = get(),
            )
        }

        single<MyCityViewModel> {
            MyCityViewModelImpl(
                myCityRepository = get(),
            )
        }

        factory<MyCitySelectionViewModel> {
            MyCitySelectionViewModelImpl(
                myCityRepository = get(),
            )
        }

        factory<CarSearchViewModel> {
            CarSearchViewModelImpl(
                carSearchRepository = get(),
            )
        }

        single<MainContentViewModel> {
            MainContentViewModelImpl(
                carSearchRepository = get(),
            )
        }

        factory<AvatarUploadViewModel> {
            AvatarUploadViewModelImpl(
                photo = get(),
                avatarRepository = get(),
            )
        }

        factory<AuthFormViewModel>(named("phone")) {
            PhoneLoginViewModel(
                createOtpRepository = get(),
                phoneValidator = get(named("phone")),
                phoneInputValidator = get(named("phoneInput")),
            )
        }

        factory<AuthFormViewModel>(named("email")) {
            EmailLoginViewModel(
                createOtpRepository = get(),
                emailValidator = get(named("email")),
                emailInputValidator = get(named("emailInput")),
            )
        }

        single<ButterViewModel> {
            ButterViewModelImpl(
                storage = get(),
                avatarRepository = get(),
                carMenuViewModel = get(),
                profileViewModel = get(),
            )
        }

        single<ProfileViewModel> {
            ProfileViewModelImpl(
                userService = get(),
                storage = get(),
            )
        }

        factory<EditProfileViewModel> { (firstName: String, lastName: String, middleName: String) ->
            EditProfileViewModelImpl(
                userService = get(),
                initialFirstName = firstName,
                initialLastName = lastName,
                initialMiddleName = middleName,
            )
        }

        factory<ValidatorViewModel>(named("phoneInputField")) {
            ValidatorViewModel(
                validator = get(named("phone")),
                inputValidator = get(named("phoneInput")),
                initialErrorMessage = "Введите номер телефона",
            )
        }

        factory<ValidatorViewModel>(named("emailInputField")) {
            ValidatorViewModel(
                validator = get(named("email")),
                inputValidator = get(named("emailInput")),
                initialErrorMessage = "Введите email",
            )
        }

        factory {
            AddressSuggestViewModel(
                addressSuggestRepository = get(),
            )
        }

        factory {
            WinCodeInputViewModel(
                winCodeValidator = get(named("winCode")),
            )
        }

        factory {
            CarBrandViewModel(
                carBrandRepository = get(),
            )
        }

        factory {
            CarModelViewModel(
                carModelRepository = get(),
            )
        }

        factory {
            BodyTypeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            DriveTypeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            EngineTypeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            TrunkSizeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            CityViewModel(
                cityRepository = get(),
            )
        }

        single<CarMenuViewModel> {
            CarMenuViewModelImpl(
                storage = get(),
                myCarRepository = get(),
            )
        }

        factory<MyCarsViewModel> {
            MyCarsViewModelImpl(
                myCarRepository = get(),
            )
        }

        factory<CarEditViewModel> {
            CarEditViewModelImpl(
                carService = get(),
            )
        }

        factory<CarEditMviViewModel> { (carId: String) ->
            CarEditMviViewModelImpl(
                carService = get(),
                myCarRepository = get(),
                carBrandViewModel = get(),
                carModelViewModel = get(),
                bodyTypeViewModel = get(),
                driveTypeViewModel = get(),
                engineTypeViewModel = get(),
                trunkSizeViewModel = get(),
                carId = carId,
            )
        }

        factory<CarPhotosViewModel> {
            CarPhotosViewModelImpl(
                photoService = get(),
            )
        }

        factory<CreateCarFromDailyRateViewModel> {
            CreateCarFromDailyRateViewModelImpl(
                carDataRepository = get(),
                createCarRepository = get(),
                hasPassportRepo = get(),
            )
        }

        factory<DescriptionInputViewModel> {
            DescriptionInputViewModelImpl(
                carDataRepository = get(),
            )
        }

        factory<PassportUploadViewModel> {
            PassportUploadViewModelImpl(
                documents = get(),
                createCarRepository = get(),
                carDataRepository = get(),
            )
        }

        factory<DocumentsViewModel> {
            DocumentsViewModelImpl(
                documents = get(),
            )
        }

        factory<SearchViewModel> {
            SearchViewModelImpl(
                dictionary = get(),
                carService = get(),
                myCityRepository = get(),
                carSearchRepository = get(),
            )
        }

        factory<CarDetailViewModel> { (carId: String) ->
            CarDetailViewModelImpl(
                carService = get(),
                carId = carId,
            )
        }
    }
