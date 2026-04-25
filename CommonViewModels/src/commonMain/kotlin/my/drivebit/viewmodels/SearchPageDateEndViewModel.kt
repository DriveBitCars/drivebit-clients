package my.drivebit.viewmodels

import my.drivebit.repositories.CurrentFiltersRepository

interface SearchPageDateEndViewModel {
    fun set(date: String?)
}

class SearchPageDateEndViewModelImpl(
    private val currentFiltersRepository: CurrentFiltersRepository,
) : SearchPageDateEndViewModel {
    override fun set(date: String?) {
        currentFiltersRepository.updateEndDate(date)
    }
}
